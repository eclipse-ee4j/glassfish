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

package com.sun.enterprise.v3.admin;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.SystemPropertyBag;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

/**
 * Create System Properties Command
 *
 * Adds or updates one or more system properties of the domain, configuration,
 * cluster, or server instance
 *
 * Usage: create-system-properties [--terse=false] [--echo=false] [--interactive=true]
 * [--host localhost] [--port 4848|4849] [--secure|-s=true] [--user admin_user]
 * [--passwordfile file_name] [--target target(Default server)] (name=value)[:name=value]*
 *
 * @author Jennifer Chou
 *
 */
@Service(name="create-system-properties")
@PerLookup
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType(value={CommandTarget.CLUSTER,
CommandTarget.CONFIG, CommandTarget.DAS, CommandTarget.DOMAIN, CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTERED_INSTANCE})
@I18n("create.system.properties")
public class CreateSystemProperties implements AdminCommand, AdminCommandSecurity.Preauthorization, AdminCommandSecurity.AccessCheckProvider {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateSystemProperties.class);

    @Param(optional=true, defaultValue=SystemPropertyConstants.DAS_SERVER_NAME)
    String target;

    @Param(name="name_value", primary=true, separator=':')
    Properties properties;

    @Inject
    Domain domain;

    private SystemPropertyBag spb;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        spb = CLIUtil.chooseTarget(domain, target);

        if (spb == null) {
            final ActionReport report = context.getActionReport();
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            String msg = localStrings.getLocalString("invalid.target.sys.props",
                    "Invalid target:{0}. Valid targets types are domain, config, cluster, default server, clustered instance, stand alone instance", target);
            report.setMessage(msg);
            return false;
        }

        return true;
    }

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        final Collection<AccessCheck> result = new ArrayList<>();
        result.add(new AccessCheck(AccessRequired.Util.resourceNameFromConfigBeanProxy(spb), "update"));
        return result;
    }

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        String sysPropName = "";
        try {
            for (final Object key : properties.keySet()) {
                final String propName = (String) key;
                sysPropName = propName;

                // skip create-system property requests that do not change the
                // value of an existing property
                if (spb.containsProperty(sysPropName) &&
                    spb.getSystemProperty(sysPropName).getValue().equals(properties.getProperty(propName))) {
                    continue;
                }
                ConfigSupport.apply(new SingleConfigCode<SystemPropertyBag>() {

                    @Override
                    public Object run(SystemPropertyBag param) throws PropertyVetoException, TransactionFailure {

                        // update existing system property
                        for (SystemProperty sysProperty : param.getSystemProperty()) {
                            if (sysProperty.getName().equals(propName)) {
                                Transaction t = Transaction.getTransaction(param);
                                sysProperty = t.enroll(sysProperty);
                                sysProperty.setValue(properties.getProperty(propName));
                                return sysProperty;
                            }
                        }

                        // create system-property
                        SystemProperty newSysProp = param.createChild(SystemProperty.class);
                        newSysProp.setName(propName);
                        newSysProp.setValue(properties.getProperty(propName));
                        param.getSystemProperty().add(newSysProp);
                        return newSysProp;
                    }
                }, spb);
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            }
        } catch(TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("create.system.properties.failed",
                    "System property {0} creation failed", sysPropName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        } catch(Exception e) {
            report.setMessage(localStrings.getLocalString("create.system.properties.failed",
                    "System property {0} creation failed", sysPropName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}
