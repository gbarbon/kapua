/*******************************************************************************
 * Copyright (c) 2017, 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.elasticsearch.client;

/**
 * {@link DatastoreClient} wrapper definition.
 *
 * @param <C> {@link DatastoreClient} type
 * @since 1.0.0
 */
public interface ClientProvider<C> {

    /**
     * Gets an intialized {@link DatastoreClient} instance.
     *
     * @return An intialized {@link DatastoreClient} instance.
     * @since 1.0.0
     */
    C getClient();
}
