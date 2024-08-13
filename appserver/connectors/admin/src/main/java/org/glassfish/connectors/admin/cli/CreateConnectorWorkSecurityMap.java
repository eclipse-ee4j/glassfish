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

package org.glassfish.connectors.admin.cli;

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.Map;
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
import org.glassfish.connectors.config.GroupMap;
import org.glassfish.connectors.config.PrincipalMap;
import org.glassfish.connectors.config.WorkSecurityMap;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.glassfish.connectors.admin.cli.CLIConstants.DESCRIPTION;
import static org.glassfish.connectors.admin.cli.CLIConstants.WSM.WSM_GROUPS_MAP;
import static org.glassfish.connectors.admin.cli.CLIConstants.WSM.WSM_MAP_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.WSM.WSM_PRINCIPALS_MAP;
import static org.glassfish.connectors.admin.cli.CLIConstants.WSM.WSM_RA_NAME;

/**
 * Create Connector Work Security Map
 *
 */
@ExecuteOn(RuntimeType.ALL)
@Service(name="create-connector-work-security-map")
@PerLookup
@I18n("create.connector.work.security.map")
public class CreateConnectorWorkSecurityMap implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(CreateConnectorWorkSecurityMap.class);

    @Param(name=WSM_RA_NAME)
    private String raName;

    @Param(name=WSM_PRINCIPALS_MAP, optional=true)
    private Properties principalsMap;

    @Param(name = WSM_GROUPS_MAP, optional=true)
    private Properties groupsMap;

    @Param(name=DESCRIPTION, optional=true)
    private String description;

    @Param(name= WSM_MAP_NAME, primary=true)
    private String mapName;

    @Inject
    private Domain domain;

    @Inject
    private Applications applications;


    //TODO common code replicated in ConnectorWorkSecurityMapManager
    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        if (mapName == null) {
            report.setMessage(localStrings.getLocalString(
                    "create.connector.work.security.map.noMapName",
                    "No mapname defined for connector work security map."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (raName == null) {
            report.setMessage(localStrings.getLocalString(
                    "create.connector.work.security.map.noRaName",
                    "No raname defined for connector work security map."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (principalsMap == null && groupsMap == null) {
            report.setMessage(localStrings.getLocalString(
                    "create.connector.work.security.map.noMap",
                    "No principalsmap or groupsmap defined for connector work security map."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (principalsMap != null && groupsMap != null) {
            report.setMessage(localStrings.getLocalString(
                    "create.connector.work.security.map.specifyPrincipalsOrGroupsMap",
                    "A work-security-map can have either (any number of) group mapping  " +
                    "or (any number of) principals mapping but not both. Specify" +
                    "--principalsmap or --groupsmap."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        // ensure we don't already have one of this name
        if (hasDuplicate(domain.getResources(), report)) return;

        //TODO ASR : need similar validation while creating app-scoped-resource of w-s-m
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
                        if(hasDuplicate(msr, report)) return;
                    }
                }
            }
        }else{
            //standalone RAR
            Application application = applications.getApplication(appName);
            if(application != null){
                Resources appScopedResources = application.getResources();
                if(appScopedResources != null){
                    if(hasDuplicate(appScopedResources, report)) return;
                }
            }
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException,
                        TransactionFailure {

                    WorkSecurityMap workSecurityMap =
                            param.createChild(WorkSecurityMap.class);
                    workSecurityMap.setName(mapName);
                    workSecurityMap.setResourceAdapterName(raName);

                    if (principalsMap != null) {
                        for (Map.Entry e : principalsMap.entrySet()) {
                            PrincipalMap principalMap = workSecurityMap.createChild(PrincipalMap.class);
                            principalMap.setEisPrincipal((String)e.getKey());
                            principalMap.setMappedPrincipal((String)e.getValue());
                            workSecurityMap.getPrincipalMap().add(principalMap);
                        }
                    } else if (groupsMap != null) {
                        for (Map.Entry e : groupsMap.entrySet()) {
                            GroupMap groupMap = workSecurityMap.createChild(GroupMap.class);
                            groupMap.setEisGroup((String)e.getKey());
                            groupMap.setMappedGroup((String)e.getValue());
                            workSecurityMap.getGroupMap().add(groupMap);
                        }
                    } else {
                        // no mapping
                    }

                    param.getResources().add(workSecurityMap);
                    return workSecurityMap;
                }
            }, domain.getResources());

        } catch (TransactionFailure tfe) {
            Logger.getLogger(CreateConnectorWorkSecurityMap.class.getName()).log(Level.SEVERE,
                    "create-connector-work-security-map failed", tfe);
            report.setMessage(localStrings.getLocalString(
                    "create.connector.work.security.map.fail",
                    "Unable to create connector work security map {0}.", mapName) +
                    " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
            return;
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private boolean hasDuplicate(Resources resources, ActionReport report) {
        for (Resource resource : resources.getResources()) {
            if (resource instanceof WorkSecurityMap) {
                if (((WorkSecurityMap) resource).getName().equals(mapName) &&
                    ((WorkSecurityMap) resource).getResourceAdapterName().equals(raName)){
                    report.setMessage(localStrings.getLocalString(
                            "create.connector.work.security.map.duplicate",
                            "A connector work security map named {0} for resource adapter {1} already exists.",
                            mapName, raName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return true;
                }
            }
        }
        return false;
    }
}
