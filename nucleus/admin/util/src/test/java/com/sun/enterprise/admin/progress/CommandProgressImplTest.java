/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.progress;

import org.glassfish.api.admin.ProgressStatus;
import org.glassfish.api.admin.progress.ProgressStatusImpl;
import org.glassfish.api.admin.progress.ProgressStatusMirroringImpl;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author mmares
 */
public class CommandProgressImplTest {
    
    public CommandProgressImplTest() {
    }

//    @BeforeClass
//    public static void setUpClass() throws Exception {
//    }
//
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//    }


    @Test
    public void testCreateMirroringChild() {
        CommandProgressImpl cp = new CommandProgressImpl("first", "a");
        cp.setTotalStepCount(2);
        ProgressStatusMirroringImpl ch1 = cp.createMirroringChild(1);
        assertNotNull(ch1);
        ProgressStatus ch2 = cp.createChild(1);
        assertNotNull(ch1);
        assertTrue(ch2 instanceof ProgressStatusImpl);
    }
    
}
