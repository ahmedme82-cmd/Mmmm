/* ============ أدوات عامة ============ */
const $ = s => document.querySelector(s);
const p2 = n => String(n).padStart(2,'0');
const toSec = hm => { const p = hm.split(':'); return (+p[0])*3600 + (+p[1])*60; };
const sec2hm = s => `${p2(Math.floor(s/3600)%24)}:${p2(Math.floor(s%3600/60))}`;
const hm12 = hm => { const h = +hm.split(':')[0]; return (h%12===0?12:h%12) + ':' + hm.split(':')[1]; };
const period = hm => (+hm.split(':')[0] < 12) ? 'ص' : 'م';
const dateOf = g => { const p = g.split('-').map(Number); return new Date(p[0], p[1]-1, p[2]); };
const WD_FMT = new Intl.DateTimeFormat('ar',{weekday:'long'});
const GREG_FMT = new Intl.DateTimeFormat('ar-EG-u-nu-latn',{weekday:'long', day:'numeric', month:'long', year:'numeric'});
const gregLabel = rec => GREG_FMT.format(dateOf(rec.g));

function cairoNow(){
  const fmt = new Intl.DateTimeFormat('en-GB',{timeZone:'Africa/Cairo',hourCycle:'h23',year:'numeric',month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit',second:'2-digit'});
  const o = {}; fmt.formatToParts(new Date()).forEach(p => o[p.type] = p.value);
  return { key:`${o.year}-${o.month}-${o.day}`, sec:(+o.hour)*3600 + (+o.minute)*60 + (+o.second), h:+o.hour, mi:+o.minute, s:+o.second };
}

/* تبديل محتوًى مع خفوت قصير (180ms) */
function fadeSwap(el, mutate){
  el.classList.add('swap');
  setTimeout(() => { mutate(); requestAnimationFrame(() => el.classList.remove('swap')); }, 180);
}

/* استنساخ من <template> */
const tpl = id => document.getElementById(id).content.firstElementChild.cloneNode(true);