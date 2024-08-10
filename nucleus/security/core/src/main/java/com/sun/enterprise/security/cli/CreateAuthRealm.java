/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.cli;

import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.security.SecurityConfigListener;
import com.sun.enterprise.security.common.Util;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

/**
 * CLI command to create JACC Provider
 *
 * Usage: create-auth-realm --classname realm_class [--terse=false] [--interactive=true] [--host localhost] [--port 4848|4849]
 * [--secure | -s] [--user admin_user] [--passwordfile file_name] [--property (name=value)[:name=value]*] [--echo=false]
 * [--target target(Default server)] auth_realm_name
 *
 * domain.xml element example <auth-realm name="file" classname="com.sun.enterprise.security.auth.realm.file.FileRealm">
 * <property name="file" value="${com.sun.aas.instanceRoot}/config/keyfile"/> <property name="jaas-context" value="fileRealm"/>
 * </auth-realm> Or
 * <auth-realm name="certificate" classname="com.sun.enterprise.security.auth.realm.certificate.CertificateRealm"> </auth-realm>
 *
 * @author Nandini Ektare
 */

@Service(name = "create-auth-realm")
@PerLookup
@I18n("create.auth.realm")
@ExecuteOn({ RuntimeType.DAS, RuntimeType.INSTANCE })
@TargetType({ CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG })
public class CreateAuthRealm implements AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateAuthRealm.class);

    @Param(name = "classname")
    private String className;

    @Param(name = "authrealmname", primary = true)
    private String authRealmName;

    @Param(optional = true, name = "property", separator = ':')
    private Properties properties;

    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    private String target;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    @Inject
    private Configs configs;

    @Inject
    private Domain domain;
    @Inject
    private SecurityConfigListener securityListener;

    //initialize the habitat in Util needed by Realm classes
    @Inject
    private Util util;

    @AccessRequired.NewChild(type = AuthRealm.class)
    private SecurityService securityService;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        config = CLIUtil.chooseConfig(domain, target, context.getActionReport());
        if (config == null) {
            return false;
        }
        securityService = config.getSecurityService();
        if (!ensureRealmIsNew(context.getActionReport())) {
            return false;
        }
        return true;
    }

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and the values the
     * parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        // No duplicate auth realms found. So add one.
        try {
            ConfigSupport.apply(new SingleConfigCode<SecurityService>() {

                @Override
                public Object run(SecurityService param) throws PropertyVetoException, TransactionFailure {
                    AuthRealm newAuthRealm = param.createChild(AuthRealm.class);
                    populateAuthRealmElement(newAuthRealm);
                    param.getAuthRealm().add(newAuthRealm);
                    //In case of cluster instances, this is required to
                    //avoid issues with the listener's callback method
                    SecurityConfigListener.authRealmCreated(config, newAuthRealm);
                    return newAuthRealm;

                }
            }, securityService);
        } catch (TransactionFailure e) {
            report.setMessage(localStrings.getLocalString("create.auth.realm.fail", "Creation of Authrealm {0} failed", authRealmName)
                + "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private void populateAuthRealmElement(AuthRealm newAuthRealm) throws PropertyVetoException, TransactionFailure {
        newAuthRealm.setName(authRealmName);
        newAuthRealm.setClassname(className);
        if (properties != null) {
            for (Object propname : properties.keySet()) {
                Property newprop = newAuthRealm.createChild(Property.class);
                newprop.setName((String) propname);
                String propValue = properties.getProperty((String) propname);
                newprop.setValue(propValue);
                newAuthRealm.getProperty().add(newprop);
            }
        }
    }

    private boolean ensureRealmIsNew(final ActionReport report) {
        if (!CLIUtil.isRealmNew(securityService, authRealmName)) {
            report.setMessage(localStrings.getLocalString("create.auth.realm.duplicatefound",
                "Authrealm named {0} exists. Cannot add duplicate AuthRealm.", authRealmName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }
        return true;
    }
}
