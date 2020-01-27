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
package org.eclipse.kapua.service.account.internal;

import org.eclipse.kapua.commons.jpa.CacheConfigurationFactory;
import org.eclipse.kapua.commons.setting.system.SystemSetting;
import org.eclipse.kapua.commons.setting.system.SystemSettingKey;

import java.util.Arrays;

public class AccountCacheConfigurationFactory extends CacheConfigurationFactory {

    private static final SystemSetting SYSTEM_SETTING = SystemSetting.getInstance();
    private static final boolean IS_ENABLED = SYSTEM_SETTING.getBoolean(SystemSettingKey.ACCOUNT_CACHE, false);

    private static final String ACCOUNTID_CACHE_NAME = "AccountId";
    private static final String ACCOUNTNAME_CACHE_NAME = "AccountName";

    private AccountCacheConfigurationFactory() {
        super(Arrays.asList(ACCOUNTID_CACHE_NAME, ACCOUNTNAME_CACHE_NAME), IS_ENABLED);
    }

    protected static AccountCacheConfigurationFactory getInstance() {
        return new AccountCacheConfigurationFactory();
    }

    protected static String getAccountIdCacheName() {
        return ACCOUNTID_CACHE_NAME;
    }

    protected static String getAccountNameCacheName() {
        return ACCOUNTNAME_CACHE_NAME;
    }
}
