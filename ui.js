/* ============ التخزين المؤقت لعناصر DOM + العرض ============ */
const DOM = {
  chipGreg: document.getElementById('chipGreg'),
  chipHijri: document.getElementById('chipHijri'),
  chipClock: document.getElementById('chipClock'),
  chipClockPeriod: document.getElementById('chipClockPeriod'),
  todayLabel: document.getElementById('todayLabel'),
  cards: document.getElementById('cards'),
  kChips: document.getElementById('kChips'),
  months: document.getElementById('months'),
  tableSub: document.getElementById('tableSub'),
  tbody: document.getElementById('tbody'),
  tableEl: document.getElementById('monthTable'),
  tableScroll: document.getElementById('tableScroll'),
  jumpToday: document.getElementById('jumpToday'),
  tl: document.getElementById('tl'),
  ringProg: document.getElementById('ringProg'),
  cdText: document.getElementById('cdText'),
  nextName: document.getElementById('nextName'),
  nextTime: document.getElementById('nextTime'),
  nextPeriod: document.getElementById('nextPeriod'),
  updateBar: document.getElementById('updateBar'),
  updateBtn: document.getElementById('updateBtn')
};

function renderChips(rec){
  DOM.chipGreg.textContent = gregLabel(rec);
  DOM.chipHijri.textContent = rec.h + ' 1448هـ';
  DOM.todayLabel.textContent = 'مدينة السويس — ' + gregLabel(rec) + ' • ' + rec.h + ' 1448هـ';
}

function renderCards(rec){
  DOM.cards.innerHTML = '';
  EVENTS.forEach((e, idx) => {
    const card = tpl('tpl-card');
    card.id = 'card-' + e.k;
    card.style.animationDelay = (idx*80) + 'ms';
    card.querySelector('.icon').innerHTML = ICONS[e.k];
    card.querySelector('h3').textContent = e.n;
    card.querySelector('.en').textContent = e.en;
    card.querySelector('.time bdi').textContent = hm12(rec[e.k]);
    card.querySelector('.time small').textContent = period(rec[e.k]);
    DOM.cards.appendChild(card);
  });
}

function renderPills(sel){
  DOM.months.innerHTML = '';
  MKEYS.forEach(k => {
    const b = tpl('tpl-pill');
    b.dataset.m = k;
    b.textContent = MONTHS[k].name;
    b.setAttribute('aria-label', 'عرض جدول ' + MONTHS[k].label);
    b.setAttribute('aria-selected', k === sel ? 'true' : 'false');
    if(k === sel) b.classList.add('on');
    DOM.months.appendChild(b);
  });
}