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

import org.glassfish.admin.amx.j2ee.J2EEDeployedObject;

/**
 */
public class J2EEDeployedObjectImplBase extends J2EEManagedObjectImplBase // implements J2EEDeployedObject
{
    public J2EEDeployedObjectImplBase(
            final ObjectName parentObjectName,
            final Metadata meta,
            final Class<? extends J2EEDeployedObject> intf) {
        super(parentObjectName, meta, intf);
    }

    public String getdeploymentDescriptor() {
        /*
         * FindBugs warns about the confusing mismatch between this method's
         * name and the name of the method it invokes.  This method - and
         * getserver below - are intended this way.
         */
        return metadata().getDeploymentDescriptor();
    }

    public String getserver() {
        return getServerObjectName().toString();
    }

    public void start() {
        checkstateManageable();
    //getDelegate().invoke( "start", null, null );
    }

    public void startRecursive() {
        start();
    }

    public void stop() {
        checkstateManageable();
    //getDelegate().invoke( "stop", null, null );
    }

    private void checkstateManageable() {
        if (!isstateManageable()) {
            throw new UnsupportedOperationException("stateManageable is false");
        }
    }
}




