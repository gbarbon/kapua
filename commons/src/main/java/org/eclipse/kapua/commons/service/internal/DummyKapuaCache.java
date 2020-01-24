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

/**
 * @deprecated use DummyJCache instead
 */
@Deprecated
public class DummyKapuaCache implements KapuaCache {

    @Override
    public KapuaNamedEntity get(Serializable key) {
        return null;
    }

    @Override
    public void put(Serializable key, KapuaNamedEntity value) {
    }

    @Override
    public Serializable remove(Serializable key) {
        return null;
    }

    @Override
    public void invalidate() {

    }

}
