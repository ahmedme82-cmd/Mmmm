/* ============ PWA: أيقونات + manifest + Service Worker + تحديث ============ */
(function(){
  /* أيقونات مولّدة (192/512 + maskable) */
  function roundRectPath(ctx,x,y,w,h,r){ctx.beginPath();ctx.moveTo(x+r,y);ctx.arcTo(x+w,y,x+w,y+h,r);ctx.arcTo(x+w,y+h,x,y+h,r);ctx.arcTo(x,y+h,x,y,r);ctx.arcTo(x,y,x+w,y,r);ctx.closePath();}
  function star8(ctx,cx,cy,r){ctx.save();ctx.translate(cx,cy);[0,Math.PI/4].forEach(a=>{ctx.save();ctx.rotate(a);ctx.strokeRect(-r*0.7,-r*0.7,r*1.4,r*1.4);ctx.restore();});ctx.restore();}
  function star4(ctx,cx,cy,r){ctx.beginPath();ctx.moveTo(cx,cy-r);ctx.lineTo(cx+r*0.35,cy-r*0.35);ctx.lineTo(cx+r,cy);ctx.lineTo(cx+r*0.35,cy+r*0.35);ctx.lineTo(cx,cy+r);ctx.lineTo(cx-r*0.35,cy+r*0.35);ctx.lineTo(cx-r,cy);ctx.lineTo(cx-r*0.35,cy-r*0.35);ctx.closePath();ctx.fill();}
  function makeIcon(px, maskable){
    const c = document.createElement('canvas'); c.width = c.height = px;
    const x = c.getContext('2d'), s = px;
    const g = x.createLinearGradient(0,0,s,s); g.addColorStop(0,'#177a6c'); g.addColorStop(1,'#072b26');
    if(maskable){ x.fillStyle = g; x.fillRect(0,0,s,s); } else { roundRectPath(x,0,0,s,s,s*0.18); x.fillStyle = g; x.fill(); }
    x.save(); x.globalAlpha = .10; x.strokeStyle = '#fff'; x.lineWidth = s*0.012;
    star8(x, s*0.16, s*0.20, s*0.10); star8(x, s*0.86, s*0.78, s*0.11); star8(x, s*0.80, s*0.14, s*0.06);
    x.restore();
    const cx = s*0.46, cy = s*0.50, R = s*(maskable?0.23:0.26);
    x.fillStyle = '#d3a545'; x.beginPath(); x.arc(cx,cy,R,0,7); x.fill();
    x.fillStyle = g; x.beginPath(); x.arc(cx+R*0.42, cy-R*0.18, R*0.82, 0, 7); x.fill();
    x.fillStyle = '#e6b354'; star4(x, cx+R*0.95, cy-R*0.42, s*0.07);
    x.strokeStyle = 'rgba(211,165,69,.9)'; x.lineWidth = s*0.03; x.lineCap = 'round';
    x.beginPath(); x.arc(s*0.5, s*1.08, s*0.62, Math.PI*1.22, Math.PI*1.78); x.stroke();
    return c.toDataURL('image/png');
  }
  const icon192 = makeIcon(192,false), icon512 = makeIcon(512,false), icon512m = makeIcon(512,true);

  /* manifest (blob بنفس أصل الصفحة) — يمنح التثبيت وشاشة البداية الملونة */
  const manifest = {
    name:'مواقيت الصلاة — السويس', short_name:'مواقيت الصلاة',
    description:'مواقيت الصلاة وأوقات الكراهة لمدينة السويس — أغسطس إلى ديسمبر 2026',
    lang:'ar', dir:'rtl',
    start_url: location.href, scope: new URL('.', location.href).href, id: location.pathname,
    display:'standalone', orientation:'portrait',
    theme_color:'#0b3d36', background_color:'#f7f4ec',
    icons:[
      {src:icon192, sizes:'192x192', type:'image/png', purpose:'any'},
      {src:icon512, sizes:'512x512', type:'image/png', purpose:'any'},
      {src:icon512m, sizes:'512x512', type:'image/png', purpose:'maskable'}
    ]
  };
  const mLink = document.createElement('link'); mLink.rel = 'manifest';
  mLink.href = URL.createObjectURL(new Blob([JSON.stringify(manifest)], {type:'application/manifest+json'}));
  document.head.appendChild(mLink);

  /* Service Worker + إشعار التحديث */
  const secure = location.protocol === 'https:' || ['localhost','127.0.0.1'].includes(location.hostname);
  if(!('serviceWorker' in navigator) || !secure) return;

  navigator.serviceWorker.register('sw.js').then(reg => {
    reg.addEventListener('updatefound', () => {
      const nw = reg.installing; if(!nw) return;
      nw.addEventListener('statechange', () => {
        if(nw.state === 'installed' && navigator.serviceWorker.controller){
          DOM.updateBar.hidden = false;   /* نسخة جديدة متاحة */
        }
      });
    });
  }).catch(() => {});

  navigator.serviceWorker.addEventListener('controllerchange', () => location.reload());
  DOM.updateBtn.addEventListener('click', () => {
    navigator.serviceWorker.getRegistration().then(reg => {
      if(reg && reg.waiting) reg.waiting.postMessage({type:'SKIP_WAITING'});
    });
  });
})();