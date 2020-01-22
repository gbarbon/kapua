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
package org.eclipse.kapua.commons.jpa;

import java.util.Collection;

public class CacheConfigurationFactory {

    private String cacheName;
    private Collection<String> cacheNames;

    /**
     * @deprecated temporarily deprecated
     * @param cacheName
     */
    @Deprecated
    public CacheConfigurationFactory(String cacheName) {
        this.cacheName = cacheName;
    }

    public CacheConfigurationFactory(Collection<String> cacheNames) {
        this.cacheNames = cacheNames;
    }

    /**
     * @deprecated temporarily deprecated
     */
    @Deprecated
    public String getCacheName() {
        return cacheName;
    }

    public Collection<String> getCacheNames() {
        return cacheNames;
    }

//    public void setCacheName(String cacheName) {
//        this.cacheName = cacheName;
//    }

}
