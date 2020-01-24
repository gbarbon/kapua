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
 * @deprecated use JCache instead
 */
@Deprecated
public interface KapuaCache {
    // FIXME: use JCache directly instead?

    KapuaNamedEntity get(Serializable key);

    void put(Serializable key, KapuaNamedEntity value);

    Serializable remove(Serializable key);

    void invalidate();
}
