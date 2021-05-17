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

import org.glassfish.j2ee.statistics.RangeStatistic;

/** An interface that Specifies standard measurements of the lowest and highest values an attribute has held
 * as well as its current value.  Extending RangeStatistic, it also provides the average value.
 * @author Larry White
 * @author Kedar Mhaswade
 * @since S1AS8.1
 * @version 1.0
 */

/**
 *
 * @author  lwhite
 */
public interface AverageRangeStatistic extends RangeStatistic {

    public long getAverage();

}
