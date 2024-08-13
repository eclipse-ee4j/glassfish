/*
 * Copyright (c) 1997, 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.admin.amx.impl.j2ee;

import javax.management.ObjectName;

import org.glassfish.admin.amx.j2ee.J2EETypes;
import org.glassfish.admin.amx.j2ee.ResourceAdapterModule;

public final class ResourceAdapterModuleImpl extends J2EEModuleImplBase {
    public static final Class<ResourceAdapterModule> INTF = ResourceAdapterModule.class;

    public ResourceAdapterModuleImpl(
            final ObjectName parentObjectName, final Metadata meta) {
        super(parentObjectName, meta, INTF);
    }

    public String[] getresourceAdapters() {
        return getChildrenAsStrings( J2EETypes.RESOURCE_ADAPTER );
    }
}
