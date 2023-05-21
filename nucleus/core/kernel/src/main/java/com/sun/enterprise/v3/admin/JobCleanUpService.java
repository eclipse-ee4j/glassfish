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

package com.sun.enterprise.v3.admin;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.progress.JobInfo;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.kernel.KernelLoggerInfo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.NotProcessed;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ManagedJobConfig;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

/**
 *
 * This is an hk2 service which will clear all expired and inactive jobs
 * 
 * @author Bhakti Mehta
 */
@Service(name = "job-cleanup")
@RunLevel(value = StartupRunLevel.VAL)
public class JobCleanUpService implements PostConstruct, ConfigListener {

    @Inject
    JobManagerService jobManagerService;

    @Inject
    Domain domain;

    private ManagedJobConfig managedJobConfig;

    private final static Logger logger = KernelLoggerInfo.getLogger();

    private ScheduledExecutorService scheduler;

    private static final LocalStringManagerImpl adminStrings = new LocalStringManagerImpl(JobCleanUpService.class);

    @Override
    public void postConstruct() {
        logger.log(Level.FINE, KernelLoggerInfo.initializingJobCleanup);

        managedJobConfig = domain.getExtensionByType(ManagedJobConfig.class);
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(managedJobConfig);
        logger.fine(KernelLoggerInfo.initializingManagedConfigBean);
        bean.addListener(this);

        scheduler = Executors.newScheduledThreadPool(10, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread result = new Thread(r);
                result.setDaemon(true);
                return result;
            }
        });

        scheduleCleanUp();
    }

    /**
     * This will schedule a cleanup of expired jobs based on configurable values
     */
    private void scheduleCleanUp() {

        logger.fine(KernelLoggerInfo.schedulingCleanup);
        // default values to 20 minutes for delayBetweenRuns and initialDelay
        long delayBetweenRuns = 1200000;
        long initialDelay = 1200000;

        delayBetweenRuns = jobManagerService.convert(managedJobConfig.getPollInterval());
        initialDelay = jobManagerService.convert(managedJobConfig.getInitialDelay());

        ScheduledFuture<?> cleanupFuture = scheduler.scheduleAtFixedRate(new JobCleanUpTask(), initialDelay, delayBetweenRuns,
                TimeUnit.MILLISECONDS);

    }

    /**
     * This method is notified for any changes in job-inactivity-limit or job-retention-period or persist, initial-delay or
     * poll-interval option in ManagedJobConfig. Any change results in the job cleanup service to change the behaviour being
     * updated.
     * 
     * @param events the configuration change events.
     * @return the unprocessed change events.
     */
    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        return ConfigSupport.sortAndDispatch(events, new PropertyChangeHandler(), logger);
    }

    private final class JobCleanUpTask implements Runnable {
        @Override
        public void run() {
            try {
                // This can have data when server starts up initially or as jobs complete
                ConcurrentHashMap<String, CompletedJob> completedJobsMap = jobManagerService.getCompletedJobsInfo();

                for (CompletedJob completedJob : new HashSet<>(completedJobsMap.values())) {
                    logger.log(Level.FINE, KernelLoggerInfo.cleaningJob, new Object[] { completedJob.getId() });

                    cleanUpExpiredJobs(completedJob.getJobsFile());
                }
            } catch (Exception e) {
                throw new RuntimeException(KernelLoggerInfo.exceptionCleaningJobs, e);
            }

        }

    }

    /**
     * This will periodically purge expired jobs
     */
    private void cleanUpExpiredJobs(File file) {
        ArrayList<JobInfo> expiredJobs = jobManagerService.getExpiredJobs(file);
        if (expiredJobs.size() > 0) {
            for (JobInfo job : expiredJobs) {
                // remove from Job registy
                jobManagerService.purgeJob(job.jobId);
                // remove from jobs.xml file
                jobManagerService.purgeCompletedJobForId(job.jobId, file);
                // remove from local cache for completed jobs
                jobManagerService.removeFromCompletedJobs(job.jobId);
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, KernelLoggerInfo.cleaningJob, job.jobId);
                }
            }
        }

    }

    class PropertyChangeHandler implements Changed {

        @Override
        public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> changedType, T changedInstance) {
            NotProcessed np = null;
            switch (type) {
            case CHANGE:
                if (logger.isLoggable(Level.FINE)) {

                    logger.log(Level.FINE, KernelLoggerInfo.changeManagedJobConfig,
                            new Object[] { changedType.getName(), changedInstance.toString() });
                }
                np = handleChangeEvent(changedInstance);
                break;
            default:
            }
            return np;
        }

        private <T extends ConfigBeanProxy> NotProcessed handleChangeEvent(T instance) {
            scheduleCleanUp();
            return null;
        }
    }
}
