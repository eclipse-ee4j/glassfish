/*
 * Copyright (c) 2024, 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.StringUtils;

import java.io.IOException;
import java.lang.System.Logger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;

/**
 * @see <a href="https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/doc-files/net-properties.html">Network Properties</a>
 */
public final class NetUtils {

    private static final Logger LOG = System.getLogger(NetUtils.class.getName());

    /** Maximal allowed port number */
    public static final int MAX_PORT = 65535;

    private static final String AS_HOSTNAME = System.getenv("AS_HOSTNAME");
    private static final int IS_LISTENING_DEFAULT_TIMEOUT = getEnv("AS_IS_LISTENING_DEFAULT_TIMEOUT", 3000);
    private static final int IS_HOST_ACCESSIBLE_TIMEOUT = getEnv("AS_IS_HOST_ACCESSIBLE_TIMEOUT", 1000);
    private static final int IS_LOCAL_HOST_ACCESSIBLE_TIMEOUT = getEnv("AS_IS_LOCAL_HOST_ACCESSIBLE_TIMEOUT", 100);

    private static final AddressComparator BEST_ADDRESS_COMPARATOR = new AddressComparator();
    private static final String LOCALHOST = "localhost";
    private static final String HOST_NAME_LOOPBACK = InetAddress.getLoopbackAddress().getHostName();
    /** Computer name loaded from an env property or provided by the hostname command, etc. */
    private static final String HOST_NAME = loadAndCheckHostName();
    private static final String HOST_NAME_CANONICAL = resolveCanonicalHostName();


    private NetUtils() {
        // Static utility class
    }

    private static String loadAndCheckHostName() {
        final String hostName = loadHostName();
        if (isResolvable(hostName)) {
            LOG.log(DEBUG, "Using the host name: " + hostName);
        } else {
            LOG.log(WARNING, "The host name " + hostName + " is not resolvable host name. Check your DNS!");
        }
        return hostName;
    }

    private static String loadHostName() {
        if (AS_HOSTNAME != null && !AS_HOSTNAME.isBlank()) {
            return AS_HOSTNAME;
        }
        // The ENV variable was not set. Now we improvise.
        try {
            // The best option - the host name is resolvable by DNS.
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.log(WARNING, "Unable to resolve the host name of the local host using InetAddress: " + e
                + ". Trying to use OS standard environment options.");
        }
        // Some operating systems set the host name to environment options.
        String envName = OS.isWindows() ? "COMPUTERNAME" : "HOSTNAME";
        String envHostName = System.getenv(envName);
        if (envHostName != null && !envHostName.isBlank()) {
            return envHostName;
        }
        LOG.log(WARNING, "Unable to resolve the host name of the local host using environment option " + envName
            + ". Using loopback host name instead.");
        return HOST_NAME_LOOPBACK;
    }

    /**
     * Gets a free port at the time of call to this method.
     * The logic leverages the built in java.net.ServerSocket implementation
     * which binds a server socket to a free port when instantiated with
     * a port <code>0</code>.
     * <p>
     * Note that this method guarantees the availability of the port
     * only at the time of call. The method does not bind to this port.
     * <p>
     * Checking for free port can fail for several reasons which may
     * indicate potential problems with the system.
     * <p>
     * If any exceptional condition is experienced, <code>0</code>
     * is returned, indicating that the method failed for some reasons and
     * the callers should take the corrective action.
     * <p>Method is synchronized on this class.
     *
     * @return integer depicting the free port number available at this time
     * @throws IllegalStateException if it was not possible to open and close the server socket.
     */
    public static synchronized int getFreePort() throws IllegalStateException {
        try {
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                // following call normally returns the free port,
                // to which the ServerSocket is bound.
                return serverSocket.getLocalPort();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not open random free local port.", e);
        }
    }

    /**
     * There are 4 possibilities when you want to setup a server socket on a port:
     * <ol>
     * <li>The port is not in valid range.
     * <li>The user does not have permission to open up shop on that port
     *    An example of (2) is a non-root user on UNIX trying to use port 80
     * <li>The port is already in use
     * <li>OK -- you can use it!
     * </ol>
     *
     * @param portNumber
     * @return one of the 4 possibilities for this port
     */
    public static PortAvailability checkPort(int portNumber) {
        if (!isPortValid(portNumber)) {
            return PortAvailability.illegalNumber;
        }

        // if we can setup a server socket on that port then it must be free.
        if (isPortFreeServer(portNumber)) {
            return PortAvailability.OK;
        }

        if (isPortFreeClient(HOST_NAME_CANONICAL, portNumber)) {
            // can not setup a server socket and can not connect as a client
            // that means we don't have permission...
            return PortAvailability.noPermission;
        }
        return PortAvailability.inUse;
    }

