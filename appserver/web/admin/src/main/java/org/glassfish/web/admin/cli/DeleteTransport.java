/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.web.admin.cli;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.Transport;
import org.glassfish.grizzly.config.dom.Transports;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.glassfish.web.admin.LogFacade;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Delete Transport command
 *
 */
@Service(name="delete-transport")
@PerLookup
@I18n("delete.transport")
@org.glassfish.api.admin.ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
public class DeleteTransport implements AdminCommand {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    @Param(name="transportname", primary=true)
    String transportName;

    Transport transportToBeRemoved = null;

    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    @Inject
    Domain domain;

    @Inject
    ServiceLocator services;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        Target targetUtil = services.getService(Target.class);
        Config newConfig = targetUtil.getConfig(target);
        if (newConfig!=null) {
            config = newConfig;
        }
        ActionReport report = context.getActionReport();

        NetworkConfig networkConfig = config.getNetworkConfig();
        Transports transports = networkConfig.getTransports();

        try {
            for (Transport transport : transports.getTransport()) {
                if (transportName.equalsIgnoreCase(transport.getName())) {
                    transportToBeRemoved = transport;
                }
            }

            if (transportToBeRemoved == null) {
                report.setMessage(MessageFormat.format(rb.getString(LogFacade.DELETE_TRANSPORT_NOT_EXISTS), transportName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            // check if the transport to be deleted is being used by
            // any network listener

            List<NetworkListener> nwlsnrList =
                transportToBeRemoved.findNetworkListeners();
            for (NetworkListener nwlsnr : nwlsnrList) {
              if (transportToBeRemoved.getName().equals(nwlsnr.getTransport())) {
                  report.setMessage(
                          MessageFormat.format(
                                  rb.getString(LogFacade.DELETE_TRANSPORT_BEINGUSED),
                                  transportName,
                                  nwlsnr.getName()));
                  report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                  return;
              }
            }

            ConfigSupport.apply(new SingleConfigCode<Transports>() {
                public Object run(Transports param) {
                    param.getTransport().remove(transportToBeRemoved);
                    return transportToBeRemoved;
                }
            }, transports);

        } catch(TransactionFailure e) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.DELETE_TRANSPORT_FAIL), transportName) +
                    "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
