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

package org.glassfish.main.test.perf.util;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.dockerjava.api.DockerClient;

import jakarta.ws.rs.client.WebTarget;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.main.test.jdbc.pool.war.JdbcDsName.JDBC_DS_POOL_A;
import static org.glassfish.main.test.jdbc.pool.war.JdbcDsName.JDBC_DS_POOL_B;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.testcontainers.utility.MountableFile.forClasspathResource;

/**
 * Environment of Eclipse GlassFish, Derby and Postgress SQL databases.
 */
public class DockerTestEnvironment {
    public static final int LIMIT_DB = 300;
    public static final int LIMIT_JDBC = 200;

    private static final Logger LOG = System.getLogger(DockerTestEnvironment.class.getName());
    private static final Logger LOG_DB = System.getLogger("DB");

    /** Docker network */
    private static final Network NET = Network.newNetwork();

    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>("postgres:17.6")
        .withNetwork(NET).withDatabaseName("testdb").withExposedPorts(5432).withCreateContainerCmdModifier(cmd -> {
            cmd.withHostName("tc-testdb");
            cmd.withAttachStderr(true);
            cmd.withAttachStdout(true);
        })
        .withLogConsumer(o -> LOG_DB.log(INFO, o.getUtf8StringWithoutLineEnding()))
        .withCommand("postgres",
            "-c", "log_statement=none",
            "-c", "log_destination=stderr",
            "-c", "max_connections=" + LIMIT_DB
        );

    @SuppressWarnings("resource")
    private static final GlassFishContainer AS_DOMAIN = new GlassFishContainer(forClasspathResource("/glassfish.zip"),
        NET, "admin", "A").withJdbcDrivers(forClasspathResource("/postgresql.jar"));

    static {
        Thread hook = new Thread(DockerTestEnvironment::stop);
        Runtime.getRuntime().addShutdownHook(hook);
        start();
    }

    private static ConnectionHolder connectionHolder;
    private static DataSetExecutor dsExecutor;

    /**
     * @return Logger printing GlassFish server.log file
     */
    public static java.util.logging.Logger getDomainLogger() {
        return AS_DOMAIN.getLogger();
    }

    /**
     * GlassFish's asadmin get -m [key] execution
     *
     * @param key monitoring property key
     * @return int value
     */
    public static int asadminMonitor(String key) {
        return AS_DOMAIN.asadminGetInt(key);
    }

    /**
     * GlassFish's asadmin script execution.
     *
     * @param commandName
     * @param arguments
     * @return result of the command
     */
    public static ExecResult asadmin(String commandName, String... arguments) {
        return AS_DOMAIN.asadmin(commandName, arguments);
    }

    /**
     * Start the environment - start Postgress database, and GlassFish's domain1.
     * Create JDBC pool and resources.
     * Start Derby (comes with GlassFish)
     */
    public static void start() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is not available on this environment");
        DATABASE.start();
        AS_DOMAIN.start();
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setDatabaseName(DATABASE.getDatabaseName());
        dataSource.setServerNames(new String[] {DATABASE.getHost()});
        dataSource.setUrl(DATABASE.getJdbcUrl());
        dataSource.setUser(DATABASE.getUsername());
        dataSource.setPassword(DATABASE.getPassword());
        dataSource.setConnectTimeout(60);
        connectionHolder = dataSource::getConnection;
        dsExecutor = DataSetExecutorImpl.instance("executor", connectionHolder);
        reinitializeDatabase();