    /**
     * @param portNumber
     * @return true if the port number is an integer between 0 and {@value #MAX_PORT} (both inclusive)
     */
    public static boolean isPortValid(String portNumber) {
        try {
            return isPortValid(Integer.parseInt(portNumber));
        } catch (NumberFormatException e) {
            LOG.log(TRACE, "Invalid port number: " + portNumber, e);
            return false;
        }
    }

    /**
     * @param portNumber
     * @return true if the port number is between 0 and {@value #MAX_PORT} (both inclusive)
     */
    public static boolean isPortValid(int portNumber) {
        return portNumber >= 0 && portNumber <= MAX_PORT;
    }

    /**
     * @param portNumber
     * @return true if the loopback host is not listening on the given port number, false otherwise.
     */
    public static boolean isPortFree(int portNumber) {
        return isPortFree(HOST_NAME_LOOPBACK, portNumber);
    }

    /**
     * @param hostName
     * @param portNumber
     * @return true if the host is not listening on the given port number, false otherwise.
     */
    public static boolean isPortFree(String hostName, int portNumber) {
        if (portNumber <= 0 || portNumber > MAX_PORT) {
            return false;
        }
        if (isLocal(hostName)) {
            return isPortFreeServer(portNumber);
        }
        return isPortFreeClient(hostName, portNumber);
    }

    private static boolean isPortFreeClient(final String hostName, final int portNumber) {
        String host = hostName == null ? InetAddress.getLoopbackAddress().getHostAddress() : hostName;
        LOG.log(DEBUG, "isPortFreeClient(host={0}, portNumber={1})", host, portNumber);
        try (Socket socket = new Socket()) {
            // Force RST on close
            socket.setSoLinger(true, 0);
            socket.connect(new InetSocketAddress(host, portNumber), IS_HOST_ACCESSIBLE_TIMEOUT);
            return false;
        } catch (IOException e) {
            LOG.log(TRACE, "Nobody is listening on host: " + host + ", port: " + portNumber, e);
            return true;
        }
    }

    /**
     * Checks if it is possible to open port on a loopback address.
     * @param port
     * @return true if the port is available.
     */
    public static boolean isPortFreeServer(int port) {
        return isPortFreeServer(InetAddress.getLoopbackAddress(), port);
    }

