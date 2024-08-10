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

import org.glassfish.admin.amx.j2ee.J2EEManagedObject;
import org.glassfish.admin.amx.j2ee.JVM;

/**
Identifies a Java VM being utilized by a server.
 */
public final class JVMImpl
        extends J2EEManagedObjectImplBase {
    public static final Class<? extends J2EEManagedObject> INTF = JVM.class;

    public JVMImpl(
            final ObjectName parentObjectName, final Metadata meta) {
        super(parentObjectName, meta, INTF);
    }

    public String getjavaVersion() {
        return System.getProperty("java.version");
    }

    public String getjavaVendor() {
        return System.getProperty("java.vendor");
    }

    public String getnode() {
        String fullyQualifiedHostName = "localhost";

        /*
        Underlying MBean does not comply with JSR77.3.4.1.3, which states:
        "Identifies the node (machine) this JVM is running on. The value of the node
        attribute must be the fully quailified hostname of the node the JVM is running on."

        value seems to be of the form: BLACK/129.150.29.138

        Roll our own instead.
         */
        try {
            fullyQualifiedHostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
        } catch (java.net.UnknownHostException e) {
        }

        return (fullyQualifiedHostName);
    }
}
