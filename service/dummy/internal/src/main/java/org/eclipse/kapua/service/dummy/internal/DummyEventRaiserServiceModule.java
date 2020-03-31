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
import org.eclipse.kapua.commons.event.ServiceEventModule;
import org.eclipse.kapua.commons.event.ServiceEventModuleConfiguration;
import org.eclipse.kapua.commons.event.ServiceInspector;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.service.dummy.DummyEventRaiserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@KapuaProvider
public class DummyEventRaiserServiceModule extends ServiceEventModule {

    private static final String UNIQUE_ID = "_" + UUID.randomUUID().toString();

    @Inject
    private DummyEventRaiserService eventRaiserService;

    @Override
    protected ServiceEventModuleConfiguration initializeConfiguration() {
        String address = "testEventRaiser";
        List<ServiceEventClientConfiguration> secc = new ArrayList<>();
        secc.addAll(ServiceInspector.getEventBusClients(eventRaiserService, DummyEventRaiserService.class));
        return new ServiceEventModuleConfiguration(
                address,
                DummyEventRaiserEntityManagerFactory.getInstance(),
                secc.toArray(new ServiceEventClientConfiguration[0]));
                //updateClientConfiguration(secc, UNIQUE_ID));
    }

}
