/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package net.max.ee.sfsb;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Remove;
import jakarta.ejb.Stateful;

/**
 *
 * @author mk
 */
@Stateful
@LocalBean
public class SimpleSessionBean
    implements Serializable {

    private static AtomicInteger ai = new AtomicInteger();

    private String id;

    private long counter = 0;

    public SimpleSessionBean() {
        id = "id-" + ai.incrementAndGet();
    }
    public String getId() {
        return id;
    }

    public long getCounter() {
        return counter;
    }

    public long  incrementCounter() {
        return counter++;
    }

    public String asString() {
        return id + ": accessCount: " + counter;
    }

    @Remove
    public void cleanup() {
    }
}
