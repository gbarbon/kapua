package org.eclipse.kapua.commons.service.internal;

import java.util.Map;

public class ServiceCacheManager {

    private Map<String, KapuaCache> caches;

    public ServiceCacheManager(Map<String, KapuaCache> caches) {
        this.caches = caches;
    }

    public KapuaCache getCache(String cacheName) {
        return caches.get(cacheName);
    }

}
