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

package com.sun.enterprise.util.net;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.testcontainers.utility.MountableFile.forHostPath;

/**
 * This test covers {@link NetUtils} behavior on systems with several /etc/hosts configurations.
 *
 * @author David Matejcek
 */
public class NetUtilsIT {

    private static final String METHOD_GET_HOST_NAME = "getHostName";
    private static final String HOSTNAME = "docker001";
    private static final String COMMAND_SUFFIX =
        "echo \"hostname: $(hostname)\n\""
         + " && echo \"/etc/hosts:\n$(cat /etc/hosts)\n\""
         + " && java -cp /app/lib.jar /app/ContainerClass.java"
    ;
    private static final String CONTAINER_CLASS_TEMPLATE = """
        import java.net.InetAddress;
        import java.net.NetworkInterface;
        import java.util.Collections;
        import java.util.stream.Collectors;

        public class ContainerClass {

            public static void main(String[] args) throws Exception {
                try {
                    System.out.println("JVM, Localhost: " + InetAddress.getLocalHost());
                } catch (Exception e) {
                    System.out.println("JVM, Localhost: Failed to resolve localhost: " + e);
                }
                System.out.println("JVM, LoopbackAddress: " + InetAddress.getLoopbackAddress());
                System.out.println("JVM, Known local addresses: "
                    + Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                        .flatMap(ni -> Collections.list(ni.getInetAddresses()).stream())
                        .map(ContainerClass::toString)
                        .collect(Collectors.joining(", ")));
                System.out.println("RESULT0: " + com.sun.enterprise.util.net.NetUtils.getHostName());
                System.out.println("RESULT_isPortFree: " + com.sun.enterprise.util.net.NetUtils.isPortFree(4848));
                System.out.println("RESULT_checkPort: " + com.sun.enterprise.util.net.NetUtils.checkPort(4848));
                System.out.println("RESULT_isLocal: " + com.sun.enterprise.util.net.NetUtils.METHOD_ISLOCAL);
                System.out.println("RESULT_isSameHost: " + com.sun.enterprise.util.net.NetUtils.METHOD_ISSAMEHOST);
                System.out.println("DONE.");
            }

            private static String toString(InetAddress address) {
                try {
                    return address.getCanonicalHostName()
                        + '/' + address.getHostAddress()
                        + '[' + (address.isReachable(100) ? "reachable" : "unreachable") + ']';
                } catch (Exception e) {
                    return address.toString();
                }
            }
        }
        """;

    private GenericContainer<?> container;

    @BeforeAll
    static void checkTcSupport() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is not available");
    }

    @BeforeEach
    void logTest(TestInfo test) {
        System.out.println("Running test: " + test.getDisplayName());
    }

    @AfterEach
    void stopContainer() {
        if (container != null) {
            container.close();
            container = null;
        }
    }

//    JVM, Localhost: docker001/172.17.0.4
//    JVM, LoopbackAddress: localhost/127.0.0.1
//    JVM, Known local addresses: docker001/172.17.0.4[reachable], localhost/0:0:0:0:0:0:0:1%lo[reachable], localhost/127.0.0.1[reachable]
//    RESULT0: docker001
    /**
     * The hostname docker001 is mapped to docker virtual network interface by default.
     * Loopback (localhost name) is mapped to 127.0.0.1, but can use also ::1 which has lower priority.
     */
    @Test
    void defaultHosts() throws Exception {
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, HOSTNAME);
        String containerClass = filterMethodIsSameHost(containerClass0, "localhost", HOSTNAME);
        container = createContainer(containerClass);
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true")),
            () -> assertThat(getResultForIsSameHost(logs), equalTo("false")) // each resolves to different addresses
        );
    }

//    JVM, Localhost: docker001/127.0.0.1
//    JVM, LoopbackAddress: localhost/127.0.0.1
//    JVM, Known local addresses: docker001/172.17.0.4[reachable], localhost/0:0:0:0:0:0:0:1%lo[reachable], localhost/127.0.0.1[reachable]
//    RESULT0: docker001
    @Test
    void extraHostnameVsLoopback() throws Exception {
        // FIXME: Controversial: known addresses contain docker001/172.17.0.4[reachable]
        //        but getLocalHost returns docker001/127.0.0.1
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, HOSTNAME);
        String containerClass = filterMethodIsSameHost(containerClass0, "localhost", HOSTNAME);
        container = createContainer(containerClass)
            .withExtraHost(HOSTNAME, "::1") // IPv6 localhost
            .withExtraHost(HOSTNAME, "127.0.0.1") // usual localhost
        ;
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true"))
        );
    }

 // Original:
