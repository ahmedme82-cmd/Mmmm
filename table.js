/* ============ بناء جدول الشهر ============ */
function renderTable(monthKey){
  const meta = MONTHS[monthKey];
  DOM.tableSub.textContent = meta.label + 'م — الموافق ' + meta.hijri;
  const frag = document.createDocumentFragment();
  meta.rows.forEach(r => {
    const dt = dateOf(r.g);
    const tr = tpl('tpl-row');
    if(dt.getDay() === 5) tr.classList.add('friday');
    if(r.g === state.todayRec.g) tr.classList.add('today');
    tr.id = 'row-' + r.g;
    const wd = tr.querySelector('.wd');
    wd.textContent = WD_FMT.format(dt);
    if(r.g === state.todayRec.g){
      const pill = document.createElement('span');
      pill.className = 'pill'; pill.textContent = 'اليوم';
      wd.appendChild(pill);
    }
    tr.querySelector('.g bdi').textContent = r.g;
    tr.querySelector('.hj').textContent = r.h + ' 1448';
    const tds = tr.querySelectorAll('td.t');
    [r.f, r.s, r.d, r.a, r.m, r.i].forEach((hm, i) => {
      tds[i].querySelector('bdi').textContent = hm12(hm);
      tds[i].querySelector('small').textContent = period(hm);
    });
    frag.appendChild(tr);
  });
  DOM.tbody.innerHTML = '';
  DOM.tbody.appendChild(frag);
}