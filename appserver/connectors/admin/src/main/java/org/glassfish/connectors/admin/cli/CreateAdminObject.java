/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.admin.cli.ResourceConstants;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.connectors.admin.cli.CLIConstants.AOR.AOR_CLASS_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.AOR.AOR_CREATE_COMMAND_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.AOR.AOR_JNDI_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.AOR.AOR_RA_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.AOR.AOR_RES_TYPE;
import static org.glassfish.connectors.admin.cli.CLIConstants.DESCRIPTION;
import static org.glassfish.connectors.admin.cli.CLIConstants.PROPERTY;
import static org.glassfish.resources.admin.cli.ResourceConstants.ADMIN_OBJECT_CLASS_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.RES_ADAPTER;
import static org.glassfish.resources.admin.cli.ResourceConstants.RES_TYPE;

/**
 * Create Admin Object Command
 *
 * @author Jennifer Chou, Jagadish Ramu
 */
@TargetType(value={CommandTarget.DAS,CommandTarget.CONFIG, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE })
@ExecuteOn(RuntimeType.ALL)
@Service(name=AOR_CREATE_COMMAND_NAME)
@PerLookup
@I18n("create.admin.object")
public class CreateAdminObject implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateAdminObject.class);

    @Param(name=AOR_RES_TYPE)
    private String resType;

    @Param(name=AOR_CLASS_NAME, optional=true)
    private String className;

    @Param(name=AOR_RA_NAME, alias="resAdapter")
    private String raName;

    @Param(name=CLIConstants.ENABLED, optional=true, defaultValue="true")
    private Boolean enabled;

    @Param(name=DESCRIPTION, optional=true)
    private String description;

    @Param(name=PROPERTY, optional=true, separator=':')
    private Properties properties;

    @Param(optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;

    @Param(name=AOR_JNDI_NAME, primary=true)
    private String jndiName;

    @Inject
    private Domain domain;

    @Inject
    private Provider<AdminObjectManager>  adminObjectManagerProvider;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        ResourceAttributes attrList = new ResourceAttributes();
        attrList.set(RES_TYPE, resType);
        attrList.set(ADMIN_OBJECT_CLASS_NAME, className);
        attrList.set(ResourceConstants.ENABLED, enabled.toString());
        attrList.set(JNDI_NAME, jndiName);
        attrList.set(ServerTags.DESCRIPTION, description);
        attrList.set(RES_ADAPTER, raName);

        ResourceStatus rs;

        try {
            AdminObjectManager adminObjMgr = adminObjectManagerProvider.get();
            rs = adminObjMgr.create(domain.getResources(), attrList, properties, target);
        } catch(Exception e) {
            Logger.getLogger(CreateAdminObject.class.getName()).log(Level.SEVERE,
                    "Something went wrong in create-admin-object", e);
            String def = "Admin object: {0} could not be created, reason: {1}";
            report.setMessage(localStrings.getLocalString("create.admin.object.fail",
                    def, jndiName) + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        if (rs.getMessage() != null) {
                report.setMessage(rs.getMessage());
        }
        if (rs.getStatus() == ResourceStatus.FAILURE) {
            ec = ActionReport.ExitCode.FAILURE;
            if(rs.getMessage() == null) {
                 report.setMessage(localStrings.getLocalString("create.admin.object.fail",
                    "Admin object {0} creation failed", jndiName, ""));
            }
            if (rs.getException() != null) {
                report.setFailureCause(rs.getException());
            }
        }
        report.setActionExitCode(ec);
    }
}
