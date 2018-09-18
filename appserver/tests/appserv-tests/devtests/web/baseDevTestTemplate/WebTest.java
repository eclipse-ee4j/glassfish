/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.devtests.web.@testpackage@;

import com.sun.appserv.test.BaseDevTest;

public class WebTest extends BaseDevTest {
    public static void main(String[] args) {
        new WebTest().run();
    }

    @Override
    protected String getTestName() {
        return "@dashed-name@";
    }

    @Override
    protected String getTestDescription() {
        return "@dashed-name@";
    }

    public void run() {
        try {
            // test content
        } finally {
            stat.printSummary();
        }
    }
}
