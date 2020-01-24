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

import org.eclipse.kapua.commons.service.internal.jcachetest.JCacheCachingProvider;
import org.eclipse.kapua.commons.service.internal.jcachetest.JCacheConfiguration;
import org.eclipse.kapua.model.KapuaNamedEntity;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class KapuaCacheManager {

    private static Map<String, Cache<String, KapuaNamedEntity>> cacheMap = new HashMap<>();

    private KapuaCacheManager() {
    }

    // TODO: use an entity for the key, composed of two KapuaId (scopeId + EntityId) -> for the one using Id
    //  For the one using the name, (scopeId, String)

    private static Cache<String, KapuaNamedEntity> addCache(String cacheName) {
        JCacheConfiguration<String, KapuaNamedEntity> config = new JCacheConfiguration<>();
        CacheManager manager = JCacheCachingProvider.getInstance().getCacheManager();
        Cache<String, KapuaNamedEntity> cache = manager.createCache(cacheName, config);
        cacheMap.put(cacheName, cache);
        return cache;
    }

    public static Cache<String, KapuaNamedEntity> getCache(String cacheName) {
        //TODO check configuration to choose the proper cache type to instantiate
        Cache<String, KapuaNamedEntity> kapuaCache = cacheMap.get(cacheName);
        if (kapuaCache == null) {
            synchronized (cacheMap) {
                kapuaCache = cacheMap.get(cacheName);
                if (kapuaCache == null) {
                    kapuaCache = addCache(cacheName);
                }
            }
        }
        return kapuaCache;
    }

    public static ServiceCacheManager<String, KapuaNamedEntity> getServiceCacheManager(Collection<String> cachesNames) {
        Map<String, Cache<String, KapuaNamedEntity>> serviceCacheMap = new HashMap<>();
        cachesNames.forEach((cacheName) -> serviceCacheMap.put(cacheName, getCache(cacheName)));
        return new ServiceCacheManager<>(serviceCacheMap);
    }

    public static void invalidateAll() {
        cacheMap.forEach((cacheKey, cache) -> cache.clear());
    }

}
