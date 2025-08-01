/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ManagedJobConfig;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.progress.JobInfo;
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

import static com.sun.enterprise.v3.admin.JobManagerService.parseJobRetentionPeriodToMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

/**
 *
 * This is an hk2 service which will clear all expired and inactive jobs
 *
 * @author Bhakti Mehta
 */
@Service(name = "job-cleanup")
@RunLevel(value = StartupRunLevel.VAL)
public class JobCleanUpService implements ConfigListener {

    private final static Logger logger = KernelLoggerInfo.getLogger();

    @Inject
    private JobManagerService jobManagerService;

    @Inject
    private Domain domain;

    private ManagedJobConfig managedJobConfig;
    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void postConstruct() {
        logger.log(FINE, KernelLoggerInfo.initializingJobCleanup);

        managedJobConfig = domain.getExtensionByType(ManagedJobConfig.class);
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(managedJobConfig);
        logger.fine(KernelLoggerInfo.initializingManagedConfigBean);
        bean.addListener(this);

        scheduleCleanUp();
    }


    @PreDestroy
    public void preDestroy() {
        logger.log(FINE, "Stopping job cleanup service.");
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(managedJobConfig);
        bean.removeListener(this);
        scheduler.shutdownNow();
    }

    /**
     * This will schedule a cleanup of expired jobs based on configurable values
     */
    private void scheduleCleanUp() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        logger.fine(KernelLoggerInfo.schedulingCleanup);
        long delayBetweenRuns = parseJobRetentionPeriodToMillis(managedJobConfig.getPollInterval());
        long initialDelay = parseJobRetentionPeriodToMillis(managedJobConfig.getInitialDelay());
        if (delayBetweenRuns <= 0) {
            if (initialDelay == 0) {
                // We will do that immediately, but then we will not schedule any further runs
                new JobCleanUpTask().run();
            }
            logger.log(INFO, "No job cleanup will be scheduled as poll-interval is set to {0} ms", delayBetweenRuns);
            return;
        }

        scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread result = new Thread(r, JobCleanUpTask.class.getSimpleName());
                result.setDaemon(true);
                return result;
            }
        });

        scheduler.scheduleAtFixedRate(new JobCleanUpTask(), initialDelay, delayBetweenRuns, MILLISECONDS);
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
                jobManagerService.getCompletedJobsInfo().values().stream().map(CompletedJob::getJobsFile).distinct()
                    .forEach(this::cleanUpExpiredJobs);
            } catch (Exception e) {
                logger.log(SEVERE, KernelLoggerInfo.exceptionCleaningJobs, e);
            }
        }

        private void cleanUpExpiredJobs(File file) {
            for (JobInfo job : jobManagerService.getExpiredJobs(file)) {
                jobManagerService.purgeJob(job.jobId);
                jobManagerService.purgeCompletedJobForId(job);
                logger.log(FINE, KernelLoggerInfo.cleaningJob, job);
            }
        }
    }

    class PropertyChangeHandler implements Changed {

        @Override
        public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> changedType, T changedInstance) {
            if (type != TYPE.CHANGE) {
                return null;
            }
            if (logger.isLoggable(FINE)) {
                logger.log(FINE, KernelLoggerInfo.changeManagedJobConfig,
                    new Object[] {changedType.getName(), changedInstance});
            }
            return handleChangeEvent(changedInstance);
        }

        private <T extends ConfigBeanProxy> NotProcessed handleChangeEvent(T instance) {
            scheduleCleanUp();
            return null;
        }
    }
}
