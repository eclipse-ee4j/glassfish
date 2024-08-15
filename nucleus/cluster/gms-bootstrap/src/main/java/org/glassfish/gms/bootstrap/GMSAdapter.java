/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.gms.bootstrap;

import com.sun.enterprise.ee.cms.core.CallBack;
import com.sun.enterprise.ee.cms.core.GroupManagementService;

import org.jvnet.hk2.annotations.Contract;

/**
 * <P>The register methods below replace GroupManagementService.addFactory methods.
 * The remove methods below replace GroupManagementService.removeFactory methods.
 *
 * <P>TODO: Example of a Leaving Listener handling FailureNotificationSignal, PlannedShutdownSignal
 * and the Rejoin subevent of AddNotificationSignal.
 */
@Contract
public interface GMSAdapter {

    GroupManagementService getModule();

    String getClusterName();

    /**
     * Registers a JoinNotification Listener.
     *
     * @param callback processes GMS notification JoinNotificationSignal
     */
    void registerJoinNotificationListener(CallBack callback);

    /**
     * Registers a JoinAndReadyNotification Listener.
     *
     * @param callback processes GMS notification JoinAndReadyNotificationSignal
     */
    void registerJoinedAndReadyNotificationListener(CallBack callback);

    /**
     * Register a listener for all events that represent a member has left the group.
     *
     * @param callback Signal can be either PlannedShutdownSignal, FailureNotificationSignal or JoinNotificationSignal(subevent Rejoin).
     */
    void registerMemberLeavingListener(CallBack callback);

    /**
     * Registers a PlannedShutdown Listener.
     *
     * @param callback processes GMS notification PlannedShutdownSignal
     */
    void registerPlannedShutdownListener(CallBack callback);

    /**
     * Registers a FailureSuspected Listener.
     *
     * @param callback processes GMS notification FailureSuspectedSignal
     */
    void registerFailureSuspectedListener(CallBack callback);

    /**
     * Registers a FailureNotification Listener.
     *
     * @param callback processes GMS notification FailureNotificationSignal
     */
    void registerFailureNotificationListener(CallBack callback);

    /**
     * Registers a FailureRecovery Listener.
     *
     * @param callback      processes GMS notification FailureRecoverySignal
     * @param componentName The name of the parent application's component that should be notified of selected for
     *                      performing recovery operations. One or more components in the parent application may
     *                      want to be notified of such selection for their respective recovery operations.
     */
    void registerFailureRecoveryListener(String componentName, CallBack callback);

    /**
     * Registers a Message Listener.
     *
     * @param componentName   Name of the component that would like to consume
     *                        Messages. One or more components in the parent application would want to
     *                        be notified when messages arrive addressed to them. This registration
     *                        allows GMS to deliver messages to specific components.
     * @param messageListener processes GMS MessageSignal
     */
    void registerMessageListener(String componentName, CallBack messageListener);

    /**
     * Registers a GroupLeadershipNotification Listener.
     *
     * @param callback processes GMS notification GroupLeadershipNotificationSignal. This event occurs when the GMS masters leaves the Group
     *                 and another member of the group takes over leadership. The signal indicates the new leader.
     */
    void registerGroupLeadershipNotificationListener(CallBack callback);

    /**
     * Remove FailureRecoveryListener for <code>componentName</code>
     * @param componentName name of the component to remove its registered CallBack.
     */
    void removeFailureRecoveryListener(String componentName);

    /**
     * Remove MessageListener for <code>componentName</code>
     * @param componentName name of the component to remove its registered CallBack.
     */
    void removeMessageListener(String componentName);

    /**
     * Remove previously registered FailureNotificationListener.
     * @param callback to be removed
     */
    void removeFailureNotificationListener(CallBack callback);

   /**
     * Remove previously registered FailureSuspectedListener.
     * @param callback to be removed
     */
    void removeFailureSuspectedListener(CallBack callback);

    /**
     * Remove previously registered JoinNotificationListener.
     * @param callback to be removed
     */
    void removeJoinNotificationListener(CallBack callback);

    /**
     * Remove previously registered JoinedAndReadyNotificationListener.
     * @param callback to be removed
     */
    void removeJoinedAndReadyNotificationListener(CallBack callback);

    /**
     * Remove previously registered PlannedShutdownListener.
     * @param callback to be removed
     */
    void removePlannedShutdownListener(CallBack callback);

    /**
     * Remove previously registered GroupLeadershipNotificationListener.
     * @param callback to be removed
     */
    void removeGroupLeadershipNotificationListener(CallBack callback);

    /**
     * Remove previously registered Listeners related to Leaving a Group.
     * Thus, listeners for PlannedShutdown, FailureNotification and Add - Rejoin Subevent are to be removed.
     * @param callback to be removed
     */
    void removeMemberLeavingListener(CallBack callback);

    // only to be called by GMSAdapterService
    boolean initialize(String clusterName);
    void complete();

    /**
     * Returns an object that contains the current health of
     * each instance.
     */
    HealthHistory getHealthHistory();

}
