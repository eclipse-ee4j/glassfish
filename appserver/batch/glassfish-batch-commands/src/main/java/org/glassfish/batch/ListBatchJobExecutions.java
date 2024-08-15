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
import jakarta.batch.operations.NoSuchJobException;
import jakarta.batch.operations.NoSuchJobExecutionException;
import jakarta.batch.operations.NoSuchJobInstanceException;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.JobInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
@Service(name = "_ListBatchJobExecutions")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("_ListBatchJobExecutions")
@ExecuteOn(value = {RuntimeType.INSTANCE})
@TargetType(value = {CommandTarget.DAS, CommandTarget.CLUSTERED_INSTANCE, CommandTarget.STANDALONE_INSTANCE})
@RestEndpoints({
        @RestEndpoint(configBean = Domain.class,
                opType = RestEndpoint.OpType.GET,
                path = "_ListBatchJobExecutions",
                description = "_List Batch Job Executions")
})
public class ListBatchJobExecutions
    extends AbstractLongListCommand {

    private static final String JOB_NAME = "jobName";

    private static final String EXECUTION_ID = "executionId";

    private static final String BATCH_STATUS = "batchStatus";

    private static final String EXIT_STATUS = "exitStatus";

    private static final String START_TIME = "startTime";

    private static final String END_TIME = "endTime";

    private static final String JOB_PARAMETERS = "jobParameters";

    private static final String STEP_COUNT = "stepCount";

    @Param(name = "executionid", shortName = "x", optional = true)
    String executionId;

    @Param(primary = true, optional = true)
    String instanceId;

    @Override
    protected void executeCommand(AdminCommandContext context, Properties extraProps)
        throws Exception {

        ColumnFormatter columnFormatter = new ColumnFormatter(getDisplayHeaders());
        List<Map<String, Object>> jobExecutions = new ArrayList<>();
        extraProps.put("listBatchJobExecutions", jobExecutions);
        if (executionId != null) {
            JobOperator jobOperator = getJobOperatorFromBatchRuntime();
            JobExecution je = jobOperator.getJobExecution(Long.parseLong(executionId));
            if (instanceId != null) {
                JobInstance ji = jobOperator.getJobInstance(Long.parseLong(executionId));
                if (ji.getInstanceId() != Long.parseLong(instanceId)) {
                    throw new RuntimeException("executionid " + executionId
                    + " is not associated with the specified instanceid (" + instanceId + ")"
                    + "; did you mean " + ji.getInstanceId() + " ?");
                }
            }
            try {
                if (glassFishBatchSecurityHelper.isVisibleToThisInstance(((TaggedJobExecution) je).getTagName()))
                    jobExecutions.add(handleJob(je, columnFormatter));
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Exception while getting jobExecution details: " + ex);
                logger.log(Level.FINE, "Exception while getting jobExecution details: ", ex);
            }
        } else if (instanceId != null) {
            for (JobExecution je : getJobExecutionForInstance(Long.parseLong(instanceId))) {
                try {
                    if (glassFishBatchSecurityHelper.isVisibleToThisInstance(((TaggedJobExecution) je).getTagName()))
                        jobExecutions.add(handleJob(je, columnFormatter));
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Exception while getting jobExecution details: " + ex);
                    logger.log(Level.FINE, "Exception while getting jobExecution details: ", ex);
                }
            }
        } else {
            JobOperator jobOperator = getJobOperatorFromBatchRuntime();
            Set<String> jobNames = jobOperator.getJobNames();
            if (jobNames != null) {
                for (String jn : jobOperator.getJobNames()) {
                    List<JobInstance> exe = jobOperator.getJobInstances(jn, 0, Integer.MAX_VALUE - 1);
                    if (exe != null) {
                        for (JobInstance ji : exe) {
                            for (JobExecution je : jobOperator.getJobExecutions(ji)) {
                                try {
                                    if (glassFishBatchSecurityHelper.isVisibleToThisInstance(((TaggedJobExecution) je).getTagName()))
                                        jobExecutions.add(handleJob(jobOperator.getJobExecution(je.getExecutionId()), columnFormatter));
                                } catch (Exception ex) {
                                    logger.log(Level.WARNING, "Exception while getting jobExecution details: " + ex);
                                    logger.log(Level.FINE, "Exception while getting jobExecution details: ", ex);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (jobExecutions.size() > 0) {
            context.getActionReport().setMessage(columnFormatter.toString());
        } else {
            throw new RuntimeException("No Job Executions found");
        }

    }

    @Override
    protected final String[] getAllHeaders() {
        return new String[]{
                JOB_NAME, EXECUTION_ID, START_TIME, END_TIME, BATCH_STATUS, EXIT_STATUS, JOB_PARAMETERS, STEP_COUNT
        };
    }

    @Override
    protected final String[] getDefaultHeaders() {
        return new String[]{JOB_NAME, EXECUTION_ID, START_TIME, END_TIME, BATCH_STATUS, EXIT_STATUS};
    }

    /*
    private boolean isSimpleMode() {
        for (String h : getOutputHeaders()) {
            if (!JOB_NAME.equals(h)) {
                return false;
            }
        }
        return true;
    }
    */

    private static List<JobExecution> getJobExecutionForInstance(long instId)
            throws JobSecurityException, NoSuchJobException, NoSuchJobInstanceException, NoSuchJobExecutionException {
        JobOperator jobOperator = AbstractListCommand.getJobOperatorFromBatchRuntime();
        JobInstance jobInstance = null;
        for (String jn : jobOperator.getJobNames()) {
            List<JobInstance> exe = jobOperator.getJobInstances(jn, 0, Integer.MAX_VALUE - 1);
            if (exe != null) {
                for (JobInstance ji : exe) {
                    if (ji.getInstanceId() == instId) {
                        jobInstance = ji;
                        break;
                    }
                }
            }
        }

        List<JobExecution> jeList = new ArrayList<JobExecution>();

        if (jobInstance == null)
            throw new RuntimeException("No Job Executions found for instanceid = " + instId);

        List<JobExecution> lst = AbstractListCommand.getJobOperatorFromBatchRuntime().getJobExecutions(jobInstance);
        if (lst != null) {
            for (JobExecution je : lst) {
                jeList.add(jobOperator.getJobExecution(je.getExecutionId()));
            }
        }

        return jeList;
    }

    private Map<String, Object> handleJob(JobExecution je, ColumnFormatter columnFormatter)
        throws JobSecurityException, NoSuchJobExecutionException {

        Map<String, Object> jobInfo = new HashMap<>();

        int jobParamIndex = -1;
        StringTokenizer st = new StringTokenizer("", "");
        String[] cfData = new String[getOutputHeaders().length];
        JobOperator jobOperator = AbstractListCommand.getJobOperatorFromBatchRuntime();
        for (int index = 0; index < getOutputHeaders().length; index++) {
            Object data = null;
            switch (getOutputHeaders()[index]) {
                case JOB_NAME:
                    data = " " + je.getJobName();
                    break;
                case EXECUTION_ID:
                    data = "" + je.getExecutionId();
                    break;
                case BATCH_STATUS:
                    data = je.getBatchStatus() != null ? je.getBatchStatus() : "";
                    break;
                case EXIT_STATUS:
                    data = je.getExitStatus() != null ? je.getExitStatus() : "";
                    break;
                case START_TIME:
                    if (je.getStartTime() != null) {
                        data = je.getStartTime().getTime();
                        cfData[index] = je.getStartTime().toString();
                    } else {
                        data = "";
                    }
                    break;
                case END_TIME:
                    if (je.getEndTime() != null) {
                        data = je.getEndTime().getTime();
                        cfData[index] = je.getEndTime().toString();
                    } else {
                        data = "";
                    }
                    break;
                case JOB_PARAMETERS:
                    data = je.getJobParameters() == null ? new Properties() : je.getJobParameters();
                    jobParamIndex = index;
                    ColumnFormatter cf = new ColumnFormatter(new String[]{"KEY", "VALUE"});
                    for (Map.Entry e : ((Properties) data).entrySet())
                        cf.addRow(new String[]{e.getKey().toString(), e.getValue().toString()});
                    st = new StringTokenizer(cf.toString(), "\n");
                    break;
                case STEP_COUNT:
                    long exeId = executionId == null ? je.getExecutionId() : Long.parseLong(executionId);
                    data = jobOperator.getStepExecutions(exeId) == null
                        ? 0 : jobOperator.getStepExecutions(exeId).size();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown header: " + getOutputHeaders()[index]);
            }
            jobInfo.put(getOutputHeaders()[index], data);
            cfData[index] = (jobParamIndex != index)
                    ? (cfData[index] == null ? data.toString() : cfData[index])
                    : (st.hasMoreTokens()) ? st.nextToken() : "";
        }
        columnFormatter.addRow(cfData);

        cfData = new String[getOutputHeaders().length];
        for (int i = 0; i < getOutputHeaders().length; i++)
            cfData[i] = "";
        while (st.hasMoreTokens()) {
            cfData[jobParamIndex] = st.nextToken();
            columnFormatter.addRow(cfData);
        }

        return jobInfo;
    }
}
