/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.logging.Logger;
import java.util.Map;
import java.util.Iterator;

import java.beans.PropertyVetoException;

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.config.*;
import org.glassfish.api.Param;
import org.glassfish.api.I18n;
import org.glassfish.api.ActionReport;
import com.sun.enterprise.util.LocalStringManagerImpl;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;

import org.glassfish.api.admin.*;
import org.glassfish.hk2.api.PerLookup;

/**
 * This is a remote command that enables lb-enabled attribute of an application
 * for cluster or instance
 * @author Yamini K B
 */
@Service(name = "configure-lb-weight")
@PerLookup
@I18n("configure.lb.weight")
@org.glassfish.api.admin.ExecuteOn(RuntimeType.DAS)
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.POST,
        path="configure-lb-weight",
        description="Configure LB Weight",
        params={
            @RestParam(name="target", value="$parent")
        }),
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.POST,
        path="configure-lb-weight",
        description="Configure LB Weight",
        params={
            @RestParam(name="target", value="$parent")
        })
})
public final class ConfigureLBWeightCommand extends LBCommandsBase
                                            implements AdminCommand {

    @Param(optional=false)
    String cluster;

    @Param(primary=true)
    String weights;

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(ConfigureLBWeightCommand.class);


    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        final Logger logger = context.getLogger();

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        Map<String,Integer> instanceWeights = null;
        try {
            instanceWeights = getInstanceWeightsMap(weights);
        } catch (CommandException ce) {
            report.setMessage(localStrings.getLocalString("InvalidWeightValue", "Invalid weight value"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(ce);
            return;
        }

        Cluster cl = domain.getClusterNamed(cluster);
        if ( cl == null){
            String msg = localStrings.getLocalString("NoSuchCluster", "No such cluster {0}", cluster);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        for (Iterator it = instanceWeights.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String instance = (String)entry.getKey();

            try {
                Server s = domain.getServerNamed(instance);
                if (s == null) {
                    String msg = localStrings.getLocalString("NoSuchInstance", "No such instance {0}", instance);
                    logger.warning(msg);
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    report.setMessage(msg);
                    return;
                }

                Cluster c = domain.getClusterForInstance(s.getName());

                if (c == null) {
                   String msg = localStrings.getLocalString("InstanceDoesNotBelongToCluster",
                            "Instance {0} does not belong to cluster {1}.", instance,cluster);
                    logger.warning(msg);
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    report.setMessage(msg);
                    return;
                }

                if (!c.getName().equals(cluster)) {
                    String msg = localStrings.getLocalString("InstanceDoesNotBelongToCluster",
                            "Instance {0} does not belong to cluster {1}.", instance,cluster);
                    logger.warning(msg);
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    report.setMessage(msg);
                    return;
                }
                updateLBWeight(s, entry.getValue().toString());
            } catch (TransactionFailure ex) {
                report.setMessage(ex.getMessage());
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setFailureCause(ex);
                return;
            }
        }

    }

    private void updateLBWeight(final Server s, final String w)
                                throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<Server>() {
                @Override
                public Object run(Server param) throws PropertyVetoException, TransactionFailure {
                    param.setLbWeight(w);
                    return Boolean.TRUE;
                }
            }, s);
    }
}
