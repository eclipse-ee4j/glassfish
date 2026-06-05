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

package org.glassfish.devtests.web.allowencodedslash;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.appserv.test.BaseDevTest;

public class AllowEncodedSlash extends BaseDevTest {
    public static void main(String[] args) throws IOException {
        new AllowEncodedSlash().run();
    }

    @Override
    protected String getTestName() {
        return "allow-encoded-slash";
    }

    @Override
    protected String getTestDescription() {
        return "allow-encoded-slash";
    }

    public void run() throws IOException {
        String adminPort = antProp("admin.port");
        try {
            setAllowed(false);
            fetch(adminPort, 500, 1);
        } finally {
            setAllowed(true);
            fetch(adminPort, 200, 1);
            stat.printSummary();
        }
    }

    private void setAllowed(final boolean allowed) {
        report("set-encoding-" + allowed, asadmin("set",
            "configs.config.server-config.network-config.protocols.protocol.admin-listener.http.encoded-slash-enabled=" + allowed));
        report("stop-domain-" + allowed, asadmin("stop-domain"));
        report("start-domain-" + allowed, asadmin("start-domain"));
    }

    private void fetch(final String adminPort, final int expectedCode, final int count) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL("http://localhost:" + adminPort + "/management/domain/resources/jdbc-resource/jdbc%2F__TimerPool.xml").openConnection();
            connection.setRequestProperty("X-GlassFish-3", "true");
            System.out.println("Connection response code returned "+connection.getResponseCode());
            report("response-" + expectedCode + "-try-" + count, expectedCode == connection.getResponseCode());
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
}
