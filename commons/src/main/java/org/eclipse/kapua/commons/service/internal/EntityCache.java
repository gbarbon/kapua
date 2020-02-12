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

import com.codahale.metrics.Counter;
import org.eclipse.kapua.commons.metric.MetricServiceFactory;
import org.eclipse.kapua.commons.service.internal.jcachetest.JCacheHashMapCache;
import org.eclipse.kapua.model.KapuaEntity;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaListResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class EntityCache {

    protected static final Logger LOGGER = LoggerFactory.getLogger(EntityCache.class);
    protected Cache<Serializable, Serializable> idCache;
    protected Cache<Serializable, Serializable> listsCache;
    protected Counter cacheMiss;
    protected Counter cacheHit;
    protected Counter cacheRemoval;

    public EntityCache(String idCacheName) {
        idCache = KapuaCacheManager.getCache(idCacheName);
        listsCache = KapuaCacheManager.getCache(idCacheName + "list");
        cacheMiss = MetricServiceFactory.getInstance().getCounter("cache", "cache", "cache_miss_count");
        cacheHit = MetricServiceFactory.getInstance().getCounter("cache", "cache", "cache_hit_count");
        cacheRemoval = MetricServiceFactory.getInstance().getCounter("cache", "cache", "cache_removal_count");
    }

    public KapuaEntity get(KapuaId scopeId, KapuaId kapuaId) {
        if (kapuaId != null) {
            KapuaEntity entity = (KapuaEntity) idCache.get(kapuaId);
            entity = checkResult(scopeId, entity);
            if (entity == null) {
                cacheMiss.inc();
                //LOGGER.info("Cache miss for entity with scopeId {} and secondId {}", scopeId, kapuaId);
                //printStackTrace("Get (miss)", idCache, kapuaId);
                LOGGER.info("{}", printCacheContent("Get (miss)", kapuaId, idCache, "idCache"));
            } else {
                cacheHit.inc();
            }
            return entity;
        }
        return null;
    }

    public KapuaListResult getList(KapuaId scopeId, Serializable secondId) {
        if (secondId != null) {
            if (secondId instanceof String && ((String) secondId).trim().length() == 0) {
                return null;
            }
            KapuaListResult entity = (KapuaListResult) listsCache.get(new CacheKey(scopeId, secondId));
            if (entity == null) {
                cacheMiss.inc();
                //LOGGER.info("Cache miss for entity with scopeId {} and secondId {}", scopeId, secondId);
                //printStackTrace("GetList (miss)", listsCache, secondId);
                LOGGER.info("{}", printCacheContent("GetList (miss)", secondId, listsCache, "idCache"));
            } else {
                cacheHit.inc();
            }
            return entity;
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
                int sizeBefore = ((JCacheHashMapCache) idCache).size();
                idCache.remove(kapuaId);
                int sizeAfter = ((JCacheHashMapCache) idCache).size();
                cacheRemoval.inc();
                //LOGGER.info("Cache removal for entity with scopeId {} and kapuaId {}", scopeId, kapuaId);
                //LOGGER.info("Removing {} from idCache. Size before: {} and after: {} ", kapuaId, sizeBefore,
                // sizeAfter);
                //printStackTrace("Removal", idCache, kapuaId);
                LOGGER.info("{}", printCacheContent("Removal", kapuaId, idCache, "idCache"));
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
            if (entity.getSize() == 0) {
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

    protected void printStackTrace(String operation, Cache cache, Serializable id) {
        StringBuilder str = new StringBuilder();
        str.append("Operation ").append(operation).append(" on cache ").append(cache).append(" on entity ").append(id.toString()).append("\n");
        for (int i = 0; i < 20; i++) {
            str.append(Thread.currentThread().getStackTrace()[i].getClassName())
                    .append("[")
                    .append(Thread.currentThread().getStackTrace()[i].getLineNumber())
                    .append("]\n");
        }
        LOGGER.info("{}", str);
    }

    protected void printStackTrace(String operation, Cache cache1, Cache cache2, Serializable id) {
        StringBuilder str = new StringBuilder();
        str.append("Operation ").append(operation).append(" on caches ").append(cache1).append(" and ").append(cache2).append(" on entity ").append(id.toString()).append("\n");
        for (int i = 0; i < 20; i++) {
            str.append(Thread.currentThread().getStackTrace()[i].getClassName())
                    .append("[")
                    .append(Thread.currentThread().getStackTrace()[i].getLineNumber())
                    .append("]\n");
        }
        LOGGER.info("{}", str);
    }

    protected StringBuilder printCacheContent(String operation, Serializable id, Cache cache, String cacheName) {
        StringBuilder str = new StringBuilder();
        str.append("\nPrinting cache ").append(cacheName).append("\n");
        str.append("After operation ").append(operation).append(" on entity ").append(id.toString()).append("\n");
        for (Object entry : ((JCacheHashMapCache) cache).entrySet()) {
            str.append(((Map.Entry) entry).getKey().toString()).append("\n");
        }
        return str;
    }

    public StringBuilder printCacheContent(Serializable id) {
        StringBuilder str = new StringBuilder();
        str.append(printCacheContent("get", id, idCache, "idCache"));
        return str;
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
