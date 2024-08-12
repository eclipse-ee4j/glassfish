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

import org.glassfish.admin.amx.j2ee.J2EEModule;
import org.glassfish.admin.amx.j2ee.J2EETypes;

/**
 */
public class J2EEModuleImplBase extends J2EEDeployedObjectImplBase {

    public J2EEModuleImplBase(
            final ObjectName parentObjectName,
            final Metadata meta,
            final Class<? extends J2EEModule> intf) {
        super(parentObjectName, meta, intf);
    }

    public String[] getjavaVMs() {
        return (getJ2EEServer().getjavaVMs());
    }

    /** jsr77 StateManageable impl. */
    public boolean isstateManageable() {
        return isStandAlone();
    }

    private boolean isStandAlone() {
        return j2eeType(getParent()).equals( J2EETypes.J2EE_APPLICATION );
    }
}


