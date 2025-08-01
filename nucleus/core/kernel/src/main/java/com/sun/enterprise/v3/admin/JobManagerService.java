/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.admin.remote.RestPayloadImpl;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ManagedJobConfig;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.v3.admin.CheckpointHelper.CheckpointFilename;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandState.State;
import org.glassfish.api.admin.Job;
import org.glassfish.api.admin.JobLocator;
import org.glassfish.api.admin.JobManager;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.admin.progress.JobInfo;
import org.glassfish.api.admin.progress.JobInfos;
import org.glassfish.api.admin.progress.JobPersistence;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.RestrictTo;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.util.SystemPropertyConstants.DROP_INTERRUPTED_COMMANDS;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

/**
 * This is the implementation for the JobManagerService The JobManager is responsible for
 * <ol>
 * <li>generating unique ids for jobs
 * <li>serving as a registry for jobs
 * <li>creating threadpools for jobs
 * <li>removing expired jobs
 * </ol>
 *
 * @author Martin Mares
 * @author Bhakti Mehta
 */
@Service(name = "job-manager")
@Singleton
public class JobManagerService implements JobManager<AdminCommandJob>, EventListener {

    private static final Logger LOG = System.getLogger(JobManagerService.class.getName());
    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(JobManagerService.class);
    private static final String CHECKPOINT_MAINDATA = "MAINCMD";
    private static final int MAX_SIZE = 65535;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    private final ConcurrentHashMap<String, AdminCommandJob> jobRegistry = new ConcurrentHashMap<>();
    private final AtomicInteger lastId = new AtomicInteger(0);

    // This will store the data related to completed jobs so that unique ids
    // can be generated for new jobs. This is populated lazily the first
    // time the JobManagerService is created, it will scan the
    // jobs.xml and load the information in memory
    private final ConcurrentHashMap<String, CompletedJob> completedJobsInfo = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CheckpointFilename> retryableJobsInfo = new ConcurrentHashMap<>();

    @Inject
    private Domain domain;

    @Inject
    private ServiceLocator serviceLocator;

    @Inject
    private Events events;

    @Inject
    private CheckpointHelper checkpointHelper;

    @Inject
    private DefaultJobManagerFile defaultJobsFile;

    @Inject
    private JobPersistence jobPersistence;

    private ExecutorService pool;

    @PostConstruct
    public void postConstruct() {
        ThreadFactory threadFactory = r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            t.setName("managed-job-" + t.getId());
            return t;
        };
        pool = Executors.newCachedThreadPool(threadFactory);
        Set<File> persistedJobFiles = locateJobFiles(defaultJobsFile.getFile(), serviceLocator);

