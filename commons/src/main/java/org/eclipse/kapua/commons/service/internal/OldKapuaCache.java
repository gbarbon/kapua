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

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.KapuaIllegalArgumentException;
import org.eclipse.kapua.model.KapuaEntity;
import org.eclipse.kapua.model.KapuaNamedEntity;
import org.eclipse.kapua.model.id.KapuaId;

import javax.cache.Cache;
import java.io.Serializable;

public class OldKapuaCache {

    //private Map<String, Cache<Serializable, Serializable>> caches;

    private Cache<Serializable, Serializable> idCache;
    private Cache<Serializable, Serializable> nameCache;

/*    public KapuaCache(Collection<String> cachesNames) {
        this.caches = new ConcurrentHashMap<>();
        cachesNames.forEach((cacheName) -> this.caches.put(cacheName, KapuaCacheManager.getCache(cacheName)));
    }

    public KapuaCache(AbstractCacheFactory abstractCacheFactory) {
        idCache = KapuaCacheManager.getCache(abstractCacheFactory.getEntityIdCacheName());
        nameCache = KapuaCacheManager.getCache(abstractCacheFactory.getEntityNameCacheName());
    }*/

    public OldKapuaCache(String idCacheName, String nameCacheName) {
        idCache = KapuaCacheManager.getCache(idCacheName);
        nameCache = KapuaCacheManager.getCache(nameCacheName);
    }

/*    public Cache<Serializable, Serializable> getCache(String cacheName) {
        return caches.get(cacheName);
    }*/

    public Serializable get(KapuaId scopeId, KapuaId kapuaId) {
        KapuaEntity entity = (KapuaEntity) idCache.get(kapuaId);
        return checkResult(scopeId, entity);
    }

    public Serializable get(KapuaId scopeId, String name) {
        KapuaEntity entity = (KapuaEntity) nameCache.get(name);
        return checkResult(scopeId, entity);
    }

    private KapuaEntity checkResult(KapuaId scopeId, KapuaEntity entity) {
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

    public void put(KapuaNamedEntity entity) {
        // FIXME: it won't work with the DeviceRegistry... Because a device is not a kapuaanmedentity!
        idCache.put(entity.getId(), entity);
        nameCache.put(entity.getName(), entity);
    }

    public void put(KapuaEntity entity) {
        idCache.put(entity.getId(), entity);
    }

    public void remove(KapuaEntity entity) {
        remove(entity.getId());
    }

    public void remove(KapuaId kapuaId) {
        KapuaEntity entity = (KapuaEntity) idCache.get(kapuaId);
        if (entity instanceof KapuaNamedEntity) {
            nameCache.remove(((KapuaNamedEntity) entity).getName());
        }
        idCache.remove(kapuaId);
    }

/*    public void remove(KapuaId scopeId, KapuaId kapuaId) throws KapuaException {
        KapuaEntity entity = (KapuaEntity) idCache.get(concatenateCacheKey(scopeId, kapuaId));
        if (entity instanceof KapuaNamedEntity) {
            nameCache.remove(concatenateCacheKey(scopeId, ((KapuaNamedEntity) entity).getName()));
        }
        idCache.remove(concatenateCacheKey(scopeId, kapuaId));
    }*/

    /**
     * Obtain a String cache key from two Serializable objects.
     *
     * @param firstKey The first half of the key.
     * @param secondKey The second half of the key.
     * @return A String which is the result of the concatenation of the two keys.
     * @throws KapuaException if one of the parameters is not an instance of String nor KapuaId.
     */
    protected String concatenateCacheKey(Serializable firstKey, Serializable secondKey) throws KapuaException {
        String firstKeyString;
        String secondKeyString;

        if (firstKey instanceof String) {
            firstKeyString = (String) firstKey;
        } else if (firstKey instanceof KapuaId) {
            firstKeyString = ((KapuaId) firstKey).toStringId();
        } else {
            throw new KapuaIllegalArgumentException("firstKey", "Unexpected type");
        }

        if (secondKey instanceof String) {
            secondKeyString = (String) secondKey;
        } else if (secondKey instanceof KapuaId) {
            secondKeyString = ((KapuaId) secondKey).toStringId();
        } else {
            throw new KapuaIllegalArgumentException("secondKey", "Unexpected type");
        }

        StringBuilder newKey = new StringBuilder();
        newKey.append(firstKeyString);
        newKey.append(":");  //FIXME : separator as static final constant
        newKey.append(secondKeyString);

        return newKey.toString();
    }

}
