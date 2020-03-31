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

import org.eclipse.kapua.commons.event.ServiceEventClientConfiguration;
import org.eclipse.kapua.commons.event.ServiceEventModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class TestServiceEventModule extends ServiceEventModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestServiceEventModule.class);

    protected ServiceEventClientConfiguration[] updateClientConfiguration(List<ServiceEventClientConfiguration> configs, String uniqueId) {
        ArrayList<ServiceEventClientConfiguration> configList = new ArrayList<>();
        for(ServiceEventClientConfiguration config : configs) {
            if(config.getEventListener() == null) {
                // config for @RaiseServiceEvent
                LOGGER.debug("Adding config for @RaiseServiceEvent - address: {}, name: {}, listener: {}", config.getAddress(), config.getClientName(), config.getEventListener());
                configList.add(config);
            } else {
                // config for @ListenServiceEvent
                String uniqueName = config.getClientName() + uniqueId;
                LOGGER.debug("Adding config for @ListenServiceEvent - address: {}, name: {}, listener: {}", config.getAddress(), uniqueName, config.getEventListener());
                configList.add(new ServiceEventClientConfiguration(config.getAddress(), uniqueName, config.getEventListener()));
            }
        }
        return configList.toArray(new ServiceEventClientConfiguration[0]);
    }

}

