/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.commons.service.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CacheManager {

    private static Map<String, KapuaCache> cacheMap = new HashMap<>();

    private CacheManager() {
    }

    public static KapuaCache getCache(String cacheName) {
        //TODO check configuration to choose the proper cache type to instantiate
        KapuaCache kapuaCache = cacheMap.get(cacheName);
        if (kapuaCache ==null) {
            synchronized (cacheMap) {
                kapuaCache = cacheMap.get(cacheName);
                if (kapuaCache == null) {
                    //kapuaCache = new DummyKapuaCache();
                    // kapuaCache = new SimpleRedissonCache();
                    kapuaCache = new HashMapCache();
                    cacheMap.put(cacheName, kapuaCache);
                }
            }
        }
        return kapuaCache;
    }

    public static ServiceCacheManager getServiceCacheManager(Collection<String> cachesNames) {
        Map<String, KapuaCache> serviceCacheMap = new HashMap<>();
        cachesNames.forEach((cacheName) -> serviceCacheMap.put(cacheName, getCache(cacheName)));
        return new ServiceCacheManager(serviceCacheMap);
    }

    public static void invalidateAll() {
        cacheMap.forEach((cacheKey, cache) -> cache.invalidate());
    }

}