//    JVM, Localhost: docker001/127.0.0.1
//    JVM, LoopbackAddress: localhost/127.0.0.1
//    JVM, Known local addresses: docker001/172.17.0.4[reachable], localhost/0:0:0:0:0:0:0:1%lo[reachable], localhost/127.0.0.1[reachable]
//    RESULT0: docker001
    @Test
    void extraHostnameUnreachableIpVsLoopback() throws Exception {
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, HOSTNAME);
        String containerClass = filterMethodIsSameHost(containerClass0, "localhost", HOSTNAME);
        container = createContainer(containerClass)
            .withExtraHost(HOSTNAME, "127.0.0.13") // unreachable IP
            .withExtraHost(HOSTNAME, "::1") // IPv6 localhost
            .withExtraHost(HOSTNAME, "127.0.0.1") // usual localhost
            .withExtraHost("dockerhost", "172.17.0.1") // remote host, not interesting for JVM.
        ;
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true"))
        );
    }

// Original:
//    JVM, Localhost: docker001/127.0.0.13
//    JVM, LoopbackAddress: localhost/127.0.0.1
//    JVM, Known local addresses: docker001/172.17.0.4[reachable], localhost/0:0:0:0:0:0:0:1%lo[reachable], localhost/127.0.0.1[reachable]
//    RESULT0: docker001
    @Test
    void extraHostnameUnreachableIP() throws Exception {
        // FIXME: There is another record of the hostname and correct IP, can we reach it?
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, HOSTNAME);
        String containerClass = filterMethodIsSameHost(containerClass0, "localhost", HOSTNAME);
        container = createContainer(containerClass)
            .withExtraHost(HOSTNAME, "127.0.0.13") // unreachable IP
        ;
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true"))
        );
    }

// Original:
//    JVM, Localhost: docker001/172.17.0.4
//    JVM, LoopbackAddress: localhost/127.0.0.1
//    JVM, Known local addresses: docker001/172.17.0.4[reachable], localhost/0:0:0:0:0:0:0:1%lo[reachable], localhost/127.0.0.1[reachable]
//    RESULT0: docker001
    /** localhost should not be mapped to an IP, but we made that, however the mapping has lower priority */
    @Test
    void extraBadLocalhostMapping() throws Exception {
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, HOSTNAME);
        String containerClass = filterMethodIsSameHost(containerClass0, "localhost", HOSTNAME);
        container = createContainer(containerClass).withExtraHost("localhost", "127.0.0.100");
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true"))
        );
    }

// Original:
//    JVM, Localhost: Failed to resolve localhost: java.net.UnknownHostException: docker001: docker001: Name or service not known
//    JVM, LoopbackAddress: localhost/127.0.0.1
//    JVM, Known local addresses: 172.17.0.4/172.17.0.4[reachable], 0:0:0:0:0:0:0:1%lo/0:0:0:0:0:0:0:1%lo[reachable], 127.0.0.1/127.0.0.1[reachable]
//    RESULT0: null
    @Test
    void emptyHosts() throws Exception {
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, "127.0.0.1");
        String containerClass = filterMethodIsSameHost(containerClass0, "localhost", "127.0.0.1");
        container = createContainerWitHosts("", containerClass);
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo("localhost")),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true"))
        );
    }

// Original:
//    JVM, Localhost: Failed to resolve localhost: java.net.UnknownHostException: docker001: docker001: Name or service not known
//    JVM, LoopbackAddress: localhost/127.0.0.1
//    JVM, Known local addresses: 172.17.0.4/172.17.0.4[reachable], localhost/0:0:0:0:0:0:0:1%lo[reachable], 127.0.0.1/127.0.0.1[reachable]
//    RESULT0: localhost
    @Test
    void justLoopback() throws Exception {
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, "localhost");
        String containerClass = filterMethodIsSameHost(containerClass0, "localhost", HOSTNAME);
        container = createContainerWitHosts("::1 localhost", containerClass);
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo("localhost")),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true"))
        );
    }

// Original:
//    JVM, Localhost: docker001/0:0:0:0:0:0:0:1
//    JVM, LoopbackAddress: localhost/127.0.0.1
//    JVM, Known local addresses: 172.17.0.4/172.17.0.4[reachable], docker001/0:0:0:0:0:0:0:1%lo[reachable], 127.0.0.1/127.0.0.1[reachable]
//    RESULT0: docker001
    @Test
    void justHost() throws Exception {
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, HOSTNAME);
        String containerClass = filterMethodIsSameHost(containerClass0, "localhost", HOSTNAME);
        container = createContainerWitHosts("::1 " + HOSTNAME, containerClass);
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true"))
        );
    }

