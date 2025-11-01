/*
 * Copyright (c) 2025 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.perf.benchmark;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ulimit;

import jakarta.ws.rs.client.WebTarget;

import java.lang.System.Logger;
import java.time.Duration;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.LIMIT_DBSERVER_CONNECTION_COUNT;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.MEM_MAX_DB_OS;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.MEM_MAX_DB_SHARED;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The environment of the test, database server, application server, network.
 */
public abstract class Environment {
    private static final Logger LOG = System.getLogger(Environment.class.getName());
    private static final Logger LOG_DB = System.getLogger("DB");

    private final Network network = Network.newNetwork();

    @SuppressWarnings("resource")
    private final PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:17.6")
        .withNetwork(network).withDatabaseName("testdb").withExposedPorts(5432).withCreateContainerCmdModifier(cmd -> {
            cmd.withHostName("tc-testdb");
            cmd.withAttachStderr(true);
            cmd.withAttachStdout(true);
            final HostConfig hostConfig = cmd.getHostConfig();
            hostConfig.withMemory(MEM_MAX_DB_OS * 1024 * 1024 * 1024L);
            hostConfig.withMemorySwappiness(0L);
            hostConfig.withUlimits(new Ulimit[] {new Ulimit("nofile", 4096L, 8192L)});
            hostConfig.withShmSize(MEM_MAX_DB_SHARED * 1024 * 1024 * 1024L);
        })
        .withLogConsumer(o -> LOG_DB.log(INFO, o.getUtf8StringWithoutLineEnding()))
        .withCommand("postgres",
            "-c", "log_statement=none",
            "-c", "log_destination=stderr",
            "-c", "max_connections=" + LIMIT_DBSERVER_CONNECTION_COUNT
        );

    private ConnectionHolder connectionHolder;
    private DataSetExecutor dsExecutor;

    /** @return Docker network */
    public Network getNetwork() {
        return network;
    }

    /**
     * @return database container
     */
    public PostgreSQLContainer<?> getDatabase() {
        return database;
    }

    /**
     * Start the environment - network and database, children may start more containers.
     * Then deploys the application.
     * @param appName
     *
     * @param war
     * @return endpoint of the application.
     */
    public WebTarget start(String appName, WebArchive war) {
        start();
        return deploy(appName, war);
    }

    /**
     * Start the environment - network and database
     */
    public void start() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is not available on this environment");
        PostgreSQLContainer<?> db = getDatabase();
        db.start();

        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setDatabaseName(db.getDatabaseName());
        dataSource.setServerNames(new String[] {db.getHost()});
        dataSource.setUrl(db.getJdbcUrl());
        dataSource.setUser(db.getUsername());
        dataSource.setPassword(db.getPassword());
        dataSource.setConnectTimeout(60);
        connectionHolder = dataSource::getConnection;
        dsExecutor = DataSetExecutorImpl.instance("executor", connectionHolder);
        reinitializeDatabase();
    }

    /**
     * Stops all virtual computers, and destroys the virtual network.
     */
    public void stop() {
        PostgreSQLContainer<?> db = getDatabase();
        if (db.isRunning()) {
            closeSilently(db);
        }
        closeSilently(getNetwork());
    }

    /**
     * Deploys the war file under the given application name
     *
     * @param appName
     * @param war
     * @return endpoint
     */
    public abstract WebTarget deploy(String appName, WebArchive war);

    /**
     * Undeploys the war file registered under the given application name.
     *
     * @param appname
     */
    public abstract void undeploy(String appname);

    /**
     * Disconnects the database for the duration. Then reconnects.
     *
     * @param delay
     * @param duration
     */
    public void disconnectDatabase(Duration delay, Duration duration) {
        Thread networkIssue = new Thread(() -> {
            try {
                Thread.sleep(delay.toMillis());
                disconnectDatabase(duration);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        networkIssue.start();
    }

    /**
     * Disconnects the database for the duration. Then reconnects.
     *
     * @param duration
     */
    public void disconnectDatabase(Duration duration) {
        DockerClient client = DockerClientFactory.instance().client();
        LOG.log(INFO, "Disconnecting database from network!");
        client.disconnectFromNetworkCmd().withNetworkId(network.getId()).withContainerId(database.getContainerId()).exec();
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        client.connectToNetworkCmd().withNetworkId(network.getId()).withContainerId(database.getContainerId()).exec();
        LOG.log(INFO, "Database reconnected to the network!");
    }

    /**
     * Drop all data and recreate just those existing before the test.
     */
    public void reinitializeDatabase() {
        dsExecutor.executeScript("initSchema.sql");
    }

    /**
     * Closes the {@link AutoCloseable} - if it throws exception, the exception is just logged.
     * Useful for situation where we are closing whole environment and we don't plan any reaction
     * to possible exceptions.
     *
     * @param closeable
     */
    protected static void closeSilently(final AutoCloseable closeable) {
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
