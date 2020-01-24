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

import org.eclipse.kapua.model.KapuaNamedEntity;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @deprecated use JCacheHashMapCache instead
 */
@Deprecated
public class HashMapCache implements KapuaCache{

    private HashMap<Serializable,KapuaNamedEntity> hashMap;

    HashMapCache() {
        hashMap = new HashMap<>();
    }


    @Override
    public KapuaNamedEntity get(Serializable key) {
        return hashMap.get(key);
    }

    @Override
    public void put(Serializable key, KapuaNamedEntity value) {
        hashMap.put(key, value);
    }

    @Override
    public Serializable remove(Serializable key) {
        return hashMap.remove(key);
    }

    @Override
    public void invalidate() {
        hashMap.clear();
    }


}
