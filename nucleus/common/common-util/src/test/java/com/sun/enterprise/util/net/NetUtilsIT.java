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
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.arrayWithSize;
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

    private static final String LOCALHOST = "localhost";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1%lo";

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
        import java.util.List;

        public class ContainerClass {

            public static void main(String[] args) throws Exception {
                try {
                    System.out.println("JVM, InetAddress.getLocalhost(): " + toString(InetAddress.getLocalHost()));
                } catch (Exception e) {
                    System.out.println("JVM, InetAddress.getLocalhost() failed: " + e.getMessage());
                }
                System.out.println("JVM, LoopbackAddress: " + InetAddress.getLoopbackAddress());
                System.out.println("JVM, Known local addresses: "
                    + Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                        .flatMap(ni -> Collections.list(ni.getInetAddresses()).stream())
                        .map(ContainerClass::toString)
                        .collect(Collectors.joining(", ")));
                System.out.println("RESULT_getHostName: " + com.sun.enterprise.util.net.NetUtils.getHostName());
                System.out.println("RESULT_getCanonicalHostName: " + com.sun.enterprise.util.net.NetUtils.getCanonicalHostName());
                System.out.println("RESULT_getHostAddresses: " + ipAddressessToString(com.sun.enterprise.util.net.NetUtils.getHostAddresses()));
                System.out.println("RESULT_getResolvableHostNames: " + listToString(com.sun.enterprise.util.net.NetUtils.getResolvableHostNames()));
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

            private static String ipAddressessToString(List<InetAddress> addresses) {
                return addresses.stream().map(ContainerClass::toString).collect(Collectors.joining(", "));
            }

            private static String listToString(List<?> objects) {
                return objects.stream().map(String::valueOf).collect(Collectors.joining(", "));
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

    /**
     * The hostname docker001 is mapped to docker virtual network interface by default.
     * Loopback (localhost name) is mapped to 127.0.0.1, but can use also ::1 which has lower priority.
     */
    @Test
    void defaultHosts() throws Exception {
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, HOSTNAME);
        String containerClass = filterMethodIsSameHost(containerClass0, LOCALHOST, HOSTNAME);
        container = createContainer(containerClass);
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetCanonicalHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetHostAddresses(logs),
                arrayContainingInAnyOrder(
                    equalTo(getContainerHostAddressInfo(HOSTNAME)),
                    equalTo("localhost/127.0.0.1[reachable]"),
                    equalTo("localhost/0:0:0:0:0:0:0:1%lo[reachable]"))),
            () -> assertThat(getResultForGetResolvableHostNames(logs), arrayContaining(HOSTNAME, LOCALHOST)),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true")),
            () -> assertThat(getResultForIsSameHost(logs), equalTo("false")) // each resolves to different addresses
        );
    }

    @Test
    void emptyHosts() throws Exception {
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, LOCALHOST_IPV4);
        String containerClass = filterMethodIsSameHost(containerClass0, LOCALHOST, LOCALHOST_IPV4);
        container = createContainerWitHosts("", containerClass);
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetCanonicalHostName(logs), equalTo(LOCALHOST)),
            () -> assertThat(getResultForGetHostAddresses(logs),
                arrayContainingInAnyOrder(
                    equalTo(getContainerHostAddressInfo()),
                    equalTo(LOCALHOST_IPV4 + "/" + LOCALHOST_IPV4 + "[reachable]"),
                    equalTo("0:0:0:0:0:0:0:1%lo/0:0:0:0:0:0:0:1%lo[reachable]"))),
            () -> assertThat(getResultForGetResolvableHostNames(logs), arrayWithSize(0)),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true")),
            () -> assertThat(getResultForIsSameHost(logs), equalTo("true"))
        );
    }

    @Test
    void extraHostnameVsLoopback() throws Exception {
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, HOSTNAME);
        String containerClass = filterMethodIsSameHost(containerClass0, LOCALHOST, HOSTNAME);
        container = createContainer(containerClass)
            .withExtraHost(HOSTNAME, "::1") // IPv6 localhost
            .withExtraHost(HOSTNAME, LOCALHOST_IPV4) // usual localhost
        ;
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetCanonicalHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetHostAddresses(logs),
                arrayContainingInAnyOrder(
                    equalTo(getContainerHostAddressInfo(HOSTNAME)),
                    equalTo(getLocalhostAddressInfoV4()),
                    equalTo(getLocalhostAddressInfoV6()))),
            () -> assertThat(getResultForGetResolvableHostNames(logs), arrayContaining(HOSTNAME, LOCALHOST)),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true")),
            () -> assertThat(getResultForIsSameHost(logs), equalTo("true"))
        );
    }

    @Test
    void extraHostnameUnreachableIpVsLoopback() throws Exception {
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, HOSTNAME);
        String containerClass = filterMethodIsSameHost(containerClass0, LOCALHOST, LOCALHOST_IPV6);
        container = createContainer(containerClass)
            .withExtraHost(HOSTNAME, "127.0.0.13") // unreachable IP
            .withExtraHost(HOSTNAME, "::1") // IPv6 localhost
            .withExtraHost(HOSTNAME, LOCALHOST_IPV4) // usual localhost
            .withExtraHost("dockerhost", "172.17.0.1") // remote host, not interesting for JVM.
        ;
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetCanonicalHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetHostAddresses(logs),
                arrayContainingInAnyOrder(
                    equalTo(getContainerHostAddressInfo(HOSTNAME)),
                    equalTo(getHostAddressInfo(HOSTNAME, "127.0.0.13", true)),
                    equalTo(getLocalhostAddressInfoV4()),
                    equalTo(getLocalhostAddressInfoV6()))),
            () -> assertThat(getResultForGetResolvableHostNames(logs), arrayContaining(HOSTNAME, LOCALHOST)),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true")),
            () -> assertThat(getResultForIsSameHost(logs), equalTo("false")) // localhost resolves to IPv4, not IPv6
        );
    }

    @Test
    void extraHostnameUnreachableIP() throws Exception {
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, HOSTNAME);
        String containerClass = filterMethodIsSameHost(containerClass0, LOCALHOST, HOSTNAME);
        container = createContainer(containerClass)
            .withExtraHost(HOSTNAME, "192.168.254.254") // unreachable IP
        ;
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetCanonicalHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetHostAddresses(logs),
                    arrayContainingInAnyOrder(
                        equalTo(getContainerHostAddressInfo(HOSTNAME)),
                        equalTo(getHostAddressInfo(HOSTNAME, "192.168.254.254", false)),
                        equalTo(getLocalhostAddressInfoV4()),
                        equalTo(getLocalhostAddressInfoV6()))),
            () -> assertThat(getResultForGetResolvableHostNames(logs), arrayContaining(HOSTNAME, LOCALHOST)),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true")),
            () -> assertThat(getResultForIsSameHost(logs), equalTo("false")) // each resolves to different addresses
        );
    }

    /** localhost should not be mapped to an IP, but we made that, however the mapping has lower priority */
    @Test
    void extraBadLocalhostMapping() throws Exception {
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, HOSTNAME);
        String containerClass = filterMethodIsSameHost(containerClass0, LOCALHOST, HOSTNAME);
        container = createContainer(containerClass).withExtraHost(LOCALHOST, "127.0.0.100");
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetCanonicalHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetHostAddresses(logs),
                arrayContainingInAnyOrder(
                    equalTo(getContainerHostAddressInfo(HOSTNAME)),
                    equalTo(getLocalhostAddressInfoV4()),
                    equalTo(getHostAddressInfo(LOCALHOST, "127.0.0.100", true)),
                    equalTo(getLocalhostAddressInfoV6()))),
            () -> assertThat(getResultForGetResolvableHostNames(logs), arrayContaining(HOSTNAME, LOCALHOST)),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true")),
            () -> assertThat(getResultForIsSameHost(logs), equalTo("false"))
        );
    }

    @Test
    void justIpv6Loopback() throws Exception {
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, LOCALHOST);
        String containerClass = filterMethodIsSameHost(containerClass0, LOCALHOST, HOSTNAME);
        container = createContainerWitHosts("::1 localhost", containerClass);
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetCanonicalHostName(logs), equalTo(LOCALHOST)),
            () -> assertThat(getResultForGetHostAddresses(logs),
                arrayContainingInAnyOrder(
                    equalTo(getContainerHostAddressInfo()),
                    equalTo(getHostAddressInfo(LOCALHOST, LOCALHOST_IPV6, true)),
                    equalTo(getHostAddressInfo(LOCALHOST_IPV4, LOCALHOST_IPV4, true)))),
            () -> assertThat(getResultForGetResolvableHostNames(logs), arrayContaining(LOCALHOST)),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true")),
            () -> assertThat(getResultForIsSameHost(logs), equalTo("false")) // each resolves to different addresses
        );
    }

    @Test
    void justIpv6Host() throws Exception {
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, HOSTNAME);
        String containerClass = filterMethodIsSameHost(containerClass0, LOCALHOST, HOSTNAME);
        container = createContainerWitHosts("::1 " + HOSTNAME, containerClass);
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetCanonicalHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetHostAddresses(logs),
                arrayContainingInAnyOrder(
                    equalTo(getContainerHostAddressInfo()),
                    equalTo(getHostAddressInfo(HOSTNAME, LOCALHOST_IPV6, true)),
                    equalTo(getHostAddressInfo(LOCALHOST_IPV4, LOCALHOST_IPV4, true)))),
            () -> assertThat(getResultForGetResolvableHostNames(logs), arrayContaining(HOSTNAME)),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("true")),
            () -> assertThat(getResultForIsSameHost(logs), equalTo("false")) // each resolves to different addresses
        );
    }

    @Test
    void hostnameUnreachable() throws Exception {
        String hosts = "127.0.0.13 " + HOSTNAME + "\n::1 " + HOSTNAME + "\n127.0.0.1 " + HOSTNAME
            + "\n172.17.0.1 dockerhost";
        String containerClass0 = filterMethodIsLocal(CONTAINER_CLASS_TEMPLATE, "dockerhost");
        String containerClass = filterMethodIsSameHost(containerClass0, LOCALHOST, HOSTNAME);
        container = createContainerWitHosts(hosts, containerClass);
        container.start();
        final String[] logs = getLogs(container);
        assertAll(container.getLogs(),
            () -> assertThat(getResultForGetHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetCanonicalHostName(logs), equalTo(HOSTNAME)),
            () -> assertThat(getResultForGetHostAddresses(logs),
                arrayContainingInAnyOrder(
                    equalTo(getContainerHostAddressInfo()),
                    equalTo(getHostAddressInfo(HOSTNAME, LOCALHOST_IPV4, true)),
                    equalTo(getHostAddressInfo(HOSTNAME, "127.0.0.13", true)),
                    equalTo(getHostAddressInfo(HOSTNAME, LOCALHOST_IPV6, true)))),
            () -> assertThat(getResultForGetResolvableHostNames(logs), arrayContaining(HOSTNAME)),
            () -> assertThat(getResultForIsPortFree(logs), equalTo("true")),
            () -> assertThat(getResultForCheckPort(logs), equalTo("OK")),
            () -> assertThat(getResultForIsLocal(logs), equalTo("false")),
            () -> assertThat(getResultForIsSameHost(logs), equalTo("true"))
        );
    }

    private GenericContainer<?> createContainerWitHosts(String hostFileContent, String containerClass) throws IOException {
        String command = "echo \"" + hostFileContent + "\" > /etc/hosts && echo \"hosts: files\" > /etc/nsswitch.conf && " + COMMAND_SUFFIX;
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
            .withCopyFileToContainer(forHostPath(classFilePath), "/app/ContainerClass.java")
            .withCommand("sh", "-c", bashCommand)
            .withCreateContainerCmdModifier(cmd -> cmd.withHostName(HOSTNAME))
            .waitingFor(Wait.forLogMessage(".*DONE.*", 1).withStartupTimeout(Duration.ofSeconds(5L)));
    }

    private String getContainerHostAddressInfo() {
        String containerHostIP = getContainerHostIP();
        return getHostAddressInfo(containerHostIP, containerHostIP, true);
    }

    private String getContainerHostAddressInfo(String hostName) {
        return getHostAddressInfo(hostName, getContainerHostIP(), true);
    }

    private String getLocalhostAddressInfoV4() {
        return getHostAddressInfo(LOCALHOST, LOCALHOST_IPV4, true);
    }

    private String getLocalhostAddressInfoV6() {
        return getHostAddressInfo(LOCALHOST, LOCALHOST_IPV6, true);
    }

    private String getHostAddressInfo(String host, String ip, boolean reachable) {
        return host + "/" + ip + (reachable ? "[reachable]" : "[unreachable]");
    }

    private String getContainerHostIP() {
        var networkSettings = container.getContainerInfo().getNetworkSettings();
        String ip = networkSettings.getGlobalIPv6Address();
        if (ip != null && !ip.isEmpty()) {
            return ip;
        }
        return networkSettings.getNetworks().values().stream()
            .map(network -> network.getIpAddress())
            .filter(addr -> addr != null && !addr.isEmpty())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Container has no IPv4 address assigned"));
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
        return getResult("RESULT_getHostName: ", logs);
    }

    private static String getResultForGetCanonicalHostName(String[] logs) {
        return getResult("RESULT_getCanonicalHostName: ", logs);
    }

    private static String[] getResultForGetHostAddresses(String[] logs) {
        return getResult("RESULT_getHostAddresses: ", logs).split(", ");
    }

    private static String[] getResultForGetResolvableHostNames(String[] logs) {
        String result = getResult("RESULT_getResolvableHostNames: ", logs);
        return result.isBlank() ? new String[0] : result.split(", ");
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
