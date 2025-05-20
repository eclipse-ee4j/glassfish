/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.tests.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author David Matejcek
 */
public class ServerUtils {

    /**
     * Tries to allocate a free local ports, avoids duplicates.
     *
     * @param count count of free ports to find.
     * @return a modifiable queue of free local port numbers.
     * @throws IllegalStateException if it fails for 20 times
     */
    public static Queue<Integer> getFreePorts(int count) throws IllegalStateException {
        final ArrayDeque<Integer> generatedPorts = new ArrayDeque<>(count);
        final Set<Integer> excludedPorts = new HashSet<>();
        for (int i = 0; i < count; i++) {
            int port = getFreePort(excludedPorts);
            generatedPorts.add(port);
            // Avoid duplicates
            excludedPorts.add(port);
        }
        return generatedPorts;
    }


    /**
     * Tries to allocate a free local port.
     *
     * @return a free local port number.
     * @throws IllegalStateException if it fails for 20 times
     */
    public static int getFreePort() throws IllegalStateException {
        return getFreePort(Set.of());
    }


    /**
     * Tries to allocate a free local port.
     *
     * @param excluded ports to avoid
     * @return a free local port number.
     * @throws IllegalStateException if it fails for 20 times
     */
    public static int getFreePort(Set<Integer> excluded) throws IllegalStateException {
        int counter = 0;
        while (true) {
            counter++;
            try (ServerSocket socket = new ServerSocket(0)) {
                final int port = socket.getLocalPort();
                socket.setSoTimeout(1);
                socket.setReuseAddress(true);
                if (excluded.contains(port) && counter >= 20) {
                    throw new IllegalStateException("Cannot open random port, tried 20 times. Port " + port
                        + " is excluded and we were not able to find another.");
                }
                return port;
            } catch (IOException e) {
                if (counter >= 20) {
                    throw new IllegalStateException("Cannot open random port, tried 20 times.", e);
                }
            }
        }
    }


    /**
     * @return the IP address of the localhost.
     */
    public static String getLocalIP4Address() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Cannot determine the local address.", e);
        }
    }


    /**
     * Creates a simple war file made of provided classes.
     *
     * @param warFile the WAR file
     * @param classes a classes to be included to the WAR file
     * @return file usable for deployment.
     */
    public static File createWar(final File warFile, final Class<?>... classes) {
        try {
            final WebArchive war = ShrinkWrap.create(WebArchive.class).addClasses(classes);
            war.as(ZipExporter.class).exportTo(warFile, true);
            return warFile;
        } catch (Exception e) {
            return fail(e);
        }
    }


    /**
     * Downloads content from the url.
     * Expects there is a service listening and returning textual response.
     * Therefore this is usable just for simple servlets.
     *
     * @param url target URL
     * @return content from the url.
     * @throws IOException if an error occurred
     */
    public static String download(final URL url) throws IOException {
        final Object object = url.getContent();
        if (object instanceof InputStream) {
            try (InputStream input = (InputStream) object; Scanner scanner = new Scanner(input, StandardCharsets.UTF_8)) {
                return scanner.nextLine();
            }
        }
        return fail("Expected input stream, but received this: " + object);
    }

    /**
     * Executes a command on embedded GlassFish instance.
     *
     * @param glassfish an embedded GlassFish instance
     * @param cmd a command to run
     * @param params command parameters
     * @return result of command execution
     * @throws GlassFishException if an error occurred
     */
    public static CommandResult runCommand(GlassFish glassfish, String cmd, String params) throws GlassFishException {
        return glassfish.getCommandRunner().run(cmd, params);
    }
}
