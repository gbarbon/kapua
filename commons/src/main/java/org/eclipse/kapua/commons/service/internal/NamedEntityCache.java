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
import org.eclipse.kapua.model.KapuaNamedEntity;
import org.eclipse.kapua.model.id.KapuaId;

import javax.cache.Cache;
import java.io.Serializable;

public class NamedEntityCache extends EntityCache {

    protected Cache<Serializable, Serializable> nameCache;

    public NamedEntityCache(String idCacheName, String nameCacheName) {
        super(idCacheName);
        nameCache = KapuaCacheManager.getCache(nameCacheName);
    }

    public KapuaEntity get(KapuaId scopeId, String name) {
        if (name != null && name.trim().length() > 0) {
            KapuaId entityId = (KapuaId) nameCache.get(name);
            return get(scopeId, entityId);
        }
        return null;
    }

    @Override
    public void put(KapuaEntity entity) {
        put((KapuaNamedEntity) entity);
    }

    public void put(KapuaNamedEntity entity) {
        if (entity != null) {
            idCache.put(entity.getId(), entity);
            nameCache.put(entity.getName(), entity.getId());
        }
    }

    @Override
    public void remove(KapuaId scopeId, KapuaId kapuaId) {
        if (kapuaId != null) {
            // First get the entity in order to perform a check of the scope id
            KapuaNamedEntity entity = (KapuaNamedEntity) get(scopeId, kapuaId);
            if (entity != null) {
                int idSizeBefore = ((JCacheHashMapCache) idCache).size();
                int nameSizeBefore = ((JCacheHashMapCache) nameCache).size();
                idCache.remove(kapuaId);
                nameCache.remove(entity.getName());
                int idSizeAfter = ((JCacheHashMapCache) idCache).size();
                int nameSizeAfter = ((JCacheHashMapCache) nameCache).size();
                cacheRemoval.inc();
                //LOGGER.info("Cache removal for entity with scopeId {} and kapuaId {}", scopeId, kapuaId);
                //LOGGER.info("Removing {} from idCache. Size before: {} and after: {} ", kapuaId, idSizeBefore,
                // idSizeAfter);
                //LOGGER.info("Removing {} from nameCache. Size before: {} and after: {} ", entity.getName(),
                // nameSizeBefore, nameSizeAfter);
                //printStackTrace("Removal (namedEntity)", idCache, nameCache, kapuaId);
                StringBuilder str = new StringBuilder();
                str.append(printCacheContent("Removal (namedEntity)", kapuaId, idCache, "idCache"));
                str.append(printCacheContent("Removal (namedEntity)", entity.getName(), nameCache, "nameCache"));
                LOGGER.info("{}", str);
            }
        }
    }

    public StringBuilder printCacheContent(Serializable id1, Serializable id2) {
        StringBuilder str = new StringBuilder();
        str.append(printCacheContent("get", id1, idCache, "idCache"));
        str.append(printCacheContent("get", id2, nameCache, "nameCache"));
        return str;
    }

    public StringBuilder printNameCacheContent(Serializable id2) {
        StringBuilder str = new StringBuilder();
        str.append(printCacheContent("get", id2, nameCache, "nameCache"));
        return str;
    }
}
