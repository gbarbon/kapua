/*******************************************************************************
 * Copyright (c) 2016 , 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.integration.testredundantqueries;

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
import org.eclipse.kapua.service.authentication.AuthenticationService;
import org.eclipse.kapua.service.authentication.CredentialsFactory;
import org.eclipse.kapua.service.authentication.LoginCredentials;
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
import org.eclipse.kapua.service.device.call.message.kura.KuraPayload;
import org.eclipse.kapua.service.device.call.message.kura.KuraPosition;
import org.eclipse.kapua.service.device.registry.DeviceRegistryService;
import org.eclipse.kapua.service.user.User;
import org.eclipse.kapua.service.user.UserCreator;
import org.eclipse.kapua.service.user.UserFactory;
import org.eclipse.kapua.service.user.UserService;
import org.eclipse.kapua.transport.message.mqtt.MqttPayload;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Long run test to check redundant queries.
 */
public class TestRedundantQueries {

    private static String clusterName = "camel-rest-cluster";
    private static int nodeCount = 1;
    private static String adminUsername = "kapua-sys";
    private static String adminPassword = "kapua-password";

    private static final Logger LOGGER = LoggerFactory.getLogger(TestRedundantQueries.class);

    protected static final String USER_PASSWORD = "Welcome12345!";
    private static final String BROKER_IP = "localhost";
    private static final int BROKER_PORT = 8161;
/*
    @Test
    public void testEcSys() throws Exception {
        doTest(new String[]{adminUsername}, adminPassword, 10, 1000);
    }*/

    @Test
    public void testAccount1() throws Exception {
        doTest(new String[]{"account-1"}, USER_PASSWORD, 1, 10);
    }

    @Test
    public void testAccounts10() throws Exception {
        String[] accountNamesArray = new String[]{"account-1", "account-2", "account-3", "account-4", "account-5",
                "account-6", "account-7", "account-8", "account-9", "account-10"};
        doTest(accountNamesArray, USER_PASSWORD, 10, 1000);
    }

    @Test
    public void testAccountsConnectDisconnect25() throws Exception {
        String[] accountNamesArray = new String[]{"account-1", "account-2", "account-3", "account-4", "account-5",
                "account-6", "account-7", "account-8", "account-9", "account-10",
                "account-11", "account-12", "account-13", "account-14", "account-15",
                "account-16", "account-17", "account-18", "account-19", "account-20",
                "account-21", "account-22", "account-23", "account-24", "account-25"};
        List<String> accountNamesList = Arrays.asList(accountNamesArray);
        List<Account> accounts = setUpAccounts(accountNamesList, USER_PASSWORD);
        //testThreadConnections(25, 3, 10, 15000, 5000, accountNamesArray, USER_PASSWORD);  // first test
        testThreadConnections(25, 10, 10, 15000, 5000, accountNamesArray, USER_PASSWORD);
    }

    private void doTest(String[] accountNames, String password, int nClients, int nMessages) throws Exception {
        List<String> accountNamesList = Arrays.asList(accountNames);
        List<Account> accounts = setUpAccounts(accountNamesList, password);
        testThread(nClients, nMessages, accountNames, password);  // use 1000 for tests
    }

    public static void sendMessages(int nMessages, MqttClient mqttClient, String accountName) throws Exception {
        sendMessageBirth(mqttClient, accountName);
        for (int i = 0; i < nMessages; i++) {
            sendMessage(accountName, "topic", mqttClient);
            if (i % 100 == 0) {
                LOGGER.info("{} msgs", i);
            }
        }
    }

    private void testThread(int nClients, int nMessages, String[] accountNamesArray, String password) throws Exception {
        ExecutorService threadExecutor = Executors.newFixedThreadPool(nClients);
        List<Callable<Integer>> callables = new ArrayList<>();
        AtomicInteger atomicInteger = new AtomicInteger();

        for (int i = 0; i < nClients; i++) {
            MqttClient mqttClient = new MqttClient("tcp://localhost:1883", "clientId" + atomicInteger.getAndIncrement(),
                    new MemoryPersistence());
            String accountName = accountNamesArray[i % accountNamesArray.length];
            callables.add(() -> {
                StressClient stressClient = new StressClient(mqttClient, nMessages, accountName, accountName, password);
                stressClient.call();
                return atomicInteger.get();
            });
        }

        // extracting results
        for (Future<Integer> future : threadExecutor.invokeAll(callables)) {
            try {
                int tmp = future.get();
                //LOGGER.info("Average time in millisecs: {}", averageTime/1000000d);
            } catch (InterruptedException e) {
                LOGGER.warn("Thread was interrupted", e);
                throw e;
            } catch (ExecutionException e) {
                LOGGER.warn("Execution exception: {}", e.getCause().getMessage());
                throw e;
            }
        }
    }

