/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.account.internal;

import org.eclipse.kapua.commons.jpa.CacheConfigurationFactory;

public class AccountCacheConfigurationFactory extends CacheConfigurationFactory {

    private AccountCacheConfigurationFactory() {
        super("Account");
    }

    public static AccountCacheConfigurationFactory getInstance() {
        return new AccountCacheConfigurationFactory();
    }
}
