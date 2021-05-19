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

package com.sun.enterprise.admin.monitor.stats;

/**
 *
 * @author  nsegura
 */
import org.glassfish.j2ee.statistics.Stats;

/**
 * Statistical information relevant to a virtual server
 */
public interface PWCVirtualServerStats extends Stats {

    /**
     * Returns the virtual server ID
     * @return virtual server ID
     */
    StringStatistic getId();

    /**
     * Returns the current virtual server mode:
     * unknown/active
     * @return virtual server mode
     */
    StringStatistic getMode();

    /**
     * Returns the hosts related to this virtual server
     * @return hosts
     */
    StringStatistic getHosts();

    /**
     * Returns the interfaces related to this virtual server
     * @return interfaces
     */
    StringStatistic getInterfaces();

}
