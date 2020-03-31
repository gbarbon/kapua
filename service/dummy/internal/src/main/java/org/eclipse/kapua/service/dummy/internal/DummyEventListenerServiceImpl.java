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
package org.eclipse.kapua.service.dummy.internal;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.service.internal.AbstractKapuaService;
import org.eclipse.kapua.event.ListenServiceEvent;
import org.eclipse.kapua.event.ServiceEvent;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.service.dummy.DummyEventListenerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KapuaProvider
public class DummyEventListenerServiceImpl extends AbstractKapuaService implements DummyEventListenerService {

    private static final Logger logger = LoggerFactory.getLogger(DummyEventListenerServiceImpl.class);

    protected DummyEventListenerServiceImpl() {
        super(DummyEventListenerEntityManagerFactory.getInstance());
    }

    @Override
    @ListenServiceEvent(fromAddress = "testEventRaiser")
    public void onKapuaEvent(ServiceEvent kapuaEvent) throws KapuaException {
        logger.info("Received test event");
    }
}
