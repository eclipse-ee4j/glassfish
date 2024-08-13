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

package org.glassfish.admin.mbeanserver;

import javax.management.MBeanServer;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.PostStartupRunLevel;
import org.jvnet.hk2.annotations.Service;

/**
    Factory for the MBeanServer.  Required so that HK2 can find an MBeanServer
    for modules doing @Inject MBeanServer.
 */
@Service
@RunLevel(mode=RunLevel.RUNLEVEL_MODE_NON_VALIDATING, value=PostStartupRunLevel.VAL)
public final class MBeanServerFactory implements Factory<MBeanServer> {
    private static void debug( final String s ) { System.out.println(s); }

    private final MBeanServer     mMBeanServer;

    public MBeanServerFactory()
    {
        // initialize eagerly; ~20ms
        mMBeanServer = java.lang.management.ManagementFactory.getPlatformMBeanServer();
    }

    public void postConstruct()
    {
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Factory#provide()
     */
    @Override
    public MBeanServer provide() {
        return mMBeanServer;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Factory#dispose(java.lang.Object)
     */
    @Override
    public void dispose(MBeanServer instance) {

    }

}











