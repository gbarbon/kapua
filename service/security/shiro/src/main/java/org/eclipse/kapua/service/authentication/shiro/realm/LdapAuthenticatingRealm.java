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

import org.apache.commons.lang3.NotImplementedException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.ldap.DefaultLdapRealm;
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
import org.eclipse.kapua.service.authentication.shiro.utils.KapuaLdapContextFactory;
import org.eclipse.kapua.service.user.User;
import org.eclipse.kapua.service.user.UserCreator;
import org.eclipse.kapua.service.user.UserFactory;
import org.eclipse.kapua.service.user.UserService;
import org.eclipse.kapua.service.user.UserStatus;

import javax.naming.AuthenticationNotSupportedException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link LdapAuthenticatingRealm} based {@link DefaultLdapRealm} implementation.
 */
public class LdapAuthenticatingRealm extends DefaultLdapRealm {

    private static final KapuaLocator LOCATOR = KapuaLocator.getInstance();

    /**
     * Realm name
     */
    private static final String REALM_NAME = "LdapAuthenticatingRealm";

    /**
     * Constructor
     *
     * @throws KapuaException
     */
    public LdapAuthenticatingRealm() throws ShiroException {

        // following part is the same as DefaultLdapRealm constructor
        setCredentialsMatcher(new AllowAllCredentialsMatcher());
        setAuthenticationTokenClass(AuthenticationToken.class);

        // starting from here the constructor differs from the DefaultLdapRealm one
        setContextFactory(new KapuaLdapContextFactory());
        setName(REALM_NAME);

        // setting DN template
        try {
            String dnTemplate = (SystemSetting.getInstance().getString(SystemSettingKey.LDAP_DN_TEMPLATE_PREFIX) +
                    SystemSetting.getInstance().getString(SystemSettingKey.LDAP_SEARCHBASE));
            setUserDnTemplate(dnTemplate);
        } catch (IllegalArgumentException iae) {
            throw new ShiroException("Invalid user DN template", iae);
        }

        KapuaLdapContextFactory cf = (KapuaLdapContextFactory) getContextFactory();

        // Set LDAP server URL; if SSL is required
        String url;
        if (SystemSetting.getInstance().getBoolean(SystemSettingKey.LDAP_SSL)) {
            url = SystemSetting.getInstance().getString(SystemSettingKey.LDAP_URL_SSL);
            cf.setSecurityProtocol("ssl");
            // FIXME: SSL not working, exception in handling certificate:
            //  javax.naming.CommunicationException: simple bind failed: localhost:636
            //  [Root exception is javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException:
            //  PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
            //  unable to find valid certification path to requested target]
        } else {
            url = SystemSetting.getInstance().getString(SystemSettingKey.LDAP_URL);
        }
        cf.setUrl(url);
        // TODO: build a regex in the CommonsValidationRegex in order to check the format of the LDAP url

        // Checking the SASL mechanism
        String ldapAuthMechs = SystemSetting.getInstance().getString(SystemSettingKey.LDAP_AUTH_MECH);
        if (!ldapAuthMechs.equals("none") && !ldapAuthMechs.equals("simple")) {

            // a SASL authentication method is provided, now check that the server provides it
            try {
                DirContext ctx = new InitialDirContext();
                Attributes attrs = ctx.getAttributes(
                        SystemSetting.getInstance().getString(SystemSettingKey.LDAP_URL),
                        new String[]{"supportedSASLMechanisms"});
                List<String> listAttr = new ArrayList<>(Arrays.asList(ldapAuthMechs.split(" ")));
                boolean atLeastOneFound = false;
                for (String myAttr : listAttr) {
                    if (attrs.get("supportedSASLMechanisms").contains(myAttr)) {
                        atLeastOneFound = true;
                        break;
                    }
                }
                if (!atLeastOneFound) {
                    throw new AuthenticationNotSupportedException(
                            "Provided LDAP authentication methods are not supported by the server");
                }
            } catch (NamingException e) {
                throw new ShiroException("Error while retrieving LDAP supported SASL mechanism", e);
            }
        }

        // Set LDAP authentication mechanism
        cf.setAuthenticationMechanism(ldapAuthMechs);

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
            // Checking user existence on ldap and extracting credentials
            AuthenticationInfo info = super.doGetAuthenticationInfo(authenticationToken);
            ldapUsername = info.getPrincipals().getPrimaryPrincipal().toString();
            // TODO: I can also extract credentials here... would this make ant sense?
        } catch (Exception e) {
            throw new UnknownAccountException("Unknown LDAP user", e);
        }

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

