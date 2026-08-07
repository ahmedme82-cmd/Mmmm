/* ============ التجميع والحالة ============ */
const state = {
  curKey: cairoNow().key,
  todayRec: null,
  selMonth: null,
  wins: [],
  nextK: null
};
state.todayRec = ALL.find(r => r.g === state.curKey) || ALL[5];
state.selMonth = MKEYS.includes(state.curKey.slice(0,7)) ? state.curKey.slice(0,7) : '2026-08';

function renderDay(){
  renderChips(state.todayRec);
  renderCards(state.todayRec);
  state.wins = computeWindows(state.todayRec);
  renderKarah(state.wins);
  renderTable(state.selMonth);
  buildTimeline(state.todayRec, state.wins);
  resetNextFlag();
}

function onRollover(rec0, now){
  state.todayRec = rec0;
  const mk = now.key.slice(0,7);
  if(MKEYS.includes(mk) && mk !== state.selMonth){ state.selMonth = mk; renderPills(state.selMonth); }
  renderDay();
}

/* تبديل الأشهر مع خفوت الجدول */
DOM.months.addEventListener('click', e => {
  const b = e.target.closest('.m-pill'); if(!b || b.dataset.m === state.selMonth) return;
  state.selMonth = b.dataset.m;
  renderPills(state.selMonth);
  fadeSwap(DOM.tableEl, () => renderTable(state.selMonth));
});

DOM.jumpToday.addEventListener('click', () => {
  const mk = state.todayRec.g.slice(0,7);
  if(mk !== state.selMonth){
    state.selMonth = mk; renderPills(mk); renderTable(mk);
  }
  const row = document.getElementById('row-' + state.todayRec.g);
  if(row) row.scrollIntoView({behavior:'smooth', block:'center'});
});

renderDay();
renderPills(state.selMonth);
startClock(state, onRollover);