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
package org.glassfish.main.test.app.persistence.data.repository.nosql;

import java.io.File;
import java.net.http.HttpResponse;

import org.glassfish.main.itest.tools.TestUtilities;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getHttpResource;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class DataNoSqlRepositoryTest {

    private static final System.Logger LOG =
            System.getLogger(DataNoSqlRepositoryTest.class.getName());

    private static final String APP_NAME =
            DataNoSqlRepositoryTest.class.getSimpleName() + "WebApp";

    private static final Package TEST_PACKAGE =
            Product.class.getPackage();

    private static final Asadmin ASADMIN = getAsadmin();

    private static final GenericContainer<?> MONGODB =
            new GenericContainer<>("mongo:latest")
                    .withExposedPorts(27017)
                    .waitingFor(Wait.defaultWaitStrategy());

    @TempDir
    private static File tempDir;

    private static File warFile;

    @BeforeAll
    public static void deploy() throws Exception {
        assumeTrue(
                DockerClientFactory.instance().isDockerAvailable(),
                "Docker is not available on this environment");

        MONGODB.start();

        String mongoDbHost =
                MONGODB.getHost() + ":" + MONGODB.getFirstMappedPort();

        LOG.log(INFO, "Started MongoDB in Docker at " + mongoDbHost);

        PomEquippedResolveStage resolver =
                Maven.resolver().loadPomFromFile("pom.xml");

        File[] mongoDependencies = resolver
                .resolve("org.eclipse.jnosql.databases:jnosql-mongodb")
                .withTransitivity()
                .asFile();

        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
                .addPackage(TEST_PACKAGE)
                .deleteClass(DataNoSqlRepositoryTest.class)
                .addAsLibraries(mongoDependencies)
                .addAsResource(
                        new StringAsset(
                                "jnosql.document.database=tck\n"
                                        + "jnosql.mongodb.host=" + mongoDbHost + "\n"),
                        "META-INF/microprofile-config.properties");

        LOG.log(INFO, webArchive.toString(true));

        warFile = new File(tempDir, APP_NAME + ".war");
        webArchive.as(ZipExporter.class).exportTo(warFile, true);

        assertThat(
                ASADMIN.exec(
                        "deploy",
                        "--target",
                        "server",
                        warFile.getAbsolutePath()),
                asadminOK());
    }

    @Test
    public void test() throws Exception {
        final HttpResponse<String> response =
                getHttpResource(APP_NAME + "/TestServlet");

        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.body(), equalTo("products=1"));
    }

    @AfterAll
    public static void cleanup() throws Exception {
        if (warFile != null) {
            ASADMIN.exec("undeploy", APP_NAME);
            TestUtilities.delete(warFile);
        }

        MONGODB.stop();
    }
}
