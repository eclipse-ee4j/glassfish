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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ulimit;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

import java.io.IOException;
import java.lang.System.Logger;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import static java.lang.System.Logger.Level.INFO;

/**
 * Simple GlassFish container based on domain1.
 */
public class GlassFishContainer extends GenericContainer<GlassFishContainer> {
    private static final Logger LOG = System.getLogger(GlassFishContainer.class.getName());
    private static final java.util.logging.Logger LOG_GF = java.util.logging.Logger.getLogger("GF");

    private static final Path PATH_DOCKER_GF_ROOT = Path.of("/opt", "glassfish7");
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
        super("eclipse-temurin:17");
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
            hostConfig.withMemory(16 * 1024 * 1024 * 1024L);
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
     * Execute asadmin command.
     *
     * @param commandName
     * @param arguments
     * @return result. Check the exit code and output.
     */
    public ExecResult asadmin(String commandName, String... arguments) {
        try {
            ExecResult result = execInContainer(toExecArgs(commandName, arguments));
            LOG.log(INFO, "Asadmin STDOUT: {0}", result.getStdout());
            LOG.log(INFO, "Asadmin STDERR: {0}", result.getStderr());
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
        final ClientConfig clientCfg = new ClientConfig();
        clientCfg.register(new JacksonFeature());
        clientCfg.register(new ObjectMapper());
        clientCfg.register(LoggingResponseFilter.class);
        clientCfg.property(ClientProperties.FOLLOW_REDIRECTS, "true");
        final ClientBuilder builder = ClientBuilder.newBuilder().withConfig(clientCfg);
        return builder.build().target(getHttpEndPoint(context));
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
        command.append(" && cd glassfish7/bin && chmod +x asadmin startserv stopserv");
        command.append(" && cd ../glassfish/bin && chmod +x asadmin appclient startserv stopserv");
        command.append(" && mv /*.jar ").append(PATH_DOCKER_GF_DOMAIN.resolve("lib"));
        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" start-domain ").append("domain1");
        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" set-log-levels ").append("org.postgresql.level=FINEST");
        command.append(" && tail -n 10000 -F ").append(PATH_DOCKER_GF_DOMAIN1_SERVER_LOG);
        return command.toString();
    }

    private String[] toExecArgs(String commandName, String... arguments) {
        ArrayList<String> command = new ArrayList<>();
        command.add(PATH_DOCKER_ASADMIN);
        command.add("--echo");
        command.add(commandName);
        if (arguments != null && arguments.length > 0) {
            command.addAll(List.of(arguments));
        }
        return command.toArray(String[]::new);
    }
}
