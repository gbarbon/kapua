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

import org.eclipse.kapua.commons.service.internal.EntityCache;
import org.eclipse.kapua.commons.service.internal.ThirdIdCache;

public abstract class AbstractThirdIdEntityCacheFactory extends AbstractEntityCacheFactory {

    private String secondIdCacheName;
    private String thirdIdCacheName;

    public AbstractThirdIdEntityCacheFactory(String idCacheName, String secondIdCacheName, String thirdIdCacheName) {
        super(idCacheName);
        this.secondIdCacheName = secondIdCacheName;
        this.thirdIdCacheName = thirdIdCacheName;
    }

    public String getSecondIdCacheName() {
        return secondIdCacheName;
    }

    public String getThirdIdCacheName() {
        return thirdIdCacheName;
    }

    @Override
    public EntityCache createCache() {
        return new ThirdIdCache(getEntityIdCacheName(), getSecondIdCacheName(), getThirdIdCacheName());
    }
}
