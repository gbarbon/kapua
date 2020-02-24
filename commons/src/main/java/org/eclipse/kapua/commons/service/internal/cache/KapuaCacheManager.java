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
package org.eclipse.kapua.commons.service.internal.cache;

import org.eclipse.kapua.commons.setting.system.SystemSetting;
import org.eclipse.kapua.commons.setting.system.SystemSettingKey;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class KapuaCacheManager {

    private static final SystemSetting SYSTEM_SETTING = SystemSetting.getInstance();
    private static final String DEFAULT_CACHING_PROVIDER =
            "org.eclipse.kapua.commons.service.internal.cache.dummy.CachingProvider";
    private static final String CACHING_PROVIDER_CLASS_NAME = SYSTEM_SETTING.getString(SystemSettingKey.CACHING_PROVIDER,
            DEFAULT_CACHING_PROVIDER);  // use the dummy cache if no provider exists

    private static Map<String, Cache<Serializable, Serializable>> cacheMap = new HashMap<>();

    private KapuaCacheManager() {
    }

    public static Cache<Serializable, Serializable> getCache(String cacheName) {
        Cache<Serializable, Serializable> cache = cacheMap.get(cacheName);
        if (cache == null) {
            synchronized (cacheMap) {
                cache = cacheMap.get(cacheName);
                if (cache == null) {
                    MutableConfiguration<Serializable, Serializable> config = new MutableConfiguration<>();
                    cache = Caching.getCachingProvider(CACHING_PROVIDER_CLASS_NAME).getCacheManager().createCache(cacheName, config);
                    cacheMap.put(cacheName, cache);
                }
            }
        }
        return cache;
    }

    // TODO: only used by tests?
    public static void invalidateAll() {
        cacheMap.forEach((cacheKey, cache) -> cache.clear());
    }

    // TODO: create an invalidateByAccount?

}
