/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.util;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.util.net.NetUtils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jvnet.hk2.config.TransactionFailure;

import static com.sun.enterprise.config.util.PortConstants.PORTSLIST;
import static com.sun.enterprise.util.net.NetUtils.MAX_PORT;

/**
 * static methods useful for dealing with ports.
 *
 * @author Byron Nevins
 */
final class PortUtils {

    private PortUtils() {
        // no instances allowed!
    }

    /**
     * Make sure all ports that are specified by the user make sense.
     *
     * @param server The new Server element
     * @return null if all went OK. Otherwise return a String with the error message.
     */
    static void checkInternalConsistency(Server server) throws TransactionFailure {
        // Make sure all the system properties for ports have different numbers.
        List<SystemProperty> sysProps = server.getSystemProperty();
        Set<Integer> ports = new TreeSet<Integer>();

        for (SystemProperty sp : sysProps) {
            String name = sp.getName();

            if (PORTSLIST.contains(name)) {
                String val = sp.getValue();

                try {
                    boolean wasAdded = ports.add(Integer.parseInt(val));

                    if (!wasAdded) //TODO unit test
                        throw new TransactionFailure(Strings.get("PortUtils.duplicate_port", val, server.getName()));
                } catch (TransactionFailure tf) {
                    // don't re-wrap the same Exception type!
                    throw tf;
                } catch (Exception e) { //TODO unit test
                    throw new TransactionFailure(Strings.get("PortUtils.non_int_port", val, server.getName()));
                }
            }
        }
        checkForLegalPorts(ports, server.getName());
    }

    /**
     *
     * @param ports
     * @param serverName
     * @return a message if error, nu8ll means A-OK
     */
    private static void checkForLegalPorts(Set<Integer> ports, String serverName) throws TransactionFailure {
        for (int port : ports)
            if (!NetUtils.isPortValid(port))
                throw new TransactionFailure(Strings.get("PortUtils.illegal_port_number", port, serverName, MAX_PORT));
    }
}
