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
import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.Statistic;

/**
 * Custom statistic type created for the Sun ONE Application Server.
 * The goal is to be able to represent changing attribute values that are strings
 * in the form of Statistics. Semantically, it is analogous to a {@link CountStatistic},
 * the only difference being in the value that is returned. Unlike a CountStatistic
 * (which always is unidirectional), this Statistic type is not having any
 * specified direction, simply because there is no natural order. An example
 * of the values that an instance of this statistic type can assume is: A State
 * Statistic which can have "CONNECTED, CLOSED, DISCONNECTED" as the permissible
 * values and the current value can be any one of them (and them only).
 * The permissible values
 * are upto a particular implementation.
 *
 * @author Muralidhar Vempaty
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */
public interface StringStatistic extends Statistic {

    /**
     * Returns the String value of the statistic.
     */
    String getCurrent();
}
