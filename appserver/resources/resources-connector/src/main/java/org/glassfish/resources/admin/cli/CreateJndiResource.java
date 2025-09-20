/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resources.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.resources.admin.cli.ResourceConstants.ENABLED;
import static org.glassfish.resources.admin.cli.ResourceConstants.FACTORY_CLASS;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_LOOKUP;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.RES_TYPE;

/**
 * Create Jndi Resource
 */
@TargetType(value={CommandTarget.DAS,CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE })
@RestEndpoints({
        @RestEndpoint(configBean=Resources.class,
                opType=RestEndpoint.OpType.POST,
                path="create-jndi-resource",
                description="create-jndi-resource")
})
@org.glassfish.api.admin.ExecuteOn(RuntimeType.ALL)
@Service(name = "create-jndi-resource")
@PerLookup
@I18n("create.jndi.resource")
public class CreateJndiResource implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(CreateJndiResource.class);

    @Param(name = "restype")
    private String resType;

    @Param(name = "factoryclass")
    private String factoryClass;

    @Param(name = "jndilookupname")
    private String jndiLookupName;

    @Param(optional = true, defaultValue = "true")
    private Boolean enabled;

    @Param(optional = true)
    private String description;

    @Param(name = "property", optional = true, separator = ':')
    private Properties properties;

    @Param(optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;

    @Param(name = "jndi_name", primary = true)
    private String jndiName;

    @Inject
    private Domain domain;

    @Inject
    private org.glassfish.resources.admin.cli.JndiResourceManager jndiResManager;


    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the parameter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        ResourceAttributes attrList = new ResourceAttributes();
        attrList.set(FACTORY_CLASS, factoryClass);
        attrList.set(RES_TYPE, resType);
        attrList.set(JNDI_LOOKUP, jndiLookupName);
        attrList.set(ENABLED, enabled.toString());
        attrList.set(JNDI_NAME, jndiName);
        attrList.set(ServerTags.DESCRIPTION, description);

        ResourceStatus rs;

        try {
            rs = jndiResManager.create(domain.getResources(), attrList, properties, target);
        } catch(Exception e) {
            Logger.getLogger(CreateJndiResource.class.getName()).log(Level.SEVERE,
                    "Unable to create jndi resource " + jndiName, e);
            String def = "jndi resource: {0} could not be created, reason: {1}";
            report.setMessage(localStrings.getLocalString("create.jndi.resource.fail",
                    def, jndiName) + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        if (rs.getStatus() == ResourceStatus.FAILURE) {
            ec = ActionReport.ExitCode.FAILURE;
            if (rs.getMessage() == null) {
                 report.setMessage(localStrings.getLocalString("create.jndi.resource.fail",
                    "jndi resource {0} creation failed", jndiName, ""));
            }
            if (rs.getException() != null) {
                report.setFailureCause(rs.getException());
            }
        }
        if(rs.getMessage() != null){
            report.setMessage(rs.getMessage());
        }
        report.setActionExitCode(ec);
    }


    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
  /*  public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        // ensure we don't already have one of this name
        if (domain.getResources().getResourceByName(BindableResource.class, jndiName) != null){
            report.setMessage(I18N.getLocalString(
                    "create.jndi.resource.duplicate.1",
                    "Resource named {0} already exists.",
                    jndiName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        Boolean enabledValueForTarget = enabled;
        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException,
                        TransactionFailure {

                    ExternalJndiResource newResource =
                            param.createChild(ExternalJndiResource.class);
                    newResource.setJndiName(jndiName);
                    newResource.setFactoryClass(factoryClass);
                    newResource.setResType(resType);
                    newResource.setJndiLookupName(jndiLookupName);
                    if(target != null){
                        enabled = Boolean.valueOf(
                                resourceUtil.computeEnabledValueForResourceBasedOnTarget(enabled.toString(), target));
                    }
                    newResource.setEnabled(enabled.toString());
                    if (description != null) {
                        newResource.setDescription(description);
                    }
                    if (properties != null) {
                        for (java.util.Map.Entry e : properties.entrySet()) {
                            Property prop = newResource.createChild(
                                    Property.class);
                            prop.setName((String) e.getKey());
                            prop.setValue((String) e.getValue());
                            newResource.getProperty().add(prop);
                        }
                    }
                    param.getResources().add(newResource);
                    return newResource;
                }
            }, domain.getResources());

            resourceUtil.createResourceRef(jndiName, enabledValueForTarget.toString(), target);
            report.setMessage(I18N.getLocalString(
                    "create.jndi.resource.success",
                    "JNDI resource {0} created.", jndiName));
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (TransactionFailure tfe) {
            report.setMessage(I18N.getLocalString(
                    "create.jndi.resource.fail",
                    "Unable to create JNDI resource {0}.", jndiName) +
                    " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }
    } */
}
