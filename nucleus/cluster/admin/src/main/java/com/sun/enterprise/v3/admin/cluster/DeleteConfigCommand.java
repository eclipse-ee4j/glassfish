/*
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

package com.sun.enterprise.v3.admin.cluster;



import java.beans.PropertyVetoException;
import java.util.List;

import jakarta.inject.Inject;


import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.*;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.*;
import org.glassfish.api.admin.config.ReferenceContainer;
import org.glassfish.hk2.api.PerLookup;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.*;

/**
 *  This is a remote command that deletes a destination config.
 * Usage: delete-config
 configuration_name

 * @author Bhakti Mehta
 */
@Service(name = "delete-config")
@PerLookup
@I18n("delete.config.command")
@RestEndpoints({
    @RestEndpoint(configBean=Config.class,
        opType=RestEndpoint.OpType.POST, // TODO: Should be DELETE
        path="delete-config",
        description="Delete Config",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public final class DeleteConfigCommand implements AdminCommand {

    @Param(primary=true)
    String destConfig;

    @Inject
    Configs configs;

    @Inject
    Domain domain;


    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DeleteConfigCommand.class);

    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        //do not delete default-config
        if (destConfig.equals("default-config") ){
            report.setMessage(localStrings.getLocalString(
                    "Config.defaultConfig", "The default configuration template " +
                            "named default-config cannot be deleted."
                    ));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        //Get the config from the domain
        //does the config exist ?
        //if not return
        final Config config = domain.getConfigNamed(destConfig);
        if (config == null ){
            report.setMessage(localStrings.getLocalString(
                    "Config.noSuchConfig", "Config {0} does not exist.", destConfig));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        // check if the config in use by some other
        // ReferenceContainer -- if so just return...
        List<ReferenceContainer> refContainers = domain.getReferenceContainersOf(config);
        if(refContainers.size() >= 1)  {
            StringBuffer namesOfContainers = new StringBuffer();
            for (ReferenceContainer rc: refContainers)  {
                namesOfContainers.append(rc.getReference()).append(',');
            }
            report.setMessage(localStrings.getLocalString(
                    "Config.inUseConfig", "Config {0} is in use " +
                            "and must be referenced by no server instances or clusters",
                    destConfig,namesOfContainers));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;

        }
        try {
            ConfigSupport.apply(new SingleConfigCode<Configs>() {

                @Override
                public Object run(Configs c) throws PropertyVetoException, TransactionFailure {
                    List<Config> configList = c.getConfig();
                    configList.remove(config);
                    return null;
                }
            }, configs);

        } catch (TransactionFailure ex) {
            report.setMessage(
                    localStrings.getLocalString("Config.deleteConfigFailed",
                            "Unable to remove config {0} ", config) + " "
                            +ex.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(ex);
        }

    }

}
