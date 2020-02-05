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

import org.eclipse.kapua.commons.jpa.AbstractSecondIdEntityCacheFactory;

public class AccessInfoCacheFactory extends AbstractSecondIdEntityCacheFactory {

    private AccessInfoCacheFactory() {
        super("AccessInfoId", "AccessInfoUserIdId");
    }

    protected static AccessInfoCacheFactory getInstance() {
        return new AccessInfoCacheFactory();
    }
}
