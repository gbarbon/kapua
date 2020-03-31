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

import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.qa.common.DBHelper;
import org.eclipse.kapua.qa.common.utils.EmbeddedBroker;
import org.eclipse.kapua.qa.common.utils.EmbeddedDatastore;
import org.eclipse.kapua.qa.common.utils.EmbeddedEventBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class EmbeddedClusterUtils {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedClusterUtils.class);

    protected static final int WAIT_MAX_LOOP = 60000;
    private static String adminUsername = "kapua-sys";
    private static String adminPassword = "kapua-password";
    private static final String ACCOUNT_NAME = "test-account";
    private static final String PASSWORD = "Welcome12345!";
    protected static final KapuaId SYS_SCOPE_ID = KapuaId.ONE;
    protected static final KapuaId SYS_USER_ID = new KapuaEid(BigInteger.ONE);

    private EmbeddedDatastore embeddedDatastore;
    private EmbeddedEventBroker embeddedEventBroker;
    private EmbeddedBroker embeddedBroker;
    private DBHelper dbHelper;

    public void init() throws Exception {
        initSystemProperties();
        dbHelper = new DBHelper();
        embeddedEventBroker = new EmbeddedEventBroker(dbHelper);
        embeddedEventBroker.start();
        embeddedDatastore = new EmbeddedDatastore();
        embeddedDatastore.setup();
//        as = KapuaLocator.getInstance().getService(AccountService.class);
//        cs = KapuaLocator.getInstance().getService(ClusterService.class);
//        deprs = KapuaLocator.getInstance().getService(DeploymentRegistryService.class);
//        deprf = KapuaLocator.getInstance().getFactory(DeploymentRegistryFactory.class);
//        accounts = AccountUtils.createSingleAccount(KapuaId.ONE, accountsName);
//        if (dbSeeding != null) {
//            dbSeeding.call();
//        }
        embeddedBroker = new EmbeddedBroker();
        embeddedBroker.start();
    }

    public void cleanUp() throws Exception {
        //shutdown
        if (embeddedBroker != null) {
            embeddedBroker.stop();
        }
        if (embeddedDatastore != null) {
            embeddedDatastore.closeNode();
        }
        if (embeddedEventBroker != null) {
            embeddedEventBroker.stop();
        }
        if (dbHelper != null) {
            dbHelper.dropAll();
            dbHelper.close();
            //dbHelper.deleteAllAndClose();
        }
    }

    public void initSystemProperties() {
        //System.setProperty("certificate.jwt.private.key", "certificates/key.pk8");
        //System.setProperty("certificate.jwt.certificate", "certificates/certificate.pem");
        //System.setProperty("commons.db.schema", "kapuadb");
        //System.setProperty("commons.db.schema.update", "true");
        //System.setProperty("commons.db.connection.host", "localhost");
        //System.setProperty("commons.db.connection.port", "3306");
        //System.setProperty("test.h2.server", "false");
        //System.setProperty("h2.bindAddress", "127.0.0.1");
        //XmlUtil.setContextProvider(new TestJAXBContextProvider());
        System.setProperty("broker.ip", "192.168.33.10");
        //System.setProperty("broker.ip", "localhost");
        //System.setProperty("certificate.jwt.certificate", "certificates/certificate.pem");
        //System.setProperty("certificate.jwt.private.key", "certificates/key.pk8");
        //System.setProperty("commons.db.connection.host", "localhost");
        //System.setProperty("commons.db.connection.port", "3306");
        System.setProperty("commons.db.schema.update", "true");
        System.setProperty("commons.db.schema", "kapuadb");
        //System.setProperty("commons.eventbus.url",  "");
        System.setProperty("commons.settings.hotswap", "true");
        System.setProperty("datastore.client.class", "org.eclipse.kapua.service.datastore.client.rest.RestDatastoreClient");
        //System.setProperty("datastore.elasticsearch.nodes",  "");
        //System.setProperty("datastore.elasticsearch.port",  "");
        //System.setProperty("datastore.index.prefix", "");
        //System.setProperty("DOCKER_CERT_PATH",  "");
        //System.setProperty("DOCKER_HOST",  "");
        //System.setProperty("h2.bindAddress", "127.0.0.1");
        //System.setProperty("kapua.config.url", "");
        //System.setProperty("kapua.config.url", "broker.setting/kapua-broker-setting-1.properties");
        System.setProperty("org.eclipse.kapua.qa.broker.extraStartupDelay", "5");
        System.setProperty("org.eclipse.kapua.qa.datastore.extraStartupDelay", "5");
        //System.setProperty("test.h2.server", "false");
        System.setProperty("test.type", "integration");
        System.setProperty("test.type", "unit");
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
