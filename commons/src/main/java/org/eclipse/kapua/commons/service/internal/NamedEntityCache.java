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

    public Serializable get(KapuaId scopeId, String name) {
        KapuaNamedEntity entity = (KapuaNamedEntity) nameCache.get(name);
        return checkResult(scopeId, entity);
    }

    @Override
    public void put(KapuaEntity entity) {
        put((KapuaNamedEntity) entity);
    }

    public void put(KapuaNamedEntity entity) {
        idCache.put(entity.getId(), entity);
        nameCache.put(entity.getName(), entity);
    }

    @Override
    public void remove(KapuaId scopeId, KapuaId kapuaId) {
        // First get the entity in order to perform a check of the scope id
        KapuaNamedEntity entity = (KapuaNamedEntity) get(scopeId, kapuaId);
        if (entity != null) {
            idCache.remove(kapuaId);
            nameCache.remove(entity.getName());
        }
    }
}
