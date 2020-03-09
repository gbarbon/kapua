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
package org.eclipse.kapua.app.console.module.authorization.shared.model;

import org.eclipse.kapua.app.console.module.api.shared.model.GwtUpdatableEntityModel;

public class GwtGroup extends GwtUpdatableEntityModel {

    private static final long serialVersionUID = 1L;

    private static final String DESCRIPTION = "description";

    public String getGroupName() {
        return get("groupName");
    }

    public void setGroupName(String name) {
        set("groupName", name);
        set("value", name);
    }

    public String getGroupDescription() {
        return get(DESCRIPTION);
    }

    public String getUnescapedDescription() {
        return (String) getUnescaped(DESCRIPTION);
    }

    public void setGroupDescription(String description) {
        set(DESCRIPTION, description);
        set("value", description);
    }

    @Override
    public String toString() {
        return getGroupName();
    }
}
