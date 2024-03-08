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

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.impl.util.Issues;
import org.glassfish.admin.amx.impl.util.ObjectNameBuilder;
import org.glassfish.admin.amx.j2ee.*;
import static org.glassfish.admin.amx.j2ee.StateManageable.*;

/**
JSR 77 extension representing an Appserver standalone server (non-clustered)

Server MBean which will reside on DAS
for enabling state management including start() and stop()
 */
public class DASJ2EEServerImpl extends J2EEServerImpl
        implements NotificationListener {

    public DASJ2EEServerImpl(final ObjectName parentObjectName, final Metadata meta) {
        super(parentObjectName, meta);

        Issues.getAMXIssues().notDone("DASJ2EEServer needs to account for DAS/non-DAS");
    }


    @Override
        protected void
    registerChildren()
    {
        super.registerChildren();

        final ObjectNameBuilder builder = getObjectNames();

        final JVMImpl jvm = new JVMImpl( getObjectName(), defaultChildMetadata() );
        final ObjectName jvmObjectName = builder.buildChildObjectName( J2EETypes.JVM, null);
        registerChild( jvm, jvmObjectName );
    }

    /*
    static private final Class[]    DOMAIN_STATUS_INTERFACES    =
    new Class[] { DomainStatusMBean.class };

    protected DomainStatusMBean
    getDomainStatus()
    {
    DomainStatusMBean    domainStatus    = null;
    try {
    final MBeanServer    mbeanServer = getMBeanServer();
    final Set<ObjectName>    candidates    = QueryMgrImpl.queryPatternObjectNameSet(
    mbeanServer, JMXUtil.newObjectNamePattern(
    "*", DomainStatusMBean.DOMAIN_STATUS_PROPS ) );
    final ObjectName on = SetUtil.getSingleton( candidates );
    domainStatus = (DomainStatusMBean)MBeanServerInvocationHandler.
    newProxyInstance( mbeanServer, on, DomainStatusMBean.class, false );
    } catch (Exception e) {
    final Throwable rootCause = ExceptionUtil.getRootCause( e );
    getMBeanLogger().warning( rootCause.toString() + "\n" +
    ExceptionUtil.getStackTrace( rootCause ) );
    }
    return( domainStatus );
    }

     */

    private boolean remoteServerIsStartable() {
        final int cState = getstate();

        return (STATE_STOPPED == cState) ||
                (STATE_FAILED == cState);
    }

    private boolean remoteServerIsStoppable() {
        int cState = getstate();

        if ((STATE_STARTING == cState) ||
                (STATE_RUNNING == cState) ||
                (STATE_FAILED == cState)) {
            return true;
        } else {
            return false;
        }
    }

    public void handleNotification(final Notification notif, final Object ignore) {
    }

    protected String getServerName() {
        return Util.getNameProp(getObjectName());
    }

    public boolean isstateManageable() {
        return false;
    }

    /*
    final RuntimeStatus
    getRuntimeStatus(final String serverName )
    {
    final MBeanServer mbeanServer = getMBeanServer();

    final OldServersMBean oldServers =
    OldConfigProxies.getInstance( mbeanServer ).getOldServersMBean( );

    final RuntimeStatus status = oldServers.getRuntimeStatus( serverName );

    return status;
    }
     */
    /**
    Convert an internal status code to JSR 77 StateManageable state.
     *
    private static int
    serverStatusCodeToStateManageableState( final int statusCode )
    {
    int state = STATE_FAILED;
    switch( statusCode )
    {
    default: throw new IllegalArgumentException( "Uknown status code: " + statusCode );

    case Status.kInstanceStartingCode: state = STATE_STARTING; break;
    case Status.kInstanceRunningCode: state = STATE_RUNNING; break;
    case Status.kInstanceStoppingCode: state = STATE_STOPPING; break;
    case Status.kInstanceNotRunningCode: state = STATE_STOPPED; break;
    }

    return state;
    }
     */
    public int getstate() {
        int state = STATE_STOPPED;
        try {
            Issues.getAMXIssues().notDone("DASJ2EEServerImpl.getRuntimeStatus: getRuntimeStatus");
            //final int internalStatus = getRuntimeStatus(getServerName()).getStatus().getStatusCode();
            //state = serverStatusCodeToStateManageableState( internalStatus );
            state = STATE_RUNNING;
        } catch (final Exception e) {
            // not available, must not be running
        }

        return state;
    }

    public void start() {
        if (remoteServerIsStartable()) {
            startRemoteServer();
        } else {
            throw new RuntimeException("server is not in a startable state");
        }
    }

    public void startRecursive() {
        start();
    }
    /** The DAS is always named "server", or so inquiries suggest */
    static final String DAS_SERVER_NAME = "server";

    /**
    Does this particular J2EEServer represent the DAS?
     */
    private boolean isDASJ2EEServer() {
        return DAS_SERVER_NAME.equals(getName());
    }

    public void stop() {
        if (isDASJ2EEServer()) {
            //getDelegate().invoke( "stop", (Object[])null, (String[])null);
        } else if (remoteServerIsStoppable()) {
            //stopRemoteServer();
        } else {
            throw new RuntimeException("server is not in a stoppable state");
        }
    }

    private void startRemoteServer() {
        Issues.getAMXIssues().notDone("DASJ2EEServerImpl.startRemoteServer");
    }

}





