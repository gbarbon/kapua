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

import org.eclipse.kapua.model.KapuaEntity;
import org.eclipse.kapua.model.id.KapuaId;

import javax.cache.Cache;
import java.io.Serializable;

public class SecondIdCache extends EntityCache {

    protected Cache<Serializable, Serializable> secondIdCache;

    public SecondIdCache(String idCacheName, String nameCacheName) {
        super(idCacheName);
        secondIdCache = KapuaCacheManager.getCache(nameCacheName);
    }

    public Serializable get(KapuaId scopeId, String name) {
        KapuaEntity entity = (KapuaEntity) secondIdCache.get(name);
        return checkResult(scopeId, entity);
    }

    @Override
    public void put(KapuaEntity entity) {
        throw new UnsupportedOperationException();
    }

    public void put(KapuaEntity entity, String secondId) {
        idCache.put(entity.getId(), entity);
        secondIdCache.put(secondId, entity);
    }

    @Override
    public void remove(KapuaId scopeId, KapuaId kapuaId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(KapuaId scopeId, KapuaEntity entity) {
        throw new UnsupportedOperationException();
    }

    public void remove(KapuaId scopeId, KapuaEntity entity, String secondId) {
        remove(scopeId, entity.getId(), secondId);
    }

    public void remove(KapuaId scopeId, KapuaId kapuaId, String secondId) {
        // First get the entity in order to perform a check of the scope id
        KapuaEntity entity = (KapuaEntity) get(scopeId, kapuaId);
        if (entity != null) {
            idCache.remove(kapuaId);
            secondIdCache.remove(secondId);
        }
    }
}
