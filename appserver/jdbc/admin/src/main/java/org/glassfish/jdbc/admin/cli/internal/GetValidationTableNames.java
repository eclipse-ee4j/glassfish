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

package org.glassfish.jdbc.admin.cli.internal;

import com.sun.enterprise.config.serverbeans.Resources;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.jdbcruntime.service.JdbcAdminServiceImpl;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

/**
 * @author Jagadish Ramu
 */
@Service(name = "_get-validation-table-names")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({
    @RestEndpoint(configBean=Resources.class,
        opType=RestEndpoint.OpType.GET,
        path="get-validation-table-names",
        description="Get Validation Table Names")
})
public class GetValidationTableNames implements AdminCommand {

    @Inject
    private JdbcAdminServiceImpl jdbcAdminService;

    @Param
    private String poolName;

    @Param(name="appname", optional=true)
    private String applicationName;

    @Param(name="modulename", optional=true)
    private String moduleName;

    /**
     * @inheritDoc
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        try {
            PoolInfo poolInfo = new PoolInfo(poolName, applicationName, moduleName);
            Set<String> validationTableNames = jdbcAdminService.getValidationTableNames(poolInfo);
            Properties extraProperties = new Properties();
            extraProperties.put("validationTableNames", new ArrayList(validationTableNames));
            report.setExtraProperties(extraProperties);
        } catch (Exception e) {
            report.setMessage("_get-validation-table-names failed : " + e.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }

        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        report.setActionExitCode(ec);
    }
}
