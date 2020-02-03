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
import org.eclipse.kapua.commons.service.internal.SecondIdCache;

public abstract class AbstractSecondIdEntityCacheFactory extends AbstractEntityCacheFactory {

    private String secondIdCacheName;

    public AbstractSecondIdEntityCacheFactory(String idCacheName, String secondIdCacheName) {
        super(idCacheName);
        this.secondIdCacheName = secondIdCacheName;
    }

    public String getSecondIdCacheName() {
        return secondIdCacheName;
    }

    @Override
    public EntityCache createCache() {
        return new SecondIdCache(getEntityIdCacheName(), getSecondIdCacheName());
    }
}
