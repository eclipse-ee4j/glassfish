/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.data;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.glassfish.common.util.HttpParser;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.DomainPropertiesBackup;
import org.glassfish.main.test.app.helpers.JakartaEE;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TransactionalAnnotationTest {

    private static final System.Logger LOG = System.getLogger(TransactionalAnnotationTest.class.getName());
    private static final String WEBAPP_NAME = "transactionApp";
    private static final String WEBAPP_WAR = "transactionApp.war";
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();
    private static final DomainPropertiesBackup DERBYPOOL_BACKUP = DomainPropertiesBackup.backupDerbyPool();

    @TempDir
    private static File webAppDir;

    @BeforeAll
    public static void deployAll() throws IOException {
        GlassFishTestEnvironment.switchDerbyPoolToEmbededded();
        File webApp = createWebApp();
        AsadminResult result = ASADMIN.exec("deploy", "--contextroot", "/" + WEBAPP_NAME, "--name", WEBAPP_NAME, webApp.getAbsolutePath());
        assertThat(result, asadminOK());
    }

    @AfterAll
    public static void undeployAll() {
        assertThat(ASADMIN.exec("undeploy", WEBAPP_NAME), asadminOK());
        DERBYPOOL_BACKUP.restore();
    }

    @Test
    public void testTransactionalRequiresNew() throws IOException {
        String response = getResponse(WEBAPP_NAME, "transaction");
        String[] transactionIds = response.split(TransactionService.SEPARATOR);

        assertNotEquals(transactionIds[0], transactionIds[1],
            "Transaction IDs should be different due to REQUIRES_NEW");
    }

    private String getResponse(String contextRoot, String endpoint) throws IOException {
        HttpURLConnection connection = openConnection(8080, "/" + contextRoot + "/" + endpoint);
        connection.setRequestMethod("GET");
        try {
            int responseCode = connection.getResponseCode();
            String reason = "";
            if (responseCode != 200) {
                String errorResponse = HttpParser.readResponseErrorStream(connection);
                reason = ("Received response code " + responseCode + " with an error: " + errorResponse);
            }
            assertThat(reason, responseCode, equalTo(200));
            return HttpParser.readResponseInputStream(connection);
        } finally {
            connection.disconnect();
        }
    }

    private static File createWebApp() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addClass(TransactionApplication.class)
            .addClass(TransactionResource.class)
            .addClass(TransactionService.class)
            .addClass(TransactionRepository.class)
            .addClass(TransactionEntity.class)
            .addPackage(JakartaEE.class.getPackage())
            .addAsResource(TransactionalAnnotationTest.class.getPackage(), "persistence.xml", "META-INF/persistence.xml");

        LOG.log(INFO, webArchive.toString(true));

        File webApp = new File(webAppDir, WEBAPP_WAR);
        webArchive.as(ZipExporter.class).exportTo(webApp, true);
        return webApp;
    }
}
