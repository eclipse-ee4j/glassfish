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

package org.glassfish.devtests.web.asadmindeletestarget;

import java.io.FileNotFoundException;

import com.sun.appserv.test.BaseDevTest;

/*
 * Unit test for asadmin deletes.
 */
public class WebTest extends BaseDevTest {
    private final String name = System.currentTimeMillis() + "";
    private static final boolean DEBUG = false;

    public static void main(String[] args) throws FileNotFoundException {
        new WebTest().run(args[0]);
    }

    @Override
    protected String getTestName() {
        return "asadmin-deletes-target";
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for deleting referenced domain.xml entities";
    }

    public void run(String clusterName) {
        final String port = "" + (Integer.valueOf(antProp("http.port")) + 20);
        report("create-threadpool", asadmin("create-threadpool", "--target", clusterName, name));
        report("create-transport", asadmin("create-transport", "--target", clusterName, name));
        report("create-protocol", asadmin("create-protocol", "--target", clusterName, name));
        report("create-http", asadmin("create-http", "--target", clusterName, "--default-virtual-server", "server", name));
        report("create-network-listener", asadmin("create-network-listener",
            "--target", clusterName,
            "--listenerport", port,
            "--protocol", name,
            "--threadpool", name,
            "--transport", name,
            name));
        report("delete-referenced-threadpool", !asadmin("delete-threadpool", "--target", clusterName, name));
        report("delete-referenced-transport", !asadmin("delete-transport", "--target", clusterName, name));
        report("delete-referenced-protocol", !asadmin("delete-protocol", "--target", clusterName, name));
        report("delete-network-listener", asadmin("delete-network-listener", "--target", clusterName, name));
        report("delete-unreferenced-protocol", asadmin("delete-protocol", "--target", clusterName, name));
        report("delete-unreferenced-threadpool", asadmin("delete-threadpool", "--target", clusterName, name));
        report("delete-unreferenced-transport", asadmin("delete-transport", "--target", clusterName, name));
        stat.printSummary();
    }
}
