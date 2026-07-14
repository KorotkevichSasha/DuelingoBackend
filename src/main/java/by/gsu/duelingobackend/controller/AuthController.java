package by.gsu.duelingobackend.controller;

import by.gsu.duelingobackend.dto.request.RefreshTokenRequest;
import by.gsu.duelingobackend.dto.request.SignInRequest;
import by.gsu.duelingobackend.dto.request.SignUpRequest;
import by.gsu.duelingobackend.dto.response.JwtAuthenticationResponse;
import by.gsu.duelingobackend.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }

    @PostMapping("/refresh-token")
    public JwtAuthenticationResponse refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return authenticationService.refreshToken(request.refreshToken());
    }
}
