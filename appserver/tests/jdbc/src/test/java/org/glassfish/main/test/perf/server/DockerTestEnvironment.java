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

package org.glassfish.main.test.perf.server;

import jakarta.ws.rs.client.WebTarget;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;

import org.glassfish.main.test.perf.benchmark.Environment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.PostgreSQLContainer;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.test.jdbc.pool.war.JdbcDsName.JDBC_DS_POOL_A;
import static org.glassfish.main.test.jdbc.pool.war.JdbcDsName.JDBC_DS_POOL_B;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.ENABLE_CONNECTION_VALIDATION;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.LIMIT_POOL_JDBC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Environment of Eclipse GlassFish server, Derby and PostgreSQL databases.
 */
public class DockerTestEnvironment extends Environment {

    private static final Logger LOG = System.getLogger(DockerTestEnvironment.class.getName());

    private static DockerTestEnvironment instance;

    private final GlassFishContainer appServer;

    /**
     * Creates network, databases and application server, but doesn't start them.
     */
    public DockerTestEnvironment() {
        appServer = new GlassFishContainer(getNetwork(), "admin", "A");
        Thread hook = new Thread(this::stop);
        Runtime.getRuntime().addShutdownHook(hook);
    }

    /**
     * @return Logger printing GlassFish server.log file
     */
    public java.util.logging.Logger getDomainLogger() {
        return appServer.getLogger();
    }

    /**
     * GlassFish's asadmin get -m [key] execution
     *
     * @param key monitoring property key
     * @return int value
     */
    public int asadminMonitor(String key) {
        return appServer.asadminGetInt(key);
    }

    /**
     * GlassFish's asadmin script execution.
     *
     * @param commandName
     * @param arguments
     * @return result of the command
     */
    public ExecResult asadmin(String commandName, String... arguments) {
        return appServer.asadmin(commandName, arguments);
    }

    /**
     * Start the environment - start Postgress database, and GlassFish's domain1.
     * Create JDBC pool and resources.
     * Start Derby (comes with GlassFish)
     */
    @Override
    public void start() {
        super.start();
        PostgreSQLContainer<?> db = getDatabase();
        appServer.start();
        for (String jndiName : new String[] {JDBC_DS_POOL_A, JDBC_DS_POOL_B}) {
            final String poolName = "domain-pool-" + jndiName.charAt(jndiName.length() - 1);
            assertEquals(0, appServer.asadmin("create-jdbc-connection-pool", "--ping",
                "--restype", "javax.sql.DataSource", //
                "--datasourceclassname", PGSimpleDataSource.class.getName(), //
                "--steadypoolsize", "0", "--maxpoolsize", Integer.toString(LIMIT_POOL_JDBC), //
                "--validationmethod", "auto-commit", //
                "--isconnectvalidatereq", Boolean.toString(ENABLE_CONNECTION_VALIDATION), "--failconnection", "true", //
                "--property", "user=" + db.getUsername() + ":password=" + db.getPassword() //
                    + ":DatabaseName=" + db.getDatabaseName() //
                    + ":ServerName=tc-testdb:port=" + 5432 + ":connectTimeout=10" //
                , //
                poolName).getExitCode());
            assertEquals(0,
                appServer.asadmin("create-jdbc-resource", "--connectionpoolid", poolName, jndiName).getExitCode());
        }
        assertEquals(0, appServer.asadmin("get", "resources.jdbc-connection-pool.*").getExitCode());
        assertEquals(0, appServer.asadmin("start-database").getExitCode());

        final String respListPools = appServer.asadmin("list-jdbc-connection-pools").getStdout();
        assertThat("list-jdbc-connection-pools response", respListPools,
            stringContainsInOrder("__TimerPool", "DerbyPool", "domain-pool-A", "domain-pool-B"));
    }


    /**
     * Stops all virtual computers, and destroys the virtual network.
     */
    @Override
    public void stop() {
        LOG.log(INFO, "Closing docker containers ...");
        if (appServer.isRunning()) {
            appServer.asadmin("stop-domain", "--kill");
            closeSilently(appServer);
        }
        super.stop();
    }


    /**
     * Create and deploy the war file.
     *
     * @param appName name and application context
     * @param classes classes included to the war file.
     * @return endpoint of the application - it is expected that the context root is the application
     *         name.
     */
    public WebTarget deploy(String appName, Class<?>... classes) {
        File warFile = getArchiveToDeploy(appName, classes);
        return deploy(appName, warFile);
    }

    /**
     * Create and deploy the war file.
     *
     * @param appName name and application context
     * @param pkgs packages included to the war file.
     * @return endpoint of the application - it is expected that the context root is the application
     *         name.
     */
    public WebTarget deploy(String appName, Package... pkgs) {
        File warFile = getArchiveToDeploy(appName, pkgs);
        return deploy(appName, warFile);
    }

    /**
     * Deploy the war file.
     *
     * @param appName name and application context
     * @param war the war file to be deployed.
     * @return endpoint of the application - it is expected that the context root is the application
     *         name.
     */
    @Override
    public WebTarget deploy(String appName, WebArchive war) {
        File warFile = toFile(appName, war);
        return deploy(appName, warFile);
    }


    /**
     * Deploy the war file.
     *
     * @param appName name and application context
     * @param warFile the war file to be deployed.
     * @return endpoint of the application - it is expected that the context root is the application
     *         name.
     */
    public WebTarget deploy(String appName, File warFile) {
        return appServer.deploy(appName, warFile);
    }

    /**
     * Undeploy the web application
     *
     * @param appName
     */
    @Override
    public void undeploy(String appName) {
        final ExecResult result = appServer.asadmin("undeploy", appName);
        assertEquals(0, result.getExitCode(), "Undeploy exit code");
    }

    /**
     * Creates the environment once, then provides always the same instance.
     *
     * @return {@link DockerTestEnvironment}
     */
    public static synchronized DockerTestEnvironment getInstance() {
        if (instance == null) {
            instance = new DockerTestEnvironment();
            instance.start();
        }
        return instance;
    }


    private static File getArchiveToDeploy(String appName, Class<?>... classes) {
        final WebArchive war = ShrinkWrap.create(WebArchive.class).addClasses(classes);
        LOG.log(INFO, war.toString(true));
        return toFile(appName, war);
    }

    private static File getArchiveToDeploy(String appName, Package... pkg) {
        final WebArchive war = ShrinkWrap.create(WebArchive.class).addPackages(true, pkg);
        return toFile(appName, war);
    }

    private static File toFile(final String appName, final WebArchive war) {
        LOG.log(INFO, () -> war.toString(true));
        File warFile;
        try {
            warFile = File.createTempFile(appName, ".war");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        war.as(ZipExporter.class).exportTo(warFile, true);
        return warFile;
    }
}