// Original:
//    JVM, Localhost: Failed to resolve localhost: java.net.UnknownHostException: docker001: docker001: Name or service not known
//    JVM, LoopbackAddress: localhost/127.0.0.1
//    JVM, Known local addresses: 172.17.0.4/172.17.0.4[reachable], 0:0:0:0:0:0:0:1%lo/0:0:0:0:0:0:0:1%lo[reachable], 127.0.0.1/127.0.0.1[reachable]
// Another variant - docker can use host's DNS!
//    JVM, Known local addresses: 172.17.0.6/172.17.0.6[reachable], netcts.cdn-apple.com/0:0:0:0:0:0:0:1%lo[reachable], localhost.lan/127.0.0.1[reachable]
//    RESULT0: localhost
    @Test
    void hostnameUnreachable() throws Exception {
        String hosts = HOSTNAME + " 127.0.0.13\n" + HOSTNAME + " ::1\n"+ HOSTNAME + " 127.0.0.1\n" + "dockerhost 172.17.0.1";
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, "dockerhost");
        String containerClass = filterMethodIsSameHost(containerClass0, "localhost", HOSTNAME);
        container = createContainerWitHosts(hosts, containerClass);
        container.start();
        final String[] logs = getLogs(container);
        // FIXME: Controversial: why prefer localhost and not the HOSTNAME with the same IP?
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo("localhost")),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("false"))
        );
    }

    private GenericContainer<?> createContainerWitHosts(String hostFileContent, String containerClass) throws IOException {
        String command = "echo \"" + hostFileContent + "\" > /etc/hosts && " + COMMAND_SUFFIX;
        return createContainer(command, containerClass);
    }

    private GenericContainer<?> createContainer(String containerClass) throws IOException {
        return createContainer(COMMAND_SUFFIX, containerClass);
    }

    // @AfterEach does it.
    @SuppressWarnings("resource")
    private GenericContainer<?> createContainer(String bashCommand, String classFileContent) throws IOException {
        final Path classFilePath = Files.writeString(Files.createTempFile("ContainerClass", ".java"), classFileContent);
        return new GenericContainer<>("eclipse-temurin:" + Runtime.version().feature())
            .withCopyFileToContainer(forHostPath(System.getProperty("jarFilePath")), "/app/lib.jar")
            .withCopyFileToContainer(forHostPath(classFilePath),
                "/app/ContainerClass.java")
            .withCommand("sh", "-c", bashCommand)
            .withCreateContainerCmdModifier(cmd -> cmd.withHostName(HOSTNAME))
            .waitingFor(Wait.forLogMessage(".*DONE.*", 1).withStartupTimeout(Duration.ofSeconds(5L)));
    }

    private static String filterMethodIsLocal(String template, String address) {
        return filterClassBody(template, "METHOD_ISLOCAL", "isLocal", "\"" + address + "\"");
    }

    private static String filterMethodIsSameHost(String template, String host1, String host2) {
        return filterClassBody(template, "METHOD_ISSAMEHOST", "isSameHost", "\"" + host1 + "\"", "\"" + host2 + "\"");
    }

    private static String filterClassBody(String template, String methodCallKey, String methodName, String... parameters) {
        String methodCall = methodName + "(" + String.join(", ", parameters) + ")";
        return template.replace(methodCallKey, methodCall);
    }

    private static String[] getLogs(GenericContainer<?> container) {
        return container.getLogs().split("\\R");
    }

    private static String getResultForGetHostName(String[] logs) {
        return getResult("RESULT0: ", logs);
    }

    private static String getResultForIsPortFree(String[] logs) {
        return getResult("RESULT_isPortFree: ", logs);
    }

    private static String getResultForCheckPort(String[] logs) {
        return getResult("RESULT_checkPort: ", logs);
    }

    private static String getResultForIsLocal(String[] logs) {
        return getResult("RESULT_isLocal: ", logs);
    }

    private static String getResultForIsSameHost(String[] logs) {
        return getResult("RESULT_isSameHost: ", logs);
    }

    private static String getResult(String resultPrefix, String[] logs) {
        for (String line : logs) {
            if (line.startsWith(resultPrefix)) {
                return line.substring(resultPrefix.length());
            }
        }
        return fail("The method call failed. Returned output: \n" + String.join("\n", logs));
    }
}
