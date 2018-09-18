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

/*
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/client/AppserverConnectionSourceTest.java,v 1.5 2007/05/05 05:23:53 tcfujii Exp $
* $Revision: 1.5 $
* $Date: 2007/05/05 05:23:53 $
*/
package org.glassfish.admin.amxtest.client;

import com.sun.appserv.management.client.AppserverConnectionSource;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.Capabilities;

import java.io.IOException;

/**
 Tests AppserverConnectionSource.
 <p/>
 Note that no actual connect test can be done through normal junit tests since there
 is no host/port available and no guarantee of a running server.  All other aspects
 can be tested.
 */
public final class AppserverConnectionSourceTest
        extends AMXTestBase {
    public AppserverConnectionSourceTest() {
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(false);
    }

    private static void
    testConnect(
            final String host,
            final int port,
            final String protocol,
            final String user,
            final String password)
            throws IOException {
        final AppserverConnectionSource source =
                new AppserverConnectionSource(protocol, host, port, user, password, null);

        source.getMBeanServerConnection(true);

    }

    public void
    testConnect()
            throws Exception {
        final String host = (String) getEnvValue("HOST");
        final String port = (String) getEnvValue("PORT");
        final String protocol = (String) getEnvValue("PROTOCOL");
        final String user = (String) getEnvValue("USER");
        final String password = (String) getEnvValue("PASSWORD");

        if (host == null || port == null || protocol == null ||
                user == null || password == null ||
                !AppserverConnectionSource.isSupportedProtocol(protocol)) {
            trace("AppserverConnectionSourceTest: skipped connect test; missing config:" +
                    "host = " + host +
                    ", port = " + port +
                    ", protocol = " + protocol +
                    ", user = " + user +
                    ", password = " + password);
        } else {
            testConnect(host, new Integer(port).intValue(), protocol, user, password);
        }
    }

    private AppserverConnectionSource
    create(final String protocol) {
        return (new AppserverConnectionSource(protocol, "localhost", 9999, "admin", "admin123", null));
    }

    public void
    testCreateS1ASHTTP() {
        create(AppserverConnectionSource.PROTOCOL_HTTP);
    }

    public void
    testCreateRMI() {
        create(AppserverConnectionSource.PROTOCOL_RMI);
    }

    public void
    testCreateIllegal() {
        try {
            create("jmxmp");
        }
        catch (IllegalArgumentException e) {
            // good
        }
    }

    public void
    testToString() {
        create(AppserverConnectionSource.PROTOCOL_RMI).toString();
        create(AppserverConnectionSource.PROTOCOL_HTTP).toString();
    }
}






