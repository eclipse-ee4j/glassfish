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

package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.List;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.connectors.config.BackendPrincipal;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.connectors.config.SecurityMap;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.glassfish.connectors.admin.cli.CLIConstants.SM.SM_CREATE_COMMAND_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.SM.SM_MAPPED_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.SM.SM_MAPPED_PASSWORD;
import static org.glassfish.connectors.admin.cli.CLIConstants.SM.SM_MAP_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.SM.SM_POOL_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.SM.SM_PRINCIPALS;
import static org.glassfish.connectors.admin.cli.CLIConstants.SM.SM_USER_GROUPS;

/**
 * Create Connector SecurityMap command
 */
@org.glassfish.api.admin.ExecuteOn(RuntimeType.ALL)
@Service(name=SM_CREATE_COMMAND_NAME)
@PerLookup
@I18n("create.connector.security.map")
public class CreateConnectorSecurityMap extends ConnectorSecurityMap implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateConnectorSecurityMap.class);

    @Param(optional = true, obsolete = true)
    private String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Param(name = SM_POOL_NAME)
    private String poolName;

    @Param(name = SM_PRINCIPALS, optional = true)
    private List<String> principals;

    @Param(name = SM_USER_GROUPS, optional = true)
    private List<String> userGroups;

    @Param(name = SM_MAPPED_NAME)
    private String mappedusername;

    @Param(name=SM_MAPPED_PASSWORD, password = true, optional = true)
    private String mappedpassword;

    @Param(name = SM_MAP_NAME, primary = true)
    private String securityMapName;

    @Inject
    private Domain domain;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the parameter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        if (securityMapName == null) {
            report.setMessage(localStrings.getLocalString("create.connector.security.map.noSecurityMapName",
                    "No security map name specified"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (principals == null && userGroups == null) {
            report.setMessage
                    (localStrings.getLocalString("create.connector.security.map.noPrincipalsOrGroupsMap",
                    "Either the principal or the user group has to be specified while creating a security map." +
                            " Both cannot be null."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (principals != null && userGroups != null) {
            report.setMessage(localStrings.getLocalString("create.connector.security.map.specifyPrincipalsOrGroupsMap",
                    "A work-security-map can have either (any number of) group mapping or (any number of) principals" +
                            " mapping but not both. Specify --principals or --usergroups."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        Collection<ConnectorConnectionPool> ccPools =  domain.getResources().getResources(ConnectorConnectionPool.class);

        if (!doesPoolNameExist(poolName, ccPools)) {
            report.setMessage(localStrings.getLocalString("create.connector.security.map.noSuchPoolFound",
                    "Connector connection pool {0} does not exist. Please specify a valid pool name.", poolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (doesMapNameExist(poolName, securityMapName, ccPools)) {
            report.setMessage(localStrings.getLocalString("create.connector.security.map.duplicate",
                    "A security map named {0} already exists for connector connection pool {1}. Please give a" +
                            " different map name.",
                    securityMapName, poolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        //get all the security maps for this pool.....
        List<SecurityMap> maps = getAllSecurityMapsForPool(poolName, ccPools);

        if (principals != null) {
            for (String principal : principals) {
                if (isPrincipalExisting(principal, maps)) {
                    report.setMessage(localStrings.getLocalString("create.connector.security.map.principal_exists",
                            "The principal {0} already exists in connector connection pool {1}. Please give a " +
                                    "different principal name.",
                            principal, poolName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
        }
        if (userGroups != null) {
            for (String userGroup : userGroups) {
                if (isUserGroupExisting(userGroup, maps)) {
                    report.setMessage(localStrings.getLocalString("create.connector.security.map.usergroup_exists",
                            "The user-group {0} already exists in connector connection pool {1}. Please give a" +
                                    " different user-group name.",
                            userGroup, poolName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
        }

        ConnectorConnectionPool connPool = null;
        for (ConnectorConnectionPool ccp : ccPools) {
            if (ccp.getName().equals(poolName)) {
                connPool = ccp;
            }
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<ConnectorConnectionPool>() {

                public Object run(ConnectorConnectionPool ccp) throws PropertyVetoException, TransactionFailure {

                    List<SecurityMap> securityMaps = ccp.getSecurityMap();

                    SecurityMap newResource = ccp.createChild(SecurityMap.class);
                    newResource.setName(securityMapName);

                    if (principals != null) {
                        for (String p : principals) {
                            newResource.getPrincipal().add(p);
                        }
                    }

                    if (userGroups != null) {
                        for (String u : userGroups) {
                            newResource.getUserGroup().add(u);
                        }
                    }

                    BackendPrincipal backendPrincipal = newResource.createChild(BackendPrincipal.class);
                    backendPrincipal.setUserName(mappedusername);
                    if (mappedpassword != null && !mappedpassword.isEmpty()) {
                        backendPrincipal.setPassword(mappedpassword);
                    }
                    newResource.setBackendPrincipal(backendPrincipal);
                    securityMaps.add(newResource);
                    return newResource;
                }
            }, connPool);

        } catch (TransactionFailure tfe) {
            Object params[] = {securityMapName, poolName};
            report.setMessage(localStrings.getLocalString("create.connector.security.map.fail",
                    "Unable to create connector security map {0} for connector connection pool {1} ", params) +
                    " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
            return;
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
