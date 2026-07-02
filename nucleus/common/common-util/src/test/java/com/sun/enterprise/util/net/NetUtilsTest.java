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

import java.lang.System.Logger;
import java.net.InetAddress;
import java.util.List;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static java.lang.System.Logger.Level.INFO;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NetUtilsTest {
    private static final Logger LOG = System.getLogger(NetUtilsTest.class.getName());

    @Test
    void getFreePort() throws Exception {
        assertThat(NetUtils.getFreePort(), allOf(greaterThan(0), lessThanOrEqualTo(NetUtils.MAX_PORT)));
    }

    @Test
    void isPortFree() throws Exception {
        assertTrue(NetUtils.isPortFree(NetUtils.getFreePort()));
    }

    @Test
    void getResolvableHostNames() throws Exception {
        List<String> hostNames = NetUtils.getResolvableHostNames();
        assertThat("Detected hostnames: " + hostNames, hostNames, not(IsEmptyCollection.empty()));
    }

    @Test
    void getHostName() throws Exception {
        String hostName = assertDoesNotThrow(() -> NetUtils.getHostName());
        LOG.log(INFO,  "Detected host name: " + hostName);
    }

    @Test
    void getCanonicalHostName() throws Exception {
        String hostName = assertDoesNotThrow(() -> NetUtils.getCanonicalHostName());
        LOG.log(INFO,  "Detected host name: " + hostName);
        // First letter is not a number, should not return an ip address
        assertThat("Canonical host name should not be an ip address", hostName,
            matchesPattern("^[a-zA-Z][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})*$"));
    }

    @Test
    void isLocalHost() throws Exception {
        final InetAddress loopbackAddress = InetAddress.getLoopbackAddress();
        final InetAddress localHost = InetAddress.getLocalHost();
        assertAll(
            () -> assertFalse(NetUtils.isLocal("8.8.8.8"), "Remote IP"),
            () -> assertFalse(NetUtils.isLocal("idonotexist____xxx"), "nonexisting hostname"),
            () -> assertTrue(NetUtils.isLocal("localhost"), "hostname - localhost"),
            () -> assertTrue(NetUtils.isLocal("127.0.0.1"), "IPv4 address, 127.0.0.1"),
            () -> assertTrue(NetUtils.isLocal("0:0:0:0:0:0:0:1"), "IPv6 address 0:0:0:0:0:0:0:1, long format"),
            () -> assertTrue(NetUtils.isLocal(loopbackAddress.getHostAddress()), loopbackAddress.toString()),
            () -> assertTrue(NetUtils.isLocal(loopbackAddress.getHostName()), loopbackAddress.toString()),
            () -> assertTrue(NetUtils.isLocal(localHost.getHostAddress()), localHost.toString()),
            () -> assertTrue(NetUtils.isLocal(localHost.getHostName()), localHost.toString())
        );
    }

    // The text behind % marks interface name.
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void isLocalHost_windows() {
        assertTrue(NetUtils.isLocal("::1%1"), "IPv6 address ::1%1, short windows format");
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void isLocalHost_linux() {
        assertTrue(NetUtils.isLocal("::1%lo"), "IPv6 address ::1%lo, short linux format");
    }

    @Test
    @EnabledOnOs(OS.MAC)
    void isLocalHost_mac() {
        assertTrue(NetUtils.isLocal("::1%lo0"), "IPv6 address ::1%lo0, short mac format");
    }

    @Test
    void getHostAddresses() throws Exception {
        List<InetAddress> addresses = NetUtils.getHostAddresses();
        assertThat(addresses, hasSize(greaterThan(0)));
    }
}
