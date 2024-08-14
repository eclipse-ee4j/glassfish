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

package com.sun.enterprise.admin.cli.cluster;

import com.sun.enterprise.admin.cli.CLICommand;
import com.sun.enterprise.gms.tools.MulticastTester;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * asadmin local command that wraps the multicast validator tool
 * in shoal-gms-impl.jar
 */
@Service(name="validate-multicast")
@PerLookup
public final class ValidateMulticastCommand extends CLICommand {

    @Param(name="multicastport", optional=true)
    private String port;

    @Param(name="multicastaddress", optional=true)
    private String address;

    @Param(name="bindaddress", optional=true)
    private String bindInterface;

    @Param(name="sendperiod", optional=true)
    private String period;

    @Param(name="timeout", optional=true)
    private String timeout;

    @Param(name="timetolive", optional=true)
    private String ttl;

    @Param(optional=true, shortName="v", defaultValue="false")
    private boolean verbose;

    @Override
    protected int executeCommand() throws CommandException {
        MulticastTester mt = new MulticastTester();
        return mt.run(createArgs());
    }

    private String [] createArgs() {
        List<String> argList = new ArrayList<String>();
        if (port != null && !port.isEmpty()) {
            argList.add(MulticastTester.PORT_OPTION);
            argList.add(port);
        }
        if (address != null && !address.isEmpty()) {
            argList.add(MulticastTester.ADDRESS_OPTION);
            argList.add(address);
        }
        if (bindInterface != null && !bindInterface.isEmpty()) {
            argList.add(MulticastTester.BIND_OPTION);
            argList.add(bindInterface);
        }
        if (period != null && !period.isEmpty()) {
            argList.add(MulticastTester.WAIT_PERIOD_OPTION);
            argList.add(period);
        }
        if (timeout != null && !timeout.isEmpty()) {
            argList.add(MulticastTester.TIMEOUT_OPTION);
            argList.add(timeout);
        }
        if (ttl != null && !ttl.isEmpty()) {
            argList.add(MulticastTester.TTL_OPTION);
            argList.add(ttl);
        }
        if (verbose) {
            argList.add(MulticastTester.DEBUG_OPTION);
        }
        return argList.toArray(new String[argList.size()]);
    }
}
