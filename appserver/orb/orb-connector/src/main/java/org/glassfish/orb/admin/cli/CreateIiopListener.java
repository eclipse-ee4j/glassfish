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

package org.glassfish.orb.admin.cli;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.glassfish.orb.admin.config.IiopListener;
import org.glassfish.orb.admin.config.IiopService;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

/**
 * Create IioP Listener Command
 *
 */
@Service(name="create-iiop-listener")
@PerLookup
@I18n("create.iiop.listener")
@ExecuteOn(value={RuntimeType.DAS,RuntimeType.INSTANCE})
@TargetType(value={CommandTarget.CLUSTER,CommandTarget.CONFIG,
    CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE }
)
public class CreateIiopListener implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new
            LocalStringManagerImpl(CreateIiopListener.class);

    @Param( name="target", optional=true,
        defaultValue=SystemPropertyConstants.DAS_SERVER_NAME)
    String target ;

    @Param(name="listeneraddress", alias="address")
    String listeneraddress;

    @Param(name="iiopport", optional=true, alias="port", defaultValue="1072")
    String iiopport;

    @Param(optional=true, defaultValue="true")
    Boolean enabled;

    @Param(name="securityenabled", optional=true, defaultValue="false", alias="security-enabled")
    Boolean securityenabled;

    @Param(name="property", optional=true, separator=':')
    Properties properties;

    @Param(name="listener_id", primary=true, alias="id")
    String listener_id;

    @Inject
    Configs configs;

    @Inject
    Servers servers;

    @Inject
    ServiceLocator services ;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the parameter names and the values the parameter values
     *
     * @param context information
     */

    @Override
    public void execute(AdminCommandContext context) {
        final Target targetUtil = services.getService(Target.class ) ;
        final Config config = targetUtil.getConfig(target ) ;
        final ActionReport report = context.getActionReport();

        IiopService iiopService = config.getExtensionByType(IiopService.class);

        // ensure we don't already have one of this name
        // check port uniqueness, only for same address
        for (IiopListener listener : iiopService.getIiopListener()) {
            if (listener.getId().equals(listener_id)) {
                String ls = localStrings.getLocalString(
                    "create.iiop.listener.duplicate",
                    "IIOP Listener named {0} already exists.", listener_id);
                report.setMessage(ls);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            if (listener.getAddress().trim().equals(listeneraddress) &&
                    listener.getPort().trim().equals((iiopport))) {
                String def = "Port [{0}] is already taken by another listener: "
                    + "[{1}] for address [{2}], choose another port.";
                String ls = localStrings.getLocalString(
                    "create.iiop.listener.port.occupied",
                    def, iiopport, listener.getId(), listeneraddress);
                report.setMessage(ls);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        try {
            ConfigSupport.apply(
                new SingleConfigCode<IiopService>() {
                    @Override
                    public Object run(IiopService param)
                        throws PropertyVetoException, TransactionFailure {

                        IiopListener newListener = param.createChild(
                            IiopListener.class);

                        newListener.setId(listener_id);
                        newListener.setAddress(listeneraddress);
                        newListener.setPort(iiopport);
                        newListener.setSecurityEnabled(securityenabled.toString());
                        newListener.setEnabled(enabled.toString());

                        //add properties
                        if (properties != null) {
                            for ( java.util.Map.Entry entry : properties.entrySet()) {
                                Property property =
                                    newListener.createChild(Property.class);
                                property.setName((String)entry.getKey());
                                property.setValue((String)entry.getValue());
                                newListener.getProperty().add(property);
                            }
                        }

                        param.getIiopListener().add(newListener);
                        return newListener;
                    }
                }, iiopService);

            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch(TransactionFailure e) {
            String actual = e.getMessage();
            String def = "Creation of: " + listener_id + "failed because of: "
                + actual;
            String msg = localStrings.getLocalString(
                "create.iiop.listener.fail", def, listener_id, actual);
            report.setMessage(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}

