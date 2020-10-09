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

import org.eclipse.kapua.commons.setting.SettingKey;

/**
 * Available settings key for email service
 */
public enum KapuaEmailSettingKeys implements SettingKey {

    /**
     * The key value in the configuration resources.
     */
    EMAIL_SMTP_USE_SMTP("email.smtp.use.smtp"),
    EMAIL_SMTP_HOST("email.smtp.host"),
    EMAIL_SMTP_PORT("email.smtp.port"),
    IFTTT_EMAIL_SMTP_HOST("ifttt.email.smtp.host"),
    IFTTT_EMAIL_SMTP_PORT("ifttt.email.smtp.port"),
    IFTTT_TEST_USERNAME("ifttt.test.username"),
    IFTTT_TEST_PASSWORD("ifttt.test.password"),
    EMAIL_SMTP_USERNAME("email.smtp.username"),
    EMAIL_SMTP_PASSWORD("email.smtp.password"),
    EMAIL_SMTP_USE_TLS("email.smtp.use.tls"),
    EMAIL_SMTP_FROM("email.smtp.from"),
    EMAIL_SMTP_CONFIGSET("email.smtp.configSet"),
    EMAIL_USE_SSL("email.use.ssl"),
    EMAIL_PARAM_NAME_TO("email.param.name.to"),
    EMAIL_PARAM_NAME_SUBJECT("email.param.name.subject"),
    EMAIL_PARAM_NAME_FORMAT("email.param.name.emailFormat"),
    EMAIL_PARAM_NAME_DEFAULT_FORMAT("email.param.default.emailFormat"),
    EMAIL_PARAM_NAME_BODY("email.param.name.body"),
    EMAIL_PARAM_DEFAULT_BODY("email.param.default.body"),
    EMAIL_PARAM_NAME_BODY_TEXT_ONLY("email.param.name.bodyTextOnly"),
    EMAIL_PARAM_DEFAULT_BODY_TEXT_ONLY("email.param.default.bodyTextOnly"),
    EMAIL_DISCLAIMER("email.disclaimer"),
    EMAIL_RECIPIENTS_SEPARATOR("email.recipients.separator");

    private final String key;

    /**
     * Set up the {@code enum} with the key value provided
     *
     * @param key
     *            The value mapped by this {@link Enum} value
     */
    private KapuaEmailSettingKeys(String key) {
        this.key = key;
    }

    /**
     * Gets the key for this {@link KapuaEmailSettingKeys}
     *
     */
    public String key() {
        return key;
    }
}
