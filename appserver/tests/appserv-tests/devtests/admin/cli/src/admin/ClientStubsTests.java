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

package admin;
/*
 * Test get-client-stubs command.
 * @author Mukesh Kumawat
 */
public class ClientStubsTests extends AdminBaseDevTest {
    private static final String DOMAIN_NAME = "clientstubtest";

    private void runTests() {
        getClientStubsNegative();
    }

    private void getClientStubsNegative() {
        final String st = "get-client-stub";
        final String appName = "foo";
        final String path = ".";
        AsadminReturn ret = asadminWithOutput("get-client-stubs", "--appname", appName, path);
        if (!ret.returnValue) {
            writeFailure();
        }
    }

    @Override
    protected String getTestDescription() {
        return "Tests Client-Stub Commands";
    }

    public static void main(String[] args) {
        new ClientStubsTests().runTests();
    }
}
