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

package org.glassfish.main.test.perf.util;

import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ulimit;

import jakarta.ws.rs.client.WebTarget;

import java.io.IOException;
import java.lang.System.Logger;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

/**
 * Simple GlassFish container based on domain1.
 */
public class GlassFishContainer extends GenericContainer<GlassFishContainer> {

    public static final int LIMIT_HTTP_THREADS = 1000;

    private static final Logger LOG = System.getLogger(GlassFishContainer.class.getName());
    private static final java.util.logging.Logger LOG_GF = java.util.logging.Logger.getLogger("GF");

    private static final Path PATH_DOCKER_GF_ROOT = Path.of("/opt", "glassfish8");
    private static final String DOMAIN_NAME = "domain1";
    private static final String PATH_DOCKER_ASADMIN = PATH_DOCKER_GF_ROOT.resolve(Path.of("bin", "asadmin")).toString();
    private static final Path PATH_DOCKER_GF_DOMAINS = PATH_DOCKER_GF_ROOT.resolve(Path.of("glassfish", "domains"));
    private static final Path PATH_DOCKER_GF_DOMAIN = PATH_DOCKER_GF_DOMAINS.resolve(DOMAIN_NAME);
    private static final Path PATH_DOCKER_GF_DOMAIN1_SERVER_LOG = PATH_DOCKER_GF_DOMAIN
        .resolve(Path.of("logs", "server.log"));

    /**
     * Creates preconfigured container with GlassFish.
     *
     * @param glassFishZip GlassFish zip file.
     * @param network
     * @param hostname
     * @param logPrefix
     */
    public GlassFishContainer(MountableFile glassFishZip, Network network, String hostname, String logPrefix) {
        super("eclipse-temurin:21");
        withNetwork(network)
        .withCopyFileToContainer(glassFishZip, "/glassfish.zip")
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
            hostConfig.withMemory(8 * 1024 * 1024 * 1024L);
            hostConfig.withMemorySwappiness(0L);
            hostConfig.withUlimits(new Ulimit[] {new Ulimit("nofile", 4096L, 8192L)});
        })
        .withLogConsumer(o -> LOG_GF.log(Level.INFO, o.getUtf8StringWithoutLineEnding()))
        .withExposedPorts(8080)
        .waitingFor(
            Wait.forLogMessage(".*Total startup time including CLI.*", 1).withStartupTimeout(Duration.ofMinutes(5L)));
    }

    /**
     * Mount provided JDBC drivers and install them to domain1 when starting the container.
     *
     * @param jdbcDriverJars
     * @return this
     */
    public GlassFishContainer withJdbcDrivers(MountableFile... jdbcDriverJars) {
        for (MountableFile jdbcDriverFile : jdbcDriverJars) {
            withCopyFileToContainer(jdbcDriverFile,
                "/" + Path.of(jdbcDriverFile.getFilesystemPath()).getFileName().toString());
        }
        return this;
    }


    /**
     * @return JUL Logger to allow attaching handlers
     */
    public java.util.logging.Logger getLogger() {
        return LOG_GF;
    }


    /**
     * Executes asadmin get -m [key]
     *
     * @param key
     * @return integer value
     */
    public int asadminGetInt(String key) {
        ExecResult monitoring = asadmin("get", "-m", key);
        String keyEquals = key + " = ";
        try {
            int index = monitoring.getStdout().indexOf(keyEquals);
            return Integer.parseInt(monitoring.getStdout().substring(index + keyEquals.length()).strip());
        } catch (Exception e) {
            LOG.log(ERROR, "Failed to parse as int: {0}", monitoring.getStdout().strip());
            return -1;
        }
    }

    /**
     * Execute asadmin command.
     *
     * @param commandName
     * @param arguments
     * @return result. Check the exit code and output.
     */
    public ExecResult asadmin(String commandName, String... arguments) {
        try {
            ExecResult result = execInContainer(toExecArgs(commandName, arguments));
            LOG.log(INFO, "Asadmin STDOUT: \n{0}", result.getStdout());
            LOG.log(INFO, "Asadmin STDERR: \n{0}", result.getStderr());
            return result;
        } catch (UnsupportedOperationException | IOException | InterruptedException e) {
            throw new RuntimeException(
                "Execution failed for command " + commandName + " with arguments: " + String.join(" ", arguments), e);
        }
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

    private static String getCommandAdmin() {
        final StringBuilder command = new StringBuilder();
        command.append("echo \"***************** Starting the GlassFish's domain1 *****************\"");
        command.append(" && set -x && set -e");
        command.append(" && export LANG=\"en_US.UTF-8\"").append(" && export LANGUAGE=\"en_US.UTF-8\"");
        command.append(" && (env | sort) && locale");
        command.append(" && ulimit -a");
        command.append(" && cat /etc/hosts && cat /etc/resolv.conf");
        command.append(" && hostname");
        command.append(" && java -version");
        command.append(" && mkdir -p /opt");
        command.append(" && cd /opt");
        command.append(" && jar xf /glassfish.zip");
        command.append(" && cd ").append(PATH_DOCKER_GF_ROOT.resolve("bin"));
        command.append(" && chmod +x asadmin startserv stopserv");
        command.append(" && cd ../glassfish/bin");
        command.append(" && chmod +x asadmin appclient startserv stopserv");
        command.append(" && mv /*.jar ").append(PATH_DOCKER_GF_DOMAIN.resolve("lib"));
        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" start-domain ").append("domain1");
        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" set-log-levels ").append("org.postgresql.level=FINEST");
        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" set ").append("configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + LIMIT_HTTP_THREADS);
        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" set ").append("configs.config.server-config.ejb-container.max-pool-size=900");
        // Monitoring takes around 10% of throughput
        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" set ").append("configs.config.server-config.monitoring-service.module-monitoring-levels.jdbc-connection-pool=HIGH");
        // Does thread dumps periodically, takes much more
//        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" set ").append("configs.config.server-config.monitoring-service.module-monitoring-levels.jvm=HIGH");
        // FIXME: Does not work, see https://github.com/eclipse-ee4j/glassfish/issues/25701
//        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" set ").append("configs.config.server-config.monitoring-service.module-monitoring-levels.thread-pool=HIGH");
        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" delete-jvm-options ").append("-Xmx512m");
        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" create-jvm-options ").append("-Xmx2g");
        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" restart-domain ").append("domain1");
        command.append(" && tail -n 10000 -F ").append(PATH_DOCKER_GF_DOMAIN1_SERVER_LOG);
        return command.toString();
    }

    private String[] toExecArgs(String commandName, String... arguments) {
        ArrayList<String> command = new ArrayList<>();
        command.add(PATH_DOCKER_ASADMIN);
        command.add("--echo");
        command.add("--terse");
        command.add(commandName);
        if (arguments != null && arguments.length > 0) {
            command.addAll(List.of(arguments));
        }
        return command.toArray(String[]::new);
    }
}
