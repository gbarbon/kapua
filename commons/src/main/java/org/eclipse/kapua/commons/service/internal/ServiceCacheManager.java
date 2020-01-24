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

import javax.cache.Cache;
import java.util.Map;

public class ServiceCacheManager {

    private Map<String, Cache> caches;

    public ServiceCacheManager(Map<String, Cache> caches) {
        this.caches = caches;
    }

    public Cache getCache(String cacheName) {
        return caches.get(cacheName);
    }

}
