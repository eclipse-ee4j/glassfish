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

package com.sun.enterprise.v3.admin;

import javax.management.MBeanServer;
import javax.management.MBeanServerBuilder;
import javax.management.MBeanServerDelegate;

/**
 * AppServer MBSBuilder for PE set as the value for javax.management.initial.builder
 * in the environment. This builder extends from javax.management.MBeanServerBuilder
 * and creates MBS with app server interceptors
 */
public class AppServerMBeanServerBuilder extends javax.management.MBeanServerBuilder {

    private static final MBeanServerBuilder defaultBuilder = new MBeanServerBuilder();
    private static MBeanServer _defaultMBeanServer;

     @Override
    public MBeanServer newMBeanServer(String defaultDomain, MBeanServer outer, MBeanServerDelegate delegate) {
         MBeanServer mbeanServer;
         synchronized (AppServerMBeanServerBuilder.class) {
             if ( _defaultMBeanServer == null ) {
                 mbeanServer = newAppServerMBeanServer(defaultDomain, delegate);
                 _defaultMBeanServer = mbeanServer;
             }
             else {
                 mbeanServer = defaultBuilder.newMBeanServer(
                     defaultDomain, outer,  delegate);
             }
         }

         return mbeanServer;
     }

     protected MBeanServer newAppServerMBeanServer(String defaultDomain, MBeanServerDelegate delegate) {
        final DynamicInterceptor result = new DynamicInterceptor();
        final MBeanServer jmxMBS = defaultBuilder.newMBeanServer(
            defaultDomain, result, delegate);

        result.setDelegateMBeanServer( jmxMBS );

        return result;
     }

    @Override
    public MBeanServerDelegate newMBeanServerDelegate()  {
        return defaultBuilder.newMBeanServerDelegate();
     }
}
