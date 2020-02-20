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
package org.eclipse.kapua.service.authorization.access.shiro;

import org.eclipse.kapua.commons.service.internal.EntityCache;
import org.eclipse.kapua.commons.service.internal.KapuaCacheManager;
import org.eclipse.kapua.model.KapuaEntity;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.authorization.access.AccessInfo;

import javax.cache.Cache;
import java.io.Serializable;

public class AccessInfoCache extends EntityCache {

    protected Cache<Serializable, Serializable> accessInfoByUserIdCache;

    public AccessInfoCache(String idCacheName, String nameCacheName) {
        super(idCacheName);
        accessInfoByUserIdCache = KapuaCacheManager.getCache(nameCacheName);
    }

    public KapuaEntity getByUserId(KapuaId scopeId, KapuaId userId) {
        if (userId != null) {
            KapuaId entityId = (KapuaId) accessInfoByUserIdCache.get(userId);
            return get(scopeId, entityId);
        }
        return null;
    }

    @Override
    public void put(KapuaEntity entity) {
        if (entity != null) {
            idCache.put(entity.getId(), entity);
            accessInfoByUserIdCache.put(((AccessInfo) entity).getUserId(), entity.getId());
        }
    }

    @Override
    public KapuaEntity remove(KapuaId scopeId, KapuaId kapuaId) {
        KapuaEntity kapuaEntity = super.remove(scopeId, kapuaId);
        if (kapuaEntity != null) {
            accessInfoByUserIdCache.remove(((AccessInfo) kapuaEntity).getUserId());
        }
        return kapuaEntity;
    }
}
