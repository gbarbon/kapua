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

import org.eclipse.kapua.commons.jpa.CacheConfigurationFactory;
import org.eclipse.kapua.commons.setting.system.SystemSetting;
import org.eclipse.kapua.commons.setting.system.SystemSettingKey;

import java.util.Arrays;

public class UserCacheConfigurationFactory extends CacheConfigurationFactory {

    private static final SystemSetting SYSTEM_SETTING = SystemSetting.getInstance();
    private static final boolean IS_ENABLED = SYSTEM_SETTING.getBoolean(SystemSettingKey.USER_CACHE, false);

    private static final String USERID_CACHE_NAME = "UserId";
    private static final String USERNAME_CACHE_NAME = "UserName";

    private UserCacheConfigurationFactory() {
        super(Arrays.asList(USERID_CACHE_NAME, USERNAME_CACHE_NAME), IS_ENABLED);
    }

    protected static UserCacheConfigurationFactory getInstance() {
        return new UserCacheConfigurationFactory();
    }

    protected static String getUserIdCacheName() {
        return USERID_CACHE_NAME;
    }

    protected static String getUserNameCacheName() {
        return USERNAME_CACHE_NAME;
    }
}
