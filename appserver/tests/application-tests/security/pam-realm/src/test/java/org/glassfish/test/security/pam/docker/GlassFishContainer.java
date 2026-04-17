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

package org.glassfish.test.security.pam.docker;

import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ulimit;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.testcontainers.utility.MountableFile.forHostPath;

public class GlassFishContainer extends GenericContainer<GlassFishContainer> {

    private static final Logger LOG = System.getLogger(GlassFishContainer.class.getName());

    private static final Path PATH_DOCKER_GF_ROOT = Path.of("/opt", "glassfish8");
    private static final String DOMAIN_NAME = "domain1";
    private static final String PATH_DOCKER_ASADMIN = PATH_DOCKER_GF_ROOT.resolve(Path.of("bin", "asadmin")).toString();
    private static final Path PATH_DOCKER_GF_DOMAINS = PATH_DOCKER_GF_ROOT.resolve(Path.of("glassfish", "domains"));
    private static final Path PATH_DOCKER_GF_SERVER_LOG = PATH_DOCKER_GF_DOMAINS.resolve(Path.of(DOMAIN_NAME, "logs", "server.log"));

    private static final String READY_MARKER = "CONTAINER INITIALIZED";

    private final Network network = Network.newNetwork();

    private Client client;

    public GlassFishContainer(String hostname, String logPrefix) {
        super("eclipse-temurin:" + Runtime.version().feature());
        withNetwork(network)
            .withCopyFileToContainer(MountableFile.forClasspathResource("/glassfish.zip"), "/glassfish.zip")
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
                hostConfig.withMemory(2 * 1024 * 1024 * 1024L);
                hostConfig.withMemorySwappiness(0L);
                hostConfig.withUlimits(new Ulimit[] {new Ulimit("nofile", 4096L, 8192L)});
            })
            .withLogConsumer(outputFrame -> {
                System.err.println(logPrefix + ": " + outputFrame.getUtf8StringWithoutLineEnding());
                System.err.flush();
            })
            .withExposedPorts(8080)
            .waitingFor(Wait.forLogMessage(".*" + READY_MARKER + ".*", 1)
                .withStartupTimeout(Duration.ofMinutes(5L)));
    }

    @Override
    public void start() {
        LOG.log(INFO, "Starting docker container ...");
        super.start();
    }

    @Override
    public void stop() {
        LOG.log(INFO, "Stopping docker container ...");
        if (client != null) {
            runQuietly(client::close, "Client close method cause an exception.");
        }
        if (isRunning()) {
            ExecResult result = asadmin("stop-domain", "--kill");
            LOG.log(INFO, "Result: {0}", result.getStdout());
            runQuietly(super::stop, "Container stop method cause an exception.");
        }
        runQuietly(network::close, "Network close method cause an exception.");
    }

    public WebTarget deploy(String appName, File warFile) {
        final String warFileInContainer = "/tmp.war";
        copyFileToContainer(forHostPath(warFile.toPath()), warFileInContainer);
        try {
            final ExecResult result = asadmin("deploy",
                "--contextroot", appName, "--name", appName, warFileInContainer);
            assertThat("deploy response", result.getStdout(),
                stringContainsInOrder("Application deployed with name " + appName));
            return getRestClient(appName);
        } finally {
            try {
                execInContainer("rm", warFileInContainer);
            } catch (UnsupportedOperationException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ExecResult asadmin(String command, String... arguments) {
        try {
            ExecResult result = execInContainer(toExecArgs(command, arguments));
            LOG.log(INFO, "Asadmin STDOUT: \n{0}", result.getStdout());
            LOG.log(INFO, "Asadmin STDERR: \n{0}", result.getStderr());
            return result;
        } catch (UnsupportedOperationException | IOException | InterruptedException e) {
            throw new RuntimeException(
                "Execution failed for command " + command + " with arguments: " + String.join(" ", arguments), e);
        }
    }

    private WebTarget getRestClient(String context) {
        client = ClientBuilder.newClient();
        return client.target(getHttpEndpoint(context));
    }

    private URI getHttpEndpoint(String context) {
        return URI.create("http://" + getHost() + ":" + getMappedPort(8080) + "/" + context);
    }

    private static String getCommandAdmin() {
        final StringBuilder command = new StringBuilder();
        command.append("echo \"***************** Starting the GlassFish's domain1 *****************\"");
        command.append(" && set -x && set -e");
        command.append(" && export LANG=\"en_US.UTF-8\" && export LANGUAGE=\"en_US.UTF-8\"");
        command.append(" && (env | sort) && locale");
        command.append(" && ulimit -a");
        command.append(" && cat /etc/hosts && cat /etc/resolv.conf");
        command.append(" && hostname");
        command.append(" && useradd -m user && echo \"user:password\" | chpasswd");
        command.append(" && getent passwd user");
        command.append(" && java -version");
        command.append(" && mkdir -p /opt");
        command.append(" && cd /opt");
        command.append(" && jar xf /glassfish.zip");
        command.append(" && cd glassfish8/bin && chmod +x asadmin startserv stopserv");
        command.append(" && cd ../glassfish/bin && chmod +x asadmin appclient startserv stopserv");
        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" start-domain");
        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" create-auth-realm")
            .append(" --classname com.sun.enterprise.security.ee.authentication.glassfish.pam.PamRealm")
            .append(" --property jaas-context=pamRealm ")
            .append("pam");
        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" list-auth-realms");
        command.append(" && echo '" + READY_MARKER + "'");
        command.append(" && tail -n 10000 -F ").append(PATH_DOCKER_GF_SERVER_LOG);
        return command.toString();
    }

    private String[] toExecArgs(String command, String... arguments) {
        List<String> execArgs = new ArrayList<>();
        execArgs.add(PATH_DOCKER_ASADMIN);
        execArgs.add("--echo=true");
        execArgs.add(command);
        if (arguments != null && arguments.length > 0) {
            execArgs.addAll(List.of(arguments));
        }
        return execArgs.toArray(String[]::new);
    }

    private void runQuietly(Runnable action, String errorMessage) {
        try {
            action.run();
        } catch (Exception e) {
            LOG.log(WARNING, errorMessage, e);
        }
    }
}
