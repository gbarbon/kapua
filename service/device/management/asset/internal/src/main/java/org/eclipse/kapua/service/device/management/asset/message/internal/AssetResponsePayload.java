/*******************************************************************************
 * Copyright (c) 2016, 2021 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.device.management.asset.message.internal;

import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.service.device.management.asset.DeviceAssetFactory;
import org.eclipse.kapua.service.device.management.asset.DeviceAssets;
import org.eclipse.kapua.service.device.management.commons.message.response.KapuaResponsePayloadImpl;
import org.eclipse.kapua.service.device.management.commons.setting.DeviceManagementSetting;
import org.eclipse.kapua.service.device.management.commons.setting.DeviceManagementSettingKey;
import org.eclipse.kapua.service.device.management.message.response.KapuaResponsePayload;

import javax.validation.constraints.NotNull;

/**
 * {@link DeviceAssets} information {@link KapuaResponsePayload}.
 *
 * @since 1.0.0
 */
public class AssetResponsePayload extends KapuaResponsePayloadImpl implements KapuaResponsePayload {

    private static final long serialVersionUID = -9087980446970521618L;

    private static final String CHAR_ENCODING = DeviceManagementSetting.getInstance().getString(DeviceManagementSettingKey.CHAR_ENCODING);

    private static final DeviceAssetFactory DEVICE_ASSET_FACTORY = KapuaLocator.getInstance().getFactory(DeviceAssetFactory.class);

    /**
     * Gets the {@link DeviceAssets} from the {@link #getBody()}.
     *
     * @return The {@link DeviceAssets} from the {@link #getBody()}.
     * @throws Exception if reading {@link #getBody()} errors.
     * @since 1.5.0
     */
    public DeviceAssets getDeviceAssets() throws Exception {
        if (!hasBody()) {
            return DEVICE_ASSET_FACTORY.newAssetListResult();
        }

        String bodyString = new String(getBody(), CHAR_ENCODING);
        return XmlUtil.unmarshal(bodyString, DeviceAssets.class);
    }

    /**
     * Sets the {@link DeviceAssets} in the {@link #getBody()}.
     *
     * @param deviceAssets The {@link DeviceAssets} in the {@link #getBody()}.
     * @throws Exception if writing errors.
     * @since 1.5.0
     */
    public void setDeviceAssets(@NotNull DeviceAssets deviceAssets) throws Exception {
        String bodyString = XmlUtil.marshal(deviceAssets);
        setBody(bodyString.getBytes(CHAR_ENCODING));
    }

}