    private void testThreadConnections(int nClients, int nMessages, int nOfConnections, long connectedTime, long disconnectedTime, String[] accountNamesArray, String password) throws Exception {
        ExecutorService threadExecutor = Executors.newFixedThreadPool(nClients);
        List<Callable<Integer>> callables = new ArrayList<>();
        AtomicInteger atomicInteger = new AtomicInteger();

        for (int i = 0; i < nClients; i++) {
            MqttClient mqttClient = new MqttClient("tcp://localhost:1883", "clientId" + atomicInteger.getAndIncrement(),
                    new MemoryPersistence());
            String accountName = accountNamesArray[i % accountNamesArray.length];
            callables.add(() -> {
                StressClientConnection stressClientConnection = new StressClientConnection(mqttClient, nMessages, nOfConnections, connectedTime, disconnectedTime, accountName, accountName, password);
                stressClientConnection.call();
                return atomicInteger.get();
            });
        }

        // extracting results
        for (Future<Integer> future : threadExecutor.invokeAll(callables)) {
            try {
                int tmp = future.get();
                //LOGGER.info("Average time in millisecs: {}", averageTime/1000000d);
            } catch (InterruptedException e) {
                LOGGER.warn("Thread was interrupted", e);
                throw e;
            } catch (ExecutionException e) {
                LOGGER.warn("Execution exception: {}", e.getCause().getMessage());
                throw e;
            }
        }
    }


    public void loginUser(String userName, String password) throws Exception {
        CredentialsFactory credentialsFactory = KapuaLocator.getInstance().getFactory(CredentialsFactory.class);
        AuthenticationService authenticationService = KapuaLocator.getInstance().getService(AuthenticationService.class);
        LoginCredentials credentials = credentialsFactory.newUsernamePasswordCredentials(userName, password);
        authenticationService.logout();
        authenticationService.login(credentials);
    }

