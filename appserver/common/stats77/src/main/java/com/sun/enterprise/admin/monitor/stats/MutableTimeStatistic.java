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
 * Provides mutable nature to the {@link TimeStatistic}. Like other MutableStatistic
 * interfaces, it does have the state to calculate the specific statistical data
 * in {@link TimeStatistic}. Unlike other mutable statistics though, in this interface
 * there is some consideration of a sequence. Once an instance of this interface
 * is created, subsequent call to {@link #incrementCount} has a twofold effect:
 * <ul>
 * <li>Increments the count for number of times the operation is executed by 1</li>
 * <li>Keeps a count of maximum/minimum/total execution time</li>
 * <ul>
 *
 * @author <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since S1AS8.0
 * @version $Revision: 1.2 $
 */
public interface MutableTimeStatistic extends MutableStatistic {

    /**
     * Increments the count for number of times an operation is called by 1 and
     * processes the given parameter in a certain manner.
     */
    void incrementCount(long currentExecutionTimeMillis);
}
