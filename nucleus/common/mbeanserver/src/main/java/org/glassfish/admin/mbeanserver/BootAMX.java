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

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.remote.JMXServiceURL;

import org.glassfish.external.amx.AMXGlassfish;
import org.glassfish.external.amx.BootAMXMBean;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.logging.annotation.LogMessageInfo;

/**
The MBean implementation for BootAMXMBean.

Public API is the name of the booter MBean eg {@link BootAMXMBean.OBJECT_NAME}
 */
final class BootAMX implements BootAMXMBean
{
    private final MBeanServer mMBeanServer;
    private final ObjectName mObjectName;
    private final ServiceLocator mHabitat;
    private ObjectName mDomainRootObjectName;

    private BootAMX(
        final ServiceLocator habitat,
        final MBeanServer mbeanServer)
    {
        mHabitat = habitat;
        mMBeanServer = mbeanServer;
        mObjectName = getBootAMXMBeanObjectName();
        mDomainRootObjectName = null;

        if (mMBeanServer.isRegistered(mObjectName))
        {
            throw new IllegalStateException("AMX Booter MBean is already registered: " + mObjectName);
        }
    }

    public static ObjectName getBootAMXMBeanObjectName()
    {
        return AMXGlassfish.DEFAULT.getBootAMXMBeanObjectName();
    }


    /**
    Create an instance of the booter.
     */
    public static synchronized BootAMX create(final ServiceLocator habitat, final MBeanServer server)
    {
        final BootAMX booter = new BootAMX(habitat, server);
        final ObjectName objectName = getBootAMXMBeanObjectName();

        try
        {
            final StandardMBean mbean = new StandardMBean(booter, BootAMXMBean.class);

            if (!server.registerMBean(mbean, objectName).getObjectName().equals(objectName))
            {
                throw new IllegalStateException();
            }
        }
        catch (JMException e)
        {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        return booter;
    }


    AMXStartupServiceMBean getLoader()
    {
        try
        {
            return mHabitat.getService(AMXStartupServiceMBean.class);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }


    /**
    We need to dynamically load the AMX module.  HOW?  we can't depend on the amx-impl module.

    For now though, assume that a well-known MBean is available through other means via
    the amx-impl module.
     */
    public synchronized ObjectName bootAMX()
    {
        if (mDomainRootObjectName == null)
        {
            getLoader();
            final ObjectName startupON = AMXStartupServiceMBean.OBJECT_NAME;
            if (!mMBeanServer.isRegistered(startupON))
            {
                //debug("Booter.bootAMX(): AMX MBean not yet available: " + startupON);
                throw new IllegalStateException("AMX MBean not yet available: " + startupON);
            }

            try
            {
                //debug( "Booter.bootAMX: invoking loadAMXMBeans() on " + startupON);
                mDomainRootObjectName = (ObjectName) mMBeanServer.invoke(startupON, "loadAMXMBeans", null, null);
                //debug( "Booter.bootAMX: domainRoot = " + mDomainRootObjectName);
            }
            catch (final JMException e)
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        return mDomainRootObjectName;
    }


    /**
    Return the JMXServiceURLs for all connectors we've loaded.
     */
    public JMXServiceURL[] getJMXServiceURLs()
    {
        return JMXStartupService.getJMXServiceURLs(mMBeanServer);
    }

    @LogMessageInfo(
            message = "Error while shutting down AMX",
            level = "WARNING")
    static final String errorDuringShutdown = Util.LOG_PREFIX + "-00008";

    public void shutdown()
    {
        try
        {
            mMBeanServer.unregisterMBean(getBootAMXMBeanObjectName());
        }
        catch( final Exception e )
        {
            Util.getLogger().log(java.util.logging.Level.WARNING, errorDuringShutdown, e);
        }
   }
}


















