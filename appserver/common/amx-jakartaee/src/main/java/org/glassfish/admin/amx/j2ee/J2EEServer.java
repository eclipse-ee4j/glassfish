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

package org.glassfish.admin.amx.j2ee;

import javax.management.ObjectName;

import org.glassfish.admin.amx.annotation.Description;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;


/**
 */
@AMXMBeanMetadata(type = J2EETypes.J2EE_SERVER)
public interface J2EEServer extends J2EELogicalServer {

    /**
     * Restart the server.
     * <b>Enterprise Edition only.</b>
     */
    @ManagedOperation
    void restart();


    /**
     * Note that the Attribute name is case-sensitive
     * "deployedObjects" as defined by JSR 77.
     *
     * @return the ObjectNames as Strings
     */
    @ManagedAttribute
    String[] getdeployedObjects();


    /**
     * In 8.1, there will only ever be one JVM for a J2EEServer.
     * Note that the Attribute name is case-sensitive
     * "javaVMs" as defined by JSR 77.
     *
     * @return the ObjectNames as Strings
     */
    @ManagedAttribute
    String[] getjavaVMs();


    /**
     * There is always a single JVM for a J2EEServer.
     *
     * @return JVM
     */
    @ManagedAttribute
    String getjvm();


    /**
     * Note that the Attribute name is case-sensitive
     * "resources" as defined by JSR 77.
     *
     * @return the ObjectNames as Strings
     */
    @ManagedAttribute
    String[] getresources();


    /**
     * Note that the Attribute name is case-sensitive
     * "serverVendor" as defined by JSR 77.
     *
     * @return the server vendor, a free-form String
     */
    @ManagedAttribute
    String getserverVendor();


    /**
     * Note that the Attribute name is case-sensitive
     * "serverVersion" as defined by JSR 77.
     *
     * @return the server version, a free-form String
     */
    @ManagedAttribute
    String getserverVersion();


    @ManagedAttribute
    @Description("Get the ObjectName of the corresponding config MBean, if any")
    ObjectName getCorrespondingConfig();
}


