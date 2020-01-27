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

public class CacheConfigurationFactory {

    private Collection<String> cacheNames;
    private boolean isEnabled;

    public CacheConfigurationFactory(Collection<String> cacheNames, boolean isEnabled) {
        this.cacheNames = cacheNames;
        this.isEnabled = isEnabled;
    }

    public Collection<String> getCacheNames() {
        return cacheNames;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

}
