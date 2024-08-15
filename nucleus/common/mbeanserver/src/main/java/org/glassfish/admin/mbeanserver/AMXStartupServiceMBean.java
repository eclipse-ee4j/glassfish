/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.mbeanserver;

import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

import org.glassfish.api.amx.AMXLoader;
import org.glassfish.external.amx.AMXUtil;
import org.jvnet.hk2.annotations.Contract;

/**
    MBean representing AMX, once started.

    @see org.glassfish.admin.amx.loader.AMXStartupService
 */
@Contract
public interface AMXStartupServiceMBean extends AMXLoader
{
    public ObjectName getDomainRoot();

    public JMXServiceURL[] getJMXServiceURLs();

    /** ObjectName of the MBean which actually laods AMX MBeans; that MBean references this constant */
    public static final ObjectName OBJECT_NAME = AMXUtil.newObjectName(LOADER_PREFIX + "startup");

}




