/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;

/**
 *
 * @author Byron Nevins
 */
public class Constants {
    public static final String pFile = System.getenv("APS_HOME") +
             File.separator + "config" + File.separator + "adminpassword.txt";
    public static final String tmpFile = System.getenv("APS_HOME") +
            File.separator + "config" + File.separator + "adminpassword.tmp";
    // if you set this the tests will bail after one failure.  sysprop OR env var
    public static final String FAIL_FAST = "AS_TESTS_FAIL_FAST";
    static File ERRORS_WERE_REPORTED_FILE = new File("errors.txt");
}
