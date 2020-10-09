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
package org.eclipse.kapua.service.email;

import org.eclipse.kapua.model.KapuaEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * Email entity definition, generic email object.
 */
@XmlRootElement(name = "kapuaEmail")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {
        "sender",
        "recipients",
        "subject",
        "body",
        "bodyTextOnly",
        "bodyFormat"
}, factoryClass = EmailXmlRegistry.class, factoryMethod = "newEmail")
public interface KapuaEmail extends KapuaEntity {

    String TYPE = "kapuaEmail";

    @Override
    default String getType() {
        return TYPE;
    }

    /**
     * Get the email sender
     *
     * @return the email sender
     */
    @XmlElement(name = "sender")
    String getSender();

    /**
     * Set the eamil sender
     *
     * @param sender the email sender
     */
    void setSender(String sender);

    /**
     * Get the email recipients
     *
     * @return the email recipients
     */
    @XmlElementWrapper(name = "recipients")
    @XmlElement(name = "recipient")
    List<String> getRecipients();

    /**
     * Set the email recipients
     *
     * @param recipients the email recipients
     */
    void setRecipients(List<String> recipients);

    /**
     * Get the email subject
     *
     * @return the email subject
     */
    @XmlElement(name = "subject")
    String getSubject();

    /**
     * Set the email subject
     *
     * @param subject the email subject
     */
    void setSubject(String subject);

    /**
     * Get the email body
     *
     * @return the email body
     */
    @XmlElement(name = "body")
    String getBody();

    /**
     * Set the email body
     *
     * @param body the email body
     */
    void setBody(String body);

    /**
     * Get the email body
     *
     * @return the email body
     */
    @XmlElement(name = "bodyTextOnly")
    String getBodyTextOnly();

    /**
     * Set the email body
     *
     * @param bodyTextOnly the email body
     */
    void setBodyTextOnly(String bodyTextOnly);

    /**
     * Get the email body format
     *
     * @return the email body format
     */
    @XmlElement(name = "bodyFormat")
    String getBodyFormat();

    /**
     * Set the email body format
     *
     * @param bodyFormat the email body format
     */
    void setBodyFormat(String bodyFormat);

}
