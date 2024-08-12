/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.connectors.admin.cli;

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.util.HashMap;
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
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.connectors.config.ResourceAdapterConfig;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.connectors.admin.cli.CLIConstants.OBJECT_TYPE;
import static org.glassfish.connectors.admin.cli.CLIConstants.PROPERTY;
import static org.glassfish.connectors.admin.cli.CLIConstants.RAC.RAC_CREATE_RAC_COMMAND;
import static org.glassfish.connectors.admin.cli.CLIConstants.RAC.RAC_RA_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.RAC.RAC_THREAD_POOL_ID;
import static org.glassfish.connectors.admin.cli.CLIConstants.TARGET;
import static org.glassfish.resources.admin.cli.ResourceConstants.RESOURCE_ADAPTER_CONFIG_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.THREAD_POOL_IDS;

/**
 * Create RA Config Command
 *
 */
@ExecuteOn(RuntimeType.ALL)
@Service(name=RAC_CREATE_RAC_COMMAND)
@PerLookup
@I18n("create.resource.adapter.config")
public class CreateResourceAdapterConfig implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(CreateResourceAdapterConfig.class);

    @Param(name=RAC_RA_NAME, primary=true)
    private String raName;

    @Param(name=PROPERTY, optional=true, separator=':')
    private Properties properties;

    @Param(name = TARGET, optional = true, obsolete = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;

    @Param(name=RAC_THREAD_POOL_ID, optional=true, alias="threadPoolIds")
    private String threadPoolIds;

    @Param(name=OBJECT_TYPE, defaultValue="user", optional=true)
    private String objectType;

    @Inject
    private Applications applications;

    @Inject
    private Domain domain;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        HashMap<String, String> attrList = new HashMap<>();
        attrList.put(RESOURCE_ADAPTER_CONFIG_NAME, raName);
        attrList.put(THREAD_POOL_IDS, threadPoolIds);
        attrList.put(ServerTags.OBJECT_TYPE, objectType);

        ResourceStatus rs;

        //TODO ASR : need similar validation while creating app-scoped-resource of resource-adapter-config
        String appName = raName;
        if (!ConnectorsUtil.isStandAloneRA(raName)) {
            appName = ConnectorsUtil.getApplicationNameOfEmbeddedRar(raName);

            Application application = applications.getApplication(appName);
            if(application != null){
                //embedded RAR
                String resourceAdapterName = ConnectorsUtil.getRarNameFromApplication(raName);
                Module module = application.getModule(resourceAdapterName);
                if(module != null){
                    Resources msr = module.getResources();
                    if(msr != null){
                        if(hasDuplicate(msr, report)) {
                            return;
                        }
                    }
                }
            }
        }else{
            //standalone RAR
            Application application = applications.getApplication(appName);
            if(application != null){
                Resources appScopedResources = application.getResources();
                if(appScopedResources != null){
                    if(hasDuplicate(appScopedResources, report)) {
                        return;
                    }
                }
            }
        }

        ResourceAdapterConfigManager resAdapterConfigMgr = new ResourceAdapterConfigManager();
        try {
            rs = resAdapterConfigMgr.create(domain.getResources(), attrList, properties, target);
        } catch (Exception ex) {
            Logger.getLogger(CreateResourceAdapterConfig.class.getName()).log(
                    Level.SEVERE,
                    "Unable to create resource adapter config for " + raName, ex);
            String def = "Resource adapter config: {0} could not be created, reason: {1}";
            report.setMessage(localStrings.getLocalString("create.resource.adapter.config.fail",
                    def, raName) + " " + ex.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(ex);
            return;
        }
        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        if (rs.getStatus() == ResourceStatus.FAILURE) {
            ec = ActionReport.ExitCode.FAILURE;
            if (rs.getMessage() != null) {
                report.setMessage(rs.getMessage());
            } else {
                 report.setMessage(localStrings.getLocalString("create.resource.adapter.config.fail",
                    "Resource adapter config {0} creation failed", raName, ""));
            }
            if (rs.getException() != null) {
                report.setFailureCause(rs.getException());
            }
        }
        report.setActionExitCode(ec);
    }

    private boolean hasDuplicate(Resources resources, ActionReport report) {
        final SimpleJndiName jndiName = new SimpleJndiName(raName);
        if (resources.getResourceByName(ResourceAdapterConfig.class, jndiName) != null) {
            String msg = localStrings.getLocalString("create.resource.adapter.config.duplicate",
                    "Resource adapter config already exists for RAR", jndiName);
            report.setMessage(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return true;
        }
        return false;
    }
}
