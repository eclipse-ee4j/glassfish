/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amxtest;

import com.sun.appserv.management.util.misc.StringUtil;
import org.glassfish.admin.amxtest.config.DanglingRefsTest;

import java.io.File;
import java.io.PrintStream;


/**
 This test should normally be run before the generic tests
 so that it can set up default items for many of the config elements
 so that the generic tests will actually test them. Otherwise,
 when the generic tests are run, they won't see any instances
 of many of the AMXConfig MBeans.
 <p/>
 If there are errors doing this, disable this test in amxtest.classes,
 fix the error in the specific place it's occurring, then re-enabled
 this test.
 */
public final class RunMeLastTest
        extends AMXTestBase {
    public RunMeLastTest() {
    }

    private void
    emitCoverage()
            throws java.io.IOException {
        final CoverageInfoAnalyzer analyzer =
                new CoverageInfoAnalyzer(getDomainRoot());

        final String summary = analyzer.getCoverageSummary();

        final File dataFile = new File("amx-tests.coverage");
        final PrintStream out = new PrintStream(dataFile);
        out.println(summary);
        out.close();

        if (getVerbose()) {
            trace("NOTE: code coverage data save in file " +
                    StringUtil.quote("" + dataFile));
        }
    }

    public void
    testLast()
            throws Exception {
        emitDanglingRefs();

        if (getTestUtil().asAMXDebugStuff(getDomainRoot()) != null) {
            emitCoverage();
        }
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(true);
    }


    public void
    emitDanglingRefs()
            throws ClassNotFoundException {
        new DanglingRefsTest().testAllDangling();
    }
}

















