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


import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.CopyConfig;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.beans.PropertyVetoException;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoint.OpType;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 *  This is a remote command that copies a config to a destination config.
 * Usage: copy-config
         [--systemproperties  (name=value)[:name=value]*]
        source_configuration_name destination_configuration_name
 * @author Bhakti Mehta
 */
@Service(name = "copy-config")
@I18n("copy.config.command")
@PerLookup
//        {"Configs", "copy-config", "POST", "copy-config", "Copy Config"},
@RestEndpoints({
    @RestEndpoint(configBean=Configs.class, opType=OpType.POST, path="copy-config", description="Copy Config")
})
public final class CopyConfigCommand extends CopyConfig {

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CopyConfigCommand.class);

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        if (configs.size() != 2) {
            report.setMessage(localStrings.getLocalString("Config.badConfigNames",
                    "You must specify a source and destination config."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        final String srcConfig = configs.get(0);
        final String destConfig = configs.get(1);
        //Get the config from the domain
        //does the src config exist
        final Config config = domain.getConfigNamed(srcConfig);
        if (config == null ){
            report.setMessage(localStrings.getLocalString(
                    "Config.noSuchConfig", "Config {0} does not exist.", srcConfig));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        //does dest config exist
        final Config destinationConfig = domain.getConfigNamed(destConfig);
        if (destinationConfig != null ){
            report.setMessage(localStrings.getLocalString(
                    "Config.configExists", "Config {0} already exists.", destConfig));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        //Copy the config
        final String configName = destConfig ;
        final Logger logger = context.getLogger();
        try {
            ConfigSupport.apply(new SingleConfigCode<Configs>(){
                @Override
                public Object run(Configs configs ) throws PropertyVetoException, TransactionFailure {
                    return copyConfig(configs,config,configName,logger);
                }
            }   ,domain.getConfigs());


        } catch (TransactionFailure e) {
            report.setMessage(
                localStrings.getLocalString(
                    "Config.copyConfigError",
                    "CopyConfig error caused by " ,
                 e.getLocalizedMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

}
