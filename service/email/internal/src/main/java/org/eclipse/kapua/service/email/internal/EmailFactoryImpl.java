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

import org.eclipse.kapua.service.email.KapuaEmail;
import org.eclipse.kapua.service.email.EmailFactory;

public class EmailFactoryImpl implements EmailFactory {

    @Override
    public KapuaEmail newKapuaEmail() {
        return new EmailImpl();
    }
}
