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
package org.eclipse.kapua.service.email.internal;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.eclipse.kapua.KapuaIllegalNullArgumentException;
import org.eclipse.kapua.commons.util.ArgumentValidator;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.service.email.KapuaEmail;
import org.eclipse.kapua.service.email.EmailService;
import org.eclipse.kapua.service.email.exception.KapuaEmailErrorCodes;
import org.eclipse.kapua.service.email.exception.KapuaEmailException;
import org.eclipse.kapua.service.email.internal.settting.KapuaEmailSetting;
import org.eclipse.kapua.service.email.internal.settting.KapuaEmailSettingKeys;

/**
 * {@link EmailService} implementation.
 */
@KapuaProvider
public class EmailServiceImpl implements EmailService {

    private final KapuaEmailSetting settings = KapuaEmailSetting.getInstance();

    private static final String HTML_BODY_FORMAT = "html";
    private static final String SMTP_SES_CONFIG_HEADER = "X-SES-CONFIGURATION-SET";

    @Override
    public void sendEmail(KapuaEmail email) throws KapuaIllegalNullArgumentException, KapuaEmailException {

        // Check the email fields and set eventually the defaults values
        ArgumentValidator.notNull(email, "email");
        ArgumentValidator.notEmptyOrNull(email.getSender(), "email.sender");
        ArgumentValidator.notEmptyOrNull(email.getRecipients(), "email.recipients");

        //checkEmailFields(email);

        // ArgumentValidator.notEmptyOrNull(email.getSubject(), "email.subject");
        if (email.getSubject() == null) {
            //s_logger.info("Parameter {} is not present or is null", configuration.getParamNameSubject());
            email.setSubject("");
        }

        // ArgumentValidator.notEmptyOrNull(email.getBody(), "email.body");
        if (email.getBody() == null) {
            //s_logger.info("Parameter {} is not present or is null. Body will be set to: {}", configuration.getParamNameBody(), configuration
            // .getParamDefaultBody());

            // Null or empty body is not allowed by Email so set a default one.
            email.setBody(settings.getString(KapuaEmailSettingKeys.EMAIL_PARAM_DEFAULT_BODY));
        }

        if (email.getBodyTextOnly() == null) {
            // s_logger.info("Parameter {} is not present or is null. Body text only will be set to: {}", configuration.getParamNameBodyTextOnly(),
            // configuration.getParamDefaultBodyTextOnly());

            // Null or empty text only body is not allowed by Email so set a default one.
            email.setBodyTextOnly(settings.getString(KapuaEmailSettingKeys.EMAIL_PARAM_DEFAULT_BODY_TEXT_ONLY));
        }

        // ArgumentValidator.notEmptyOrNull(email.getBodyFormat(), "email.format");
        if (email.getBodyFormat() == null) {
            //s_logger.info("Parameter {} is not present or is null. Body format will be set to: {}", configuration.getEmailBodyFormat(), configuration
            // .getEmailBodyFormatDefault());
            email.setBodyFormat(settings.getString(KapuaEmailSettingKeys.EMAIL_PARAM_NAME_DEFAULT_FORMAT));
        }

        // Evaluate the content of the email with a Velocity template if a Velocity context is provided
        //if (email.getCtx()!=null) {
        //    evaluateVelocityTemplate(email);
        //}
        // TODO: is it required a Velocity Engine?
        //  The Apache Velocity Engine allows to build 'stuff' from a template

        // Send the email
        sendEmailSmtp(email);
    }

    /**
     *
     * Send a Smtp email to the passed recipients.
     *
     * @param kapuaEmail
     */
    private void sendEmailSmtp(KapuaEmail kapuaEmail) throws KapuaEmailException {
        Email email;

        if (kapuaEmail.getBodyFormat().equals(HTML_BODY_FORMAT)) {
            email = new HtmlEmail();
        } else {
            email = new SimpleEmail();
        }

        email.setHostName(settings.getString(KapuaEmailSettingKeys.EMAIL_SMTP_HOST));
        email.setSmtpPort(settings.getInt(KapuaEmailSettingKeys.EMAIL_SMTP_PORT));
        email.setAuthenticator(new DefaultAuthenticator(settings.getString(KapuaEmailSettingKeys.EMAIL_SMTP_USERNAME),
                settings.getString(KapuaEmailSettingKeys.EMAIL_SMTP_PASSWORD)));
        email.setStartTLSEnabled(settings.getBoolean(KapuaEmailSettingKeys.EMAIL_SMTP_USE_TLS));
        email.setSSLOnConnect(settings.getBoolean(KapuaEmailSettingKeys.EMAIL_USE_SSL));

        if (settings.getString(KapuaEmailSettingKeys.EMAIL_SMTP_CONFIGSET) != null) {
            email.addHeader(SMTP_SES_CONFIG_HEADER, settings.getString(KapuaEmailSettingKeys.EMAIL_SMTP_CONFIGSET));
        }

        // setting sender address
        try {
            email.setFrom(kapuaEmail.getSender() != null ? kapuaEmail.getSender() : settings.getString(KapuaEmailSettingKeys.EMAIL_SMTP_FROM));
        } catch (EmailException e) {
            throw new KapuaEmailException(KapuaEmailErrorCodes.ILLEGAL_ARGUMENT, null, "kapuaEmail.sender");
        }

        // setting email subject
        email.setSubject(kapuaEmail.getSubject());

        // setting email recipients
        for (String recipient : kapuaEmail.getRecipients()) {
            try {
                email.addTo(recipient);
            } catch (EmailException e) {
                throw new KapuaEmailException(KapuaEmailErrorCodes.ILLEGAL_ARGUMENT, null, "kapuaEmail.recipients");
            }
        }

        // setting email body
        try {
            if (kapuaEmail.getBodyFormat().equals(HTML_BODY_FORMAT)) {
                assert email instanceof HtmlEmail;
                ((HtmlEmail) email).setHtmlMsg(kapuaEmail.getBody());
                ((HtmlEmail) email).setTextMsg(kapuaEmail.getBodyTextOnly());
            } else {
                email.setMsg(kapuaEmail.getBody());
            }
        } catch (EmailException e) {
            throw new KapuaEmailException(KapuaEmailErrorCodes.ILLEGAL_ARGUMENT, null, "kapuaEmail.body");
        }

        // finally sending the email
        try {
            email.send();
        } catch (EmailException e) {
            throw new KapuaEmailException(KapuaEmailErrorCodes.SENDING_FAILURE, e);
        }
    }
}
