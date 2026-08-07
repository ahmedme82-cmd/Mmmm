/* ============ أوقات الكراهة (حنفي) ============ */
function computeWindows(rec){
  return [
    {id:'k-sun', name:'كراهة الشروق',   a:toSec(rec.s),               b:toSec(rec.s)+K_SUNRISE*60},
    {id:'k-zen', name:'كراهة الاستواء', a:toSec(rec.d)-K_ZENITH*60,   b:toSec(rec.d)},
    {id:'k-isf', name:'كراهة الاصفرار', a:toSec(rec.m)-K_ISFIRAR*60,  b:toSec(rec.m)}
  ];
}
/* نطاق زمني bidi-سليم: جزيرتا LTR مستقلتان — البداية تُقرأ يميناً والنهاية يساراً */
function renderKarah(wins){
  DOM.kChips.innerHTML = '';
  wins.forEach(w => {
    const chip = tpl('tpl-kchip');
    chip.id = w.id;
    chip.querySelector('b').textContent = w.name;
    chip.querySelector('span').innerHTML =
      `<bdi dir="ltr">${hm12(sec2hm(w.a))}</bdi> – <bdi dir="ltr">${hm12(sec2hm(w.b))}</bdi> <small>${period(sec2hm(w.b))}</small>`;
    DOM.kChips.appendChild(chip);
  });
}
function updateKarah(wins, sec){
  wins.forEach(w => {
    const el = document.getElementById(w.id); if(!el) return;
    const em = el.querySelector('em');
    if(sec >= w.a && sec < w.b){
      el.classList.add('active');
      const left = w.b - sec;
      em.hidden = false;
      em.textContent = 'الآن — ينتهي خلال ' + p2(Math.floor(left/60)) + ':' + p2(left%60);
    } else { el.classList.remove('active'); em.hidden = true; }
  });
}