const CACHE_NAME = 'wfm-v31';
const ASSETS = ['./', './index.html', './manifest.json', './sw.js'];

self.addEventListener('install', event => {
  event.waitUntil(caches.open(CACHE_NAME).then(cache => cache.addAll(ASSETS)));
  self.skipWaiting();
});

self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
    )
  );
  self.clients.claim();
});

// Network-first: 優先攞網絡最新版，離線先 fallback cache
self.addEventListener('fetch', event => {
  event.respondWith(
    fetch(event.request).then(response => {
      // 網絡成功：更新 cache 並返回最新版
      if (response && response.status === 200) {
        caches.open(CACHE_NAME).then(cache => cache.put(event.request, response.clone()));
      }
      return response;
    }).catch(() => {
      // 離線：用 cache
      return caches.match(event.request);
    })
  );
});
