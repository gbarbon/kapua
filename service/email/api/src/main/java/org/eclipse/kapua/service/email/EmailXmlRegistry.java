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

import javax.xml.bind.annotation.XmlRegistry;

import org.eclipse.kapua.locator.KapuaLocator;

/**
 * Email xml factory class
 */
@XmlRegistry
public class EmailXmlRegistry {

    private final KapuaLocator locator = KapuaLocator.getInstance();
    private final EmailFactory factory = locator.getFactory(EmailFactory.class);

    /**
     * Creates a new email
     *
     * @return
     */
    public KapuaEmail newKapuaEmail() {
        return factory.newKapuaEmail();
    }

}
