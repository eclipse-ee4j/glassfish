/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package admin;
/**
 * This class is here just for doing things ONE time before the rest.
 * Remember that ant  creates a new JVM for every test class!
 *
 * @author wnevins
 */
public class PreRun extends AdminBaseDevTest {

    public PreRun() {
    }

    public static void main(String[] args) {
        // this calls baseclass which calls our subrun() method
        new PreRun().run();
    }

    @Override
    public String getTestName() {
        return "Pre Run";
    }

    @Override
    protected String getTestDescription() {
        return "Pre Run";
    }

    @Override
    public void subrun() {
        TestUtils.resetErrorFile();
        String dir = System.getProperty("user.home");
        System.out.println("The current working directory is " + dir);
        dir = dir.replace('\\', '_').replace('/', '_');
        report("HOME IS__" + dir, true);
        report("reset error file", true);
    }
}