        for (String jndiName : new String[] {JDBC_DS_POOL_A, JDBC_DS_POOL_B}) {
            final String poolName = "domain-pool-" + jndiName.charAt(jndiName.length() - 1);
            assertEquals(0, AS_DOMAIN.asadmin("create-jdbc-connection-pool", "--ping",
                "--restype", "javax.sql.DataSource", //
                "--datasourceclassname", PGSimpleDataSource.class.getName(), //
                "--steadypoolsize", "0", "--maxpoolsize", Integer.toString(LIMIT_JDBC), //
                "--validationmethod", "auto-commit", //
//                "--isconnectvalidatereq", "false", "--failconnection", "false", //
                "--isconnectvalidatereq", "true", "--failconnection", "true", //
                "--property", "user=" + DATABASE.getUsername() + ":password=" + DATABASE.getPassword() //
                    + ":DatabaseName=" + DATABASE.getDatabaseName() //
                    + ":ServerName=tc-testdb:port=" + 5432 + ":connectTimeout=10" //
                , //
                poolName).getExitCode());
            assertEquals(0,
                AS_DOMAIN.asadmin("create-jdbc-resource", "--connectionpoolid", poolName, jndiName).getExitCode());
        }

        assertEquals(0, AS_DOMAIN.asadmin("start-database").getExitCode());

        final String respListPools = AS_DOMAIN.asadmin("list-jdbc-connection-pools").getStdout();
        assertThat("list-jdbc-connection-pools response", respListPools,
            stringContainsInOrder("__TimerPool", "DerbyPool", "domain-pool-A", "domain-pool-B"));
    }


    public static void disconnectDatabase(int seconds) {
        DockerClient client = DockerClientFactory.instance().client();
        LOG.log(INFO, "Disconnecting database from network!");
        client.disconnectFromNetworkCmd().withNetworkId(NET.getId()).withContainerId(DATABASE.getContainerId()).exec();
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        client.connectToNetworkCmd().withNetworkId(NET.getId()).withContainerId(DATABASE.getContainerId()).exec();
        LOG.log(INFO, "Database reconnected to the network!");
    }


    /**
     * Stops all virtual computers, and destroys the virtual network.
     */
    public static void stop() {
        LOG.log(INFO, "Closing docker containers ...");
        if (AS_DOMAIN.isRunning()) {
            AS_DOMAIN.asadmin("stop-domain", "--kill");
            closeSilently(AS_DOMAIN);
        }
        if (DATABASE.isRunning()) {
            closeSilently(DATABASE);
        }
        closeSilently(NET);
    }


    /**
     * Drop all data and recreate just those existing before the test.
     */
    public static void reinitializeDatabase() {
        dsExecutor.executeScript("initSchema.sql");
    }

    /**
     * Create and deploy the war file.
     *
     * @param appName name and application context
     * @param classes classes included to the war file.
     * @return endpoint of the application - it is expected that the context root is the application
     *         name.
     */
    public static WebTarget deploy(String appName, Class<?>... classes) {
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
    public static WebTarget deploy(String appName, Package... pkgs) {
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
    public static WebTarget deploy(String appName, WebArchive war) {
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
    public static WebTarget deploy(String appName, File warFile) {
        final String warFileInContainer = "/tmp.war";
        AS_DOMAIN.copyFileToContainer(MountableFile.forHostPath(warFile.toPath()), warFileInContainer);
        try {
            final ExecResult result = AS_DOMAIN.asadmin("deploy", "--contextroot", appName, "--name", appName,
                warFileInContainer);
            assertThat("deploy response", result.getStdout(),
                stringContainsInOrder("Application deployed with name " + appName));
            return AS_DOMAIN.getRestClient(appName);
        } finally {
            try {
                AS_DOMAIN.execInContainer("rm", warFileInContainer);
            } catch (UnsupportedOperationException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Undeploy the web application
     *
     * @param appName
     */
    public static void undeploy(String appName) {
        final ExecResult result = AS_DOMAIN.asadmin("undeploy", appName);
        assertEquals(0, result.getExitCode(), "Undeploy exit code");
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


    // TODO: Move to jdke
    private static void closeSilently(final AutoCloseable closeable) {
        LOG.log(TRACE, "closeSilently(closeable={0})", closeable);
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (final Exception e) {
            LOG.log(WARNING, "Close method caused an exception.", e);
        }
    }
}
