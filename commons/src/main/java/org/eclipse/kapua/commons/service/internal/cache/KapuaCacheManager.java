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

import org.eclipse.kapua.KapuaErrorCodes;
import org.eclipse.kapua.KapuaRuntimeException;
import org.eclipse.kapua.commons.setting.KapuaSettingException;
import org.eclipse.kapua.commons.setting.system.SystemSetting;
import org.eclipse.kapua.commons.setting.system.SystemSettingKey;
import org.eclipse.kapua.commons.util.KapuaFileUtils;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KapuaCacheManager {

    private static final SystemSetting SYSTEM_SETTING = SystemSetting.getInstance();
    private static final String CACHING_PROVIDER_CLASS_NAME = SYSTEM_SETTING.getString(SystemSettingKey.CACHING_PROVIDER);
    private static final Map<String, Cache<Serializable, Serializable>> CACHE_MAP = new ConcurrentHashMap<>();
    private static final URI JCACHE_CONFIG_URI = getJcacheConfig();

    private KapuaCacheManager() {
    }

    private static URI getJcacheConfig() {
        String configurationFileName = SystemSetting.getInstance().getString(SystemSettingKey.JCACHE_CONFIG_URL);
        if (configurationFileName != null) {
            try {
                return KapuaFileUtils.getAsURL(configurationFileName).toURI();
            } catch (KapuaSettingException | URISyntaxException e) {
                throw new KapuaRuntimeException(KapuaErrorCodes.INTERNAL_ERROR, e, String.format("Unable to load cache config file (%s)", configurationFileName));
            }
        }
        return null;
    }

    public static Cache<Serializable, Serializable> getCache(String cacheName) {
        Cache<Serializable, Serializable> cache = CACHE_MAP.get(cacheName);
        if (cache == null) {
            synchronized (CACHE_MAP) {
                cache = CACHE_MAP.get(cacheName);
                if (cache == null) {
                    MutableConfiguration<Serializable, Serializable> config = new MutableConfiguration<>();
                    CachingProvider cachingProvider;
                    if (CACHING_PROVIDER_CLASS_NAME != null) {
                        cachingProvider = Caching.getCachingProvider(CACHING_PROVIDER_CLASS_NAME);
                    } else {
                        cachingProvider = Caching.getCachingProvider();
                    }
                    cache = cachingProvider.getCacheManager(JCACHE_CONFIG_URI, null).createCache(cacheName, config);
                    CACHE_MAP.put(cacheName, cache);
                }
            }
        }
        return cache;
    }

    /**
     * Utility method to cleanup the whole cache.
     */
    public static void invalidateAll() {
        CACHE_MAP.forEach((cacheKey, cache) -> cache.clear());
    }

    // TODO: create an invalidateByAccount?

}