        User user;
        Account account;
        Credential credential;

        // Get the associated user by name
        try {
            user = KapuaSecurityUtils.doPrivileged(() -> userService.findByName(ldapUsername));
        } catch (AuthenticationException ae) {
            throw ae;
        } catch (Exception e) {
            throw new ShiroException("Error while finding user!", e);
        }

        // Now checking for the user existence
        if (user != null) {
            // The user exists on Kapua database: fill LoginAuthenticationInfo with the info from Kapua

            // Check disabled
            if (UserStatus.DISABLED.equals(user.getStatus())) {
                throw new DisabledAccountException();
            }

            // Check if expired
            if (user.getExpirationDate() != null && !user.getExpirationDate().after(new Date())) {
                throw new ExpiredCredentialsException();
            }

            final KapuaId userScopeID = user.getScopeId();
            final KapuaId userId = user.getId();

            // Find account
            try {
                account = KapuaSecurityUtils.doPrivileged(() -> accountService.find(userScopeID));
            } catch (AuthenticationException ae) {
                throw ae;
            } catch (Exception e) {
                throw new ShiroException("Error while finding account!", e);
            }

            // TODO: should I handle also the case in which the account is different from the ldap group?
            //  (match the retrieved account name with the ldap group, if they are not the same throw an exception)
            //  note: this make sense only if we are using LDAP groups as kapua accounts

            // Check account existence
            if (account == null) {
                throw new UnknownAccountException();
            }

            // Check account expired
            if (account.getExpirationDate() != null && !account.getExpirationDate().after(new Date())) {
                throw new ExpiredAccountException(account.getExpirationDate());
            }

            // Find credentials
            try {
                credential = KapuaSecurityUtils.doPrivileged(() -> {
                    CredentialListResult credentialList = credentialService.findByUserId(userScopeID, userId);

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
                throw new ShiroException("Error while finding credentials!", e);
            }

            // Note that the LDAP authentication is always performed at the beginning of this method, the credentials
            // are 'dummy'

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

        } else {
            // create new user and the credentials

            // Get Factories
            UserFactory userFactory;
            AccountFactory accountFactory;
            CredentialFactory credentialFactory;
            try {
                userFactory = LOCATOR.getFactory(UserFactory.class);
                accountFactory = LOCATOR.getFactory(AccountFactory.class);
                credentialFactory = LOCATOR.getFactory(CredentialFactory.class);
            } catch (KapuaRuntimeException kre) {
                throw new ShiroException("Error while getting factories!", kre);
            }

            // Handle ldap account policies
            if (SystemSetting.getInstance().getBoolean(SystemSettingKey.LDAP_USE_GROUPS)) {

                // TODO:
                //  - retrieve the account as group name from ldap by checking the memberOf attribute of the user
                //  (gather the account information from the ldap groups to which the user belong):
                //      - if it is set, then use the name of the group as account name:
                //          - add the user to it if such account already exists in Kapua
                //          - or create a new account (if not existing) with the group name and add the user
                //      - otherwise the authentication fails (throw an exception)
                //  PROBLEM: I must also verify that the groups are actually kapua accounts
                //  SOLUTION1: however, the DN already contains the ou=kapua-accounts, so it is sufficient to check that!
                //  (the DN of the group that is contained as value of the memberOf attribute)
                //  OR (SOLUTION2) we can target the memberOf only for groups that are in the ou=kapua-accounts
                //  (using the search filter)?

                // Retrieve the account
                try {
                    String accountName = getAccountForUser(ldapUsername);
                } catch (NamingException ne) {
                    throw new ShiroException("Error while finding account!", ne);
                }

                // FIXME: two main problems: cannot see memberOF as ldap attribute AND cannot perform a search without
                //  being admin (I guess because I should enable anonymous ldapsearch)

                // TODO: alternative solution (but I don't like it):
                //  - retrieve all the account names
                //  - for each account, check if the user belongs (memberOf) to one of these (by looping)
                //  if so, break loop and retrieve account information
                //      - if the an account corresponding to that group already exists, use that one
                //      - otherwise, create a new account (with the group name)
                //  else, do nothing

                throw new NotImplementedException("LDAP groups are not implemented yet");
            } else {
                // if I cannot access ldap group, then use the default account or create personal accounts

                String ldapAccountName;
                if (SystemSetting.getInstance().getBoolean(SystemSettingKey.LDAP_USERS_DEFAULT_ACCOUNT)) {
                    // a default name is used for the account,  which is the same for all the users
                    ldapAccountName = SystemSetting.getInstance().getString(SystemSettingKey.LDAP_ACCOUNT);
                } else {
                    // in this case every user has its own account
                    ldapAccountName = ldapUsername;
                }

                // Retrieve the parent (admin) account
                Account adminAccount;
                try {
                    String adminAccountName = SystemSetting.getInstance().getString(SystemSettingKey.SYS_ADMIN_ACCOUNT);
                    adminAccount = KapuaSecurityUtils.doPrivileged(() -> accountService.findByName(adminAccountName));
                } catch (KapuaException ke) {
                    throw new ShiroException("Error while retrieving the parent account", ke);
                }

                // Check parent account existence
                if (adminAccount == null) {
                    throw new UnknownAccountException("Unknown parent account");
                }

                // Create a new child account of the parent one
                //  - if an account do not exist, create a new one
                //  - if the account already exists, add the user to the account
                try {
                    final AccountCreator accountCreator = accountFactory.newCreator(adminAccount.getId(), ldapAccountName);
                    accountCreator.setOrganizationName(adminAccount.getOrganization().getName());
                    accountCreator.setOrganizationEmail(ldapAccountName + "@eclipse.org"); // FIXME: where can I find the domain?
                    account = KapuaSecurityUtils.doPrivileged(() -> accountService.create(accountCreator));
                } catch (KapuaDuplicateNameException dne) {
                    try {
                        account = KapuaSecurityUtils.doPrivileged(() -> accountService.findByName(ldapAccountName));

                        // Check account existence
                        if (account == null) {
                            throw new UnknownAccountException();
                        }

                        // Check account expired
                        if (account.getExpirationDate() != null && !account.getExpirationDate().after(new Date())) {
                            throw new ExpiredAccountException(account.getExpirationDate());
                        }
                    } catch (KapuaException ke) {
                        throw new ShiroException("Error while finding the account", ke);
                    }
                } catch (KapuaException ke) {
                    throw new ShiroException("Error while creating the account", ke);
                }

                // retrieving the new account id
                final KapuaId newAccountId = account.getId();
                final KapuaId parentAccountId = account.getScopeId();

                // setting the infiniteChild Entities attribute
                try {
                    final Map<String, Object> currentConfig = KapuaSecurityUtils.doPrivileged(() ->
                            userService.getConfigValues(newAccountId));
                    if (!((boolean) currentConfig.get("infiniteChildEntities"))) {
                        Map<String, Object> valueMap = new HashMap<>();
                        valueMap.put("maxNumberChildEntities", "1000");
                        valueMap.put("infiniteChildEntities", true);
                        KapuaSecurityUtils.doPrivileged(() ->
                                userService.setConfigValues(newAccountId, parentAccountId, valueMap));
                    }
                } catch (KapuaException ke) {
                    throw new ShiroException("Error while setting infinitechild users for the account " +
                            account.getName(), ke);
                }

                // Create the ldap user (since the user only exists in ldap, the user must be created on Kapua)
                // Note that the user must have the same username in ldap and in kapua; if the username is changed in ldap,
                // a new user will be created in kapua (there's no way to synchronize them)
                final UserCreator userCreator;
                try {
                    userCreator = userFactory.newCreator(newAccountId, ldapUsername);
                    user = KapuaSecurityUtils.doPrivileged(() -> userService.create(userCreator));
                    // TODO: set email etc? Maybe I can retrieve all those from ldap?
                    //  note that this kind of info should stay only in LDAP, since an update after the user creation can
                    //  compromise data integrity.
                } catch (KapuaDuplicateNameException dne) {
                    // this should never happen, since the user existence is already checked at the beginning of the method
                    throw new ShiroException("User name already exists", dne);
                } catch (KapuaException ke) {
                    throw new ShiroException("Error while creating the ldap user on the db", ke);
                }

                // Check user existence
                // TODO: remove this check? Is it useful?
                if (user == null) {
                    throw new UnknownAccountException();
                }

                // Create credential entity
                // (credentials are externally managed by ldap, CredentialType.LDAP is used for this credential type)
                final CredentialCreator credentialCreator;
                try {
                    credentialCreator = credentialFactory.newCreator(newAccountId,
                            user.getId(),
                            CredentialType.LDAP,
                            null,
                            CredentialStatus.ENABLED,
                            null);
                    credential = KapuaSecurityUtils.doPrivileged(() -> credentialService.create(credentialCreator));
                } catch (KapuaException ke) {
                    throw new ShiroException("Error while creating the ldap credentials", ke);
                }
            }
        }

        // Check if lockout policy is blocking credential
        Map<String, Object> credentialServiceConfig;
        try {
            final KapuaId accountId = account.getId();
            credentialServiceConfig = KapuaSecurityUtils.doPrivileged(() ->
                    credentialService.getConfigValues(accountId));
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

        // BuildAuthenticationInfo
        return new LoginAuthenticationInfo(getName(),
                account,
                user,
                credential,
                credentialServiceConfig);
    }


    @Override
    protected void assertCredentialsMatch(AuthenticationToken authcToken, AuthenticationInfo info)
            throws AuthenticationException {
        final LoginAuthenticationInfo kapuaInfo = (LoginAuthenticationInfo) info;

        super.assertCredentialsMatch(authcToken, info);

        final Subject currentSubject = SecurityUtils.getSubject();
        Session session = currentSubject.getSession();
        session.setAttribute("scopeId", kapuaInfo.getUser().getScopeId());
        session.setAttribute("userId", kapuaInfo.getUser().getId());
    }

    @Override
    public boolean supports(AuthenticationToken authenticationToken) {
        return authenticationToken instanceof UsernamePasswordCredentialsImpl;
    }

    /**
     * Retrieves the kapua ldap account name for the given user
     *
     * @param username the name of the user to look for
     * @return the string containing the account name
     * @throws NamingException is thrown by the ldap search
     */
    private String getAccountForUser(String username) throws NamingException {
        Set<String> groupsForUser = new LinkedHashSet<>();

        LdapContext ldapContext = getContextFactory().getLdapContext(
                "cn=admin,dc=example,dc=org", "admin");
        // LdapContext ldapContext = getContextFactory().getSystemLdapContext();
        // FIXME: not possible to perform a search if the user is not the admin...
        //  I should enable anonymous ldapsearch in the ldap server

        // parameters for the ldapsearch
        String searchBase = SystemSetting.getInstance().getString(SystemSettingKey.LDAP_SEARCHBASE);
        String searchFilter = SystemSetting.getInstance().getString(SystemSettingKey.LDAP_GROUP_SEARCHFILTER);
        Object[] searchArguments = new Object[]{username};
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        // perform ldapsearch and analyze the answer
        NamingEnumeration answer = ldapContext.search(searchBase, searchFilter, searchArguments, searchCtls);
        while (answer.hasMoreElements()) {
            SearchResult sr = (SearchResult) answer.next();
            Attributes attrs = sr.getAttributes();

            if (attrs != null) {
                NamingEnumeration ae = attrs.getAll();
                while (ae.hasMore()) {
                    Attribute attr = (Attribute) ae.next();

                    // note that there will be only one attribute which is 'memberOf'
                    // then all the groups to which the user belongs will be listed there (am I sure of this?)
                    if (attr.getID().equals("memberOf")) {

                        // Collection<String> groupNames = LdapUtils.getAllAttributeValues(attr);
                        // groupNames contains the groups found for the user
                        // TODO: check if groupNames contains "kapua-accounts" -> to use as a property

                        //groupsForUser.addAll(groupNames);
                    }
                }
            }
        }

        throw new NotImplementedException("LDAP groups are not implemented yet");
        //return null;
    }
}
