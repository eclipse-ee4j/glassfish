/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal.process;

import com.sun.enterprise.util.OS;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author bnevins
 */
public class ProcessManagerTest {

    private static String textfile;

    @BeforeAll
    public static void setUpClass() throws Exception {
        textfile = new File(ProcessManagerTest.class.getClassLoader().getResource("process/lots_o_text.txt").getPath()).getAbsolutePath();
        assertTrue(textfile != null && textfile.length() > 0);
    }

    /**
     * Temporary Test of ProcessManager
     * This stuff is platform dependent.
     */
    @Test
    public void test1() throws ProcessManagerException {
        ProcessManager pm;

        System.out.println("If it is FROZEN RIGHT NOW -- then Houston, we have a problem!");
        System.out.println("ProcessManager must have the write to stdin before the reader threads have started!");

        if (OS.isWindows()) {
            pm = new ProcessManager("cmd", "/c", "type", textfile);
        } else {
            pm = new ProcessManager("cat", textfile);
        }

        pm.setStdinLines(hugeInput());
        pm.setEcho(false);
        pm.execute();
    }

    private List<String> hugeInput() {
        List<String> l = new ArrayList<>();

        for (int i = 0; i < 50000; i++) {
            l.add("line number " + i + "here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }

        return l;
    }
}
