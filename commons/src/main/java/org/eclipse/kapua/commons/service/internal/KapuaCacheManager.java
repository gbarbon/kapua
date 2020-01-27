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

import org.eclipse.kapua.commons.setting.system.SystemSetting;
import org.eclipse.kapua.commons.setting.system.SystemSettingKey;
import org.eclipse.kapua.model.KapuaNamedEntity;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class KapuaCacheManager {

    private static final SystemSetting SYSTEM_SETTING = SystemSetting.getInstance();
    private static final String DUMMY_CACHING_PROVIDER =
            "org.eclipse.kapua.commons.service.internal.dummycache.DummyCachingProvider";
    private static final String CACHING_PROVIDER_CLASS_NAME = SYSTEM_SETTING.getString(SystemSettingKey.CACHING_PROVIDER,
            DUMMY_CACHING_PROVIDER);  // use the dummy cache if no provider exists

    private static Map<String, Cache<Serializable, KapuaNamedEntity>> cacheMap = new HashMap<>();

    private KapuaCacheManager() {
    }

    private static CacheManager getCacheManager(boolean isEnabled) {
        CachingProvider cachingProvider;
        if (isEnabled) {
            cachingProvider = Caching.getCachingProvider(CACHING_PROVIDER_CLASS_NAME);
        } else {
            // cache not enabled for the given service, using the dummy one
            cachingProvider = Caching.getCachingProvider(DUMMY_CACHING_PROVIDER);
        }
        return cachingProvider.getCacheManager();
    }

    public static Cache<Serializable, KapuaNamedEntity> getCache(String cacheName, boolean isEnabled) {
        Cache<Serializable, KapuaNamedEntity> cache = cacheMap.get(cacheName);
        if (cache == null) {
            synchronized (cacheMap) {
                cache = cacheMap.get(cacheName);
                if (cache == null) {
                    MutableConfiguration<Serializable, KapuaNamedEntity> config = new MutableConfiguration<>();
                    cache = getCacheManager(isEnabled).createCache(cacheName, config);
                    cacheMap.put(cacheName, cache);
                }
            }
        }
        return cache;
    }

    /**
     * Get the cache manager for a given service.
     *
     * @param cachesNames collection of caches names for the given service.
     * @return the ServiceCacheManager fro the given service.
     */
    public static ServiceCacheManager<Serializable, KapuaNamedEntity> getServiceCacheManager(Collection<String> cachesNames, boolean isEnabled) {
        Map<String, Cache<Serializable, KapuaNamedEntity>> serviceCacheMap = new HashMap<>();
        cachesNames.forEach((cacheName) -> serviceCacheMap.put(cacheName, getCache(cacheName, isEnabled)));
        return new ServiceCacheManager<>(serviceCacheMap);
    }

    public static void invalidateAll() {
        cacheMap.forEach((cacheKey, cache) -> cache.clear());
    }

}
