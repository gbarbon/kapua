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
package org.eclipse.kapua.service.authentication.shiro.realm;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.realm.ldap.DefaultLdapRealm;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.eclipse.kapua.KapuaDuplicateNameException;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.KapuaRuntimeException;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.setting.system.SystemSetting;
import org.eclipse.kapua.commons.setting.system.SystemSettingKey;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountCreator;
import org.eclipse.kapua.service.account.AccountFactory;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.authentication.credential.Credential;
import org.eclipse.kapua.service.authentication.credential.CredentialCreator;
import org.eclipse.kapua.service.authentication.credential.CredentialFactory;
import org.eclipse.kapua.service.authentication.credential.CredentialListResult;
import org.eclipse.kapua.service.authentication.credential.CredentialService;
import org.eclipse.kapua.service.authentication.credential.CredentialStatus;
import org.eclipse.kapua.service.authentication.credential.CredentialType;
import org.eclipse.kapua.service.authentication.shiro.UsernamePasswordCredentialsImpl;
import org.eclipse.kapua.service.authentication.shiro.exceptions.ExpiredAccountException;
import org.eclipse.kapua.service.authentication.shiro.exceptions.TemporaryLockedAccountException;
import org.eclipse.kapua.service.user.User;
import org.eclipse.kapua.service.user.UserCreator;
import org.eclipse.kapua.service.user.UserFactory;
import org.eclipse.kapua.service.user.UserService;
import org.eclipse.kapua.service.user.UserStatus;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LdapAuthenticatingRealm extends DefaultLdapRealm {

    private static final KapuaLocator LOCATOR = KapuaLocator.getInstance();

    /**
     * Realm name
     */
    public static final String REALM_NAME = "LdapAuthenticatingRealm";

    /**
     * Constructor
     *
     * @throws KapuaException
     */
    public LdapAuthenticatingRealm() throws KapuaException {
        super();
        setName(REALM_NAME);

        // setting DN template
        String dnTemplate = (SystemSetting.getInstance().getString(SystemSettingKey.LDAP_DN_TEMPLATE_PREFIX) +
                SystemSetting.getInstance().getString(SystemSettingKey.LDAP_SEARCHBASE)).
                replace("\"", ""); // 'replace' needed to remove quotes
        setUserDnTemplate(dnTemplate);

        // setting ldap url
        JndiLdapContextFactory cf = (JndiLdapContextFactory) getContextFactory();
        String url = SystemSetting.getInstance().getString(SystemSettingKey.LDAP_URL);
        cf.setUrl(url);
    }

    /**
     * Retrieves the authentication data from LDAP
     *
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
            throws AuthenticationException {

        String ldapUsername;
        try {
            // Checking user existence and extracting credentials
            AuthenticationInfo info = super.doGetAuthenticationInfo(authenticationToken);
            ldapUsername = info.getPrincipals().getPrimaryPrincipal().toString();
        } catch (Exception e) {
            throw new UnknownAccountException("Unknown LDAP user", e);
        }

        /*
        TODO : insert here the logic for creating the new user
            - if the user exists on Kapua, fill LoginAuthenticationInfo with the info from Kapua (use findByName)
            - if the user does not exist on Kapua, create the user with UserService,
                use Credential Factory for adding the credentials
                - credentials remain external, andled by ldap, we must create an ldap type for the password
                - account:  - if the account already exists, add the user to the account gathered with ldap
                          - if the account do not exist, add to a dummy one (for testing "ldap-account")
         */

        // ASSUMPTION: user must have the same username in ldap and in kapua

        // Now checking for the user existence

        //
        // Get Services
        UserService userService;
        AccountService accountService;
        CredentialService credentialService;

        try {
            userService = LOCATOR.getService(UserService.class);
            accountService = LOCATOR.getService(AccountService.class);
            credentialService = LOCATOR.getService(CredentialService.class);
        } catch (KapuaRuntimeException kre) {
            throw new ShiroException("Error while getting services!", kre);
        }

        //
        // Get the associated user by name
        final User user;
        try {
            user = KapuaSecurityUtils.doPrivileged(() -> userService.findByName(ldapUsername));
        } catch (AuthenticationException ae) {
            throw ae;
        } catch (Exception e) {
            throw new ShiroException("Error while find user!", e);
        }

        if (user != null) {  // the user exists in the database

            // Check disabled
            if (UserStatus.DISABLED.equals(user.getStatus())) {
                throw new DisabledAccountException();
            }

            // Check if expired
            if (user.getExpirationDate() != null && !user.getExpirationDate().after(new Date())) {
                throw new ExpiredCredentialsException();
            }

            //
            // Find account
            final Account account;
            try {
                account = KapuaSecurityUtils.doPrivileged(() -> accountService.find(user.getScopeId()));
            } catch (AuthenticationException ae) {
                throw ae;
            } catch (Exception e) {
                throw new ShiroException("Error while find account!", e);
            }

            // FIXME: should I handle also the case in which the account is different from the ldap group?

            // Check account existence
            if (account == null) {
                throw new UnknownAccountException();
            }

            // Check account expired
            if (account.getExpirationDate() != null && !account.getExpirationDate().after(new Date())) {
                throw new ExpiredAccountException(account.getExpirationDate());
            }

            //
            // Find credentials
            // FIXME: manage multiple credentials and multiple credentials type
            Credential credential;
            try {
                credential = KapuaSecurityUtils.doPrivileged(() -> {
                    CredentialListResult credentialList = credentialService.findByUserId(user.getScopeId(), user.getId());

                    if (credentialList != null && !credentialList.isEmpty()) {
                        Credential credentialMatched = null;
                        for (Credential c : credentialList.getItems()) {
                            if (CredentialType.LDAP.equals(c.getCredentialType())) {
                                credentialMatched = c;
                                break;
                            }
                        }
                        return credentialMatched;
                    } else {
                        return null;
                    }
                });
            } catch (AuthenticationException ae) {
                throw ae;
            } catch (Exception e) {
                throw new ShiroException("Error while find credentials!", e);
            }

            // Check existence
            if (credential == null) {
                throw new UnknownAccountException();
            }

            // Check credential disabled
            if (CredentialStatus.DISABLED.equals(credential.getStatus())) {
                throw new DisabledAccountException();
            }

            // Check if credential expired
            if (credential.getExpirationDate() != null && !credential.getExpirationDate().after(new Date())) {
                throw new ExpiredCredentialsException();
            }

            // Check if lockout policy is blocking credential
            Map<String, Object> credentialServiceConfig;
            try {
                credentialServiceConfig = KapuaSecurityUtils.doPrivileged(() -> credentialService.getConfigValues(account.getId()));
                boolean lockoutPolicyEnabled = (boolean) credentialServiceConfig.get("lockoutPolicy.enabled");
                if (lockoutPolicyEnabled) {
                    Date now = new Date();
                    if (credential.getLockoutReset() != null && now.before(credential.getLockoutReset())) {
                        throw new TemporaryLockedAccountException(credential.getLockoutReset());
                    }
                }
            } catch (KapuaException kex) {
                throw new ShiroException("Error while checking lockout policy", kex);
            }

            //
            // BuildAuthenticationInfo
            return new LoginAuthenticationInfo(getName(),
                    account,
                    user,
                    credential,
                    credentialServiceConfig);
        } else {
            // create new user and the credentials
            User newUser = null;
            Account newAccount;
            Credential newCredential = null;

            // Get Factories
            UserFactory userFactory;
            AccountFactory accountFactory;
            CredentialFactory credentialFactory;

            try {
                userFactory = LOCATOR.getFactory(UserFactory.class);
                accountFactory = LOCATOR.getFactory(AccountFactory.class);
                credentialFactory = LOCATOR.getFactory(CredentialFactory.class);
            } catch (KapuaRuntimeException kre) {
                throw new ShiroException("Error while getting services!", kre);
            }

            try {

                // First gather the account information from the ldap groups to which the user belong
                // TODO: implement the ldap group name gathering

                // if no groups are available, assign to an existing group (dummy) for the moment
                // I create it

                // FIXME: I am checking if the account I want to create already exists, but I don't like how this is implemented!
                try {
                    // retriving the parent (system) account
                    String adminAccountName = SystemSetting.getInstance().getString(SystemSettingKey.SYS_ADMIN_ACCOUNT);
                    Account adminAccount = KapuaSecurityUtils.doPrivileged(() -> accountService.findByName(adminAccountName));

                    // creating a new account
                    final AccountCreator accountCreator = accountFactory.newCreator(adminAccount.getId(), ldapUsername);
                    accountCreator.setOrganizationName(adminAccount.getOrganization().getName());
                    //String adminEmail = adminAccount.getOrganization().
                    accountCreator.setOrganizationEmail(ldapUsername + "@eclipse.org"); // FIXME: where can I find the domain?
                    // FIXME: settare il numero di figli come massimo
                    newAccount = KapuaSecurityUtils.doPrivileged(() -> accountService.create(accountCreator));
                } catch (KapuaDuplicateNameException dne) {
                    newAccount = KapuaSecurityUtils.doPrivileged(() -> accountService.findByName(ldapUsername));
                } catch (Exception e) {
                    throw e;
                }
                // retrieving the new account id
                KapuaId newAccountId = newAccount.getId();
                KapuaId parentAccountId = newAccount.getScopeId();

                // setting the infiniteChild Entities attribute
                Map<String, Object> valueMap = new HashMap<>();
                valueMap.put("maxNumberChildEntities", "100");
                valueMap.put("infiniteChildEntities", true);
                try {
                    KapuaSecurityUtils.doPrivileged(() -> userService.setConfigValues(newAccountId, parentAccountId, valueMap));

                    Map<String, Object> finalConfig = KapuaSecurityUtils.doPrivileged(() -> userService.getConfigValues(newAccountId));
                    boolean allowInfiniteChildEntities = (boolean) finalConfig.get("infiniteChildEntities");
                    System.out.println("allowInfiniteChildEntities is : " + allowInfiniteChildEntities);

                } catch (KapuaException ex) {
                    ex.printStackTrace();
                }

                // create the new user
                final UserCreator userCreator = userFactory.newCreator(newAccountId, ldapUsername);
                // FIXME: null for the moment, maybe I can retrieve all those from ldap?
                //userCreator.setEmail(null);
                //userCreator.setUserStatus(null);

                //
                // Create the User
                newUser = KapuaSecurityUtils.doPrivileged(() -> userService.create(userCreator));

                //
                // Create credentials
                CredentialCreator credentialCreator = credentialFactory.newCreator(newAccountId,
                        newUser.getId(),
                        CredentialType.LDAP,  // FIXME: change to LDAPsomething...
                        "LDAP", // something.getPassword(),
                        CredentialStatus.ENABLED,
                        null);
                newCredential = KapuaSecurityUtils.doPrivileged(() -> credentialService.create(credentialCreator));

                //
                // BuildAuthenticationInfo
                return new LoginAuthenticationInfo(getName(),
                        newAccount,
                        newUser,
                        newCredential,
                        null);
            } catch (Throwable t) {
                //KapuaExceptionHandler.handle(t);
                t.printStackTrace();
            }

            throw new UnknownAccountException();
        }

        //return null;
    }


    @Override
    protected void assertCredentialsMatch(AuthenticationToken authcToken, AuthenticationInfo info)
            throws AuthenticationException {
        final LoginAuthenticationInfo kapuaInfo = (LoginAuthenticationInfo) info;

        try {
            super.assertCredentialsMatch(authcToken, info);
        } catch (AuthenticationException authenticationEx) {

            // do something?
            throw authenticationEx;
        }

        final Subject currentSubject = SecurityUtils.getSubject();
        Session session = currentSubject.getSession();
        session.setAttribute("scopeId", kapuaInfo.getUser().getScopeId());
        session.setAttribute("userId", kapuaInfo.getUser().getId());
    }

    @Override
    public boolean supports(AuthenticationToken authenticationToken) {
        return authenticationToken instanceof UsernamePasswordCredentialsImpl;
    }
}
