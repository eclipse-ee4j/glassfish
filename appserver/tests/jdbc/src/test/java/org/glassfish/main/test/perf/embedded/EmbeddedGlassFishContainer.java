/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.perf.embedded;

import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ulimit;

import jakarta.ws.rs.client.WebTarget;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;
import java.util.logging.Level;

import org.glassfish.main.test.perf.rest.RestClientUtilities;
import org.glassfish.tests.utils.junit.JUnitSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.CLASS_LOG_MANAGER_GLASSFISH;
import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.JVM_OPT_LOGGING_CFG_DEFAULT_LEVEL;
import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.JVM_OPT_LOGGING_CFG_USE_DEFAULTS;
import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.JVM_OPT_LOGGING_MANAGER;
import static org.glassfish.main.test.jdbc.pool.war.JdbcDsName.JDBC_DS_POOL_A;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.ENABLE_CONNECTION_VALIDATION;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.LIMIT_HTTP_REQUEST_TIMEOUT;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.LIMIT_POOL_EJB;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.LIMIT_POOL_HTTP_THREADS;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.LIMIT_POOL_JDBC;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.MEM_MAX_APP_HEAP;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.MEM_MAX_APP_OS;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.SERVER_LOG_LEVEL;
import static org.testcontainers.utility.MountableFile.forHostPath;

/**
 * Simple Embedded GlassFish container
 */
public class EmbeddedGlassFishContainer extends GenericContainer<EmbeddedGlassFishContainer> {

    private static final java.util.logging.Logger LOG_GF = java.util.logging.Logger.getLogger("EGF");

    public static final String WAR_FILE = "/tmp.war";

    /**
     * Creates preconfigured container with GlassFish.
     *
     * @param network
     * @param hostname
     * @param logPrefix
     */
    public EmbeddedGlassFishContainer(Network network, String hostname, String logPrefix, PostgreSQLContainer<?> db) {
        super("eclipse-temurin:" + Runtime.version().feature());
        PomEquippedResolveStage resolver = Maven.resolver()
            .loadPomFromFile(JUnitSystem.detectBasedir().resolve("pom.xml").toFile());
        Path jarFile = resolver.resolve("org.glassfish.main.extras:glassfish-embedded-all")
            .withoutTransitivity().asSingleFile().toPath();
        Path jdbcDriverFile = resolver.resolve("org.postgresql:postgresql")
            .withoutTransitivity().asSingleFile().toPath();
        final String poolName = "domain-pool-" + JDBC_DS_POOL_A.charAt(JDBC_DS_POOL_A.length() - 1);
        Path properties = generateProperties(db, poolName);
        withNetwork(network)
        .withCopyFileToContainer(forHostPath(jarFile), "/embedded-glassfish.jar")
        .withCopyFileToContainer(forHostPath(jdbcDriverFile), "/postgresql.jar")
        .withCopyFileToContainer(forHostPath(properties), "/gf-cfg.properties")
        .withEnv("TZ", "UTC").withEnv("LC_ALL", "en_US.UTF-8")
        .withStartupAttempts(1)
        .withStartupTimeout(Duration.ofSeconds(30L))
        .withCreateContainerCmdModifier(cmd -> {
            cmd.withEntrypoint("/bin/sh", "-c");
            cmd.withCmd(getCommandAdmin());
            cmd.withHostName(hostname);
            cmd.withAttachStderr(true);
            cmd.withAttachStdout(true);
            final HostConfig hostConfig = cmd.getHostConfig();
            hostConfig.withMemory(MEM_MAX_APP_OS * 1024 * 1024 * 1024L);
            hostConfig.withMemorySwappiness(0L);
            hostConfig.withUlimits(new Ulimit[] {new Ulimit("nofile", 4096L, 8192L)});
        })
        .withLogConsumer(o -> LOG_GF.log(Level.INFO, o.getUtf8StringWithoutLineEnding()))
        .withExposedPorts(8080)
            .waitingFor(Wait.forLogMessage(".*########### GLASSFISH STARTED ###########.*", 1)
                .withStartupTimeout(Duration.ofMinutes(5L)));
    }

