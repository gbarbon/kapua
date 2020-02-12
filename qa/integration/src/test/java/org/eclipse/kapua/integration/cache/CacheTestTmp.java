/*******************************************************************************
 * Copyright (c) 2011, 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.integration.cache;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountCreator;
import org.eclipse.kapua.service.account.AccountFactory;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.account.steps.AccountServiceSteps;
import org.eclipse.kapua.service.authentication.AuthenticationService;
import org.eclipse.kapua.service.authentication.CredentialsFactory;
import org.eclipse.kapua.service.authentication.LoginCredentials;
import org.eclipse.kapua.service.device.call.message.kura.KuraPayload;
import org.eclipse.kapua.service.device.call.message.kura.KuraPosition;
import org.eclipse.kapua.transport.message.mqtt.MqttPayload;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
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

public class CacheTestTmp {

    private static final int BROKER_START_WAIT_MILLIS = 5000;
    private static final String SYS_USERNAME = "kapua-sys";
    private static final String SYS_PASSWORD = "kapua-password";
    protected static final KapuaId SYS_SCOPE_ID = KapuaId.ONE;
    protected static final KapuaId SYS_USER_ID = new KapuaEid(BigInteger.ONE);

    private static final String CLIENT_USER = "kapua-broker";
    private static final String CLIENT_PASSWORD = "kapua-password";

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceSteps.class);

    //MqttDevice mqttDevice;
    private static KapuaLocator locator = KapuaLocator.getInstance();

    private static AuthenticationService authenticationService = locator.getService(AuthenticationService.class);
    private static AccountService accountService = locator.getService(AccountService.class);
    private static AccountFactory accountFactory = locator.getFactory(AccountFactory.class);
    private static CredentialsFactory credentialsFactory = locator.getFactory(CredentialsFactory.class);

    @Test
    public void test() throws Exception {

        //mqttDevice = new MqttDevice();
        //MqttClient mqttClient = startMqttDevice();
        testThread(1000);  // use 1000 for tests

    }

    public static void sendMessages(int nMessages, MqttClient mqttClient) throws Exception {
        sendMessageBirth(mqttClient, SYS_USERNAME);
        for (int i = 0; i < nMessages; i++) {
            sendMessage(SYS_USERNAME,"topic", mqttClient);
            if (i % 100 == 0) {
                LOGGER.info("{} msgs", i);
            }
        }
    }

    private void testThread(int nMessages) throws Exception {
        int numOfThreads = 10;
        ExecutorService threadExecutor = Executors.newFixedThreadPool(numOfThreads);
        List<Callable<Integer>> callables = new ArrayList<>();
        AtomicInteger atomicInteger = new AtomicInteger();

        for (int i = 0; i < numOfThreads; i++) {
            MqttClient mqttClient = new MqttClient("tcp://localhost:1883", "clientId"+atomicInteger.getAndIncrement(),
                    new MemoryPersistence());
            callables.add(() -> {
                StressClient stressClient = new StressClient(mqttClient, nMessages);
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

    public void loginUser(String userName, String password) throws Exception {
        LoginCredentials credentials = credentialsFactory.newUsernamePasswordCredentials(userName, password);
        authenticationService.logout();
        authenticationService.login(credentials);
    }

    private Account createAccount(String accountName) throws KapuaException {
        AccountCreator accountCreator = prepareRegularAccountCreator(SYS_SCOPE_ID, accountName);
        return accountService.create(accountCreator);
    }

    public static void startMqttDevice(MqttClient mqttClient) throws Exception {
        //mqttDevice.mqttSubscriberConnect();
        MqttConnectOptions clientOpts = new MqttConnectOptions();
        clientOpts.setUserName(SYS_USERNAME);
        clientOpts.setPassword(SYS_PASSWORD.toCharArray());
        mqttClient.connect(clientOpts);
        // Wait for broker to start
        waitInMillis(BROKER_START_WAIT_MILLIS);
        // Login with system user
        //String passwd = SYS_PASSWORD;
        //LoginCredentials credentials = credentialsFactory.newUsernamePasswordCredentials(SYS_USERNAME, passwd);
        //authenticationService.login(credentials);
    }

    public List<MqttClient> createClients(int n) throws Exception {
        List<MqttClient> mqttClientList = new ArrayList<>();
        for (int i = 0 ; i<n; i++) {
            MqttClient mqttClient = new MqttClient("tcp://localhost:1883", "clientId"+i, new MemoryPersistence());
            mqttClientList.add(mqttClient);
        }
        return mqttClientList;
    }

    /**
     * Simple wait implementation.
     *
     * @param millis milli seconds
     */
    private static void waitInMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private static void sendMessage(String accountName, String semanticTopic, MqttClient mqttClient) throws Exception {

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

    protected static void sendMessageBirth(MqttClient mqttClient, String accountName) throws Exception {
        int qos = 1;
        boolean retained = false;
        String topicStr = "$EDC/" + accountName + "/" + mqttClient.getClientId() + "/MQTT/BIRTH";
        KuraPayload kuraPayload = createMessageBirth();
        MqttPayload payload = new MqttPayload(kuraPayload.toByteArray());
        MqttMessage mqttMessage = new MqttMessage(payload.getBody());
        mqttClient.publish(topicStr, mqttMessage);
    }

    private static KuraPayload createMessageBirth() {
        KuraPayload kuraPayload = new KuraPayload();
        kuraPayload.setTimestamp(new Date());
        return kuraPayload;
    }

}

class StressClient {

    MqttClient mqttClient;
    int nMessages;

    StressClient(MqttClient mqttClient, int nMessages) throws Exception {
        this.mqttClient = mqttClient;
        this.nMessages = nMessages;
        CacheTestTmp.startMqttDevice(this.mqttClient);
    }

    public Void call() throws Exception {
        CacheTestTmp.sendMessages(nMessages, mqttClient);
        return null;
    }
}
