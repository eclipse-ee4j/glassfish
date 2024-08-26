/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class NetUtils {

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

        if (isPortFreeClient(null, portNumber)) {
            // can not setup a server socket and can not connect as a client
            // that means we don't have permission...
            return PortAvailability.noPermission;
        }
        return PortAvailability.inUse;
    }

    public static boolean isPortStringValid(String portNumber) {
        try {
            return isPortValid(Integer.parseInt(portNumber));
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static boolean isPortValid(int portNumber) {
        return portNumber >= 0 && portNumber <= MAX_PORT;
    }

    public static boolean isPortFree(int portNumber) {
        return isPortFree(null, portNumber);
    }

    public static boolean isPortFree(String hostName, int portNumber) {
        if (portNumber <= 0 || portNumber > MAX_PORT) {
            return false;
        }

        if (hostName == null || isThisHostLocal(hostName)) {
            return isPortFreeServer(portNumber);
        }
        return isPortFreeClient(hostName, portNumber);
    }

    private static boolean isPortFreeClient(String hostName, int portNumber) {
        try {
            // WBN - I have no idea why I'm messing with these streams!
            // I lifted the code from installer.  Apparently if you just
            // open a socket on a free port and catch the exception something
            // will go wrong in Windows.
            // Feel free to change it if you know EXACTLY what you're doing

            //If the host name is null, assume localhost
            if (hostName == null) {
                hostName = getHostName();
            }
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(hostName, portNumber), IS_PORT_FREE_TIMEOUT);
            }
        } catch (Exception e) {
            // Nobody is listening on this port
            return true;
        }

        return false;
    }

    private static boolean isPortFreeServer(int port) {
        // check 3 different ip-port combinations.
        // Amazingly I have seen all 3 possibilities -- so just checking on 0.0.0.0
        // is not good enough.
        // Usually it is the 0.0.0.0 -- but JMS (default:7676)
        // only returns false from the "localhost":port combination.
        // We want to be aggressively disqualifying ports rather than the other
        // way around

        // JIRA 19391 April 2013  Byron Nevins
        // If DNS can not resolve the hostname, then
        // InetAddress.getLocalHost() will throw an UnknownHostException
        // Before this change we caught ALL Exceptions and returned false.
        // So if, say, the system has a bad hostname setup, this method
        // would say the port is in use.  Which probably is not true.
        // Change:  log it as a warning the first time and then log it as a FINE.

        try {
            InetAddress add = InetAddress.getByAddress(new byte[] {0, 0, 0, 0});

            if (!isPortFreeServer(port, add)) {
                // return immediately on "not-free"
                return false;
            }

            try {
                add = InetAddress.getLocalHost();
            } catch (UnknownHostException uhe) {
                // Ignore. This exception should be already logged on startup.
            }

            if (!isPortFreeServer(port, add)) {
                return false;
            }

            add = InetAddress.getByName("localhost");
            return isPortFreeServer(port, add);
        } catch (Exception e) {
            // If we can't get an IP address then we can't check
            return false;
        }
    }

    private static boolean isPortFreeServer(int port, InetAddress add) {
        try (ServerSocket ss = new ServerSocket(port, 10, add)) {
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
                host = InetAddress.getByName(null).getHostName();
            }

            InetSocketAddress whom = new InetSocketAddress(host, port);
            server.connect(whom, timeoutMilliseconds);
            return true;
        } catch (Exception ex) {
            return false;
        } finally {
            try {
                server.close();
            } catch (IOException ex) {
                // nothing to do
            }
        }
    }


    public static boolean isRemote(String ip) {
        return !isLocal(ip);
    }

    public static boolean isLocal(String ip) {
        if (ip == null) {
            return false;
        }

        ip = trimIP(ip);

        if (ip.equals(LOCALHOST_IP)) {
            return true;
        }

        String[] myIPs = getHostIPs();

        if (myIPs == null) {
            return false;
        }

        for (String myIP : myIPs) {
            if (ip.equals(myIP)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return true if hostname represents the current machine.
     * A null or empty hostname is considered local, as is the
     * name "localhost".  Otherwise, all the IP addresses
     * corresponding to hostname are compared with all the IP addresses
     * corresponding to "localhost", as well as all the IP addresses
     * for all the network interfaces on this machine.  Note that
     * hostname can also be an IP address in string form.
     *
     * @return true if hostname is the local host
     */
    public static boolean isThisHostLocal(String hostname) {
        // optimize common cases
        if (hostname == null || hostname.isEmpty() || hostname.equalsIgnoreCase("localhost")) {
            return true;
        }

        // now check all the addresses of "localhost"
        InetAddress hostAddrs[] = null;
        try {
            hostAddrs = InetAddress.getAllByName(hostname);
            assert hostAddrs != null;

            // any address that's a loopback address is a local address
            for (InetAddress ia : hostAddrs) {
                if (ia.isLoopbackAddress()) {
                    return true;
                }
            }

            // are any of our addresses the same as any address of "localhost"?
            // XXX - redundant with the above check?
            for (InetAddress lia : InetAddress.getAllByName("localhost")) {
                for (InetAddress ia : hostAddrs) {
                    if (lia.equals(ia)) {
                        return true;
                    }
                }
            }
        } catch (UnknownHostException ex) {
            // ignore it
        }

        // it's not localhost, perhaps it's one of the addresses of this host?
        Enumeration<NetworkInterface> eni = null;
        try {
            eni = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            return false;
        }
        if (hostAddrs != null) {
            while (eni.hasMoreElements()) {
                NetworkInterface ni = eni.nextElement();
                for (InterfaceAddress intf : ni.getInterfaceAddresses()) {
                    for (InetAddress ia : hostAddrs) {
                        if (intf.getAddress().equals(ia)) {
                            return true;
                        }
                    }
                }
            }
        }
        // nothing matched, not local
        return false;
    }

    /**
     * Resolves both hosts and then compares their internet addresses.
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

    public static String[] getHostIPs() {
        try {
            InetAddress[] adds = getHostAddresses();
            if (adds == null) {
                return null;
            }

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

    public static InetAddress[] getHostAddresses() {
        try {
            String hname = getHostName();
            if (hname == null) {
                return null;
            }
            return InetAddress.getAllByName(hname);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return null;
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
}
