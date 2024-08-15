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
import org.glassfish.admin.amx.core.AMXMBeanMetadata;

/**
 * The discovery and navigation of all managed objects in the J2EE
 * management system begins with the J2EEDomain.
 *
 * @see J2EEServer
 * @see J2EEApplication
 * @see JVM
 * @see AppClientModule
 * @see EJBModule
 * @see WebModule
 * @see ResourceAdapterModule
 * @see EntityBean
 * @see StatefulSessionBean
 * @see StatelessSessionBean
 * @see MessageDrivenBean
 * @see Servlet
 * @see JavaMailResource
 * @see JCAResource
 * @see JCAConnectionFactory
 * @see JCAManagedConnectionFactory
 * @see JDBCResource
 * @see JDBCDataSource
 * @see JDBCDriver
 * @see JMSResource
 * @see JNDIResource
 * @see JTAResource
 * @see RMI_IIOPResource
 * @see URLResource
 */
@AMXMBeanMetadata(type = J2EETypes.J2EE_DOMAIN, singleton = true)
public interface J2EEDomain extends J2EEManagedObject {

    /**
     * Note that the Attribute name is case-sensitive
     * "servers" as defined by JSR 77.
     *
     * @return the ObjectNames of the J2EEServers, as Strings
     */
    @ManagedAttribute
    String[] getservers();


    @ManagedAttribute
    @Description("Get the ObjectName of the corresponding config MBean, if any")
    ObjectName getCorrespondingConfig();
}







