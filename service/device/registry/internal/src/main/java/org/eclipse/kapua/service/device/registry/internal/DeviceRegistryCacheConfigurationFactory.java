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
package org.eclipse.kapua.service.device.registry.internal;

import org.eclipse.kapua.commons.jpa.CacheConfigurationFactory;
import org.eclipse.kapua.commons.setting.system.SystemSetting;
import org.eclipse.kapua.commons.setting.system.SystemSettingKey;

import java.util.Arrays;

public class DeviceRegistryCacheConfigurationFactory extends CacheConfigurationFactory {

    private static final SystemSetting SYSTEM_SETTING = SystemSetting.getInstance();
    private static final boolean IS_ENABLED = SYSTEM_SETTING.getBoolean(SystemSettingKey.DEVICE_REGISTRY_CACHE, false);

    private static final String DEVICEID_CACHE_NAME = "DeviceId";
    private static final String DEVICE_CLIENTID_CACHE_NAME = "DeviceClientId";

    private DeviceRegistryCacheConfigurationFactory() {
        super(Arrays.asList(DEVICEID_CACHE_NAME, DEVICE_CLIENTID_CACHE_NAME), IS_ENABLED);
    }

    protected static DeviceRegistryCacheConfigurationFactory getInstance() {
        return new DeviceRegistryCacheConfigurationFactory();
    }

    protected static String getDeviceIdCacheName() {
        return DEVICEID_CACHE_NAME;
    }

    protected static String getDeviceClientIdCacheName() {
        return DEVICE_CLIENTID_CACHE_NAME;
    }
}
