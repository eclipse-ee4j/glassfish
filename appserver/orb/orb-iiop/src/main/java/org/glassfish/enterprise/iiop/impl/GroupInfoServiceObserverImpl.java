/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.enterprise.iiop.impl;

import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.corba.ee.spi.folb.GroupInfoServiceObserver;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.glassfish.enterprise.iiop.impl.IIOPImplLogFacade.GROUPINFOSERVICE_MEMBERSHIP_NOTIFICATION_PROBLEM;

/**
 * Called when the GroupInfoService that you register with
 * has a change.  You should call the GroupInfoService
 * <code>getClusterInstanceInfo</code> method to get
 * updated info.
 * @author Ken Cavanaugh
 * @author Sheetal Vartak
 */
public class GroupInfoServiceObserverImpl implements GroupInfoServiceObserver {
    private static final Logger LOG = IIOPImplLogFacade.getLogger(GroupInfoServiceObserverImpl.class);

    private final GroupInfoService gis;
    private final RoundRobinPolicy rr;

    public GroupInfoServiceObserverImpl(GroupInfoService gis, RoundRobinPolicy rr) {
        this.gis = gis;
        this.rr = rr;
    }

    // This method is called for internally forced updates:
    // see SerialInitContextFactory.getInitialContext.
    public void forceMembershipChange() {
        doMembershipChange();
        LOG.log(Level.FINE, "GroupInfoServiceObserverImpl.forceMembershipChange called");
    }

    @Override
    // This method is called when the client is informed about a cluster
    // membership change through ClientGroupManager.receive_star.
    public void membershipChange() {
        doMembershipChange();
        LOG.log(Level.FINE, "GroupInfoServiceObserverImpl.membershipChange called");
    }

    private void doMembershipChange() {
        try {
            List<ClusterInstanceInfo> instanceInfoList = gis.getClusterInstanceInfo(null, rr.getHostPortList());
            if (instanceInfoList != null && !instanceInfoList.isEmpty()) {
                rr.setClusterInstanceInfo(instanceInfoList);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, GROUPINFOSERVICE_MEMBERSHIP_NOTIFICATION_PROBLEM, e);
        }
    }
}
