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
@Service(name = "_ListBatchJobs")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("_ListBatchJobs")
@ExecuteOn(value = {RuntimeType.INSTANCE})
@TargetType(value = {CommandTarget.DAS, CommandTarget.CLUSTERED_INSTANCE, CommandTarget.STANDALONE_INSTANCE})
@RestEndpoints({
        @RestEndpoint(configBean = Domain.class,
                opType = RestEndpoint.OpType.GET,
                path = "_ListBatchJobs",
                description = "_List Batch Jobs")
})
public class ListBatchJobs
    extends AbstractLongListCommand {


    private static final String JOB_NAME = "jobName";

    private static final String APP_NAME = "appName";

    private static final String INSTANCE_COUNT = "instanceCount";

    private static final String INSTANCE_ID = "instanceId";

    private static final String EXECUTION_ID = "executionId";

    private static final String BATCH_STATUS = "batchStatus";

    private static final String EXIT_STATUS = "exitStatus";

    private static final String START_TIME = "startTime";

    private static final String END_TIME = "endTime";

    @Param(primary = true, optional = true)
    String jobName;

    @Override
    protected void executeCommand(AdminCommandContext context, Properties extraProps)
        throws Exception {

        ColumnFormatter columnFormatter = new ColumnFormatter(getDisplayHeaders());
        if (isSimpleMode()) {
            extraProps.put("simpleMode", true);
            extraProps.put("listBatchJobs", findSimpleJobInfo(columnFormatter));
        } else {
            extraProps.put("simpleMode", false);
            List<Map<String, Object>> jobExecutions = new ArrayList<>();
            extraProps.put("listBatchJobs", jobExecutions);
            for (JobExecution je : findJobExecutions()) {
                try {
                    if (glassFishBatchSecurityHelper.isVisibleToThisInstance(((TaggedJobExecution) je).getTagName()))
                        jobExecutions.add(handleJob(je, columnFormatter));
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Exception while getting jobExecution details: " + ex);
                    logger.log(Level.FINE, "Exception while getting jobExecution details ", ex);
                }
            }
        }
        context.getActionReport().setMessage(columnFormatter.toString());
    }

    @Override
    protected final String[] getAllHeaders() {
        return new String[]{
                JOB_NAME, APP_NAME, INSTANCE_COUNT, INSTANCE_ID, EXECUTION_ID, BATCH_STATUS,
                START_TIME, END_TIME, EXIT_STATUS
        };
    }

    @Override
    protected final String[] getDefaultHeaders() {
        return new String[]{JOB_NAME, INSTANCE_COUNT};
    }

    private boolean isSimpleMode() {

        for (String h : getOutputHeaders()) {
            if (!JOB_NAME.equals(h) && !INSTANCE_COUNT.equals(h)) {
                return false;
            }
        }
        return getOutputHeaders().length == 2;
    }

    private Map<String, Integer> findSimpleJobInfo(ColumnFormatter columnFormatter)
        throws JobSecurityException, NoSuchJobException {

        Map<String, Integer> jobToInstanceCountMap = new HashMap<>();
        List<JobExecution> jobExecutions = findJobExecutions();
        for (JobExecution je : jobExecutions) {
            if (glassFishBatchSecurityHelper.isVisibleToThisInstance(((TaggedJobExecution) je).getTagName())) {
                String jobName = je.getJobName();
                int count = 0;
                if (jobToInstanceCountMap.containsKey(jobName)) {
                    count = jobToInstanceCountMap.get(jobName);
                }
                jobToInstanceCountMap.put(jobName, count+1);
            }
        }
        for (Map.Entry<String, Integer> e : jobToInstanceCountMap.entrySet())
            columnFormatter.addRow(new Object[] {e.getKey(), e.getValue()});
        return jobToInstanceCountMap;
    }

    private List<JobExecution> findJobExecutions()
        throws JobSecurityException, NoSuchJobException, NoSuchJobInstanceException {
        List<JobExecution> jobExecutions = new ArrayList<>();

        JobOperator jobOperator = AbstractListCommand.getJobOperatorFromBatchRuntime();
        if (jobName != null) {
            List<JobInstance> exe = jobOperator.getJobInstances(jobName, 0, Integer.MAX_VALUE - 1);
            if (exe != null) {
                for (JobInstance ji : exe) {
                    jobExecutions.addAll(jobOperator.getJobExecutions(ji));
                }
            }
        } else {
            Set<String> jobNames = jobOperator.getJobNames();
            if (jobNames != null) {
                for (String jn : jobOperator.getJobNames()) {
                    List<JobInstance> exe = jobOperator.getJobInstances(jn, 0, Integer.MAX_VALUE - 1);
                    if (exe != null) {
                        for (JobInstance ji : exe) {
                            jobExecutions.addAll(jobOperator.getJobExecutions(ji));
                        }
                    }
                }
            }
        }

        return jobExecutions;
    }

    private Map<String, Object> handleJob(JobExecution je, ColumnFormatter columnFormatter)
        throws  JobSecurityException, NoSuchJobException, NoSuchJobExecutionException {
        Map<String, Object> jobInfo = new HashMap<>();

        String[] cfData = new String[getOutputHeaders().length];
        JobOperator jobOperator = AbstractListCommand.getJobOperatorFromBatchRuntime();
        for (int index = 0; index < getOutputHeaders().length; index++) {
            Object data = null;
            switch (getOutputHeaders()[index]) {
                case JOB_NAME:
                    data = "" + je.getJobName();
                    break;
                case APP_NAME:
                    try {
                        String appName = "" + ((TaggedJobExecution) je).getTagName();
                        int semi = appName.indexOf(':');
                        data = appName.substring(semi+1);
                    } catch (Exception ex) {
                        logger.log(Level.FINE, "Error while calling ((TaggedJobExecution) je).getTagName() ", ex);
                        data = ex.toString();
                    }
                    break;
                case INSTANCE_COUNT:
                    data = jobOperator.getJobInstanceCount(je.getJobName());
                    break;
                case INSTANCE_ID:
                    data = jobOperator.getJobInstance(je.getExecutionId()).getInstanceId();
                    break;
                case EXECUTION_ID:
                    data = je.getExecutionId();
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
                default:
                    throw new IllegalArgumentException("Unknown header: " + getOutputHeaders()[index]);
            }
            jobInfo.put(getOutputHeaders()[index], data);
            if (cfData[index] == null)
                cfData[index] = data.toString();
        }
        columnFormatter.addRow(cfData);

        return jobInfo;
    }
}
