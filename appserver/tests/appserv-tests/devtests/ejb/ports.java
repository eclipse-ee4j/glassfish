/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ports {

    public static void main(String... args) throws Exception {
        System.out.println(getFreePorts(12).stream().collect(Collectors.joining(" ")));
    }

    /**
     * Tries to allocate a free local ports, avoids duplicates.
     *
     * @param count count of free ports to find.
     * @return a modifiable queue of free local port numbers.
     * @throws IllegalStateException if it fails for 20 times
     */
    private static Queue<String> getFreePorts(int count) throws IllegalStateException {
        final ArrayDeque<String> generatedPorts = new ArrayDeque<>(count);
        final Set<String> excludedPorts = new HashSet<>();
        for (int i = 0; i < count; i++) {
            String port = getFreePort(excludedPorts);
            generatedPorts.add(port);
            // Avoid duplicates
            excludedPorts.add(port);
        }
        return generatedPorts;
    }

    private static String getFreePort(Set<String> excluded) throws IllegalStateException {
        int counter = 0;
        while (true) {
            counter++;
            try (ServerSocket socket = new ServerSocket(0)) {
                final int port = socket.getLocalPort();
                socket.setSoTimeout(1);
                if (excluded.contains(port) && counter >= 20) {
                    throw new IllegalStateException("Cannot open random port, tried 20 times. Port " + port
                        + " is excluded and we were not able to find another.");
                }
                return Integer.toString(port);
            } catch (IOException e) {
                if (counter >= 20) {
                    throw new IllegalStateException("Cannot open random port, tried 20 times.", e);
                }
            }
        }
    }
}
