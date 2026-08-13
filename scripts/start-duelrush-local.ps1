param(
    [switch]$RebuildBackend,
    [switch]$RebuildAndroid,
    [switch]$InstallAndroid,
    [switch]$SkipAdminPanel,
    [switch]$SkipAndroidLaunch
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$backendRoot = Split-Path -Parent $PSScriptRoot
$workspaceRoot = Split-Path -Parent $backendRoot
$androidRoot = Join-Path $workspaceRoot "DuelingoFront"
$healthUrl = "http://127.0.0.1:8082/actuator/health"
$health = $null

Push-Location $backendRoot
try {
    if ($RebuildBackend) {
        docker compose up -d --build
    } else {
        docker compose up -d
    }
} finally {
    Pop-Location
}

$deadline = (Get-Date).AddSeconds(120)
do {
    try {
        $health = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 4
        if ($health.status -eq "UP") { break }
    } catch {
        Start-Sleep -Seconds 2
    }
} while ((Get-Date) -lt $deadline)

if ($null -eq $health -or $health.status -ne "UP") {
    throw "DuelRush API did not become healthy at $healthUrl"
}

if (-not $SkipAdminPanel) {
    $adminRoot = Join-Path $backendRoot "admin-panel"
    $adminRunning = Get-NetTCPConnection -State Listen -LocalPort 5173 -ErrorAction SilentlyContinue
    if (-not $adminRunning) {
        $npm = (Get-Command npm.cmd -ErrorAction Stop).Source
        Start-Process -FilePath $npm `
            -ArgumentList @("run", "dev", "--", "--host", "127.0.0.1") `
            -WorkingDirectory $adminRoot `
            -RedirectStandardOutput (Join-Path $adminRoot "vite.out.log") `
            -RedirectStandardError (Join-Path $adminRoot "vite.err.log") `
            -WindowStyle Hidden | Out-Null
    }
}

$adbCandidates = @((Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"))
if ($env:ANDROID_HOME) {
    $adbCandidates += Join-Path $env:ANDROID_HOME "platform-tools\adb.exe"
}
if ($env:ANDROID_SDK_ROOT) {
    $adbCandidates += Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe"
}
$adbCandidates = $adbCandidates | Where-Object { $_ -and (Test-Path $_) }

$adb = $adbCandidates | Select-Object -First 1
$devices = @()
if ($adb) {
    $devices = & $adb devices | Select-Object -Skip 1 | ForEach-Object {
        if ($_ -match "^(\S+)\s+device$") { $Matches[1] }
    }

    foreach ($device in $devices) {
        & $adb -s $device reverse tcp:8082 tcp:8082 | Out-Null
    }
}

if ($RebuildAndroid -or ($InstallAndroid -and -not (Test-Path (Join-Path $androidRoot "app\build\outputs\apk\debug\app-debug.apk")))) {
    Push-Location $androidRoot
    try {
        & (Join-Path $androidRoot "gradlew.bat") assembleDebug
        if ($LASTEXITCODE -ne 0) { throw "Android build failed" }
    } finally {
        Pop-Location
    }
}

if ($InstallAndroid) {
    if (-not $adb) { throw "ADB was not found" }
    $apk = Join-Path $androidRoot "app\build\outputs\apk\debug\app-debug.apk"
    foreach ($device in $devices) {
        & $adb -s $device install -r $apk | Out-Null
        if ($LASTEXITCODE -ne 0) { throw "Could not install DuelRush on $device" }
    }
}

if (-not $SkipAndroidLaunch -and $adb) {
    foreach ($device in $devices) {
        & $adb -s $device shell am force-stop com.example.duelingo.debug | Out-Null
        & $adb -s $device shell am start -n com.example.duelingo.debug/com.example.duelingo.activity.MainActivity | Out-Null
    }
}

Write-Host "DuelRush API: UP ($healthUrl)"
if ($SkipAdminPanel) {
    Write-Host "Admin panel: skipped"
} else {
    Write-Host "Admin panel: http://127.0.0.1:5173"
}
Write-Host "Android devices prepared: $($devices.Count)"
