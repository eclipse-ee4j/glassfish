/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.clusterssh;

import java.io.File;
import java.lang.System.Logger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;

import org.glassfish.main.test.clusterssh.docker.GlassFishContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.testcontainers.containers.BindMode.READ_ONLY;
import static org.testcontainers.utility.MountableFile.forHostPath;

@DisabledOnOs(OS.WINDOWS)
@TestMethodOrder(OrderAnnotation.class)
public class SshClusterITest {

    private static final Logger LOG = System.getLogger(SshClusterITest.class.getName());

    private static final String MSG_NODE_STARTED = "NODE STARTED!";

    private static final String DOMAIN_NAME = "domain1";

    private static final Path PATH_DOCKER_GF_ROOT = Path.of("/opt", "glassfish8");
    private static final Path PATH_DOCKER_GF_DOMAINS = PATH_DOCKER_GF_ROOT.resolve(Path.of("glassfish", "domains"));
    private static final Path PATH_DOCKER_GF_NODES = PATH_DOCKER_GF_ROOT.resolve(Path.of("glassfish", "nodes"));
    private static final Path PATH_DOCKER_GF_DOMAIN1_SERVER_LOG = PATH_DOCKER_GF_DOMAINS
        .resolve(Path.of(DOMAIN_NAME, "logs", "server.log"));
    private static final Path PATH_DOCKER_GF_NODE1_SERVER_LOG = PATH_DOCKER_GF_NODES
        .resolve(Path.of("node1", "agent", "logs", "server.log"));
    private static final String PATH_DOCKER_ASADMIN = PATH_DOCKER_GF_ROOT.resolve(Path.of("bin", "asadmin")).toString();

    private static final String PATH_ETC_ENVIRONMENT = "/etc/environment";
    private static final String PATH_SSH_USERDIR = "/root/.ssh";
    private static final String PATH_PRIVATE_KEY = PATH_SSH_USERDIR + "/id_rsa";
    private static final String PATH_SSHD_CFG = "/etc/ssh/sshd_config";
    private static final String PATH_SSHD_LOG = "/var/log/sshd.log";

    @TempDir
    private static File tmpDir;

    /** Docker network */
    private static Network network = Network.newNetwork();

    @SuppressWarnings("resource")
    private static final GlassFishContainer AS_DOMAIN = new GlassFishContainer(network, "admin", "A", getCommandAdmin())
        .withCopyFileToContainer(MountableFile.forClasspathResource("/glassfish.zip"), "/glassfish.zip")
        .withClasspathResourceMapping("password_update.txt", "/password_update.txt", READ_ONLY)
        .withClasspathResourceMapping("password.txt", "/password.txt", READ_ONLY)
        .withExposedPorts(4848)
        .withAsTrace(false)
        .waitingFor(
            Wait.forLogMessage(".*Total startup time including CLI.*", 1).withStartupTimeout(Duration.ofSeconds(60L)));

    @SuppressWarnings("resource")
    private static final GlassFishContainer AS_NODE_1 = new GlassFishContainer(network, "node1", "N1", getCommandNode())
        .withAsTrace(false)
        .withExposedPorts(22, 4848, 8080)
        .waitingFor(
            Wait.forLogMessage(".*" + MSG_NODE_STARTED + ".*", 1).withStartupTimeout(Duration.ofSeconds(60L)));

    @BeforeAll
    public static void start() throws Exception {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is not available on this environment");
        AS_DOMAIN.start();
        ExecResult keygenResult = AS_DOMAIN.execInContainer(UTF_8, "ssh-keygen", "-b", "4096",
            "-t", "rsa", "-f", PATH_PRIVATE_KEY, "-q", "-N", "");
        assertEquals(0, keygenResult.getExitCode(), keygenResult.getStdout() + keygenResult.getStderr());
        File pubKey = new File(tmpDir, "adminkey.pub");
        AS_DOMAIN.copyFileFromContainer(PATH_SSH_USERDIR + "/id_rsa.pub", pubKey.getAbsolutePath());
        AS_NODE_1.withCopyFileToContainer(forHostPath(pubKey.getAbsolutePath()), "/" + pubKey.getName());
        AS_NODE_1.start();
    }

    @AfterAll
    public static void stop() throws Exception {
        LOG.log(INFO, "Closing docker containers ...");
        if (AS_NODE_1.isRunning()) {
            ExecResult result = AS_DOMAIN.execInContainer(PATH_DOCKER_ASADMIN, "stop-node", "--kill",
                "node1");
            LOG.log(INFO, "Result: {0}", result.getStdout());
            closeSilently(AS_NODE_1);
        }
        if (AS_DOMAIN.isRunning()) {
            ExecResult result = AS_DOMAIN.execInContainer(PATH_DOCKER_ASADMIN, "stop-domain", "--kill");
            LOG.log(INFO, "Result: {0}", result.getStdout());
            closeSilently(AS_DOMAIN);
        }
        closeSilently(network);
    }


    /**
     * First verify the we can connect using command line ssh and just execute something.
     * This is to prove that the server setting is all right for ssh and that it is possible to
     * connect with standard SSH client.
     *
     * @throws Exception
     */
    @Test
    @Order(1)
    public void ssh() throws Exception {
        ExecResult sshResult = AS_DOMAIN.execInContainer(UTF_8,
            // Under some circumstances it can stuck -> now it has 5 seconds.
            "timeout", "5",
            // It always asks for a passphrase so we have to specify it even if it is empty.
            "sshpass", /*"-v",*/ "-P", "passphrase", "-p", "",
            "ssh", /*"-vvvvv",*/
            // Not recommended on production but useful here. Accept server's pub key as trusted.
            "-o", "StrictHostKeyChecking=accept-new",
            "-o", "KbdInteractiveAuthentication=no",
            "-o", "PasswordAuthentication=no",
            "-i", PATH_PRIVATE_KEY, "root@node1",
            // Execute some command on the server which will create a log visible in the output.
            "echo 'I am there!' >> " + PATH_SSHD_LOG);
        assertEquals(0, sshResult.getExitCode(), () -> sshResult.getStdout() + sshResult.getStderr());
    }


