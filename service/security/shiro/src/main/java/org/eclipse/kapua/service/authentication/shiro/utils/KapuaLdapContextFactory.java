/*******************************************************************************
 * Copyright (c) 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.authentication.shiro.utils;

import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.util.StringUtils;

import javax.naming.Context;
import java.util.Map;

/**
 * {@link KapuaLdapContextFactory} based {@link JndiLdapContextFactory} implementation.
 * Allows setting ldap environment properties.
 */
public class KapuaLdapContextFactory extends JndiLdapContextFactory {

    /**
     * Public version of the superclass method
     *
     * @param name the name of the property
     * @return the property
     */
    public Object getEnvironmentProperty(String name) {
        Map<String, Object> environment = getEnvironment();
        return environment.get(name);
    }

    /**
     * Public version of the superclass method
     *
     * @param name the name of the property
     * @param value the value of the property
     */
    public void setEnvironmentProperty(String name, String value) {
        Map<String, Object> environment = getEnvironment();
        if (StringUtils.hasText(value)) {
            environment.put(name, value);
        } else {
            environment.remove(name);
        }
        setEnvironment(environment);
    }

    /**
     * Set the java.naming.security.protocol
     *
     * @param securityProtocol the chosen protocol
     */
    public void setSecurityProtocol(String securityProtocol) {
        setEnvironmentProperty(Context.SECURITY_PROTOCOL, securityProtocol);
    }
}
