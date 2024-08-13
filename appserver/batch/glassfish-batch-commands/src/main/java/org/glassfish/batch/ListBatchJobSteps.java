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

import com.ibm.jbatch.spi.TaggedJobExecution;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.ColumnFormatter;

import jakarta.batch.operations.JobOperator;
import jakarta.batch.operations.JobSecurityException;
import jakarta.batch.operations.NoSuchJobExecutionException;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.Metric;
import jakarta.batch.runtime.StepExecution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;

import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
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
@Service(name = "_ListBatchJobSteps")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("_ListBatchJobSteps")
@ExecuteOn(value = {RuntimeType.INSTANCE})
@TargetType(value = {CommandTarget.DAS, CommandTarget.CLUSTERED_INSTANCE, CommandTarget.STANDALONE_INSTANCE})
@RestEndpoints({
        @RestEndpoint(configBean = Domain.class,
                opType = RestEndpoint.OpType.GET,
                path = "_ListBatchJobSteps",
                description = "_List Batch Job Steps")
})
public class ListBatchJobSteps
    extends AbstractLongListCommand {

    private static final String NAME = "stepName";

    private static final String STEP_ID = "stepId";

    private static final String BATCH_STATUS = "batchStatus";

    private static final String EXIT_STATUS = "exitStatus";

    private static final String START_TIME = "startTime";

    private static final String END_TIME = "endTime";

    private static final String STEP_METRICS = "stepMetrics";

    @Param(primary = true)
    String executionId;

    @Override
    protected void executeCommand(AdminCommandContext context, Properties extraProps)
        throws Exception {

        ColumnFormatter columnFormatter = new ColumnFormatter(getDisplayHeaders());
        List<Map<String, Object>> jobExecutions = new ArrayList<>();
        extraProps.put("listBatchJobSteps", jobExecutions);
        for (StepExecution je : findStepExecutions()) {
            try {
                jobExecutions.add(handleJob(je, columnFormatter));
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Exception while getting jobExecution details: " + ex);
                logger.log(Level.FINE, "Exception while getting jobExecution details ", ex);
            }
        }
        context.getActionReport().setMessage(columnFormatter.toString());
    }

    @Override
    protected final String[] getAllHeaders() {
        return new String[] {
                NAME, STEP_ID, START_TIME, END_TIME, BATCH_STATUS, EXIT_STATUS, STEP_METRICS
        };
    }

    @Override
    protected final String[] getDefaultHeaders() {
        return new String[] {NAME, STEP_ID, START_TIME, END_TIME, BATCH_STATUS, EXIT_STATUS};
    }

    private List<StepExecution> findStepExecutions()
        throws JobSecurityException, NoSuchJobExecutionException {
        JobOperator jobOperator = AbstractListCommand.getJobOperatorFromBatchRuntime();
        JobExecution je = jobOperator.getJobExecution(Long.parseLong(executionId));
        if (!glassFishBatchSecurityHelper.isVisibleToThisInstance(((TaggedJobExecution) je).getTagName()))
            throw new NoSuchJobExecutionException("No job execution exists for job execution id: " + executionId);

        List<StepExecution> stepExecutions = jobOperator.getStepExecutions(Long.parseLong(executionId));
        if (stepExecutions == null || stepExecutions.size() == 0)
            throw new NoSuchJobExecutionException("No job execution exists for job execution id: " + executionId);

        return stepExecutions;
    }

    private Map<String, Object> handleJob(StepExecution stepExecution, ColumnFormatter columnFormatter) {
        Map<String, Object> jobInfo = new HashMap<>();

        int stepMetricsIndex = -1;
        StringTokenizer st = new StringTokenizer("", "");
        String[] cfData = new String[getOutputHeaders().length];
        for (int index = 0; index < getOutputHeaders().length; index++) {
            Object data = null;
            switch (getOutputHeaders()[index]) {
                case NAME:
                    data = stepExecution.getStepName();
                    break;
                case STEP_ID:
                    data = stepExecution.getStepExecutionId();
                    break;
                case BATCH_STATUS:
                    data = stepExecution.getBatchStatus() != null ? stepExecution.getBatchStatus() : "";
                    break;
                case EXIT_STATUS:
                    data = stepExecution.getExitStatus() != null ? stepExecution.getExitStatus() : "";
                    break;
                case START_TIME:
                    if (stepExecution.getStartTime() != null) {
                        data = stepExecution.getStartTime().getTime();
                        cfData[index] = stepExecution.getStartTime().toString();
                    } else {
                        data = "";
                    }
                    break;
                case END_TIME:
                    if (stepExecution.getEndTime() != null) {
                        data = stepExecution.getEndTime().getTime();
                        cfData[index] = stepExecution.getEndTime().toString();
                    } else {
                        data = "";
                    }
                    break;
                case STEP_METRICS:
                    stepMetricsIndex = index;
                    Map<String, Long> metricMap = new HashMap<>();
                    if (stepExecution.getMetrics() != null) {
                        ColumnFormatter cf = new ColumnFormatter(new String[]{"METRICNAME", "VALUE"});
                        for (Metric metric : stepExecution.getMetrics()) {
                            metricMap.put(metric.getType().name(), metric.getValue());
                            cf.addRow(new Object[] {metric.getType().name(), metric.getValue()});
                        }
                        st = new StringTokenizer(cf.toString(), "\n");
                    }
                    data = metricMap;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown header: " + getOutputHeaders()[index]);
            }
            jobInfo.put(getOutputHeaders()[index], data);
            cfData[index] = (stepMetricsIndex != index)
                    ? (cfData[index] == null ? data.toString() : cfData[index])
                    : (st.hasMoreTokens() ? st.nextToken() : "");
        }
        columnFormatter.addRow(cfData);

        cfData = new String[getOutputHeaders().length];
        for (int i = 0; i < getOutputHeaders().length; i++)
            cfData[i] = "";
        while (st.hasMoreTokens()) {
            cfData[stepMetricsIndex] = st.nextToken();
            columnFormatter.addRow(cfData);
        }

        return jobInfo;
    }

}
