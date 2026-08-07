/* ============ اشتقاقات البيانات ============ */
const EVENTS = [
  {k:'f', n:'فجر', en:'FAJR'}, {k:'s', n:'شروق', en:'SUNRISE'}, {k:'d', n:'ظهر', en:'DUHR'},
  {k:'a', n:'عصر', en:'ASR'}, {k:'m', n:'مغرب', en:'MAGHRIB'}, {k:'i', n:'عشاء', en:'ISHA'}
];

/* أوقات الكراهة (حنفي) — ثوابت بالدقائق قابلة للضبط */
const K_SUNRISE = 20;  /* بعد الشروق حتى ارتفاع الشمس */
const K_ZENITH  = 10;  /* قبل الظهر (الاستواء) */
const K_ISFIRAR = 30;  /* قبل المغرب (اصفرار الشمس) */

const ICONS = {
  f:'<svg aria-hidden="true" width="27" height="27" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3.5 18h17"/><path d="M8.3 18a3.7 3.7 0 0 1 7.4 0"/><path d="M12 10.8V9"/><path d="M6.4 12.8 5.2 11.6"/><path d="M17.6 12.8l1.2-1.2"/><circle cx="18.6" cy="5.6" r="1" fill="currentColor" stroke="none"/></svg>',
  s:'<svg aria-hidden="true" width="27" height="27" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3.5 18h17"/><path d="M8.3 18a3.7 3.7 0 0 1 7.4 0"/><path d="M12 9.4V4.8"/><path d="M9.8 7 12 4.8 14.2 7"/><path d="M5.6 12.6 4.4 11.4"/><path d="M18.4 12.6l1.2-1.2"/></svg>',
  d:'<svg aria-hidden="true" width="27" height="27" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3.6"/><path d="M12 4.6V3"/><path d="M12 21v-1.6"/><path d="M4.6 12H3"/><path d="M21 12h-1.6"/><path d="m6.2 6.2 1.1 1.1"/><path d="m17.8 17.8-1.1-1.1"/><path d="m6.2 17.8 1.1-1.1"/><path d="m17.8 6.2-1.1 1.1"/></svg>',
  a:'<svg aria-hidden="true" width="27" height="27" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="13.6" r="3.2"/><path d="M12 7.6V6"/><path d="M5.2 13.6H3.6"/><path d="M20.4 13.6h-1.6"/><path d="M6.9 8.7 5.8 7.6"/><path d="m17.1 8.7 1.1-1.1"/></svg>',
  m:'<svg aria-hidden="true" width="27" height="27" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3.5 18h17"/><path d="M9 18a3 3 0 0 1 6 0"/><path d="M12 4.6v4.2"/><path d="M9.8 6.6 12 8.8l2.2-2.2"/><path d="M5.6 12.6 4.4 11.4"/><path d="M18.4 12.6l1.2-1.2"/></svg>',
  i:'<svg aria-hidden="true" width="27" height="27" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M19.2 13.4A7.6 7.6 0 1 1 10.6 4.8a6 6 0 0 0 8.6 8.6Z"/><path d="M17.8 4.4v3"/><path d="M16.3 5.9h3"/></svg>'
};

/* تطبيع الصفوف مرة واحدة */
const MONTHS = PRAYER_DATA;
Object.values(MONTHS).forEach(m => m.rows = m.rows.map(r => ({g:r[0],h:r[1],f:r[2],s:r[3],d:r[4],a:r[5],m:r[6],i:r[7]})));
const MKEYS = Object.keys(MONTHS);
const ALL = MKEYS.flatMap(k => MONTHS[k].rows);