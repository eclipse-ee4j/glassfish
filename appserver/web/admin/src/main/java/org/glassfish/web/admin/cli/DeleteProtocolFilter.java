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

package org.glassfish.web.admin.cli;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.ProtocolChain;
import org.glassfish.grizzly.config.dom.ProtocolChainInstanceHandler;
import org.glassfish.grizzly.config.dom.ProtocolFilter;
import org.glassfish.grizzly.config.dom.Protocols;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.glassfish.web.admin.LogFacade;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

@Service(name = "delete-protocol-filter")
@PerLookup
@I18n("delete.protocol.filter")
@org.glassfish.api.admin.ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=Protocol.class,
        opType=RestEndpoint.OpType.DELETE,
        path="delete-protocol-filter",
        description="Delete",
        params={
            @RestParam(name="protocol", value="$parent")
        })
})
public class DeleteProtocolFilter implements AdminCommand {
    @Param(name = "name", primary = true)
    String name;
    @Param(name = "protocol", optional = false)
    String protocolName;
    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    String target;
    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;
    @Inject
    Domain domain;
    @Inject
    ServiceLocator services;
    private ActionReport report;

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();


    @Override
    public void execute(AdminCommandContext context) {
        Target targetUtil = services.getService(Target.class);
        Config newConfig = targetUtil.getConfig(target);
        if (newConfig!=null) {
            config = newConfig;
        }
        report = context.getActionReport();
        try {
            final Protocols protocols = config.getNetworkConfig().getProtocols();
            final Protocol protocol = protocols.findProtocol(protocolName);
            validate(protocol, LogFacade.CREATE_HTTP_FAIL_PROTOCOL_NOT_FOUND, protocolName);
            ProtocolChainInstanceHandler handler = getHandler(protocol);
            ProtocolChain chain = getChain(handler);
            ConfigSupport.apply(new SingleConfigCode<ProtocolChain>() {
                @Override
                public Object run(ProtocolChain param) throws PropertyVetoException, TransactionFailure {
                    final List<ProtocolFilter> list = param.getProtocolFilter();
                    List<ProtocolFilter> newList = new ArrayList<ProtocolFilter>();
                    for (final ProtocolFilter filter : list) {
                        if (!name.equals(filter.getName())) {
                            newList.add(filter);
                        }
                    }
                    if (list.size() == newList.size()) {
                        throw new RuntimeException(
                            String.format("No filter named %s found for protocol %s", name, protocolName));
                    }
                    param.setProtocolFilter(newList);
                    return null;
                }
            }, chain);
            cleanChain(chain);
            cleanHandler(handler);
        } catch (ValidationFailureException e) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
            report.setMessage(
                    MessageFormat.format(rb.getString(LogFacade.DELETE_FAIL),
                            name,
                            e.getMessage() == null ? "No reason given" : e.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;

        }
    }

    private ProtocolChain getChain(ProtocolChainInstanceHandler handler) throws TransactionFailure {
        ProtocolChain chain = handler.getProtocolChain();
        if ((chain == null) && (report != null)) {
            report.setMessage(
                    MessageFormat.format(rb.getString(LogFacade.NOT_FOUND),
                            "protocol-chain",
                            handler.getParent(Protocol.class).getName()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
        return chain;
    }

    private void cleanChain(ProtocolChain chain) throws TransactionFailure {
        if (chain != null && chain.getProtocolFilter().isEmpty()) {
            ConfigSupport.apply(new SingleConfigCode<ProtocolChainInstanceHandler>() {
                @Override
                public Object run(ProtocolChainInstanceHandler param)
                    throws PropertyVetoException, TransactionFailure {
                    param.setProtocolChain(null);
                    return null;
                }
            }, chain.getParent(ProtocolChainInstanceHandler.class));
        }
    }

    private ProtocolChainInstanceHandler getHandler(Protocol protocol) throws TransactionFailure {
        ProtocolChainInstanceHandler handler = protocol.getProtocolChainInstanceHandler();
        if ((handler == null) && (report != null)) {
            report.setMessage(
                    MessageFormat.format(rb.getString(LogFacade.NOT_FOUND),
                            "protocol-chain-instance-handler",
                            protocol.getName()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
        return handler;
    }

    private void cleanHandler(ProtocolChainInstanceHandler handler) throws TransactionFailure {
        if (handler != null && handler.getProtocolChain() == null) {
            ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                @Override
                public Object run(Protocol param)
                    throws PropertyVetoException, TransactionFailure {
                    param.setProtocolChainInstanceHandler(null);
                    return null;

                }
            }, handler.getParent(Protocol.class));
        }
    }

    private void validate(ConfigBeanProxy check, String key, String... arguments)
        throws ValidationFailureException {
        if ((check == null) && (report != null)) {
            report.setMessage(MessageFormat.format(rb.getString(key), arguments));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            throw new ValidationFailureException();
        }
    }

    private static class ValidationFailureException extends Exception {
    }
}
