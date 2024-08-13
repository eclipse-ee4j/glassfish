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

package org.glassfish.flashlight.statistics.factory;

import org.glassfish.flashlight.statistics.Counter;
import org.glassfish.flashlight.statistics.impl.CounterImpl;

/**
 *
 * @author Harpreet Singh
 */
public class CounterFactory {

    public static Counter createCount(long... seed) {
        Counter count;
        if (seed.length == 0) {
            count = new CounterImpl();
        } else {
            count = new CounterImpl();
            count.setCount(seed[0]);
        }
        count.setEnabled(true);
        return count;
    }
}
