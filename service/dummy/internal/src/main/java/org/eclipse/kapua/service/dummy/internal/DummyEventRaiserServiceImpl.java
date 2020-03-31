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
import org.eclipse.kapua.event.RaiseServiceEvent;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.service.dummy.DummyEventRaiserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KapuaProvider
public class DummyEventRaiserServiceImpl extends AbstractKapuaService implements DummyEventRaiserService {

    private static final Logger logger = LoggerFactory.getLogger(DummyEventRaiserServiceImpl.class);

    protected DummyEventRaiserServiceImpl() {
        super(DummyEventRaiserEntityManagerFactory.getInstance());
    }

    @Override
    @RaiseServiceEvent
    public void produceEvent() throws KapuaException {
        logger.info("Produced route event");
    }
}
