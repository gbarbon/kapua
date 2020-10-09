/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
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
package org.eclipse.kapua.service.email.internal.settting;

import org.eclipse.kapua.commons.setting.AbstractKapuaSetting;

/**
 * Class that offers access to email settings
 */
public class KapuaEmailSetting extends AbstractKapuaSetting<KapuaEmailSettingKeys> {

    /**
     * Resource file from which source properties.
     *
     */
    private static final String EMAIL_CONFIG_RESOURCE = "kapua-email-setting.properties";

    private static final KapuaEmailSetting INSTANCE = new KapuaEmailSetting();

    /**
     * Initialize the {@link AbstractKapuaSetting} from {@link KapuaEmailSetting#EMAIL_CONFIG_RESOURCE}
     *
     */
    private KapuaEmailSetting() {
        super(EMAIL_CONFIG_RESOURCE);
    }

    /**
     * Gets a singleton instance of {@link KapuaEmailSetting}.
     *
     * @return A singleton instance of KapuaAccountSetting.
     */
    public static KapuaEmailSetting getInstance() {
        return INSTANCE;
    }
}
