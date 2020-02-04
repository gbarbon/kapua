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

import org.eclipse.kapua.commons.jpa.AbstractEntityCacheFactory;

public class AccessRoleCacheFactory extends AbstractEntityCacheFactory {

    private AccessRoleCacheFactory() {
        super("AccessRoleId");
    }

    protected static AccessRoleCacheFactory getInstance() {
        return new AccessRoleCacheFactory();
    }
}
