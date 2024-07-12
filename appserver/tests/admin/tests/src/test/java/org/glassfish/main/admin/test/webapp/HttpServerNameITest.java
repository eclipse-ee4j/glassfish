/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.glassfish.main.admin.test.webapp;

import static org.glassfish.main.admin.test.ConnectionUtils.getContent;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;
import org.glassfish.main.admin.test.TestResources;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class HttpServerNameITest {

    private static final String TEST_APP_NAME = HttpServerNameITest.class.getSimpleName();
    private static final int HTTP_PORT = 8080;
    private static final String SERVER_NAME_PROPERTY = "configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.server-name";
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    private static final AtomicBoolean APP_DEPLOYED = new AtomicBoolean();

    @ParameterizedTest(name = "[{index}] testServerName: {0}")
    @CsvSource({
        "'Plain host name', hostname1, http://hostname1:8080/",
        "'Plain domain name', my.domain.com, http://my.domain.com:8080/",
        "'Domain name and port', my.domain.com:123, http://my.domain.com:123/",
        "'Domain name and standard port', my.domain.com:80, http://my.domain.com/",
        "'Secure domain name', https://my.domain.com, https://my.domain.com:8080/",
        "'Secure domain name and port', https://my.domain.com:123, https://my.domain.com:123/",
        "'Secure domain name and standard port', https://my.domain.com:443, https://my.domain.com/"
    })
    public void testHostName(String description, String serverName, String expectedUrlPrefix) throws IOException, InterruptedException {
        assertThat(ASADMIN.exec("set", SERVER_NAME_PROPERTY + "=" + serverName), asadminOK());

        final HttpURLConnection conn = GlassFishTestEnvironment.openConnection(HTTP_PORT, "/" + TEST_APP_NAME);
        conn.setInstanceFollowRedirects(false);

        assertThat(conn.getHeaderField("Location"), stringContainsInOrder(expectedUrlPrefix));
        assertThat(getContent(conn), stringContainsInOrder("This document has moved <a href=\"" + expectedUrlPrefix));
    }

    @BeforeAll
    static void deploy() {
        String warFile = TestResources.createSimpleWarDeployment(TEST_APP_NAME).getAbsolutePath();
        assertThat(ASADMIN.exec("deploy", "--name", TEST_APP_NAME, "--contextroot", TEST_APP_NAME,
                warFile), asadminOK());
        APP_DEPLOYED.set(true);
    }

    @AfterAll
    static void undeploy() {
        Assumptions.assumeTrue(APP_DEPLOYED.get());
        assertThat(ASADMIN.exec("undeploy", TEST_APP_NAME), asadminOK());
    }

    @AfterEach
    void cleanup() {
        ASADMIN.exec("set", SERVER_NAME_PROPERTY + "=");
    }

}
