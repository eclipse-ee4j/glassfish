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

package ear.earwithall2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TestDeplOrder {

    public static void main (String[] args) {
        TestDeplOrder client = new TestDeplOrder();
        client.doTest(args);
    }

    public void doTest(String[] args) {

        String path = args[0];
        try {
            log("Test: devtests/deployment/ear/earwithall2");
            log("looking at " + path);
            boolean success = readFile(path, "Loading application WebNBean_ejb", "Loading application WebNBean_war");
            if (success) {
              pass();
            } else {
              fail();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private boolean readFile(String path, String first, String second)
            throws IOException, FileNotFoundException {
        int firstLine = -1;
        int secondLine = -1;
        BufferedReader reader =
          new BufferedReader(new FileReader(new File(path)));
        String line = reader.readLine();
        int totalLines = 0;
        while (line != null) {
            ++totalLines;
            if ((firstLine < 0) && (line.contains(first))) {
                firstLine = totalLines;
            }
            if ((secondLine < 0) && (line.contains(second))) {
                secondLine = totalLines;
            }
            line = reader.readLine();
        }
        reader.close();
        log("first line:  " + firstLine);
        log("second line:  " + secondLine);
        if ((firstLine < 0) ||
            (secondLine < 0))
          return false;
        if (firstLine < secondLine) {
          return true;
        }
        return false;
    }
    private void log(String message) {
        System.err.println("[ear.earwithall2.TestDeplOrder]:: " + message);
    }

    private void pass() {
        log("PASSED: devtests/deployment/ear/earwithall2");
        System.exit(0);
    }

    private void fail() {
        log("FAILED: devtests/deployment/ear/earwithall2");
        System.exit(-1);
    }
}
