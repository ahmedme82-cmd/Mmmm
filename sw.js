/* ============================================================
   Service Worker — تحميل فوري + تخزين الخطوط والأيقونات + تحديث
   عند نشر نسخة جديدة: غيّر CACHE_VERSION ليظهر زر «تحديث الآن».
============================================================ */
const CACHE_VERSION = 'mawaqit-v1';
const CORE = [
  './','index.html','styles.css','fonts.css',
  'prayer-data.js','utils.js','data.js','ui.js','table.js',
  'karah.js','timeline.js','clock.js','pwa.js','app.js'
];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE_VERSION).then(c => c.addAll(CORE)));
});

self.addEventListener('activate', e => {
  e.waitUntil((async () => {
    const keys = await caches.keys();
    await Promise.all(keys.filter(k => k !== CACHE_VERSION).map(k => caches.delete(k)));
    await self.clients.claim();
  })());
});

self.addEventListener('message', e => {
  if(e.data && e.data.type === 'SKIP_WAITING') self.skipWaiting();
});

self.addEventListener('fetch', e => {
  const req = e.request;
  if(req.method !== 'GET') return;
  const url = new URL(req.url);

  /* خطوط CDN: stale-while-revalidate */
  if(url.hostname.includes('cdn.jsdelivr.net')){
    e.respondWith((async () => {
      const cache = await caches.open(CACHE_VERSION);
      const hit = await cache.match(req);
      const net = fetch(req).then(res => { if(res && (res.ok || res.type === 'opaque')) cache.put(req, res.clone()); return res; }).catch(() => hit);
      return hit || net;
    })());
    return;
  }

  /* نفس الأصل: cache-first مع سقوط للشبكة */
  if(url.origin === location.origin){
    e.respondWith((async () => {
      const cache = await caches.open(CACHE_VERSION);
      const hit = await cache.match(req);
      if(hit) return hit;
      const res = await fetch(req);
      if(res && res.ok) cache.put(req, res.clone());
      return res;
    })());
  }
});