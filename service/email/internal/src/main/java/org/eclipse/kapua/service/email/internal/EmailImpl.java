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

import org.eclipse.kapua.commons.model.AbstractKapuaEntity;
import org.eclipse.kapua.service.email.KapuaEmail;

import java.util.List;

/**
 * Email entity implementation.
 */
public class EmailImpl extends AbstractKapuaEntity implements KapuaEmail {

    private static final long serialVersionUID = -5524510075576375543L;

    private String sender;
    private List<String> recipients;
    private String subject;
    private String body;
    private String bodyTextOnly;
    private String bodyFormat;

    @Override
    public String getSender() {
        return sender;
    }

    @Override
    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public List<String> getRecipients() {
        return recipients;
    }

    @Override
    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String getBodyTextOnly() {
        return bodyTextOnly;
    }

    @Override
    public void setBodyTextOnly(String bodyTextOnly) {
        this.bodyTextOnly = bodyTextOnly;
    }

    @Override
    public String getBodyFormat() {
        return bodyFormat;
    }

    @Override
    public void setBodyFormat(String bodyFormat) {
        this.bodyFormat = bodyFormat;
    }

}
