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
package org.eclipse.kapua.commons.service.internal.jcachetest;

import javax.cache.CacheManager;
import javax.cache.configuration.OptionalFeature;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.util.Properties;

public class JCacheCachingProvider implements CachingProvider {

    private static JCacheCachingProvider instance;

    public static JCacheCachingProvider getInstance() {
        if (instance == null) {
            synchronized (JCacheCachingProvider.class) {
                if (instance == null) {
                    instance = new JCacheCachingProvider();
                }
            }
        }
        return instance;
    }

    @Override
    public CacheManager getCacheManager(URI uri, ClassLoader classLoader, Properties properties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoader getDefaultClassLoader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getDefaultURI() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Properties getDefaultProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CacheManager getCacheManager(URI uri, ClassLoader classLoader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CacheManager getCacheManager() {
        return JCacheCacheManager.getInstance();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close(ClassLoader classLoader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close(URI uri, ClassLoader classLoader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSupported(OptionalFeature optionalFeature) {
        throw new UnsupportedOperationException();
    }
}
