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

import java.io.Serializable;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.ObjectName;

import org.glassfish.admin.amx.j2ee.J2EELogicalServer;

/**
Base interface only (for cluster and standalone server)
 */
public class J2EELogicalServerImplBase
        extends J2EEManagedObjectImplBase {

    public J2EELogicalServerImplBase(
            final ObjectName parentObjectName,
            final Metadata meta,
            final Class<? extends J2EELogicalServer> intf) {
        super(parentObjectName, meta, intf);
    }

    public int getstate() {
        throw new RuntimeException(new AttributeNotFoundException("state"));
    }

    public void start() {
        throw new RuntimeException("can't start");
    }

    public void startRecursive() {
        throw new RuntimeException("can't startRecursive");
    }

    public void stop() {
        throw new RuntimeException("can't stop");
    }

    /**
     * starts the app
     */
    public void startApp(String appID, Map<String, Serializable> optional) {
        /*
        final OldApplicationsConfigMBean oldApplicationsMBean =
        getOldConfigProxies().getOldApplicationsConfigMBean();

        final Map<String,Serializable> m = TypeCast.asMap(
        oldApplicationsMBean.startAndReturnStatusAsMap( appID, getSelfName(), optional ) );
        checkDeploymentStatusForExceptions( m );
         */
    }

    /**
     * stops the app
     */
    public void stopApp(String appID, Map<String, Serializable> optional) {
        /*
        final OldApplicationsConfigMBean oldApplicationsMBean =
        getOldConfigProxies().getOldApplicationsConfigMBean();

        final Map<String,Serializable>    m = TypeCast.asMap(
        oldApplicationsMBean.stopAndReturnStatusAsMap( appID, getSelfName(), optional ) );

        checkDeploymentStatusForExceptions( m );
         */
    }
    /**
     * Checks the DeploymentStatus and all substages.
     *
     * Can't depend on SUCCESS or FAILURE as the backend.DeploymentStatus sets
     * the stageStatus to its own codes. Cannot import backend.DeploymentStatus
     * to translate the codes.
    private void
    checkDeploymentStatusForExceptions( Map<String,Serializable > m )
    {
    DeploymentStatus status = DeploymentSupport.mapToDeploymentStatus( m );

    Throwable t = status.getStageThrowable();

    final Iterator<DeploymentStatus> it = status.getSubStagesList().iterator();
    while ( ( t == null ) && ( it.hasNext() ) )
    {
    final DeploymentStatus m1 = it.next();
    t = status.getThrowable();
    }
    if ( null != t )
    {
    throw new RuntimeException( status.getStageStatusMessage() );
    }
    }
     */
}

