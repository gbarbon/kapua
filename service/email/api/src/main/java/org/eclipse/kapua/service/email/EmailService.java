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

import org.eclipse.kapua.KapuaIllegalNullArgumentException;
import org.eclipse.kapua.service.KapuaService;
import org.eclipse.kapua.service.email.exception.KapuaEmailException;

/**
 * EmailService definition.
 */
public interface EmailService extends KapuaService {

    /**
     * Send an {@link KapuaEmail}
     *
     * @param email the {@link KapuaEmail} to be sent
     */
    void sendEmail(KapuaEmail email) throws KapuaIllegalNullArgumentException, KapuaEmailException;

}
