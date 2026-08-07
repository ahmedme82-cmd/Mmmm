/* ============ محرك الوقت: الساعة، العدّ التنازلي، تغيّر اليوم ============ */
const RING_R = 60, RING_C = 2 * Math.PI * RING_R;
DOM.ringProg.style.strokeDasharray = RING_C;
DOM.ringProg.style.strokeDashoffset = RING_C;

let _lastNextKey = '';
function resetNextFlag(){ _lastNextKey = ''; }

function startClock(state, onRollover){
  const tick = () => {
    const now = cairoNow();
    const h12 = now.h%12===0 ? 12 : now.h%12;
    /* الأرقام داخل bdi‏(ltr) والفترة ص/م عنصر شقيق — ترتيب عربي سليم */
    DOM.chipClock.textContent = `${h12}:${p2(now.mi)}:${p2(now.s)}`;
    DOM.chipClockPeriod.textContent = now.h < 12 ? 'ص' : 'م';

    /* تغيّر اليوم بعد منتصف الليل */
    if(now.key !== state.curKey){
      state.curKey = now.key;
      const rec0 = ALL.find(r => r.g === now.key);
      if(rec0 && rec0 !== state.todayRec) onRollover(rec0, now);
    }

    updateKarah(state.wins, now.sec);

    const rec = ALL.find(r => r.g === now.key) || state.todayRec;
    const idx = ALL.indexOf(rec);
    const evs = EVENTS.map(e => ({...e, sec: toSec(rec[e.k])}));

    let next = evs.find(e => e.sec > now.sec), nextIsTomorrow = false;
    if(!next){ const tmr = ALL[idx+1] || rec; next = {k:'f', n:'فجر الغد', sec: toSec(tmr.f) + 86400}; nextIsTomorrow = true; }
    let prev = [...evs].reverse().find(e => e.sec <= now.sec);
    if(!prev){ const yst = ALL[idx-1] || rec; prev = {k:'i', n:'عشاء', sec: toSec(yst.i) - 86400}; }

    const remaining = next.sec - now.sec;
    const span = Math.max(1, next.sec - prev.sec);
    DOM.ringProg.style.strokeDashoffset = RING_C * (remaining / span);
    DOM.cdText.innerHTML = `<b>${p2(Math.floor(remaining/3600))}:${p2(Math.floor(remaining%3600/60))}:${p2(remaining%60)}</b>`;

    state.nextK = nextIsTomorrow ? null : next.k;
    updateTimeline(now.sec, state.nextK);

    const key = next.k + '|' + next.sec;
    if(key !== _lastNextKey){
      _lastNextKey = key;
      DOM.nextName.textContent = next.n;
      DOM.nextTime.textContent = hm12(nextIsTomorrow ? rec.f : rec[next.k]);
      DOM.nextPeriod.textContent = period(nextIsTomorrow ? rec.f : rec[next.k]);
      DOM.cards.querySelectorAll('.card').forEach(c => { c.classList.remove('next'); c.querySelector('.flag').hidden = true; });
      if(!nextIsTomorrow){
        const card = document.getElementById('card-' + next.k);
        if(card){ card.classList.add('next'); card.querySelector('.flag').hidden = false; }
      }
    }
  };
  tick(); setInterval(tick, 1000);
}