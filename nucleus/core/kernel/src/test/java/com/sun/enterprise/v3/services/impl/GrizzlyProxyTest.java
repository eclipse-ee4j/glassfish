/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.v3.services.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.kernel.KernelLoggerInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GrizzlyProxyTest {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private static final String FILE_SEP = System.getProperty("file.separator");
    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String BASE_PATH = USER_DIR + FILE_SEP + "target";
    private static final String TEST_LOG = BASE_PATH + FILE_SEP + "test.log";
    private static final Logger logger = KernelLoggerInfo.getLogger();

    private static FileHandler handler;
    private static GrizzlyService service;

    @BeforeAll
    public static void initializeLoggingAnnotationsTest() throws IOException {
        handler = new FileHandler(TEST_LOG);
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
    }

    @BeforeEach
    public void prepare() {
        service = createMock(GrizzlyService.class);
        expect(service.getLogger()).andReturn(logger);
        replay(service);
    }

    @Test
    public void testInitPort() throws IOException {
        int port = 80;

        NetworkListener listener = createMock(NetworkListener.class);
        expect(listener.getPort()).andReturn("" + port);
        replay(listener);
        GrizzlyProxy grizzlyProxy = new GrizzlyProxy(service, listener);

        int actual = grizzlyProxy.initPort(listener);

        assertEquals(port, actual);
    }

    @Test
    public void testInitPortWithNull() throws IOException {
        String port = null;
        String[] expectedMessage = { "SEVERE", "Cannot find port information from domain.xml" };

        NetworkListener listener = createMock(NetworkListener.class);
        expect(listener.getPort()).andReturn(port);
        replay(listener);
        GrizzlyProxy grizzlyProxy = new GrizzlyProxy(service, listener);

        Throwable exception = assertThrows(RuntimeException.class,
                () -> grizzlyProxy.initPort(listener));

        assertEquals("Cannot find port information from domain configuration", exception.getMessage());
        handler.flush();
        validateLogContents(expectedMessage);
    }

    @Test
    public void testInitPortWithInvalidVal() throws IOException {
        String port = "invalid";
        String[] expectedMessage = { "SEVERE", "Cannot parse port value:", port, ", using port 8080" };

        NetworkListener listener = createMock(NetworkListener.class);
        expect(listener.getPort()).andReturn(port);
        replay(listener);
        GrizzlyProxy grizzlyProxy = new GrizzlyProxy(service, listener);

        int actual = grizzlyProxy.initPort(listener);

        assertEquals(8080, actual);
        handler.flush();
        validateLogContents(expectedMessage);
    }

    @Test
    public void testInitAddress() throws IOException {
        String address = "127.0.0.1";

        NetworkListener listener = createMock(NetworkListener.class);
        expect(listener.getAddress()).andReturn(address);
        replay(listener);
        GrizzlyProxy grizzlyProxy = new GrizzlyProxy(service, listener);

        InetAddress rtn = grizzlyProxy.initAddress(listener);

        assertEquals(address, rtn.getHostAddress());
    }

    @Test
    public void testInitAddressWithInvalidVal() throws IOException {
        String address = "invalid";
        String[] expectedMessage = { "SEVERE", "Unknown address invalid", address };

        NetworkListener listener = createMock(NetworkListener.class);
        expect(listener.getAddress()).andReturn(address);
        replay(listener);
        GrizzlyProxy grizzlyProxy = new GrizzlyProxy(service, listener);

        InetAddress rtn = grizzlyProxy.initAddress(listener);

        assertNull(rtn);
        handler.flush();
        validateLogContents(expectedMessage);
    }

    private static void validateLogContents(String[] messages) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(TEST_LOG))) {
            StringBuilder buf = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buf.append(line);
                buf.append(LINE_SEP);
            }
            assertThat("File " + TEST_LOG + " does not contain expected log messages", buf.toString(), stringContainsInOrder(messages));
        }
    }

    @AfterAll
    public static void cleanupLoggingAnnotationsTest() throws Exception {
        logger.removeHandler(handler);
        handler.close();
    }
}
