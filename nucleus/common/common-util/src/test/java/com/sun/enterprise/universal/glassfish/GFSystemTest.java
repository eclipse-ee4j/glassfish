/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.universal.glassfish;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 *
 * @author bnevins
 */
public class GFSystemTest {

    public static volatile boolean failed = false;

    /**
     * Test of GFSystem for the case where there are multiple instances in a JVM
     */
    @Test
    public void threadTest() {
        try {
            Thread t1 = new ParentThread("xxx");
            Thread t2 = new ParentThread("yyy");
            Thread t3 = new ParentThread("zzz");
            t1.start();
            t2.start();
            t3.start();
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(GFSystemTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertFalse(failed);
    }
    public static synchronized void setFailure() {
        failed = true;
    }
}

class ParentThread extends Thread {
    ParentThread(String name) {
        super(name);
    }
    @Override
    public void run() {
        try {
            GFSystem.setProperty("foo", getName());
            Thread t = new ChildThread(getName(), getName() + "__child");
            t.start();
            String result = GFSystem.getProperty("foo");

            if (result.equals(getName())) {
            } else {
                GFSystemTest.setFailure();
            }
            t.join();
        } catch (InterruptedException ex) {
        }
    }
}


class ChildThread extends Thread {
    String parentName;
    ChildThread(String parentName, String name) {
        super(name);
        this.parentName = parentName;
    }
    @Override
    public void run() {
        try {
            Thread t = new GrandChildThread(parentName, getName() + "__grandchild");
            t.start();
            String result = GFSystem.getProperty("foo");

            if (result.equals(parentName)) {
            } else {
                GFSystemTest.setFailure();
            }
            t.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(ChildThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class GrandChildThread extends Thread {
    String grandParentName;
    GrandChildThread(String grandParentName, String name) {
        super(name);
        this.grandParentName = grandParentName;
    }
    @Override
    public void run() {
        String result = GFSystem.getProperty("foo");

        if(!result.equals(grandParentName)) {
            GFSystemTest.setFailure();
        }
    }
}
