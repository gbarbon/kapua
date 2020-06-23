/*******************************************************************************
 * Copyright (c) 2016, 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.authentication.shiro.realm;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.authentication.credential.Credential;
import org.eclipse.kapua.service.user.User;

import java.util.Map;

/**
 * Kapua {@link AuthenticationInfo} implementation
 * 
 * @since 1.0
 *
 */
public class LoginAuthenticationInfo implements AuthenticationInfo {

    private static final long serialVersionUID = -8682457531010599453L;

    private String realmName;
    private Account account;
    private User user;
    // TODO: use a list of credentials?
    private Credential passwordCredential;
    private Credential authenticationKeyCredential;
    // TODO: also the scratch codes should be maintained here?
    private Map<String, Object> credentialServiceConfig;

    /**
     * Constructor
     * 
     * @param realmName
     * @param account
     * @param user
     * @param passwordCredential
     */
    public LoginAuthenticationInfo(String realmName,
            Account account,
            User user,
            Credential passwordCredential,
            Credential authenticationKeyCredential,
            Map<String, Object> credentialServiceConfig) {
        this.realmName = realmName;
        this.account = account;
        this.user = user;
        this.passwordCredential = passwordCredential;
        this.authenticationKeyCredential = authenticationKeyCredential;
        this.credentialServiceConfig = credentialServiceConfig;
    }

    /**
     * Return the user
     * 
     * @return
     */
    public User getUser() {
        return user;
    }

    /**
     * Return the account
     * 
     * @return
     */
    public Account getAccount() {
        return account;
    }

    public String getRealmName() {
        return realmName;
    }

    @Override
    public PrincipalCollection getPrincipals() {
        return new SimplePrincipalCollection(getUser(), getRealmName());
    }

    @Override
    public Object getCredentials() {
        return passwordCredential;
    }

    public Credential getAuthenticationKeyCredential() {
        return authenticationKeyCredential;
    }

    public Map<String, Object> getCredentialServiceConfig() {
        return credentialServiceConfig;
    }

    // TODO: note that getter and setter in the old EC4 for the authenticationKeyCredential were using encrypt/decrypt algorithm.. should we do the same?
}
