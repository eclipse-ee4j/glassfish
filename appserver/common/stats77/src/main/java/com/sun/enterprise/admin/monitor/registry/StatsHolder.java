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

import java.util.Collection;

import javax.management.ObjectName;

import org.glassfish.j2ee.statistics.Stats;


/**
 * Provides the ability to associate various j2ee components
 * and sub components in a hierarchical tree. Holds references to
 * underlying Stats objects. On instantiation, the Stats object is
 * converted to a DynamicMBean instance. When monitoring level is
 * changed from OFF to LOW or HIGH, the MBean is registered with an
 * MBeanServer. Calls made to the MBean are delegated to this object
 * which in turn delegates it to underlying Stats object.
 * @author  Shreedhar Ganapathy <mailto:shreedhar.ganapathy@sun.com>
 */
public interface StatsHolder {
    /**
     * Add a child node or leaf to this node.
     * @param statsHolder
     */
    StatsHolder addChild(String name, MonitoredObjectType type);

    /**
     * return an array of StatHolder objects each representing a child
     * of this node.
     * @return Collection
     */
    Collection getAllChildren();

    /**
     * removes all children belonging to this node.
     */
    void removeAllChildren();

    /**
     * Returns name of this hierarchical node
     */
    String getName();

    /**
     * Returns type of this hierarchical node
     */
    MonitoredObjectType getType();

    /**
     * sets this hierarchical node's associated stats object. Used when node was
     * originally created without a Stats implementation or a new monitoring
     * level has been set requiring a new Stats registration
     */
    void setStats(Stats stats);

    Stats getStats();

    void setStatsClass(Class c);

    Class getStatsClass();

    void setStatsClassName(String cName);

    String getStatsClassName();

    /**
     * Sets the ObjectName pertaining to the MBean for this node.
     */
    void setObjectName(ObjectName name);

    /**
     * Gets the ObjectName pertaining to the MBean for this node.
     */
    ObjectName getObjectName();

    /**
     * Sets the hierarchically denoted dotted name for this node.
     */
    void setDottedName(String dottedName);

    /**
     * Gets the hierarchically denoted dotted name for this node.
     */
    String getDottedName();

    /**
     * Registers a monitoring MBean with the MBeanServer
     */
    void registerMBean();

    /**
     * Unregisters a monitoring MBean from the MBean Server
     */
    void unregisterMBean();

    void setType(MonitoredObjectType type);

    StatsHolder getChild(String name);

    void removeChild(String name);
}
