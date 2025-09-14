/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;


/**
 * Create Custom Resource
 *
 */
@TargetType(value={CommandTarget.DAS,CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE })
@RestEndpoints({
        @RestEndpoint(configBean=Resources.class,
                opType=RestEndpoint.OpType.POST,
                path="create-custom-resource",
                description="create-custom-resource")
})
@org.glassfish.api.admin.ExecuteOn(RuntimeType.ALL)
@Service(name="create-custom-resource")
@PerLookup
@I18n("create.custom.resource")
public class CreateCustomResource implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(CreateCustomResource.class);

    @Param(name = "restype")
    private String resType;

    @Param(name = "factoryclass")
    private String factoryClass;

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
    private org.glassfish.resources.admin.cli.CustomResourceManager customResMgr;

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
        attrList.set("factory-class", factoryClass);
        attrList.set("res-type", resType);
        attrList.set(ResourceConstants.ENABLED, enabled.toString());
        attrList.set(JNDI_NAME, jndiName);
        attrList.set(ServerTags.DESCRIPTION, description);

        ResourceStatus rs;

        try {
            rs = customResMgr.create(domain.getResources(), attrList, properties, target);
        } catch(Exception e) {
            Logger.getLogger(CreateCustomResource.class.getName()).log(Level.SEVERE,
                    "Unable to create custom resource " + jndiName, e);
            String def = "Custom resource: {0} could not be created, reason: {1}";
            report.setMessage(localStrings.getLocalString("create.custom.resource.fail",
                    def, jndiName) + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        if (rs.getStatus() == ResourceStatus.FAILURE) {
            ec = ActionReport.ExitCode.FAILURE;
            if (rs.getMessage() == null) {
                 report.setMessage(localStrings.getLocalString("create.custom.resource.fail",
                    "Custom resource {0} creation failed", jndiName, ""));
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
}
