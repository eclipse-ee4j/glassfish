/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.web.admin.cli;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
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
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Protocols;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.glassfish.web.admin.LogFacade;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Command to delete &lt;http-redirect&gt; elements within a specific
 * <code>protocol</code>.
 */
@Service(name = "delete-http-redirect")
@PerLookup
@I18n("delete.http.redirect")
@org.glassfish.api.admin.ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.DELETE,
        path="delete-http-redirect",
        description="delete-http-redirect"),
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.DELETE,
        path="delete-http-redirect",
        description="delete-http-redirect")
})
public class DeleteHttpRedirect implements AdminCommand {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    @Param(name = "protocolname", primary = true)
    String protocolName;

    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    @Inject
    Domain domain;

    @Inject
    ServiceLocator services;


    // ----------------------------------------------- Methods from AdminCommand


    @Override
    public void execute(AdminCommandContext context) {
        Target targetUtil = services.getService(Target.class);
        Config newConfig = targetUtil.getConfig(target);
        if (newConfig != null) {
            config = newConfig;
        }
        Protocol protocolToBeRemoved = null;
        ActionReport report = context.getActionReport();
        NetworkConfig networkConfig = config.getNetworkConfig();
        Protocols protocols = networkConfig.getProtocols();
        try {
            for (Protocol protocol : protocols.getProtocol()) {
                if (protocolName.equalsIgnoreCase(protocol.getName())) {
                    protocolToBeRemoved = protocol;
                }
            }
            if (protocolToBeRemoved == null) {
                report.setMessage(MessageFormat.format(rb.getString(LogFacade.DELETE_HTTP_NOTEXISTS), protocolName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
            // check if the protocol whose http to be deleted is being used by
            // any network listener, then do not delete it.
            List<NetworkListener> nwlsnrList =
                    protocolToBeRemoved.findNetworkListeners();
            for (NetworkListener nwlsnr : nwlsnrList) {
                if (protocolToBeRemoved.getName().equals(nwlsnr.getProtocol())) {
                    report.setMessage(
                            MessageFormat.format(
                                    rb.getString(LogFacade.DELETE_PROTOCOL_BEING_USED),
                                    protocolName, nwlsnr.getName()));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
            ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                public Object run(Protocol param) {
                    param.setHttpRedirect(null);
                    return null;
                }
            }, protocolToBeRemoved);

        } catch (TransactionFailure e) {
            report.setMessage(
                    MessageFormat.format(rb.getString(LogFacade.DELETE_HTTP_FAIL), protocolName) +
                    e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

}
