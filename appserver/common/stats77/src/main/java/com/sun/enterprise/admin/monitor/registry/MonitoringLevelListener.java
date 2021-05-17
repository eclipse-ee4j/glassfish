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

package com.sun.enterprise.admin.monitor.registry;

/**
 * Implementation of this interface enables notifications to
 * be received for change in monitoring level from a prior
 * level to a new one. Monitoring levels are defined by the
 * constants class MonitoringLevel and are currently defined as
 * OFF, LOW and HIGH.
 * @author  <href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy
 */
public interface MonitoringLevelListener {
    /**
     * Sets the monitoring level to a new level. Values are defined by
     * MonitoringLevel.OFF, MonitoringLevel.LOW, and MonitoringLevel.High
     * @param level corresponding to MonitoringLevel OFF, LOW or HIGH
     * @deprecated
     */
    public void setLevel(MonitoringLevel level);

    /**
     * Method to convey the change in monitoring level. It is a usual practice that
     * various components may have <em> single instance of listener </em> to listen
     * to the changes in monitoring-level for various registered Stats objects. This
     * method gives a context for such components to be returned when it is
     * called.
     * @deprecated
     * @param from        the MonitoringLevel before the change
     * @param to        the MonitoringLevel after the change
     * @param handback    the Stats object that was passed to the registry during registration. It
     * is guaranteed that it will be unchanged by monitoring framework.
     */
    public void changeLevel(MonitoringLevel from, MonitoringLevel to,
        org.glassfish.j2ee.statistics.Stats handback);

    /**
     * Method to convey the change in monitoring level. The configuration
     * of monitoring pertains to certain components like ejb-container, web-container,
     * orb, transaction-service, connection-pools, thread-pools etc. The third
     * parameter loosely corresponds to the configuration in domain.xml as follows:
     * <ul>
     * <li> connector-connection-pool : MonitoredObjectType#CONNECTOR_CONN_POOL </li>
     * <li> ejb-container : MonitoredObjectType#EJB </li>
     * <li> http-service, http-listeners etc. : MonitoredObjectType#HTTP_SERVICE </li>
     * <li> jdbc-connection-pool : MonitoredObjectType#JDBC_CONN_POOL </li>
     * <li> orb : MonitoredObjectType#ORB </li>
     * <li> thread-pool : MonitoredObjectType#THREAD_POOL </li>
     * <li> transaction-service : MonitoredObjectType#TRANSACTION_SERVICE </li>
     * <li> web-container : MonitoredObjectType#WEB_COMPONENT </li>
     * <li> </li>
     * </ul>
     * The core components are expected to follow the above.
     * When the level changes through administrative interfaces, the notification
     * is sent to the registered listeners for corresponding types and thus the
     * dynamic reconfiguration is done.
     * @param from        the MonitoringLevel before the change
     * @param to        the MonitoringLevel after the change
     * @param type        the MonitoredObjectType that had the level changed
     */
    public void changeLevel(MonitoringLevel from, MonitoringLevel to,
        MonitoredObjectType type);
}
