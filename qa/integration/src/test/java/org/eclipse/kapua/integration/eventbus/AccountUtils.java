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
package org.eclipse.kapua.integration.eventbus;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.broker.BrokerDomains;
import org.eclipse.kapua.broker.core.BrokerJAXBContextProvider;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.model.domain.Actions;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountCreator;
import org.eclipse.kapua.service.account.AccountFactory;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.authentication.credential.CredentialCreator;
import org.eclipse.kapua.service.authentication.credential.CredentialFactory;
import org.eclipse.kapua.service.authentication.credential.CredentialService;
import org.eclipse.kapua.service.authentication.credential.CredentialStatus;
import org.eclipse.kapua.service.authentication.credential.CredentialType;
import org.eclipse.kapua.service.authorization.access.AccessInfo;
import org.eclipse.kapua.service.authorization.access.AccessInfoCreator;
import org.eclipse.kapua.service.authorization.access.AccessInfoFactory;
import org.eclipse.kapua.service.authorization.access.AccessInfoService;
import org.eclipse.kapua.service.authorization.access.AccessPermissionCreator;
import org.eclipse.kapua.service.authorization.access.AccessPermissionFactory;
import org.eclipse.kapua.service.authorization.access.AccessPermissionService;
import org.eclipse.kapua.service.authorization.permission.Permission;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.device.registry.DeviceRegistryService;
import org.eclipse.kapua.service.user.User;
import org.eclipse.kapua.service.user.UserCreator;
import org.eclipse.kapua.service.user.UserFactory;
import org.eclipse.kapua.service.user.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountUtils {

    private AccountUtils() {
    }

    public static Account setUpAccount(String accountName, String password) throws KapuaException {
        Account account = createSingleAccount(KapuaId.ONE, accountName);
        createUser(accountName, accountName, password, accountName + "@test.com");
        return account;
    }

    public static Account createSingleAccount(KapuaId scopeId, String accountName) throws KapuaException {
        AccountService accountService = KapuaLocator.getInstance().getService(AccountService.class);
        if (accountName != null && accountName.length()>0) {
            KapuaSecurityUtils.doPrivileged(() -> {
                Account accountFound = accountService.findByName(accountName);
                if (accountFound == null) {
                    return createAccount(accountName, scopeId);
                } else {
                    return accountFound;
                }
            });
        }
        return null;
    }

    public static List<Account> setUpAccounts(List<String> accounts, String password) throws KapuaException {
        List<Account> accountList = createAccounts(KapuaId.ONE, accounts);
        for (String accountName : accounts) {
            createUser(accountName, accountName, password, accountName + "@test.com");
        }
        return accountList;
    }

    public static List<Account> createAccounts(KapuaId scopeId, List<String> accountsName) throws KapuaException {
        AccountService accountService = KapuaLocator.getInstance().getService(AccountService.class);
        List<Account> accountCreated = new ArrayList<>();
        if (accountsName != null) {
            KapuaSecurityUtils.doPrivileged(() -> {
                for (String account : accountsName) {
                    Account accountFound = accountService.findByName(account);
                    if (accountFound == null) {
                        accountCreated.add(createAccount(account, scopeId));
                    } else {
                        accountCreated.add(accountFound);
                    }
                }
            });
        }
        return accountCreated;
    }

    private static Account createAccount(String accountName, KapuaId scopeId) throws KapuaException {
        return KapuaSecurityUtils.doPrivileged(() -> {
            XmlUtil.setContextProvider(new BrokerJAXBContextProvider());
            AccountService accountService = KapuaLocator.getInstance().getService(AccountService.class);
            AccountCreator accountCreator = KapuaLocator.getInstance().getFactory(AccountFactory.class).newCreator(scopeId);
            accountCreator.setName(accountName);
            accountCreator.setOrganizationName(accountName + "_ORGANIZATION");
            accountCreator.setOrganizationEmail(accountName + "_email@email.it");
            Account account = accountService.create(accountCreator);
            Map<String, Object> values = new HashMap<>();
            values.put("infiniteChildEntities", true);
            values.put("maxNumberChildEntities", 1000);
            accountService.setConfigValues(account.getId(), scopeId, values);
            Map<String, Object> valuesDevice = new HashMap<>();
            valuesDevice.put("infiniteChildEntities", true);
            valuesDevice.put("maxNumberChildEntities", 1000);
            DeviceRegistryService drs = KapuaLocator.getInstance().getService(DeviceRegistryService.class);
            drs.setConfigValues(account.getId(), scopeId, valuesDevice);
            return account;
        });
    }

    private static User createUser(String account, String username, String password, String email) throws KapuaException {
        AccountService accountService = KapuaLocator.getInstance().getService(AccountService.class);
        Account acc = KapuaSecurityUtils.doPrivileged(() -> accountService.findByName(account));
        return createUser(acc, username, password, email);
    }

    private static User createUser(Account account, String username, String password, String email) throws KapuaException {
        return KapuaSecurityUtils.doPrivileged(() -> {
            XmlUtil.setContextProvider(new BrokerJAXBContextProvider());
            UserService us = KapuaLocator.getInstance().getService(UserService.class);
            User userExist = us.findByName(username);
            if (userExist != null) {
                return userExist;
            }
            Map<String, Object> values = new HashMap<>();
            values.put("infiniteChildEntities", true);
            values.put("maxNumberChildEntities", 1000);
            us.setConfigValues(account.getId(), account.getScopeId(), values);
            UserFactory uf = KapuaLocator.getInstance().getFactory(UserFactory.class);
            UserCreator userCreator = uf.newCreator(account.getId());
            userCreator.setEmail(email);
            userCreator.setName(username);
            User user = us.create(userCreator);
            //credentialservice
            CredentialService cs = KapuaLocator.getInstance().getService(CredentialService.class);
            CredentialFactory cf = KapuaLocator.getInstance().getFactory(CredentialFactory.class);
            CredentialCreator cc = cf.newCreator(user.getScopeId(), user.getId(), CredentialType.PASSWORD, password, CredentialStatus.ENABLED, null);
            cs.create(cc);

            AccessInfoFactory aif = KapuaLocator.getInstance().getFactory(AccessInfoFactory.class);
            AccessInfoCreator aic = aif.newCreator(account.getId());
            aic.setUserId(user.getId());
            AccessInfoService ais = KapuaLocator.getInstance().getService(AccessInfoService.class);
            AccessInfo ai = ais.create(aic);

            AccessPermissionFactory apf = KapuaLocator.getInstance().getFactory(AccessPermissionFactory.class);
            PermissionFactory pf = KapuaLocator.getInstance().getFactory(PermissionFactory.class);
            Permission p = pf.newPermission(BrokerDomains.BROKER_DOMAIN, Actions.connect, account.getId());

            AccessPermissionCreator apc = apf.newCreator(account.getId());
            apc.setAccessInfoId(ai.getId());
            apc.setPermission(p);
            AccessPermissionService aps = KapuaLocator.getInstance().getService(AccessPermissionService.class);
            aps.create(apc);
            return user;
        });
    }

}