    /**
     * @return JUL Logger to allow attaching handlers
     */
    public java.util.logging.Logger getLogger() {
        return LOG_GF;
    }

    /**
     * @param context
     * @return HTTP REST client following redirects, logging requests and responses
     */
    public WebTarget getRestClient(String context) {
        return RestClientUtilities.getWebTarget(getHttpEndPoint(context), true);
    }

    private URI getHttpEndPoint(String context) {
        return URI.create("http://" + getHost() + ":" + getMappedPort(8080) + "/" + context);
    }

    private static Path generateProperties(PostgreSQLContainer<?> db, final String poolName) {
        try {
            Path properties = Files.createTempFile("gf-cfg", "properties");
            Properties cfg = new Properties();
            cfg.setProperty("command.0.addLibrary", "add-library " + "/postgresql.jar");
            cfg.setProperty("command.1.createPool", "create-jdbc-connection-pool"
//                + " --ping"
                + " --restype javax.sql.DataSource"
                + " --datasourceclassname " + PGSimpleDataSource.class.getName()
                + " --steadypoolsize 0 --maxpoolsize " + LIMIT_POOL_JDBC
                + " --validationmethod auto-commit"
                + " --isconnectvalidatereq " + ENABLE_CONNECTION_VALIDATION + " --failconnection true" //
                + " --property user=" + db.getUsername() + ":password=" + db.getPassword() //
                    + ":DatabaseName=" + db.getDatabaseName() //
                    + ":ServerName=tc-testdb:port=" + 5432 + ":connectTimeout=10 " //
                + poolName
            );
            cfg.setProperty("command.2.createResource",
                "create-jdbc-resource --connectionpoolid " + poolName + ' ' + JDBC_DS_POOL_A);
//            cfg.setProperty("command.3.get", "get *");

            cfg.setProperty("configs.config.server-config.ejb-container.max-pool-size",
                Integer.toString(LIMIT_POOL_EJB));
            cfg.setProperty(
                "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size",
                Integer.toString(LIMIT_POOL_HTTP_THREADS));
            cfg.setProperty(
                "configs.config.server-config.monitoring-service.module-monitoring-levels.jdbc-connection-pool",
                "HIGH");
            cfg.setProperty(
                "configs.config.server-config.network-config.protocols.protocol.https-listener.http.request-timeout-seconds",
                Integer.toString(LIMIT_HTTP_REQUEST_TIMEOUT));

            try (FileOutputStream outputStream = new FileOutputStream(properties.toFile())) {
                cfg.store(outputStream, null);
            }
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String getCommandAdmin() {
        final StringBuilder command = new StringBuilder();
        command.append("echo \"***************** Starting Embedded GlassFish *****************\"");
        command.append(" && set -x && set -e");
        command.append(" && export LANG=\"en_US.UTF-8\"").append(" && export LANGUAGE=\"en_US.UTF-8\"");
        command.append(" && (env | sort) && locale");
        command.append(" && ulimit -a");
        command.append(" && cat /etc/hosts && cat /etc/resolv.conf");
        command.append(" && hostname");
        command.append(" && java -version");
        command.append(" && mkdir -p /opt");
        command.append(" && cd /opt");
        command.append(" && cat /gf-cfg.properties");
        command.append(" && java ")
            .append(" -D").append(JVM_OPT_LOGGING_MANAGER).append('=').append(CLASS_LOG_MANAGER_GLASSFISH)
            .append(" -D").append(JVM_OPT_LOGGING_CFG_USE_DEFAULTS).append('=').append(true)
            .append(" -D").append(JVM_OPT_LOGGING_CFG_DEFAULT_LEVEL).append('=').append(SERVER_LOG_LEVEL)
            .append(" -Xmx").append(MEM_MAX_APP_HEAP).append('g')
            .append(" -jar /embedded-glassfish.jar --properties=").append("/gf-cfg.properties")
            .append(" --httpPort=8080 ")
            .append(WAR_FILE);
        return command.toString();
    }
}
