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

import com.sun.enterprise.util.StringUtils;

import java.io.IOException;
import java.lang.System.Logger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;

public final class NetUtils {

    private static final Logger LOG = System.getLogger(NetUtils.class.getName());
    /** Maximal allowed port number */
    public static final int MAX_PORT = 65535;
    private static final String LOCALHOST_IP = "127.0.0.1";

    private static final int IS_RUNNING_DEFAULT_TIMEOUT = 3000;
    private static final int IS_PORT_FREE_TIMEOUT = 1000;

    private NetUtils() {
        // Static utility class
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

        if (isPortFreeClient(getHostName(), portNumber)) {
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
     * @return true if the local/loopback host is not listening on the given port number, false otherwise.
     */
    public static boolean isPortFree(int portNumber) {
        return isPortFree(getHostName(), portNumber);
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

    private static boolean isPortFreeClient(String hostName, int portNumber) {
        if (hostName == null) {
            hostName = getHostName();
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(hostName, portNumber), IS_PORT_FREE_TIMEOUT);
            return false;
        } catch (IOException e) {
            LOG.log(TRACE, "Nobody is listening on host: " + hostName + ", port: " + portNumber, e);
            return true;
        }
    }

    private static boolean isPortFreeServer(int port) {
        try {
            InetAddress address;
            try {
                // System supplies any address mapped to the local host.
                // That doesn't mean that it is possible to allocate port on it.
                // The IP/host mapping could be explicitly set in /etc/hosts file, but in fact
                // no network device has assigned it now.
                // Then the isPortFreeServer would fail.
                address = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                LOG.log(WARNING,
                    "Could not resolve address of the local host. I will use the loopback address temporarily.", e);
                address = InetAddress.getLoopbackAddress();
            }

            return isPortFreeServer(port, address);
        } catch (Exception e) {
            LOG.log(TRACE, "Could not resolve address of the local host.", e);
            return false;
        }
    }

    private static boolean isPortFreeServer(int port, InetAddress address) {
        try (ServerSocket ss = new ServerSocket(port, 10, address)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Calls {@link #isRunning(String, int, int)}
     * with {@value #IS_RUNNING_DEFAULT_TIMEOUT} ms timeout.
     *
     * @param host
     * @param port port to check.
     * @return true if there's something listening on the port.
     */
    public static boolean isRunning(String host, int port) {
        return isRunning(host, port, IS_RUNNING_DEFAULT_TIMEOUT);
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
     * @param timeoutMilliseconds timeout in milliseconds
     * @return true if there's something listening on th port.
     */
    public static boolean isRunning(String host, int port, int timeoutMilliseconds) {
        Socket server = new Socket();
        try {
            if (host == null) {
                host = getHostName();
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
     * @param address IP or hostname
     * @return true if the address represents the local host, false otherwise.
     */
    public static boolean isLocal(final String address) {
        if (address == null) {
            return false;
        }

        if (LOCALHOST_IP.equals(address)) {
            return true;
        }

        String[] myIPs = getHostIPs();
        for (String myIP : myIPs) {
            if (address.equals(myIP)) {
                return true;
            }
        }

        if (getHostName().equals(address)) {
            // if the address is same as the host name, it is considered local too
            return true;
        }
        try {
            return InetAddress.getByName(address).isLoopbackAddress();
        } catch (UnknownHostException e) {
            LOG.log(TRACE, "Unable to resolve the address.", e);
            return false;
        }
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
     * @return all the IP addresses of the local host. Never null.
     */
    public static String[] getHostIPs() {
        try {
            InetAddress[] adds = getHostAddresses();
            String[] ips = new String[adds.length];
            for (int i = 0; i < adds.length; i++) {
                String ip = trimIP(adds[i].toString());
                ips[i] = ip;
            }

            return ips;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return all the addresses of the local host. Never null.
     */
    public static InetAddress[] getHostAddresses() {
        try {
            return InetAddress.getAllByName(getHostName());
        } catch (Exception e) {
            LOG.log(TRACE, "Could not resolve address of the local host.", e);
            return new InetAddress[0];
        }
    }


    /**
     * @return resolved host name of the local host (see <code>hostname</code> command on linux) or
     *         loopback hostname.
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return InetAddress.getLoopbackAddress().getHostName();
        }
    }

    /**
     * This method returns the fully qualified name of the host.
     * If the name can't be resolved (on Windows if there isn't a domain specified),
     * just host name is returned
     *
     * @return fully qualified name of the local host.
     * @throws UnknownHostException so it can be handled on a case by case basis
     */
    public static String getCanonicalHostName() throws UnknownHostException {
        final String defaultHostname = InetAddress.getLocalHost().getHostName();
        // short-circuit out if user has reverse-DNS issues
        if (Boolean.parseBoolean(System.getenv("AS_NO_REVERSE_DNS"))) {
            return defaultHostname;
        }

        // look for full name
        // check to see if ip returned or canonical hostname is different than hostname
        // It is possible for dhcp connected computers to have an erroneous name returned
        // that is created by the dhcp server. If that happens, return just the default hostname
        final String hostname = InetAddress.getLocalHost().getCanonicalHostName();
        if (hostname.equals(InetAddress.getLocalHost().getHostAddress())
                || !hostname.startsWith(defaultHostname)) {
            // don't want IP or canonical hostname, this will cause a lot of problems for dhcp users
            // get just plain host name instead
            return defaultHostname;
        }

        // If the DNS is inconsistent between domain hosts, it is sometimes better to use
        // any reachable public IP address. This check is not perfect - however usually if
        // the host name doesn't contain any dots, it is already a bad name for wider networks.
        if (!hostname.contains(".")) {
            ThrowingPredicate<NetworkInterface> isLoopback = NetworkInterface::isLoopback;
            try {
                String host = NetworkInterface.networkInterfaces().filter(Predicate.not(isLoopback))
                    .flatMap(NetworkInterface::inetAddresses)
                    .map(InetAddress::getHostAddress)
                    .filter(name -> name.indexOf('.') > 0)
                    .findFirst().orElse(hostname);
                return host;
            } catch (SocketException e) {
                LOG.log(WARNING, "Failed to list network interfaces.", e);
            }
        }
        return hostname;
    }

    private static String trimIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return ip;
        }

        int index = ip.lastIndexOf('/');

        if (index >= 0) {
            return ip.substring(++index);
        }

        return ip;
    }

    public enum PortAvailability {
        illegalNumber, noPermission, inUse, unknown, OK
    }


    @FunctionalInterface
    private static interface ThrowingPredicate<T> extends Predicate<T> {

        boolean throwing(T object) throws Exception;

        @Override
        public default boolean test(T object) {
            try {
                return throwing(object);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
