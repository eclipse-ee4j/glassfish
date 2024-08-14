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

import java.util.Set;

import javax.management.ObjectName;

import org.glassfish.admin.amx.j2ee.J2EEApplication;
import org.glassfish.admin.amx.util.SetUtil;

import static org.glassfish.admin.amx.j2ee.J2EETypes.APP_CLIENT_MODULE;
import static org.glassfish.admin.amx.j2ee.J2EETypes.EJB_MODULE;
import static org.glassfish.admin.amx.j2ee.J2EETypes.RESOURCE_ADAPTER_MODULE;
import static org.glassfish.admin.amx.j2ee.J2EETypes.WEB_MODULE;

public final class J2EEApplicationImpl
        extends J2EEDeployedObjectImplBase {
    public static final Class<J2EEApplication> INTF = J2EEApplication.class;

    public J2EEApplicationImpl(
            final ObjectName parentObjectName, final Metadata meta) {
        super(parentObjectName, meta, INTF);
    }

    private static final Set<String> MODULE_TYPES    = SetUtil.newUnmodifiableStringSet(
        WEB_MODULE,
        EJB_MODULE,
        APP_CLIENT_MODULE,
        RESOURCE_ADAPTER_MODULE
    );

    public String[] getmodules() {
        return getChildrenAsStrings(MODULE_TYPES);
    }

    /** jsr77 StateManageable impl. */
    public boolean isstateManageable() {
        return true;
    }
}
