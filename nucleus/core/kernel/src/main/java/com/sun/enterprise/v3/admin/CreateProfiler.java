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

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.Profiler;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;
import java.util.Map;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static org.glassfish.api.ActionReport.ExitCode.FAILURE;
import static org.glassfish.api.ActionReport.ExitCode.SUCCESS;

/**
 * Create Profiler Command
 *
 */
@Service(name="create-profiler")
@PerLookup
@I18n("create.profiler")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=JavaConfig.class,
        opType=RestEndpoint.OpType.POST,
        path="create-profiler",
        description="Create Profiler")
})
public class CreateProfiler implements AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateProfiler.class);

    @Param(optional=true)
    String classpath;

    @Param(optional=true, defaultValue="true")
    Boolean enabled;

    @Param(name="nativelibrarypath", optional=true)
    String nativeLibraryPath;

    @Param(name="profiler_name", primary=true)
    String name;

    @Param(name="property", optional=true, separator=':')
    Properties properties;

    @Param(name="target", optional=true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    String target;

    @Inject
    Target targetService;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    @AccessRequired.To("update")
    private JavaConfig javaConfig;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        config = CLIUtil.chooseConfig(targetService, config, target);
        javaConfig = config.getJavaConfig();
        return true;
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

        if (javaConfig.getProfiler() != null) {
            System.out.println("profiler exists. Please delete it first");
            report.setMessage(
                localStrings.getLocalString("create.profiler.alreadyExists",
                "profiler exists. Please delete it first"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<JavaConfig>() {

                @Override
                public Object run(JavaConfig param) throws PropertyVetoException, TransactionFailure {
                    Profiler newProfiler = param.createChild(Profiler.class);
                    newProfiler.setName(name);
                    newProfiler.setClasspath(classpath);
                    newProfiler.setEnabled(enabled.toString());
                    newProfiler.setNativeLibraryPath(nativeLibraryPath);
                    if (properties != null) {
                        for ( Map.Entry e : properties.entrySet()) {
                            Property prop = newProfiler.createChild(Property.class);
                            prop.setName((String)e.getKey());
                            prop.setValue((String)e.getValue());
                            newProfiler.getProperty().add(prop);
                        }
                    }
                    param.setProfiler(newProfiler);
                    return newProfiler;
                }
            }, javaConfig);

        } catch(TransactionFailure e) {
            report.setMessage(localStrings.getLocalString("create.profiler.fail", "{0} create failed ", name));
            report.setActionExitCode(FAILURE);
            report.setFailureCause(e);
        }

        report.setActionExitCode(SUCCESS);
    }
}
