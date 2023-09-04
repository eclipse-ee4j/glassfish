/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.app.concurrency.executor;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.net.HttpURLConnection;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.lang.System.Logger.Level.INFO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ManagedExecutorTasksSubmittedDuringDeploymentTest {

    private static final Logger LOG = System.getLogger(ManagedExecutorTasksSubmittedDuringDeploymentTest.class.getName());
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();
    private static final String APP_NAME = ManagedExecutorTasksSubmittedDuringDeploymentTest.class.getSimpleName();

    @BeforeAll
    static void deploy() {
        File war = createDeployment();
        try {
            AsadminResult result = ASADMIN.exec("deploy", "--contextroot", "/", "--name", APP_NAME,
                war.getAbsolutePath());
            assertThat(result, AsadminResultMatcher.asadminOK());
        } finally {
            war.delete();
        }
    }


    @AfterAll
    static void undeploy() {
        AsadminResult result = ASADMIN.exec("undeploy", APP_NAME);
        assertThat(result, AsadminResultMatcher.asadminOK());
    }


    @Test
    void testSubmittedTaskExecuted() throws Exception {
        HttpURLConnection connection = GlassFishTestEnvironment.openConnection(8080, "/");
        connection.setRequestMethod("GET");
        assertEquals(200, connection.getResponseCode());
        assertEquals("true", connection.getHeaderField(ManagedExecutorTasksSubmittedDuringDeploymentServlet.HEADER_EXECUTED));
    }


    private static File createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, ManagedExecutorTasksSubmittedDuringDeploymentTest.class.getSimpleName() + ".war")
                .addClasses(ManagedExecutorTasksSubmittedDuringDeploymentServlet.class,
                        StartupBean.class);
        LOG.log(INFO, war.toString(true));
        try {
            File tempFile = File.createTempFile(APP_NAME, ".war");
            war.as(ZipExporter.class).exportTo(tempFile, true);
            return tempFile;
        } catch (IOException e) {
            throw new IllegalStateException("Deployment failed - cannot load the input archive!", e);
        }
    }
}
