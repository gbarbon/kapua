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

import org.eclipse.kapua.KapuaErrorCode;

/**
 * Email error codes
 */
public enum KapuaEmailErrorCodes implements KapuaErrorCode {
    /**
     * Internal error
     */
    INTERNAL_ERROR,
    /**
     * Illegal argument
     */
    ILLEGAL_ARGUMENT,
    /**
     * Operation not allowed
     */
    OPERATION_NOT_ALLOWED,
    /**
     * Email sending failed
     */
    SENDING_FAILURE,

    /**
     * Invalid sender address
     */
    //INVALID_SENDER_ADDRESS,
    /**
     * Invalid recipient address
     */
    //INVALID_RECIPIENT_ADDRESS,
    /**
     * Invalid email body
     */
    //INVALID_BODY,
    ;
}
