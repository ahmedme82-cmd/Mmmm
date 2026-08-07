/* ============ خط اليوم الزمني (24 ساعة) ============ */
let tlDot = null;
function buildTimeline(rec, wins){
  DOM.tl.innerHTML = '';
  wins.forEach(w => {
    const seg = document.createElement('div');
    seg.className = 'tl-seg';
    seg.style.left  = (w.a/86400*100) + '%';
    seg.style.width = ((w.b-w.a)/86400*100) + '%';
    DOM.tl.appendChild(seg);
  });
  EVENTS.forEach(e => {
    const mk = document.createElement('div');
    mk.className = 'tl-mark'; mk.dataset.k = e.k;
    mk.style.left = (toSec(rec[e.k])/86400*100) + '%';
    mk.title = e.n;
    const tick = document.createElement('i'); tick.setAttribute('aria-hidden','true');
    const lbl = document.createElement('b'); lbl.textContent = hm12(rec[e.k]);
    mk.append(tick, lbl);
    DOM.tl.appendChild(mk);
  });
  tlDot = document.createElement('div');
  tlDot.className = 'tl-dot';
  DOM.tl.appendChild(tlDot);
}
function updateTimeline(sec, nextKey){
  if(tlDot) tlDot.style.left = (sec/86400*100) + '%';
  DOM.tl.querySelectorAll('.tl-mark').forEach(mk => mk.classList.toggle('next', mk.dataset.k === nextKey));
}