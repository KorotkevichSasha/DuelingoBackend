import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptsDir = path.dirname(fileURLToPath(import.meta.url));
const projectDir = path.dirname(scriptsDir);
const source = path.join(projectDir, 'src', 'main', 'resources', 'data', 'questions.json');
const output = path.join(projectDir, 'admin-panel', 'public', 'question-catalog.html');
const questions = JSON.parse(fs.readFileSync(source, 'utf8').replace(/^\uFEFF/, ''));
const safeData = JSON.stringify(questions).replaceAll('<', '\\u003c');

const html = `<!doctype html>
<html lang="ru">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>DuelRush — каталог вопросов</title>
  <style>
    :root { color-scheme: dark; --bg:#0b1018; --surface:#141c29; --surface2:#1b2636; --text:#f4f7fb; --muted:#9aa8ba; --blue:#2a8cff; --line:#2a394d; --green:#62d995; --amber:#ffc857; }
    * { box-sizing:border-box } body { margin:0; font:15px/1.5 Inter,Segoe UI,Arial,sans-serif; background:radial-gradient(circle at 75% -10%,#173966 0,transparent 35%),var(--bg); color:var(--text) }
    .wrap { max-width:1500px; margin:auto; padding:28px }
    header { display:flex; align-items:flex-end; justify-content:space-between; gap:20px; margin-bottom:22px }
    h1 { margin:0; font-size:clamp(28px,4vw,46px); letter-spacing:-1.5px } .lead { color:var(--muted); margin:7px 0 0 }
    .stats { display:flex; gap:10px; flex-wrap:wrap } .stat { background:var(--surface); border:1px solid var(--line); border-radius:16px; padding:11px 16px; min-width:110px } .stat b { display:block; font-size:22px }
    .toolbar { position:sticky; top:0; z-index:5; display:grid; grid-template-columns:minmax(220px,2fr) repeat(3,minmax(150px,1fr)) auto; gap:10px; padding:14px; background:rgba(11,16,24,.9); backdrop-filter:blur(16px); border:1px solid var(--line); border-radius:20px; box-shadow:0 12px 35px #0006 }
    input,select,button { min-height:46px; border:1px solid var(--line); border-radius:13px; background:var(--surface2); color:var(--text); padding:0 14px; font:inherit }
    input:focus,select:focus { outline:2px solid var(--blue); border-color:transparent } button { cursor:pointer; font-weight:700 } button:hover { border-color:var(--blue) }
    .meta { display:flex; justify-content:space-between; align-items:center; gap:10px; margin:18px 4px 10px; color:var(--muted) }
    .list { display:grid; gap:12px }
    article { display:grid; grid-template-columns:72px minmax(0,1fr); gap:16px; background:linear-gradient(135deg,var(--surface),#111925); border:1px solid var(--line); border-radius:18px; padding:18px; box-shadow:0 8px 24px #0003 }
    .number { width:56px; height:56px; display:grid; place-items:center; border-radius:16px; background:#102c4f; color:#8bc2ff; font-size:18px; font-weight:800 }
    .tags { display:flex; gap:7px; flex-wrap:wrap; margin-bottom:9px } .tag { padding:3px 9px; border-radius:999px; background:#202f43; color:#c7d5e7; font-size:12px; font-weight:700 }
    .tag.easy { color:#83e8ac;background:#173927 }.tag.medium { color:#ffd985;background:#3d3218 }.tag.hard { color:#ff9b9b;background:#411f25 }
    .question { font-size:18px; font-weight:750; margin-bottom:12px; overflow-wrap:anywhere }
    .row { display:grid; grid-template-columns:130px 1fr; gap:10px; margin-top:7px }.label { color:var(--muted) }.answer { color:var(--green); font-weight:750 }.options { color:#cfdbeb }
    .pager { display:flex; align-items:center; justify-content:center; gap:12px; margin:22px 0 40px }.pager span { color:var(--muted) }.empty { padding:60px; text-align:center; color:var(--muted); background:var(--surface); border-radius:18px }
    @media(max-width:950px){.toolbar{grid-template-columns:1fr 1fr}.toolbar input{grid-column:1/-1}header{align-items:flex-start;flex-direction:column}} @media(max-width:600px){.wrap{padding:16px}.toolbar{grid-template-columns:1fr}article{grid-template-columns:1fr}.number{width:48px;height:48px}.row{grid-template-columns:1fr;gap:2px}}
  </style>
</head>
<body><main class="wrap">
  <header><div><h1>Каталог вопросов DuelRush</h1><p class="lead">Все задания, варианты и правильные ответы в одном месте.</p></div><div class="stats" id="stats"></div></header>
  <section class="toolbar">
    <input id="search" type="search" placeholder="Поиск по вопросу, ответу или варианту…" />
    <select id="topic"><option value="">Все темы</option></select>
    <select id="difficulty"><option value="">Любая сложность</option><option>EASY</option><option>MEDIUM</option><option>HARD</option></select>
    <select id="type"><option value="">Любой тип</option><option>FILL_IN_CHOICE</option><option>FILL_IN_INPUT</option><option>SENTENCE_CONSTRUCTION</option><option>AUDIO_RECOGNITION</option></select>
    <button id="csv">Скачать CSV</button>
  </section>
  <div class="meta"><span id="found"></span><span>50 на странице</span></div><section class="list" id="list"></section><nav class="pager" id="pager"></nav>
</main>
<script>
const all=${safeData}; const size=50; let page=0, filtered=all;
const el=id=>document.getElementById(id), esc=v=>String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const typeNames={FILL_IN_CHOICE:'Выбор ответа',FILL_IN_INPUT:'Свободный ввод',SENTENCE_CONSTRUCTION:'Составить предложение',AUDIO_RECOGNITION:'Аудирование'};
const topics=[...new Set(all.map(q=>q.topic))].sort((a,b)=>a.localeCompare(b)); topics.forEach(t=>el('topic').insertAdjacentHTML('beforeend','<option>'+esc(t)+'</option>'));
el('stats').innerHTML='<div class="stat"><b>'+all.length+'</b>вопросов</div><div class="stat"><b>'+topics.length+'</b>тем</div><div class="stat"><b>'+all.filter(q=>q.type==='AUDIO_RECOGNITION').length+'</b>аудио</div>';
function apply(){const s=el('search').value.trim().toLocaleLowerCase('ru'); filtered=all.filter(q=>(!el('topic').value||q.topic===el('topic').value)&&(!el('difficulty').value||q.difficulty===el('difficulty').value)&&(!el('type').value||q.type===el('type').value)&&(!s||[q.questionText,...(q.correctAnswers||[]),...(q.options||[])].join(' ').toLocaleLowerCase('ru').includes(s)));page=0;render()}
function render(){const pages=Math.max(1,Math.ceil(filtered.length/size));page=Math.min(page,pages-1);const rows=filtered.slice(page*size,(page+1)*size);el('found').textContent='Найдено: '+filtered.length;el('list').innerHTML=rows.length?rows.map((q,i)=>'<article><div class="number">'+(page*size+i+1)+'</div><div><div class="tags"><span class="tag">'+esc(q.topic)+'</span><span class="tag '+q.difficulty.toLowerCase()+'">'+q.difficulty+'</span><span class="tag">'+esc(typeNames[q.type]||q.type)+'</span></div><div class="question">'+esc(q.questionText)+'</div><div class="row"><span class="label">Правильный ответ</span><span class="answer">'+esc((q.correctAnswers||[]).join(' / '))+'</span></div>'+(q.options?.length?'<div class="row"><span class="label">Варианты / слова</span><span class="options">'+q.options.map(esc).join(' · ')+'</span></div>':'')+'</div></article>').join(''):'<div class="empty">По выбранным фильтрам ничего не найдено.</div>';el('pager').innerHTML='<button id="prev" '+(page===0?'disabled':'')+'>← Назад</button><span>Страница '+(page+1)+' из '+pages+'</span><button id="next" '+(page>=pages-1?'disabled':'')+'>Вперёд →</button>';el('prev').onclick=()=>{page--;render();scrollTo({top:0,behavior:'smooth'})};el('next').onclick=()=>{page++;render();scrollTo({top:0,behavior:'smooth'})}}
['search','topic','difficulty','type'].forEach(id=>el(id).addEventListener(id==='search'?'input':'change',apply));
el('csv').onclick=()=>{const cells=v=>'"'+String(v??'').replaceAll('"','""')+'"';const csv=['Тема;Сложность;Тип;Вопрос;Правильный ответ;Варианты',...filtered.map(q=>[q.topic,q.difficulty,q.type,q.questionText,(q.correctAnswers||[]).join(' / '),(q.options||[]).join(' | ')].map(cells).join(';'))].join('\\r\\n');const a=document.createElement('a');a.href=URL.createObjectURL(new Blob(['\\uFEFF'+csv],{type:'text/csv'}));a.download='duelrush-questions.csv';a.click();URL.revokeObjectURL(a.href)};render();
</script></body></html>`;

fs.mkdirSync(path.dirname(output), { recursive: true });
fs.writeFileSync(output, html, 'utf8');
console.log(`Generated ${output} with ${questions.length} questions.`);
