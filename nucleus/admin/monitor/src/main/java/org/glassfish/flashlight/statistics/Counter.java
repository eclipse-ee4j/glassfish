/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics;

import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.TreeElement;
import org.jvnet.hk2.annotations.Contract;

/**
 * TBD Implement Java EE Statistics
 *
 * @author Harpreet Singh
 */
@Contract
public interface Counter extends TreeNode, CountStatistic {

    public void decrement();

    public long getCount();

    public void increment();

    public void setCount(long count);

    public void setReset(boolean reset);

    public void increment(long delta);
}
