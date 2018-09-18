/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.util.Map;
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

public class ASenvPropertyReaderTest {

    public ASenvPropertyReaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        installDir = new File(
            ASenvPropertyReaderTest.class.getClassLoader().getResource
            ("config/asenv.bat").getPath()).getParentFile().getParentFile();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        pr = new ASenvPropertyReader(installDir);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test() {
        //System.out.println(pr);
    }
    ASenvPropertyReader pr;
    private static File installDir;
}
