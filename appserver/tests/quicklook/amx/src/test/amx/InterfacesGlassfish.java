/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package amxtest;

import org.glassfish.admin.amx.j2ee.*;

/** Adds glassfish interfaces (those that are present in the glassfish distribution
  as opposed to the basic web distribution */
final class InterfacesGlassfish extends Interfaces {
    public InterfacesGlassfish()
    {
        add(
            AppClientModule.class,
            EJBModule.class,
            EntityBean.class,
            J2EEApplication.class,
            J2EEDomain.class,
            J2EEResource.class,
            J2EEServer.class,
            JCAConnectionFactory.class,
            JCAManagedConnectionFactory.class,
            JCAResource.class,
            JDBCDataSource.class,
            JDBCDriver.class,
            org.glassfish.admin.amx.j2ee.JDBCResource.class,
            JMSResource.class,
            org.glassfish.admin.amx.j2ee.JNDIResource.class,
            JTAResource.class,
            JVM.class,
            JavaMailResource.class,
            MessageDrivenBean.class,
            RMI_IIOPResource.class,
            org.glassfish.admin.amx.j2ee.ResourceAdapter.class,
            ResourceAdapterModule.class,
            Servlet.class,
            StatefulSessionBean.class,
            StatelessSessionBean.class,
            URLResource.class,
            WebModule.class
        );
    }
}


































