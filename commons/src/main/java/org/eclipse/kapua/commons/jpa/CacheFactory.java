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
package org.eclipse.kapua.commons.jpa;

import java.util.Collection;

public class CacheFactory {

    private Collection<String> cacheNames;

    public CacheFactory(Collection<String> cacheNames) {
        this.cacheNames = cacheNames;
    }

    public Collection<String> getCacheNames() {
        return cacheNames;
    }

}
