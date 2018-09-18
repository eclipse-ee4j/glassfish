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

package com.sun.enterprise.universal;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */
public class DurationTest {

    public DurationTest() {
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
    @Test
    public void test1() {
        long msec = Duration.MSEC_PER_WEEK * 3 +
                    Duration.MSEC_PER_DAY * 6 +
                    Duration.MSEC_PER_HOUR * 23 +
                    Duration.MSEC_PER_MINUTE * 59 +
                    Duration.MSEC_PER_SECOND * 59;
        
        Duration d = new Duration(msec);
        assertTrue(d.numWeeks == 3);
        assertTrue(d.numDays == 6);
        assertTrue(d.numHours == 23);
        assertTrue(d.numMinutes == 59);
        assertTrue(d.numSeconds == 59);
        
    }
    @Test
    public void test2() {
        long msec = Duration.MSEC_PER_WEEK * 7 +
                    Duration.MSEC_PER_DAY * 6 +
                    Duration.MSEC_PER_HOUR * 23 +
                    Duration.MSEC_PER_MINUTE * 59 +
                    Duration.MSEC_PER_SECOND * 59 +
                    999;
                    
        
        Duration d = new Duration(msec);
        assertTrue(d.numWeeks == 7);
        assertTrue(d.numDays == 6);
        assertTrue(d.numHours == 23);
        assertTrue(d.numMinutes == 59);
        assertTrue(d.numSeconds == 59);
        assertTrue(d.numMilliSeconds == 999);
    }
    @Test
    public void test3() {
        long msec = System.currentTimeMillis();
        Duration d = new Duration(msec);
        assertTrue(d.numWeeks > 38 * 52);
    }
    @Test
    public void test4() {
        Duration d = new Duration(27188);
        assertTrue(d.numSeconds == 27);
        assertTrue(d.numMilliSeconds == 188);
    }
    @Test
    public void test5() {
        Duration d = new Duration(2);
        assertTrue(d.numSeconds == 0);
        assertTrue(d.numMilliSeconds == 2);
    }
}
    