        // Check if there are jobs.xml files which have completed jobs so that
        // unique ids get generated
        for (File jobfile : persistedJobFiles) {
            reapCompletedJobs(jobfile);
            boolean dropInterruptedCommands = Boolean.getBoolean(DROP_INTERRUPTED_COMMANDS);
            Collection<CheckpointFilename> listed = checkpointHelper.listCheckpoints(jobfile.getParentFile());
            for (CheckpointFilename cf : listed) {
                if (dropInterruptedCommands) {
                    LOG.log(INFO, "Dropping checkpoint: {0}", cf.getFile());
                    deleteCheckpoint(cf.getParentDir(), cf.getJobId());
                } else {
                    this.retryableJobsInfo.put(cf.getJobId(), cf);
                }
            }
        }
        events.register(this);
    }

    @Override
    public AdminCommandJob createJob(String scope, String name, Subject subject, boolean isManagedJob,
        ParameterMap parameters, ActionReport report) {
        if (!isManagedJob) {
            return new AdminCommandJob(name, scope, subject, false, parameters, report);
        }
        AdminCommandJob job = new AdminCommandJob(getNewId(), name, scope, subject, true, parameters, report);
        job.setJobsFile(defaultJobsFile.getFile());
        return job;
    }

    @Override
    public synchronized String getNewId() {
        int nextId = lastId.incrementAndGet();
        if (nextId > MAX_SIZE) {
            reset();
        }
        String nextIdToUse = String.valueOf(nextId);
        return isIdInUse(nextIdToUse) ? getNewId() : String.valueOf(nextId);
    }

    @Override
    public JobInfo getCompletedJobForId(String id, File file) {
        for (JobInfo jobInfo : getCompletedJobs(file).getJobInfoList()) {
            if (jobInfo.jobId.equals(id)) {
                return jobInfo;
            }
        }
        return null;
    }

    /**
     * This resets the id to 0
     */
    private void reset() {
        lastId.set(0);
    }

    /**
     * This method will return if the id is in use
     *
     * @param id
     * @return true if id is in use
     */
    private boolean isIdInUse(String id) {
        return jobRegistry.containsKey(id) || completedJobsInfo.containsKey(id) || retryableJobsInfo.containsKey(id);
    }

    /**
     * This adds the jobs
     *
     * @param job
     * @throws IllegalArgumentException
     */
    @Override
    public synchronized void registerJob(AdminCommandJob job) throws IllegalArgumentException {
        if (job == null) {
            throw new IllegalArgumentException(I18N.getLocalString("job.cannot.be.null", "Job cannot be null"));
        }
        if (jobRegistry.containsKey(job.getId())) {
            throw new IllegalArgumentException(I18N.getLocalString("job.id.in.use", "Job id is already in use."));
        }

        retryableJobsInfo.remove(job.getId());
        jobRegistry.put(job.getId(), job);
    }

    /**
     * This returns all the jobs in the registry
     *
     * @return The iterator of jobs
     */
    @Override
    public Iterator<AdminCommandJob> getJobs() {
        return jobRegistry.values().iterator();
    }

    @Override
    public AdminCommandJob get(String id) {
        return jobRegistry.get(id);
    }

    /**
     * This will return a list of jobs which have crossed the JOBS_RETENTION_PERIOD and need to be purged
     *
     * @return list of jobs to be purged
     */
    public ArrayList<JobInfo> getExpiredJobs(File jobsFile) {
        final ArrayList<JobInfo> expiredJobs = new ArrayList<>();
        final long currentTime = System.currentTimeMillis();
        final ManagedJobConfig managedJobConfig = domain.getExtensionByType(ManagedJobConfig.class);
        final long jobsRetentionPeriod = parseJobRetentionPeriodToMillis(managedJobConfig.getJobRetentionPeriod());
        for (JobInfo job : getCompletedJobs(jobsFile).getJobInfoList()) {
            if (currentTime - job.commandExecutionDate > jobsRetentionPeriod
                && (job.state.equals(State.COMPLETED.name()) || job.state.equals(State.REVERTED.name()))) {
                expiredJobs.add(job);
            }
        }
        return expiredJobs;
    }

    public static long parseJobRetentionPeriodToMillis(String input) {
        String period = input.substring(0, input.length() - 1);
        long timeInterval = Long.parseLong(period);
        char unit = Character.toLowerCase(input.charAt(input.length() - 1));
        if (unit == 's') {
            return timeInterval * 1000;
        } else if (unit == 'h') {
            return timeInterval * 3600 * 1000;
        } else if (unit == 'm') {
            return timeInterval * 60 * 1000;
        } else {
            return DAY_IN_MILLIS;
        }
    }

    @Override
    public void purgeJob(final String id) {
        Job job = jobRegistry.remove(id);
        if (job != null) {
            LOG.log(DEBUG, "Removed job from the cache: {0}", job);
        }
    }

    public void deleteCheckpoint(final File parentDir, final String jobId) {
        // list all related files
        File[] toDelete = parentDir
            .listFiles((dir, name) -> name.startsWith(jobId + ".") || name.startsWith(jobId + "-"));
        for (File td : toDelete) {
            td.delete();
        }
    }

    @Override
    public void start(AsyncAdminCommandExecution command) {
        pool.execute(command);
        LOG.log(DEBUG, "Job {0} was submitted to pool {1}", command, pool);
    }

    @Override
    public JobInfos getCompletedJobs(File jobsFile) {
        return jobPersistence.load(jobsFile);
    }

    @Override
    public JobInfos purgeCompletedJobForId(JobInfo job) {
        removeFromCompletedJobs(job.jobId);
        return jobPersistence.remove(job);
    }

    public void moveToCompletedJobs(JobInfo job) {
        purgeJob(job.jobId);
        completedJobsInfo.put(job.jobId, new CompletedJob(job.jobId, job.commandCompletionDate, job.getJobsFile()));
        jobPersistence.add(job);
    }

    public void removeFromCompletedJobs(String id) {
        completedJobsInfo.remove(id);
    }

    public ConcurrentHashMap<String, CompletedJob> getCompletedJobsInfo() {
        return completedJobsInfo;
    }

    public ConcurrentHashMap<String, CheckpointFilename> getRetryableJobsInfo() {
        return retryableJobsInfo;
    }

    @Override
    public void checkpoint(AdminCommandContext context, Serializable data) throws IOException {
        checkpoint((AdminCommand) null, context);
        if (data != null) {
            checkpointAttachement(context.getJobId(), CHECKPOINT_MAINDATA, data);
        }
    }

    @Override
    public void checkpoint(AdminCommand command, AdminCommandContext context) throws IOException {
        if (!StringUtils.ok(context.getJobId())) {
            throw new IllegalArgumentException("Command is not managed");
        }
        AdminCommandJob job = get(context.getJobId());
        Checkpoint<AdminCommandJob> chkp = new Checkpoint(job, command, context);
        checkpointHelper.save(chkp);
        job.setState(State.RUNNING_RETRYABLE);
    }

    public void checkpointAttachement(String jobId, String attachId, Serializable data) throws IOException {
        AdminCommandJob job = get(jobId);
        checkpointHelper.saveAttachment(data, job, attachId);
    }


    public <T extends Serializable> T loadCheckpointAttachement(String jobId, String attachId)
        throws IOException, ClassNotFoundException {
        AdminCommandJob job = get(jobId);
        return checkpointHelper.loadAttachment(job, attachId);
    }

    @Override
    public AdminCommandJob loadCheckpointData(String jobId) throws IOException, ClassNotFoundException {
        return loadCheckpointAttachement(jobId, CHECKPOINT_MAINDATA);
    }


    public Checkpoint<AdminCommandJob> loadCheckpoint(String jobId, Payload.Outbound outbound)
        throws IOException, ClassNotFoundException {
        AdminCommandJob job = get(jobId);
        CheckpointFilename cf = null;
        if (job == null) {
            cf = getRetryableJobsInfo().get(jobId);
            if (cf == null) {
                cf = CheckpointFilename.createBasic(jobId, defaultJobsFile.getFile());
            }
        } else {
            cf = CheckpointFilename.createBasic(job);
        }
        return loadCheckpoint(cf, outbound);
    }


    private Checkpoint<AdminCommandJob> loadCheckpoint(CheckpointFilename cf, Payload.Outbound outbound)
        throws IOException, ClassNotFoundException {
        Checkpoint<AdminCommandJob> result = checkpointHelper.load(cf, outbound);
        if (result == null) {
            return null;
        }
        serviceLocator.inject(result.getJob());
        serviceLocator.postConstruct(result.getJob());
        if (result.getCommand() != null) {
            serviceLocator.inject(result.getCommand());
            serviceLocator.postConstruct(result.getCommand());
        }
        return result;
    }

    /*
     * This method will look for completed jobs from the jobs.xml files and load the information in a local datastructure
     * for faster access
     */
    protected void reapCompletedJobs(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        for (JobInfo jobInfo : getCompletedJobs(file).getJobInfoList()) {
            CompletedJob job = new CompletedJob(jobInfo.jobId, jobInfo.commandCompletionDate, jobInfo.getJobsFile());
            completedJobsInfo.put(jobInfo.jobId, job);
        }
    }

    @Override
    public void event(@RestrictTo(EventTypes.SERVER_READY_NAME) Event<?> event) {
        if (event.is(EventTypes.SERVER_READY)) {
            if (!retryableJobsInfo.isEmpty()) {
                Runnable runnable = () -> {
                    LOG.log(DEBUG, "Restarting retryable jobs");
                    for (CheckpointFilename cf : retryableJobsInfo.values()) {
                        reexecuteJobFromCheckpoint(cf);
                    }
                };
                pool.submit(runnable);
            } else {
                LOG.log(DEBUG, "No retryable job found");
            }
        }
    }


    private void reexecuteJobFromCheckpoint(CheckpointFilename cf) {
        Checkpoint<AdminCommandJob> checkpoint = null;
        try {
            RestPayloadImpl.Outbound outbound = new RestPayloadImpl.Outbound(true);
            checkpoint = loadCheckpoint(cf, outbound);
        } catch (Exception ex) {
            LOG.log(WARNING, "Unable to load checkpoint", ex);
        }
        if (checkpoint != null) {
            final AdminCommandJob job = checkpoint.getJob();
            final AdminCommandContext context = checkpoint.getContext();
            final ActionReport report = context.getActionReport();
            LOG.log(INFO, "Resuming command {0} from its last checkpoint.", job);
            CommandRunnerExecutionContext ec = new CommandRunnerExecutionContext(job.getScope(), job.getName(),
                report, context.getSubject(), false, false, serviceLocator.getService(CommandRunnerImpl.class));
            ec.executeFromCheckpoint(checkpoint, false);
            LOG.log(INFO, "Automatically resumed command {0} finished with exit code {1}. \nMessage: {2}",
                checkpoint.getJob().getName(), report.getActionExitCode(), report.getTopMessagePart());
        }
    }


    private static Set<File> locateJobFiles(File defaultJobsFile, ServiceLocator serviceLocator) {
        Collection<JobLocator> services = serviceLocator.getAllServices(JobLocator.class);
        Set<File> persistedJobFiles = new HashSet<>();
        for (JobLocator locator : services) {
            persistedJobFiles.addAll(locator.locateJobXmlFiles());
        }
        persistedJobFiles.add(defaultJobsFile);
        return persistedJobFiles;
    }
}
