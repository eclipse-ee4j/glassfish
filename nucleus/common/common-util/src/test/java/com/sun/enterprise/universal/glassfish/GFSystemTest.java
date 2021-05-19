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

package com.sun.enterprise.universal.glassfish;

import com.sun.enterprise.universal.collections.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */
public class GFSystemTest {

    public GFSystemTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

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
    public static volatile boolean failed = false;
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
    String parentName;
}

class GrandChildThread extends Thread {
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
    String grandParentName;
}


/*
/*
    public static void main(String[] args) {
        Thread t = new TestThread("thread1");
        Thread t2 = new TestThread("thread2");
        t.start();
        t2.start();
    }

}



 */
