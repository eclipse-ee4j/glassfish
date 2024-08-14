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
import java.util.List;
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
import org.glassfish.grizzly.config.dom.PortUnification;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.ProtocolFinder;
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

@Service(name = "create-protocol-finder")
@PerLookup
@I18n("create.protocol.finder")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=Protocol.class,
        opType=RestEndpoint.OpType.POST,
        path="create-protocol-finder",
        description="Create",
        params={
            @RestParam(name="protocol", value="$parent")
        })
})
public class CreateProtocolFinder implements AdminCommand {
    @Param(name = "name", primary = true)
    String name;
    @Param(name = "protocol", optional = false)
    String protocolName;
    @Param(name = "targetprotocol", optional = false)
    String targetName;
    @Param(name = "classname", optional = false)
    String classname;
    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;
    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;
    @Inject
    Domain domain;
    private ActionReport report;
    @Inject
    ServiceLocator services;
    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    @Override
    public void execute(AdminCommandContext context) {
        Target targetUtil = services.getService(Target.class);
        Config newConfig = targetUtil.getConfig(target);
        if (newConfig!=null) {
            config = newConfig;
        }
        report = context.getActionReport();
        final Protocols protocols = config.getNetworkConfig().getProtocols();
        final Protocol protocol = protocols.findProtocol(protocolName);
        final Protocol target = protocols.findProtocol(targetName);
        try {
            validate(protocol, LogFacade.CREATE_HTTP_FAIL_PROTOCOL_NOT_FOUND, protocolName);
            validate(target, LogFacade.CREATE_HTTP_FAIL_PROTOCOL_NOT_FOUND, targetName);
            final Class<?> finderClass = Thread.currentThread().getContextClassLoader().loadClass(classname);
            if(!org.glassfish.grizzly.portunif.ProtocolFinder.class.isAssignableFrom(finderClass)) {
                report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_PORTUNIF_FAIL_NOTFINDER), name, classname));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
            PortUnification unif = (PortUnification)ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                @Override
                public Object run(Protocol param) throws PropertyVetoException, TransactionFailure {
                         PortUnification pu = param.getPortUnification();
                          if(pu == null) {
                              pu = param.createChild(PortUnification .class);
                                param.setPortUnification(pu);
                          }
                          return pu;
                }
            }, protocol);
            ConfigSupport.apply(new SingleConfigCode<PortUnification>() {
                @Override
                public Object run(PortUnification param) throws PropertyVetoException, TransactionFailure {
                    final List<ProtocolFinder> list = param.getProtocolFinder();
                    for (ProtocolFinder finder : list) {
                        if (name.equals(finder.getName())) {
                            throw new TransactionFailure(
                                String.format("A protocol finder named %s already exists.", name));
                        }
                    }
                    final ProtocolFinder finder = param.createChild(ProtocolFinder.class);
                    finder.setName(name);
                    finder.setProtocol(targetName);
                    finder.setClassname(classname);
                    list.add(finder);
                    return null;
                }
            }, unif);
        } catch (ValidationFailureException e) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
            report.setMessage(MessageFormat.format(
                    rb.getString(LogFacade.CREATE_PORTUNIF_FAIL),
                    name,
                    e.getMessage() == null ? "No reason given" : e.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
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
