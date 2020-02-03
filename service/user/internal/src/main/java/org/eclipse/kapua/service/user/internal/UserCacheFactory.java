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
package org.eclipse.kapua.service.user.internal;

import org.eclipse.kapua.commons.jpa.CacheFactory;

import java.util.Arrays;

public class UserCacheFactory extends CacheFactory {

    private static final String USERID_CACHE_NAME = "UserId";
    private static final String USERNAME_CACHE_NAME = "UserName";
    private static final String EXTERNALID_CACHE_NAME = "ExternalId";

    private UserCacheFactory() {
        super(Arrays.asList(USERID_CACHE_NAME, USERNAME_CACHE_NAME, EXTERNALID_CACHE_NAME));
    }

    protected static UserCacheFactory getInstance() {
        return new UserCacheFactory();
    }

    protected static String getUserIdCacheName() {
        return USERID_CACHE_NAME;
    }

    protected static String getUserNameCacheName() {
        return USERNAME_CACHE_NAME;
    }

    protected static String getUserExternalIdCacheName() {
        return EXTERNALID_CACHE_NAME;
    }
}
