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

package org.glassfish.loadbalancer.admin.cli;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandException;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Base class for all the LB commands
 * @author Yamini K B
 */
public class LBCommandsBase {

    @Inject
    Domain domain;

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(LBCommandsBase.class);

    Map<String,Integer> getInstanceWeightsMap(String weights) throws CommandException
    {
        HashMap<String,Integer> map = new HashMap();
        StringTokenizer st = new StringTokenizer(weights, ":");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            String insName = null;
            String value = null;
            insName = token.substring(0, token.indexOf("="));
            value = token.substring(token.indexOf("=") + 1);
            Integer weightInt;
            try
            {
                weightInt = Integer.valueOf(value);
            }
            catch (NumberFormatException nfe)
            {
                throw new CommandException("Invalid weight value");
            }
            map.put(insName, weightInt);
        }
        return map;
    }

    void updateLBForCluster(ActionReport report, String clusterName, String value, String timeout) {
        Cluster c = domain.getClusterNamed(clusterName);
        if ( c == null ) {
            report.setMessage("Cluster not defined");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        for (ServerRef sRef:c.getServerRef()) {
            try {
                updateLbEnabled(sRef, value, timeout);
            } catch(TransactionFailure ex) {
                report.setMessage("Failed to update lb-enabled");
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }
    }

    ServerRef getServerRefFromCluster(ActionReport report, String target) {
        // check if this server is part of cluster
        Cluster c = domain.getClusterForInstance(target);

        if (c == null) {
            report.setMessage("ServerNotDefined");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return null;
        } else {
            return c.getServerRefByRef(target);
        }
    }

    void updateLbEnabled(final ServerRef ref, final String v, final String tOut)
                    throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<ServerRef>() {
                @Override
                public Object run(ServerRef param) throws PropertyVetoException, TransactionFailure {
                    param.setLbEnabled(v);
                    if(v.equals("false") && tOut != null) {
                        param.setDisableTimeoutInMinutes(tOut);
                    }
                    return Boolean.TRUE;
                }
        }, ref);
    }

    void checkCommandStatus(AdminCommandContext context) throws CommandException {
        if(context.getActionReport().getActionExitCode() != ExitCode.SUCCESS) {
            throw new CommandException(context.getActionReport().getMessage());
        }
    }
}