    /**
     * Checks if it is possible to open port on a provided address.
     * @param address
     * @param port
     * @return true if the port is available.
     */
    public static boolean isPortFreeServer(InetAddress address, int port) {
        LOG.log(DEBUG, "isPortFreeServer(address={0}, portNumber={1})", address, port);
        try (ServerSocket socket = new ServerSocket(port, 10, address)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Calls {@link #isRunning(String, int, int)}
     * with {@value #IS_LISTENING_DEFAULT_TIMEOUT} ms timeout.
     *
     * @param host
     * @param port port to check.
     * @return true if there's something listening on the port.
     */
    public static boolean isRunning(String host, int port) {
        return isRunning(host, port, IS_LISTENING_DEFAULT_TIMEOUT);
    }

    /**
     * There is sometimes a need for subclasses to know if a
     * <code> local domain </code> is running. An example of such a command is
     * change-master-password command. The stop-domain command also needs to
     * know if a domain is running <i> without </i> having to provide user
     * name and password on command line (this is the case when I own a domain
     * that has non-default admin user and password) and want to stop it
     * without providing it.
     * <p>
     * In such cases, we need to know if the domain is running and this method
     * provides a way to do that.
     * @param host host name or an ip address or null, then we use {@link #getCanonicalHostName()}
     * @param port port in a range of 0 - 65535
     * @param timeoutMilliseconds timeout in milliseconds
     * @return true if there's something listening on th port.
     */
    public static boolean isRunning(String host, int port, int timeoutMilliseconds) {
        Socket server = new Socket();
        try {
            if (host == null) {
                host = HOST_NAME_LOOPBACK;
            }
            InetSocketAddress address = new InetSocketAddress(host, port);
            server.connect(address, timeoutMilliseconds);
            return true;
        } catch (Exception ex) {
            return false;
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                LOG.log(WARNING, "Failed to close the server socket: " + server, e);
            }
        }
    }

    /**
     * Checks if the address represents the local host. The address can be an IP or a hostname.
     * The method checks if the address is a loopback address, or if it matches any of the IP
     * addresses of the local host. A null address is considered not local.
     * If the given address is same as the host name, it is considered local too.
     * A null address is considered not local.
     *
     * @param hostname IP or hostname
     * @return true if the address represents the local host, false otherwise.
     */
    public static boolean isLocal(final String hostname) {
        if (hostname == null) {
            return false;
        }
        final InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            LOG.log(TRACE, "Unable to resolve the address.", e);
            return false;
        }
        if (inetAddress.isLoopbackAddress()) {
            return true;
        }
        // if the address is same as the host name, it is considered local too
        return getHostAddressesAsStream().anyMatch(a -> Objects.equals(a, inetAddress));
    }

    /**
     * Resolves both hosts and then compares their host IP addresses.
     * If they match at least once, those hosts are considered as same.
     * If there is no such address, returns false.
     *
     * @param host1
     * @param host2
     * @return true if parameters resolved to same internet address(es)
     */
    public static boolean isSameHost(String host1, String host2) {
        List<String> host1_ips = new ArrayList<>();
        List<String> host2_ips = new ArrayList<>();
        try {
            if (!StringUtils.ok(host1) && !StringUtils.ok(host2)) {
                // edge case ==> both are null or empty
                return true;
            }

            if (!StringUtils.ok(host1) || !StringUtils.ok(host2)) {
                // just one of them is null or empty
                return false;
            }

            InetAddress[] adds1 = InetAddress.getAllByName(host1);
            InetAddress[] adds2 = InetAddress.getAllByName(host2);

            if (adds1.length == 0 && adds2.length == 0) {
                return true;
            }

            if (adds1.length == 0 || adds2.length == 0) {
                return false;
            }

            for (InetAddress ia : adds1) {
                host1_ips.add(ia.getHostAddress());
            }

            for (InetAddress ia : adds2) {
                host2_ips.add(ia.getHostAddress());
            }

            for (String h1ip : host1_ips) {
                for (String h2ip : host2_ips) {
                    if (h1ip.equals(h2ip)) {
                        return true;
                    }
                }
            }

            return false;
        } catch (UnknownHostException ex) {
            return false;
        }
    }

    /**
     * Returns the host name. There is no guarantee that it will be resolvable by DNS.
     *
     * @return host name of the local host or loopback hostname.
     */
    public static String getHostName() {
        return HOST_NAME;
    }

    /**
     * Returns the loopback host name. Suitable for local socket communication.
     *
     * @return loopback hostname.
     */
    public static String getLoopbackHostName() {
        return HOST_NAME_LOOPBACK;
    }

    /**
     * Returns the loopback host name. Suitable for local socket communication.
     *
     * @return loopback hostname.
     */
    public static String getCanonicalHostName() {
        return HOST_NAME_CANONICAL;
    }

    /**
     * This method tries to choose the best fully qualified name of this host.
     *
     * @return fully qualified name of the local host.
     */
    private static String resolveCanonicalHostName() {
        // User defined.
        if (AS_HOSTNAME != null && !AS_HOSTNAME.isBlank()) {
            return AS_HOSTNAME;
        }
        // No checking, use the host name set in the OS.
        if (Boolean.parseBoolean(System.getenv("AS_NO_REVERSE_DNS"))) {
            return HOST_NAME;
        }
        final List<InetAddress> addresses = getHostAddresses();
        for (InetAddress address : addresses) {
            resolveHostName(address);
            if (hasHostName(address) && address.getCanonicalHostName().contains(".")) {
                return address.getCanonicalHostName();
            }
        }
        for (InetAddress address : addresses) {
            if (hasHostName(address)) {
                return address.getCanonicalHostName();
            }
        }
        LOG.log(WARNING, "Could not choose any usable canonical hostname. Using loopback " + HOST_NAME_LOOPBACK + ".");
        return HOST_NAME_LOOPBACK;
    }

    /**
     * Goes over network interfaces and collects resolvable host names of this hosts.
     * The list is ordered, loopback host names are last.
     * Host names with dots are preferred to those without.
     *
     * @return list of autodetected host names except loopbacks and unresolvable host names. Never null.
     */
    public static List<String> getResolvableHostNames() {
        return getResolvableHostNamesAsStream().toList();
    }

    private static Stream<String> getResolvableHostNamesAsStream() {
        return getHostAddressesAsStream().map(NetUtils::resolveHostName).filter(NetUtils::hasHostName)
            .map(InetAddress::getHostName).distinct();
    }

    /**
     * @return all the addresses of the local host. Never null, but may be empty.
     */
    public static List<InetAddress> getHostAddresses() {
        return getHostAddressesAsStream().toList();
    }

    /**
     * @return all the addresses of the local host. Never null, but may be empty.
     */
    public static Stream<InetAddress> getHostAddressesAsStream() {
        Stream<InetAddress> networkAddresses;
        try {
            networkAddresses = NetworkInterface.networkInterfaces().flatMap(NetworkInterface::inetAddresses);
        } catch (SocketException e) {
            LOG.log(WARNING, "Could not list network interfaces.", e);
            networkAddresses = Stream.empty();
        }
        // These host names might be resolvable even if they are not present
        // on any network interface, so we add them manually.
        Stream<InetAddress> specials = Stream.of(HOST_NAME, HOST_NAME_LOOPBACK, LOCALHOST)
            .flatMap(NetUtils::resolveHostName).filter(Objects::nonNull);
        return Stream.concat(networkAddresses, specials).distinct();
    }

    /**
     * @param address
     * @return true if the {@link InetAddress#getHostName()} gives different result than
     *         {@link InetAddress#getHostAddress()}
     */
    private static boolean hasHostName(InetAddress address) {
        LOG.log(TRACE, "hasHostName(address={0})", address);
        return !address.toString().startsWith("/") && !address.getHostAddress().equals(address.getHostName());
    }

    /**
     * @param hostName host name or null for loopback host.
     * @return true if the host name can be converted to an IP address.
     */
    private static boolean isResolvable(String hostName) {
        try {
            InetAddress.getByName(hostName);
            return true;
        } catch (UnknownHostException e) {
            LOG.log(INFO, "Unable to resolve host name: " + hostName, e);
            return false;
        }
    }

    private static boolean isLocalHostAccessible(final InetAddress address) {
        final InetSocketAddress endpoint;
        try (ServerSocket server = new ServerSocket(0, 10, address)) {
            endpoint = new InetSocketAddress(address, server.getLocalPort());
            if (!isLocalHostAccessible(endpoint, false)) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return isLocalHostAccessible(endpoint, true);
    }

    private static boolean isLocalHostAccessible(final InetSocketAddress endpoint, final boolean refusalOk) {
        try (Socket socket = new Socket()) {
            // Force RST on close
            socket.setSoLinger(true, 0);
            socket.connect(endpoint, IS_LOCAL_HOST_ACCESSIBLE_TIMEOUT);
            LOG.log(TRACE, () -> endpoint + " is accessible, ok.");
            return true;
        } catch (SocketTimeoutException e) {
            LOG.log(TRACE, () -> endpoint + " is blocked, bad.", e);
            return false;
        } catch (Exception e) {
            if (refusalOk) {
                LOG.log(TRACE, () -> endpoint + " is refused, nothing is listening, ok.", e);
                return true;
            }
            LOG.log(TRACE, () -> endpoint + " is refused, nothing is listening, bad.", e);
            return false;
        }
    }

    /**
     * Gets the real remote host based on proxy headers.
     * <p>
     * If behindProxy is true, checks X-Real-IP and X-Forwarded-For headers
     * to get the original client IP when behind a reverse proxy.
     * Otherwise, returns the remote host.
     *
     * @param requestInfoProvider provides access to request info
     * @param behindProxy true if the server is behind a reverse proxy
     * @return the real remote host IP/hostname
     */
    public static String getRemoteHost(RequestInfoProvider requestInfoProvider, boolean behindProxy) {
        if (behindProxy) {

            // Check X-Real-IP first (set by closest proxy like nginx)
            String xRealIP = requestInfoProvider.getHeader("X-Real-IP");
            if (xRealIP != null && !xRealIP.isBlank()) {
                return xRealIP;
            }

            // Check X-Forwarded-For (can contain multiple IPs in format "client, proxy1, proxy2")
            String xForwardedFor = requestInfoProvider.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                int commaIndex = xForwardedFor.indexOf(',');
                return commaIndex > 0 ? xForwardedFor.substring(0, commaIndex).trim() : xForwardedFor.trim();
            }
        }

        // Fall back to the remote host
        return requestInfoProvider.getRemoteHost();
    }

    private static int getEnv(String name, int defaultValue) {
        String value = System.getenv().get(name);
        return value == null ? defaultValue : Integer.parseInt(value);
    }

    private static InetAddress resolveHostName(InetAddress address) {
        long startShort = System.currentTimeMillis();
        String hostName = address.getHostName();
        LOG.log(TRACE, () -> "Resolving hostname " + hostName + " took "
            + (System.currentTimeMillis() - startShort) + " ms");
        long startCanon = System.currentTimeMillis();
        String canonicalhostName = address.getCanonicalHostName();
        LOG.log(TRACE, () -> "Resolving canonical hostname " + canonicalhostName + " took "
            + (System.currentTimeMillis() - startCanon) + " ms");
        return address;
    }

    private static Stream<InetAddress> resolveHostName(String hostname) {
        try {
            return streamFromArray(InetAddress.getAllByName(hostname));
        } catch (UnknownHostException e) {
            LOG.log(TRACE, "Unresolvable hostname: " + hostname, e);
            return Stream.empty();
        }
    }

    private static <T> Stream<T> streamFromArray(T[] a) {
        return StreamSupport.stream(
            Spliterators.spliterator(a, Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
    }

    public enum PortAvailability {
        illegalNumber, noPermission, inUse, unknown, OK
    }

    /**
     * Interface for accessing HTTP headers, allowing different request types to be used
     * with the getRemoteHost method.
     */
    public interface RequestInfoProvider {
        /**
         * Gets the value of an HTTP header.
         *
         * @param name the header name
         * @return the header value, or null if not present
         */
        String getHeader(String name);

        /**
         * Gets the value of the remote host.
         *
         * @return remote host of the HTTP request
         */
        String getRemoteHost();
    }

    @FunctionalInterface
    private static interface ThrowingPredicate<T> extends Predicate<T> {

        boolean throwing(T object) throws Exception;

        @Override
        default boolean test(T object) {
            try {
                return throwing(object);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @FunctionalInterface
    private static interface HostnameResolutionFunction<P, R> extends Function<P, R> {

        R throwing(P object) throws Exception;

        @Override
        default R apply(P object) {
            try {
                return throwing(object);
            } catch (RuntimeException e) {
                throw e;
            } catch (UnknownHostException e) {
                return null;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static class AddressComparator implements Comparator<InetAddress> {

        @Override
        public int compare(InetAddress a, InetAddress b) {
            // Loopback addresses should be last
            if (a.isLoopbackAddress() && !b.isLoopbackAddress()) {
                return 1;
            }
            if (!a.isLoopbackAddress() && b.isLoopbackAddress()) {
                return -1;
            }
            // Prefer addresses that have a resolvable host name
            boolean aHasHostName = hasHostName(a);
            boolean bHasHostName = hasHostName(b);
            if (aHasHostName && !bHasHostName) {
                return -1;
            }
            if (!aHasHostName && bHasHostName) {
                return 1;
            }
            boolean aIsLocalhost = LOCALHOST.equals(a.getHostName());
            boolean bIsLocalhost = LOCALHOST.equals(b.getHostName());
            if (aIsLocalhost && !bIsLocalhost) {
                return 1;
            }
            if (!aIsLocalhost && bIsLocalhost) {
                return -1;
            }
            // Prefer host names with dots
            boolean aHostNameHasDots = aHasHostName && a.getCanonicalHostName().contains(".");
            boolean bHostNameHasDots = bHasHostName && b.getCanonicalHostName().contains(".");
            if (aHostNameHasDots && !bHostNameHasDots) {
                return -1;
            }
            if (!aHostNameHasDots && bHostNameHasDots) {
                return 1;
            }
            // both have same host name or both don't have host name, prefer IPv4 over IPv6
            if ((aHasHostName && a.getCanonicalHostName().equals(b.getCanonicalHostName()))
                || (!aHasHostName && !bHasHostName)) {
                boolean aIsIpv4 = a.getHostAddress().indexOf(':') < 0;
                boolean bIsIpv4 = b.getHostAddress().indexOf(':') < 0;
                if (aIsIpv4 && !bIsIpv4) {
                    return -1;
                }
                if (!aIsIpv4 && bIsIpv4) {
                    return 1;
                }
            }
            // otherwise, sort by host name/IP
            return a.getCanonicalHostName().compareTo(b.getCanonicalHostName());
        }
    }
}
