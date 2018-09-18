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

/** An interface that imparts mutability to a {@link CountStatistic} by the
 * virtue of extending MutableStatistic.
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */

public interface MutableCountStatistic extends MutableStatistic {
    
    /** Provides the mutator to the only statistic in the implementing class that
     * changes - Count. It is expected that the count is monotonically increasing
     * on a temporal scale.
     * @param current       long that specifies the value when measured (sampled).
     */
    public void setCount(long current);
}
