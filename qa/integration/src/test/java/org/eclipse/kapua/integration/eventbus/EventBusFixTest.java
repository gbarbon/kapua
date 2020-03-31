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

import org.eclipse.kapua.commons.core.ServiceModuleBundle;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.security.KapuaSession;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.eclipse.kapua.service.dummy.DummyEventRaiserService;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.qa.common.DBHelper;
import org.eclipse.kapua.qa.common.TestJAXBContextProvider;
import org.eclipse.kapua.qa.common.utils.EmbeddedBroker;
import org.eclipse.kapua.qa.common.utils.EmbeddedDatastore;
import org.eclipse.kapua.qa.common.utils.EmbeddedEventBroker;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Ignore
public class EventBusFixTest {

    private static final Logger logger = LoggerFactory.getLogger(EventBusFixTest.class);

    protected static final KapuaId SYS_SCOPE_ID = KapuaId.ONE;
    protected static final KapuaId SYS_USER_ID = new KapuaEid(BigInteger.ONE);
    protected static final int DEFAULT_SCOPE_ID = 42;
    protected static final KapuaId DEFAULT_ID = new KapuaEid(BigInteger.valueOf(DEFAULT_SCOPE_ID));
    private static String adminUsername = "kapua-sys";
    private static String adminPassword = "kapua-password";
    private static final String ACCOUNT_NAME = "test-account";
    private static final String PASSWORD = "Welcome12345!";
    private boolean doTest;

    private EmbeddedDatastore embeddedDatastore;
    private EmbeddedEventBroker embeddedEventBroker;
    private EmbeddedBroker embeddedBroker;
    private DBHelper dbHelper;
    private EmbeddedClusterUtils embeddedClusterUtils;

    //private DummyEventListenerService eventListenerService = KapuaLocator.getInstance().getService(DummyEventListenerService.class);
    private DummyEventRaiserService eventRaiserService = KapuaLocator.getInstance().getService(DummyEventRaiserService.class);

    @Before
    public void setUp() {
        try {
            initSystemProperties();
            //embeddedClusterUtils = new EmbeddedClusterUtils();
            //embeddedClusterUtils.init();
            doTest = true;
            ServiceModuleBundle application = new ServiceModuleBundle() {

            };
            application.startup();

            // Create KapuaSession using KapuaSecurtiyUtils and kapua-sys user as logged in user.
            // All operations on database are performed using system user. Only for unit tests.
            KapuaSession kapuaSession = new KapuaSession(null, SYS_SCOPE_ID, SYS_USER_ID);
            KapuaSecurityUtils.setSession(kapuaSession);
            XmlUtil.setContextProvider(new TestJAXBContextProvider());
        } catch (Exception e) {
            doTest = false;
            tearDown();
        }
    }

    @After
    public void tearDown() {
        try {
            KapuaSecurityUtils.clearSession();
            //embeddedClusterUtils.cleanUp();
        } catch (Exception e) {
            // do nothing
        }
    }

    @Test
    public void test() throws Exception {
        if (doTest) {
            //TestUtils.setUpAccount(ACCOUNT_NAME, PASSWORD);
            doProduceEvents(1);

            // TODO:
            // - kill the EventBroker after a given amount of time
            // - let the EventBroker perform retries for a given amount of time
            // - start the event broker and check that everything is ok
        } else {
            logger.warn("Tests not executed, error during environment set up.");
        }
    }

    private void doProduceEvents(int nProducers) throws Exception {
        ExecutorService threadExecutor = Executors.newFixedThreadPool(nProducers);
        List<Callable<Integer>> callables = new ArrayList<>();
        AtomicInteger atomicInteger = new AtomicInteger();

        for (int i = 0; i < nProducers; i++) {
            callables.add(() -> {
                ProducerThread producerThread = new ProducerThread();
                producerThread.call();
                return atomicInteger.get();
            });
        }

        // extracting results
        for (Future<Integer> future : threadExecutor.invokeAll(callables)) {
            try {
                int tmp = future.get();
                //LOGGER.info("Average time in millisecs: {}", averageTime/1000000d);
            } catch (InterruptedException e) {
                logger.warn("Producer thread was interrupted", e);
                throw e;
            } catch (ExecutionException e) {
                logger.warn("Execution exception: {}", e.getCause().getMessage());
                throw e;
            }
        }
    }

    public void initSystemProperties() {
        //System.setProperty("certificate.jwt.private.key", "certificates/key.pk8");
        //System.setProperty("certificate.jwt.certificate", "certificates/certificate.pem");
        System.setProperty("commons.db.schema", "kapuadb");
        System.setProperty("commons.db.schema.update", "true");
        System.setProperty("commons.db.connection.host", "localhost");
        System.setProperty("commons.db.connection.port", "3306");
        //System.setProperty("test.h2.server", "false");
        //System.setProperty("h2.bindAddress", "127.0.0.1");
        //XmlUtil.setContextProvider(new TestJAXBContextProvider());
        //System.setProperty("broker.ip", "192.168.33.10");
        System.setProperty("broker.ip", "localhost");
        //System.setProperty("commons.eventbus.url",  "");
        System.setProperty("commons.settings.hotswap", "true");
        System.setProperty("datastore.client.class", "org.eclipse.kapua.service.datastore.client.rest.RestDatastoreClient");
        //System.setProperty("datastore.elasticsearch.nodes",  "");
        //System.setProperty("datastore.elasticsearch.port",  "");
        //System.setProperty("datastore.index.prefix", "");
        //System.setProperty("DOCKER_CERT_PATH",  "");
        //System.setProperty("DOCKER_HOST",  "");
        //System.setProperty("kapua.config.url", "");
        //System.setProperty("kapua.config.url", "broker.setting/kapua-broker-setting-1.properties");
        System.setProperty("org.eclipse.kapua.qa.broker.extraStartupDelay", "5");
        System.setProperty("org.eclipse.kapua.qa.datastore.extraStartupDelay", "5");
        //System.setProperty("test.h2.server", "false");
        System.setProperty("test.type", "integration");
        //System.setProperty("test.type", "unit");
    }

    class ProducerThread {

        private static final int WAIT_MILLIS = 5000;

        ProducerThread() throws Exception {
        }

        public Void call() throws Exception {
            // TODO: remove the session from here ? No, I can't, nullPointer otherwise at org.eclipse.kapua.commons.event.RaiseServiceEventInterceptor.invoke(RaiseServiceEventInterceptor.java:83)
            KapuaSession kapuaSession = new KapuaSession(null, SYS_SCOPE_ID, SYS_USER_ID);
            KapuaSecurityUtils.setSession(kapuaSession);
            eventRaiserService.produceEvent();
            waitInMillis(WAIT_MILLIS);
            return null;
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
}