    public List<MqttClient> createClients(int n) throws Exception {
        List<MqttClient> mqttClientList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            MqttClient mqttClient = new MqttClient("tcp://localhost:1883", "clientId" + i, new MemoryPersistence());
            mqttClientList.add(mqttClient);
        }
        return mqttClientList;
    }

    static void sendMessage(String accountName, String semanticTopic, MqttClient mqttClient) throws Exception {

        int qos = 1;
        boolean retained = false;
        String topicStr = accountName + "/" + mqttClient.getClientId() + "/" + semanticTopic;
        //MqttTopic topic = new MqttTopic(topicStr);
        KuraPayload kuraPayload = createKuraPayload();
        MqttPayload payload = new MqttPayload(kuraPayload.toByteArray());
        MqttMessage mqttMessage = new MqttMessage(payload.getBody());
        mqttClient.publish(topicStr, mqttMessage);
        /*if (waitFor>0) {
            Util.waitFor("Waiting for message publishing", waitFor);
        }*/
    }

    private static KuraPayload createKuraPayload() {

        KuraPayload kuraPayload = new KuraPayload();
        kuraPayload.setBody("abcdefgh".getBytes());
        kuraPayload.setTimestamp(new Date());
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("eseqn", 98);
        kuraPayload.setMetrics(metrics);
        KuraPosition position = new KuraPosition();
        position.setLatitude(45.234);
        position.setLongitude(-7.3456);
        position.setAltitude(1.0);
        position.setHeading(5.4);
        position.setPrecision(0.1);
        position.setSpeed(23.5);
        position.setTimestamp(new Date());
        position.setSatellites(3);
        position.setStatus(2);
        kuraPayload.setPosition(position);

        return kuraPayload;
    }

    private AccountCreator prepareRegularAccountCreator(KapuaId parentId, String name) {
        AccountFactory accountFactory = KapuaLocator.getInstance().getFactory(AccountFactory.class);
        AccountCreator tmpAccCreator = accountFactory.newCreator(parentId, name);

        tmpAccCreator.setOrganizationName("org_" + name);
        tmpAccCreator.setOrganizationPersonName(String.format("person_%s", name));
        tmpAccCreator.setOrganizationCountry("home_country");
        tmpAccCreator.setOrganizationStateProvinceCounty("home_province");
        tmpAccCreator.setOrganizationCity("home_city");
        tmpAccCreator.setOrganizationAddressLine1("address_line_1");
        tmpAccCreator.setOrganizationAddressLine2("address_line_2");
        tmpAccCreator.setOrganizationEmail("org_" + name + "@org.com");
        tmpAccCreator.setOrganizationZipPostCode("1234");
        tmpAccCreator.setOrganizationPhoneNumber("012/123-456-789");

        return tmpAccCreator;
    }

    protected static void sendMessageLifecycle(MqttClient mqttClient, String accountName, String type) throws Exception {
        int qos = 1;
        boolean retained = false;
        String topicStr = "$EDC/" + accountName + "/" + mqttClient.getClientId() + "/MQTT/" + type;
        KuraPayload kuraPayload = createMessageBirth();
        MqttPayload payload = new MqttPayload(kuraPayload.toByteArray());
        MqttMessage mqttMessage = new MqttMessage(payload.getBody());
        mqttClient.publish(topicStr, mqttMessage);
    }

    public static void sendMessageBirth(MqttClient mqttClient, String accountName) throws Exception {
        sendMessageLifecycle(mqttClient, accountName, "BIRTH");
    }

    public static void sendMessageDisconnect(MqttClient mqttClient, String accountName) throws Exception {
        sendMessageLifecycle(mqttClient, accountName, "DEATH");
    }

    private static KuraPayload createMessageBirth() {
        KuraPayload kuraPayload = new KuraPayload();
        kuraPayload.setTimestamp(new Date());
        return kuraPayload;
    }

    public List<Account> setUpAccounts(List<String> accounts, String password) throws KapuaException {
        List<Account> accountList = createAccounts(KapuaId.ONE, accounts);
        for (String accountName : accounts) {
            createUser(accountName, accountName, password, accountName + "@test.com");
        }
        return accountList;
    }

    public List<Account> createAccounts(KapuaId scopeId, List<String> accountsName) throws KapuaException {
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

    public static Account createAccount(String accountName, KapuaId scopeId) throws KapuaException {
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

    public User createUser(String account, String username, String password, String email) throws KapuaException {
        AccountService accountService = KapuaLocator.getInstance().getService(AccountService.class);
        Account acc = KapuaSecurityUtils.doPrivileged(() -> accountService.findByName(account));
        return createUser(acc, username, password, email);
    }

    public User createUser(Account account, String username, String password, String email) throws KapuaException {
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

class StressClient {

    private static final int BROKER_START_WAIT_MILLIS = 5000;

    MqttClient mqttClient;
    int nMessages;
    String accountName;

    StressClient(MqttClient mqttClient, int nMessages, String accountName, String userName, String password) throws Exception {
        this.mqttClient = mqttClient;
        this.nMessages = nMessages;
        this.accountName = accountName;

        startMqttDevice(userName, password);
    }

    public Void call() throws Exception {
        TestRedundantQueries.sendMessages(nMessages, mqttClient, accountName);
        return null;
    }

    public void startMqttDevice(String userName, String password) throws Exception {
        MqttConnectOptions clientOpts = new MqttConnectOptions();
        clientOpts.setUserName(userName);
        clientOpts.setPassword(password.toCharArray());
        mqttClient.connect(clientOpts);
        waitInMillis(BROKER_START_WAIT_MILLIS);
    }

    /**
     * Simple wait implementation.
     *
     * @param millis milli seconds
     */
    private void waitInMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}

class StressClientConnection {

    int nMessages;
    String accountName;
    int nOfConnections;
    long connectedTime;
    long disconnectedTime;
    MqttClient mqttClient;
    MqttConnectOptions clientOpts;

    StressClientConnection(MqttClient mqttClient, int nMessages, int nOfConnections, long connectedTime, long disconnectedTime, String accountName, String userName, String password) throws Exception {
        this.mqttClient = mqttClient;
        this.nMessages = nMessages;
        this.accountName = accountName;
        this.nOfConnections = nOfConnections;
        this.connectedTime = connectedTime;
        this.disconnectedTime = disconnectedTime;
        clientOpts = new MqttConnectOptions();

        clientOpts.setUserName(userName);
        clientOpts.setPassword(password.toCharArray());
    }

    public Void call() throws Exception {
        Thread.sleep(((long) (Math.random()*(connectedTime+disconnectedTime))));
        long waitTime = connectedTime / (nMessages + 1);
        for (int i = 0; i < nOfConnections; i++) {
            connectDevice();
            for (int j = 0; j < nMessages; j++) {
                Thread.sleep(waitTime);
                TestRedundantQueries.sendMessage(accountName, "topic", mqttClient);
            }
            Thread.sleep(waitTime);
            disconnectDevice();
            Thread.sleep(disconnectedTime);
        }
        return null;
    }

    public void connectDevice() throws Exception {
        mqttClient.connect(clientOpts);
        TestRedundantQueries.sendMessageBirth(mqttClient, accountName);
    }

    public void disconnectDevice() throws Exception {
        TestRedundantQueries.sendMessageDisconnect(mqttClient, accountName);
        mqttClient.disconnect();
    }
}

