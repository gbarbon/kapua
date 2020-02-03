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

import org.eclipse.kapua.commons.jpa.CacheFactory;

import java.util.Arrays;

public class DeviceRegistryCacheFactory extends CacheFactory {

    private static final String DEVICEID_CACHE_NAME = "DeviceId";
    private static final String DEVICE_CLIENTID_CACHE_NAME = "DeviceClientId";

    private DeviceRegistryCacheFactory() {
        super(Arrays.asList(DEVICEID_CACHE_NAME, DEVICE_CLIENTID_CACHE_NAME));
    }

    protected static DeviceRegistryCacheFactory getInstance() {
        return new DeviceRegistryCacheFactory();
    }

    protected static String getDeviceIdCacheName() {
        return DEVICEID_CACHE_NAME;
    }

    protected static String getDeviceClientIdCacheName() {
        return DEVICE_CLIENTID_CACHE_NAME;
    }
}
