/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.glassfish.main.test.app.persistence.oracle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.test.app.persistence.oracle.webapp.OracleEntity;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import oracle.jdbc.pool.OracleDataSource;
import oracle.sql.TIMESTAMP;
import oracle.sql.TIMESTAMPLTZ;
import oracle.sql.TIMESTAMPTZ;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminError;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Verifies that the EclipseLink Oracle extension shipped in {@code modules} can be used together
 * with an Oracle JDBC driver installed in {@code domain-dir/lib/ext}, as described in the
 * Administration Guide.
 * <p>
 * Two things have to hold for that. The extension is an OSGi fragment of the EclipseLink core
 * bundle, so it has to resolve, otherwise no {@code Oracle*Platform} class can be loaded at all.
 * And its platform classes link against the driver, which they can only see through the packages
 * the system bundle exports from the server class path - which is where jars in
 * {@code domain-dir/lib/ext} end up.
 * <p>
 * The Oracle JDBC driver cannot be redistributed, so a stub providing just the driver classes the
 * platform touches stands in for it. Deployment is therefore expected to fail, but only when
 * EclipseLink asks the connection pool for a connection - by then everything this test is about
 * has already happened.
 *
 * @see <a href="https://github.com/eclipse-ee4j/glassfish/issues/24212">issue 24212</a>
 */
public class OracleDatabasePlatformTest {

    private static final System.Logger LOG = System.getLogger(OracleDatabasePlatformTest.class.getName());

    private static final Package TEST_PACKAGE = OracleDatabasePlatformTest.class.getPackage();
    private static final String APP_NAME = OracleDatabasePlatformTest.class.getSimpleName() + "WebApp";

    private static final String POOL_NAME = "OracleDriverStubPool";
    private static final String RESOURCE_NAME = "jdbc/__oracleDriverStub";
    private static final String DRIVER_STUB_JAR = "oracle-driver-stub.jar";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;

    private static File warFile;

    @BeforeAll
    public static void installDriverStub() throws IOException {
        warFile = createDeployment();
        createDriverStubJar(getDriverStubPath());
        // lib/ext is read by the launcher, so the server has to be started again to see the jar.
        assertThat(ASADMIN.exec("restart-domain", "domain1"), asadminOK());
        assertThat(ASADMIN.exec("create-jdbc-connection-pool",
            "--datasourceclassname", OracleDataSource.class.getName(),
            "--restype", "javax.sql.DataSource", POOL_NAME), asadminOK());
        assertThat(ASADMIN.exec("create-jdbc-resource", "--connectionpoolid", POOL_NAME, RESOURCE_NAME), asadminOK());
    }


    @AfterAll
    public static void uninstallDriverStub() throws IOException {
        ASADMIN.exec("undeploy", APP_NAME);
        assertAll(
            () -> assertThat(ASADMIN.exec("delete-jdbc-resource", RESOURCE_NAME), asadminOK()),
            () -> assertThat(ASADMIN.exec("delete-jdbc-connection-pool", POOL_NAME), asadminOK()));
        // The jar is on the server class path, so the server has to let go of it before it can
        // be deleted; a restart would just put it on the class path of the new process again.
        assertThat(ASADMIN.exec("stop-domain"), asadminOK());
        Files.deleteIfExists(getDriverStubPath());
        assertThat(ASADMIN.exec("start-domain"), asadminOK());
    }


    @Test
    public void oraclePlatformIsUsableWithDriverInDomainLibExt() {
        AsadminResult result = ASADMIN.exec("deploy", "--name", APP_NAME, "--contextroot", "/" + APP_NAME,
            warFile.getAbsolutePath());
        assertAll(
            // Happens when the Oracle extension fragment did not resolve and its classes are invisible.
            () -> assertThat("Oracle platform class not found", result.getOutput(),
                not(containsString("Database platform class"))),
            // Happens when the platform class is visible, but the driver classes it links against are not.
            () -> assertThat("Oracle platform class not initialized", result.getOutput(),
                not(containsString("NoClassDefFoundError"))),
            // How far the stub driver can get: EclipseLink has the platform and wants a connection.
            () -> assertThat(result, asadminError("Failed to obtain/create connection from connection pool")));
    }


    private static Path getDriverStubPath() {
        return GlassFishTestEnvironment.getDomain1Directory().resolve(Path.of("lib", "ext", DRIVER_STUB_JAR));
    }


    private static void createDriverStubJar(Path target) throws IOException {
        JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(TIMESTAMP.class, TIMESTAMPTZ.class, TIMESTAMPLTZ.class, OracleDataSource.class);
        LOG.log(INFO, javaArchive.toString(true));
        Files.createDirectories(target.getParent());
        javaArchive.as(ZipExporter.class).exportTo(target.toFile(), true);
    }


    private static File createDeployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addClass(OracleEntity.class)
            .addAsResource(TEST_PACKAGE, "persistence.xml", "META-INF/persistence.xml");
        LOG.log(INFO, webArchive.toString(true));
        File war = new File(tempDir, APP_NAME + ".war");
        webArchive.as(ZipExporter.class).exportTo(war, true);
        return war;
    }
}
