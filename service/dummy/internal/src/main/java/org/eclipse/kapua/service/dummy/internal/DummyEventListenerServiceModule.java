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

import org.eclipse.kapua.commons.event.ServiceEventClientConfiguration;
import org.eclipse.kapua.commons.event.ServiceEventModuleConfiguration;
import org.eclipse.kapua.commons.event.ServiceInspector;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.service.KapuaService;
import org.eclipse.kapua.service.dummy.DummyEventListenerService;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@KapuaProvider
public class DummyEventListenerServiceModule extends TestServiceEventModule implements KapuaService {

    private static final String UNIQUE_ID = "_" + UUID.randomUUID().toString();

    @Inject
    private DummyEventListenerService eventListerService;

    @Override
    protected ServiceEventModuleConfiguration initializeConfiguration() {
        String address = "testEventListener";
        List<ServiceEventClientConfiguration> secc = ServiceInspector.getEventBusClients(eventListerService, DummyEventListenerService.class);
        return new ServiceEventModuleConfiguration(
                address,
                DummyEventListenerEntityManagerFactory.getInstance(),
                updateClientConfiguration(secc, UNIQUE_ID));
    }

}
