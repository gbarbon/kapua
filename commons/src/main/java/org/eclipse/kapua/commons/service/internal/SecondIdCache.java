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

import org.eclipse.kapua.commons.service.internal.jcachetest.JCacheHashMapCache;
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
        if (kapuaId != null && secondId != null && secondId.trim().length() > 0) {
            // First get the entity in order to perform a check of the scope id
            KapuaEntity entity = (KapuaEntity) get(scopeId, kapuaId);
            if (entity != null) {
                int idSizeBefore = ((JCacheHashMapCache) idCache).size();
                int secondSizeBefore = ((JCacheHashMapCache) secondIdCache).size();
                idCache.remove(kapuaId);
                secondIdCache.remove(secondId);
                int idSizeAfter = ((JCacheHashMapCache) idCache).size();
                int secondSizeAfter = ((JCacheHashMapCache) secondIdCache).size();
                cacheRemoval.inc();
                //LOGGER.info("Cache removal for entity with scopeId {} and kapuaId {}", scopeId, kapuaId);
                //LOGGER.info("Removing {} from idCache. Size before: {} and after: {} ", kapuaId, idSizeBefore,
                // idSizeAfter);
                //LOGGER.info("Removing {} from secondIdCache. Size before: {} and after: {} ", secondId,
                // secondSizeBefore, secondSizeAfter);
                //printStackTrace("Removal (SecondIdCache)", idCache, secondIdCache, kapuaId);
                StringBuilder str = new StringBuilder();
                str.append(printCacheContent("Removal (SecondIdCache)", kapuaId, idCache, "idCache"));
                str.append(printCacheContent("Removal (SecondIdCache)", secondId, secondIdCache,
                        "secondIdCache"));
                LOGGER.info("{}", str);
            }
        }
    }

    public StringBuilder printCacheContent(Serializable id1, Serializable id2) {
        StringBuilder str = new StringBuilder();
        str.append(printCacheContent("get", id1, idCache, "idCache"));
        str.append(printCacheContent("get", id2, secondIdCache, "secondIdCache"));
        return str;
    }

    public StringBuilder printSecondIdCacheContent(Serializable id2) {
        StringBuilder str = new StringBuilder();
        str.append(printCacheContent("get", id2, secondIdCache, "secondIdCache"));
        return str;
    }
}
