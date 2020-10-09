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
package org.eclipse.kapua.service.email.exception;

import org.eclipse.kapua.KapuaException;

/**
 * Email exception
 */
public class KapuaEmailException extends KapuaException {

    private static final long serialVersionUID = -4933859787694274079L;

    private static final String KAPUA_ERROR_MESSAGES = "kapua-email-service-error-messages";

    /**
     * Builds a new KapuaException instance based on the supplied KapuaErrorCode.
     * 
     * @param code
     */
    public KapuaEmailException(KapuaEmailErrorCodes code) {
        super(code);
    }

    /**
     * Builds a new KapuaException instance based on the supplied KapuaErrorCode
     * and optional arguments for the associated exception message.
     * 
     * @param code
     * @param arguments
     */
    public KapuaEmailException(KapuaEmailErrorCodes code, Object... arguments) {
        super(code, arguments);
    }

    /**
     * Builds a new KapuaEmailException instance based on the supplied KapuaEmailErrorCode,
     * an Throwable cause, and optional arguments for the associated exception message.
     * 
     * @param code
     * @param cause
     * @param arguments
     */
    public KapuaEmailException(KapuaEmailErrorCodes code, Throwable cause, Object... arguments) {
        super(code, cause, arguments);
    }

    /**
     * Factory method to build an KapuaEmailException with the KapuaEmailErrorCode.INTERNAL_ERROR,
     * and optional arguments for the associated exception message.
     * 
     * @param message
     * @return
     */
    public static KapuaEmailException internalError(String message) {
        return new KapuaEmailException(KapuaEmailErrorCodes.INTERNAL_ERROR, null, message);
    }

    @Override
    protected String getKapuaErrorMessagesBundle() {
        return KAPUA_ERROR_MESSAGES;
    }
}