    @Test
    @Order(2)
    public void createNode1() throws Exception {
        ExecResult result = AS_DOMAIN.execInContainer(UTF_8, PATH_DOCKER_ASADMIN, "--user", "admin",
            "--passwordfile", "/password.txt", "create-node-ssh", "--nodehost", "node1", "--install", "true",
            "--sshkeyfile", PATH_PRIVATE_KEY, "--sshuser", "root",
            "node1");
        assertEquals(0, result.getExitCode(), result.getStdout() + result.getStderr());
    }


    @Test
    @Order(10)
    @Disabled("Not finished yet")
    void getRootOfNode1() throws Exception {
        URL url = URI.create("http://localhost:" + AS_DOMAIN.getMappedPort(4848) + "/").toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("GET");
            assertEquals(200, connection.getResponseCode(), "Response code");
        } finally {
            connection.disconnect();
        }
    }

    private static String getCommandAdmin() {
        final StringBuilder command = new StringBuilder();
        command.append("echo \"***************** Useful informations about admin domain *****************\"");
        command.append(" && set -x && set -e");
        command.append(" && export LANG=\"en_US.UTF-8\"").append(" && export LANGUAGE=\"en_US.UTF-8\"");
        command.append(" && (env | sort) && locale");
        command.append(" && ulimit -a");
        command.append(" && cat /etc/hosts && cat /etc/resolv.conf");
        command.append(" && hostname");
        command.append(" && java -version");
        command.append(" && apt-get update && apt-get install -y unzip openssh-client sshpass");
        command.append(" && unzip -q /glassfish.zip -d /opt");
        command.append(" && mkdir -p " + PATH_SSH_USERDIR);
        command.append(" && touch " + PATH_SSH_USERDIR + "/known_hosts");
        command.append(getCommandCreatePrivateDir(PATH_SSH_USERDIR));
        command.append(" && ls -la ").append(PATH_DOCKER_GF_ROOT);
        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" start-domain ").append("domain1");
        command.append(" && ").append(PATH_DOCKER_ASADMIN)
            .append(" --user admin --passwordfile /password_update.txt change-admin-password");
        command.append(" && ").append(PATH_DOCKER_ASADMIN)
            .append(" --user admin --passwordfile /password.txt enable-secure-admin");
        command.append(" && ").append(PATH_DOCKER_ASADMIN).append(" restart-domain");
        command.append(" && tail -n 10000 -F ").append(PATH_DOCKER_GF_DOMAIN1_SERVER_LOG);
        return command.toString();
    }

    private static String getCommandNode() {
        final StringBuilder command = new StringBuilder();
        command.append("echo \"***************** Useful informations about node1 *****************\"");
        command.append(" && set -x && set -e");

        // Replace the original which did not mention java -> affects ssh clients
        command.append(" && echo \"JAVA_HOME=${JAVA_HOME}\" > " + PATH_ETC_ENVIRONMENT);
        command.append(" && echo \"PATH=${PATH}\" >> " + PATH_ETC_ENVIRONMENT);

        command.append(" && apt-get update && apt-get install -y unzip openssh-server");
        command.append(" && echo 'PermitRootLogin prohibit-password' > " + PATH_SSHD_CFG);
        command.append(" && echo 'PasswordAuthentication no' >> " + PATH_SSHD_CFG);
        command.append(" && echo 'PubkeyAuthentication yes' >> " + PATH_SSHD_CFG);
        command.append(" && echo 'ChallengeResponseAuthentication no' >> " + PATH_SSHD_CFG);
        // UsePAM no would mean that the /etc/environment file would not be used.
        command.append(" && echo 'UsePAM yes' >> " + PATH_SSHD_CFG);
        command.append(" && echo 'AllowUsers root' >> " + PATH_SSHD_CFG);
        command.append(" && echo 'LogLevel INFO' >> " + PATH_SSHD_CFG);
        command.append(" && echo 'Subsystem sftp /usr/lib/openssh/sftp-server' >> " + PATH_SSHD_CFG);
        command.append(" && cat " + PATH_SSHD_CFG);

        // Bug in sshd - doesn't create it automatically
        command.append(getCommandCreatePrivateDir("/var/run/sshd"));
        // The directory must exist to create the file. The content must be private.
        command.append(" && mkdir -p /root/.ssh");
        command.append(" && cat /adminkey.pub >> /root/.ssh/authorized_keys");
        command.append(getCommandCreatePrivateDir(PATH_SSH_USERDIR));
        command.append(" && /usr/sbin/sshd -E " + PATH_SSHD_LOG);
        command.append(" && sleep 1");
        command.append(" && ps -lAf");
        command.append(" && echo \"" + MSG_NODE_STARTED + "\"");
        command.append(" && tail -n 10000 -F ").append(PATH_SSHD_LOG).append(' ')
            .append(PATH_DOCKER_GF_NODE1_SERVER_LOG);
        return command.toString();
    }

    private static StringBuilder getCommandCreatePrivateDir(String path) {
        StringBuilder command = new StringBuilder();
        command.append(" && mkdir -p ").append(path);
        command.append(" && chmod -R go-rwx ").append(path);
        command.append(" && chown -R root:root ").append(path);
        return command;
    }

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
