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

package org.glassfish.admin.amx.impl.mbean;

import javax.management.MBeanServer;

/**
Factory to create the org.glassfish.admin.amx.base.SystemInfo implementation.
For now, only one implementation instance is allowed.
 */
public final class SystemInfoFactory {

    static SystemInfoImpl INSTANCE = null;

    /**
    Return the actual implementation class, because some method(s) are needed internal to the
    server, but not appropriate for the MBean clients.

    @return the SystemInfoImpl, *or null if not yet initialized*
     */
    public static synchronized SystemInfoImpl getInstance() {
        return INSTANCE;
    }

    /**
    Create the singleton instance.  Intended for exclusive use by the appropriate code
    to initialize once at startup.
     */
    public static synchronized SystemInfoImpl createInstance(final MBeanServer server) {
        if (INSTANCE == null) {
            INSTANCE = new SystemInfoImpl(server);

            new SystemInfoIniter(server, INSTANCE).init();
        } else {
            //throw new RuntimeException("can only initialize once--bug");
        }
        return INSTANCE;
    }

    public static synchronized void removeInstance() {
        if(INSTANCE != null) {
            INSTANCE = null;
        }
    }
}








