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

    public KapuaEntity get(KapuaId scopeId, String secondId) {
        if (secondId != null && secondId.trim().length() > 0) {
            KapuaId entityId = (KapuaId) secondIdCache.get(secondId);
            return get(scopeId, entityId);
        }
        return null;
    }

    @Override
    public void put(KapuaEntity entity) {
        throw new UnsupportedOperationException();
    }

    public void put(KapuaEntity entity, String secondId) {
        if (secondId != null) {
            idCache.put(entity.getId(), entity);
            secondIdCache.put(secondId, entity.getId());
        }
    }

    @Override
    public KapuaEntity remove(KapuaId scopeId, KapuaId kapuaId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KapuaEntity remove(KapuaId scopeId, KapuaEntity entity) {
        throw new UnsupportedOperationException();
    }

    public KapuaEntity remove(KapuaId scopeId, KapuaEntity entity, String secondId) {
        return remove(scopeId, entity.getId(), secondId);
    }

    public KapuaEntity remove(KapuaId scopeId, KapuaId kapuaId, String secondId) {
        if (kapuaId != null && secondId != null && secondId.trim().length() > 0) {
            // First get the entity in order to perform a check of the scope id
            KapuaEntity entity = (KapuaEntity) get(scopeId, kapuaId);
            if (entity != null) {
                idCache.remove(kapuaId);
                secondIdCache.remove(secondId);
                cacheRemoval.inc();
                return entity;
            }
        }
        return null;
    }
}
