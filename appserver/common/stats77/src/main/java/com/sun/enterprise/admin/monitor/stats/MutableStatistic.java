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
import java.io.Serializable;

import org.glassfish.j2ee.statistics.Statistic;

/**
 * An interface that gives a flexibility to set various values for a particular Statistic.
 * This provision is basically to have the same data structure to collect and
 * return the (read-only copy of) statistical data. This extends the
 * java.io.Serializable, so that its subclasses can be serialized to facilitate
 * working with other management clients.
 * Refer to the package description
 * to understand the intent of this interface.
 * <P>
 * Methods of this class should be called judiciously by the component that is
 * gathering the statistics.
 *
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */
public interface MutableStatistic extends Serializable {

    /**
     * Returns a read-only view of this Statistic. An implementing class has
     * to return the instances of Statistic interfaces defined in
     * {@link javax.management.j2ee.statistic}
     * and {@link com.sun.enterprise.admin.monitor.stats} packages.
     *
     * @return an instance of a specific Statistic interface
     */
    Statistic unmodifiableView();

    /**
     * Returns an instance of Statistic whose state can be changed by the caller. It is important
     * to know that caller should not cache the return value from this method. In general, there
     * is a problem in this contract in that, a client would not know from a Stats.getCreateCount()
     * method whether the return value is something that is being modified or is invariant. Hence
     * the caller should not cache the returned value in a collection and then collectively
     * process it. The main idea behind providing this method is to control the
     * number of instances created (per Mahesh's Suggestion).
     * @return      an instance of Statistic interface that should not be cached.
     */
    Statistic modifiableView();

    /**
     * Resets the encapsulated Statistic interface to its initial value. The idea being, if (after
     * creation of the instance) the state changes, the initial state can be easily regained by
     * calling this method. Note that the time of last sampling changes to the instant
     * when this method is invoked.
     */
    void reset();
}
