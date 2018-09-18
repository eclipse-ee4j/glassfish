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

package org.glassfish.admin.amxtest;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.jmx.MBeanRegistrationListener;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 A NotificationListener which tracks registration of MBeans.
 */
public final class RegistrationListener
        extends MBeanRegistrationListener {
    private final MBeanServerConnection mConn;

    private final Set<ObjectName> mRegistered;
    private final Set<ObjectName> mUnregistered;
    private final Set<ObjectName> mCurrentlyRegistered;

    private RegistrationListener(
            final String name,
            final MBeanServerConnection conn)
            throws InstanceNotFoundException, java.io.IOException {
        super("RegistrationListener", conn, null);

        mConn = conn;

        mRegistered = new HashSet<ObjectName>();
        mUnregistered = new HashSet<ObjectName>();
        mCurrentlyRegistered = new HashSet<ObjectName>();

        queryAllAMX();
    }

    public static RegistrationListener
    createInstance(
            final String name,
            final MBeanServerConnection conn)
            throws InstanceNotFoundException, java.io.IOException {
        final RegistrationListener listener = new RegistrationListener(name, conn);

        JMXUtil.listenToMBeanServerDelegate(conn, listener, null, null);

        return listener;
    }


    private void
    queryAllAMX() {
        try {
            final ObjectName pat = Util.newObjectNamePattern(AMX.JMX_DOMAIN, "*");
            final Set<ObjectName> all = JMXUtil.queryNames(mConn, pat, null);

            mCurrentlyRegistered.addAll(all);
        }
        catch (IOException e) {
        }
    }

    public void
    notifsLost() {
        queryAllAMX();
    }

    private boolean
    isAMX(final ObjectName objectName) {
        return objectName.getDomain().equals(AMX.JMX_DOMAIN);
    }

    protected synchronized void
    mbeanRegistered(final ObjectName objectName) {
        if (isAMX(objectName)) {
            mRegistered.add(objectName);
            mCurrentlyRegistered.add(objectName);
        }
    }

    protected synchronized void
    mbeanUnregistered(final ObjectName objectName) {
        if (isAMX(objectName)) {
            mUnregistered.add(objectName);
            mCurrentlyRegistered.remove(objectName);
        }
    }

    public Set<ObjectName>
    getRegistered() {
        return Collections.unmodifiableSet(mRegistered);
    }

    public Set<ObjectName>
    getUnregistered() {
        return Collections.unmodifiableSet(mUnregistered);
    }

    public synchronized Set<ObjectName>
    getCurrentlyRegistered() {
        final Set<ObjectName> all = new HashSet<ObjectName>(mCurrentlyRegistered);

        return all;
    }

}
















