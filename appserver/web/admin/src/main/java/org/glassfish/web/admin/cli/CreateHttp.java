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

package org.glassfish.web.admin.cli;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.grizzly.config.dom.FileCache;
import org.glassfish.grizzly.config.dom.Http;
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
 * Command to create http element within a protocol element
 *
 * Sample Usage : create-http protocol_name
 *
 * domain.xml element example
 *
 * &lt;http max-connections=&quot;250&quot; default-virtual-server=&quot;server&quot; server-name=&quot;&quot;&gt; &lt;file-cache enabled=&quot;false&quot; /&gt; &lt;/http&gt;
 *
 * @author Justin Lee
 */
@Service(name = "create-http")
@PerLookup
@I18n("create.http")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=Protocol.class,
        opType=RestEndpoint.OpType.POST,
        path="create-http",
        description="Create",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public class CreateHttp implements AdminCommand {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    @Param(name = "protocolname", primary = true)
    String protocolName;
    @Param(name = "request-timeout-seconds", optional = true, alias="requestTimeoutSeconds")
    String requestTimeoutSeconds;
    @Param(name = "timeout-seconds", defaultValue = "30", optional = true, alias="timeoutSeconds")
    String timeoutSeconds;
    @Param(name = "max-connection", defaultValue = "256", optional = true, alias="maxConnections")
    String maxConnections;
    @Param(name = "default-virtual-server", alias="defaultVirtualServer")
    String defaultVirtualServer;
    @Param(name = "dns-lookup-enabled", defaultValue = "false", optional = true, alias="dnsLookupEnabled")
    Boolean dnsLookupEnabled = false;
    @Param(name = "servername", optional = true, alias="serverName")
    String serverName;
    @Param(name = "xpowered", optional = true, defaultValue = "true", alias="xpoweredBy")
    Boolean xPoweredBy = false;
    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;
    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;
    @Inject
    ServiceLocator services;
    @Inject
    Domain domain;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the parameter names and the values the parameter
     * values.
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        Target targetUtil = services.getService(Target.class);
        Config newConfig = targetUtil.getConfig(target);
        if (newConfig!=null) {
            config = newConfig;
        }
        final ActionReport report = context.getActionReport();
        // check for duplicates
        Protocols protocols = config.getNetworkConfig().getProtocols();
        Protocol protocol = null;
        for (Protocol p : protocols.getProtocol()) {
            if(protocolName.equals(p.getName())) {
                protocol = p;
            }
        }
        if (protocol == null) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_HTTP_FAIL_PROTOCOL_NOT_FOUND), protocolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        if (protocol.getHttp() != null) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_HTTP_FAIL_DUPLICATE), protocolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;

        }
        // Add to the <network-config>
        try {
            ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                public Object run(Protocol param) throws TransactionFailure {
                    Http http = param.createChild(Http.class);
                    final FileCache cache = http.createChild(FileCache.class);
                    cache.setEnabled("false");
                    http.setFileCache(cache);
                    http.setDefaultVirtualServer(defaultVirtualServer);
                    http.setDnsLookupEnabled(dnsLookupEnabled == null ? null : dnsLookupEnabled.toString());
                    http.setMaxConnections(maxConnections);
                    http.setRequestTimeoutSeconds(requestTimeoutSeconds);
                    http.setTimeoutSeconds(timeoutSeconds);
                    http.setXpoweredBy(xPoweredBy == null ? null : xPoweredBy.toString());
                    http.setServerName(serverName);
                    param.setHttp(http);
                    return http;
                }
            }, protocol);
        } catch (TransactionFailure e) {
            report.setMessage(
                    MessageFormat.format(
                            rb.getString(LogFacade.CREATE_HTTP_REDIRECT_FAIL),
                            protocolName,
                            e.getMessage() == null ? "No reason given." : e.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
