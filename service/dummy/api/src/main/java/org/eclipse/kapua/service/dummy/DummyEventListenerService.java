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
package org.eclipse.kapua.service.dummy;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.event.ServiceEvent;
import org.eclipse.kapua.service.KapuaService;

public interface DummyEventListenerService extends KapuaService {

    void onKapuaEvent(ServiceEvent kapuaEvent) throws KapuaException;
}
