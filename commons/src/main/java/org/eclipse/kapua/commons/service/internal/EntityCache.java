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
import org.eclipse.kapua.model.query.KapuaListResult;

import javax.cache.Cache;
import java.io.Serializable;
import java.util.Objects;

public class EntityCache {

    protected Cache<Serializable, Serializable> idCache;
    protected Cache<Serializable, Serializable> listsCache;

    public EntityCache(String idCacheName) {
        idCache = KapuaCacheManager.getCache(idCacheName);
        listsCache = KapuaCacheManager.getCache(idCacheName+"list");
    }

    public KapuaEntity get(KapuaId scopeId, KapuaId kapuaId) {
        if (kapuaId != null) {
            KapuaEntity entity = (KapuaEntity) idCache.get(kapuaId);
            return checkResult(scopeId, entity);
        }
        return null;
    }

    public KapuaListResult getList(KapuaId scopeId, KapuaId kapuaId) {
        if (kapuaId != null) {
            return (KapuaListResult) listsCache.get(new CacheKey(scopeId, kapuaId));
        }
        return null;
    }

    public KapuaListResult getList(KapuaId scopeId, String secondKey) {
        if (secondKey != null && secondKey.trim().length()>0) {
            return (KapuaListResult) listsCache.get(new CacheKey(scopeId, secondKey));
        }
        return null;
    }

    public void put(KapuaEntity entity) {
        if (entity != null) {
            idCache.put(entity.getId(), entity);
        }
    }

    public void putList(KapuaId scopeId, KapuaId kapuaId, KapuaListResult list) {
        if (list != null) {
            listsCache.put(new CacheKey(scopeId, kapuaId), list);
        }
    }

    public void putList(KapuaId scopeId, String secondKey, KapuaListResult list) {
        if (list != null) {
            listsCache.put(new CacheKey(scopeId, secondKey), list);
        }
    }

    public void remove(KapuaId scopeId, KapuaEntity entity) {
        remove(scopeId, entity.getId());
    }

    public void remove(KapuaId scopeId, KapuaId kapuaId) {
        // First get the entity in order to perform a check of the scope id
        if (kapuaId != null) {
            KapuaEntity entity = (KapuaEntity) get(scopeId, kapuaId);
            if (entity != null) {
                idCache.remove(kapuaId);
            }
        }
        // TODO: invalidate the corresponding id also on the listsCache
    }

    // TODO: need to implement a removeList method too?

    protected KapuaEntity checkResult(KapuaId scopeId, KapuaEntity entity) {
        if (entity != null) {
            if (scopeId == null) {
                return entity;
            } else if (entity.getScopeId() == null) {
                return entity;
            } else if (entity.getScopeId().equals(scopeId)) {
                return entity;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    protected KapuaListResult checkResult(KapuaId scopeId, KapuaListResult entity) {
        if (entity != null) {
            if (entity.getSize()==0) {
                return entity;  // If the list is empty, I want to return the empty list
            } else if (scopeId == null) {
                return entity;
            } else if (entity.getFirstItem().getScopeId() == null) {
                return entity;
            } else if (entity.getFirstItem().getScopeId().equals(scopeId)) {
                return entity;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public class CacheKey implements Serializable {

        private Serializable firstKey;
        private Serializable secondKey;

        public CacheKey(Serializable firstKey, Serializable secondKey) {
            this.firstKey = firstKey;
            this.secondKey = secondKey;
        }

        @Override
        public int hashCode() {
            return Objects.hash(firstKey, secondKey);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CacheKey otherCacheKey = (CacheKey) obj;
            return Objects.equals(firstKey, otherCacheKey.getFirstKey()) && Objects.equals(secondKey,
                    otherCacheKey.getSecondKey());
        }

        public Serializable getFirstKey() {
            return firstKey;
        }

        public void setFirstKey(Serializable firstKey) {
            this.firstKey = firstKey;
        }

        public Serializable getSecondKey() {
            return secondKey;
        }

        public void setSecondKey(Serializable secondKey) {
            this.secondKey = secondKey;
        }
    }

}
