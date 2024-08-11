/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.batch;

import com.sun.enterprise.config.serverbeans.Domain;

import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Command to list batch jobs info
 *
 *         1      *             1      *
 * jobName --------> instanceId --------> executionId
 *
 * @author Mahesh Kannan
 */
@Service(name = "list-batch-job-steps")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.batch.job.steps")
@ExecuteOn({RuntimeType.DAS})
@TargetType({CommandTarget.CLUSTER, CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE})
@RestEndpoints({
        @RestEndpoint(configBean = Domain.class,
                opType = RestEndpoint.OpType.GET,
                path = "list-batch-job-steps",
                description = "List Batch Job Steps")
})
public class ListBatchJobStepsProxy
        extends AbstractListCommandProxy {

    @Param(primary = true)
    String executionId;

    @Override
    protected String getCommandName() {
        return "_ListBatchJobSteps";
    }
    @Override
    protected boolean preInvoke(AdminCommandContext ctx, ActionReport subReport) {
        if (executionId != null && !isLongNumber(executionId)) {
            subReport.setMessage("execution ID must be a number");
            return false;
        }

        return true;
    }

    protected void fillParameterMap(ParameterMap parameterMap) {
        super.fillParameterMap(parameterMap);
        if (executionId != null)
            parameterMap.add("DEFAULT", ""+executionId);
    }

    protected void postInvoke(AdminCommandContext context, ActionReport subReport) {
        Properties subProperties = subReport.getExtraProperties();
        Properties extraProps = context.getActionReport().getExtraProperties();
        if (subProperties.get("listBatchJobSteps") != null)
            extraProps.put("listBatchJobSteps", subProperties.get("listBatchJobSteps"));
    }

}
