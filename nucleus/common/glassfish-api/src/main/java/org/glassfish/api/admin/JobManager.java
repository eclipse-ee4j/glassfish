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

package org.glassfish.api.admin;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.System.Logger;
import java.util.Iterator;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.progress.JobInfo;
import org.glassfish.api.admin.progress.JobInfos;
import org.jvnet.hk2.annotations.Contract;

import static java.lang.System.Logger.Level.TRACE;

/**
 * This is the contract for the JobManagerService The JobManager will be responsible for 1. generating unique ids for
 * jobs 2. serving as a registry for jobs 3. creating thread pools for jobs 4.removing expired jobs
 *
 * @author Martin Mares
 * @author Bhakti Mehta
 */

@Contract
public interface JobManager<T extends Job> {

    /**
     * Container for checkpoint related objects
     */
    public class Checkpoint<T extends Job> implements Serializable {

        private static final long serialVersionUID = 1L;

        private T job;
        private AdminCommand command;
        private AdminCommandContext context;

        public Checkpoint(T job, AdminCommand command, AdminCommandContext context) {
            this.job = job;
            this.command = command;
            this.context = context;
        }

        public T getJob() {
            return job;
        }

        public AdminCommand getCommand() {
            return command;
        }

        public AdminCommandContext getContext() {
            return context;
        }

    }

    /**
     * This method is used to generate a unique id for a managed job
     *
     * @return returns a new id for the job
     */
    String getNewId();

    /**
     * This method will register the job in the job registry
     *
     * @param instance job to be registered
     * @throws IllegalArgumentException
     */
    void registerJob(T instance) throws IllegalArgumentException;

    /**
     * This method will return the list of jobs in the job registry
     *
     * @return list of jobs
     */
    Iterator<T> getJobs();

    /**
     * This method is used to get a job by its id
     *
     * @param id The id to look up the job in the job registry
     * @return the Job or null
     */
    T get(String id);

    /**
     * This will purge the job associated with the id from the registry
     *
     * @param id the id of the Job which needs to be purged
     */
    void purgeJob(String id);

    /**
     * This will get the list of jobs from the job registry which have completed
     *
     * @return the details of all completed jobs using JobInfos. Never null.
     */
    JobInfos getCompletedJobs(File jobs);

    /**
     * This is a convenience method to get a completed job with an id
     *
     * @param id the completed Job whose id needs to be looked up
     * @return the completed Job
     */
    JobInfo getCompletedJobForId(String id, File jobsFile);

    /**
     * This is used to purge a completed job.
     *
     * @param job the info about Job
     * @return the new list of completed jobs
     */
    JobInfos purgeCompletedJobForId(JobInfo job);

    /**
     * Stores current command state.
     */
    void checkpoint(AdminCommand command, AdminCommandContext context) throws IOException;

    /**
     * Stores current command state.
     */
    void checkpoint(AdminCommandContext context, Serializable data) throws IOException;

    /**
     * Load checkpoint related data.
     */
    T loadCheckpointData(String jobId) throws IOException, ClassNotFoundException;

    /**
     * This will create a new job with the name of command and a new unused id for the job
     *
     * @param scope The scope of the command or null if there is no scope
     * @param name The name of the command
     * @return a newly created job
     */
    T createJob(String scope, String name, Subject subject, boolean isManagedJob, ParameterMap parameters,
        ActionReport report);

    /**
     * Starts the command asynchronously
     *
     * @param command
     */
    void start(AsyncAdminCommandExecution command);


    @FunctionalInterface
    interface AsyncAdminCommandExecution extends Runnable {
        static final Logger LOG = System.getLogger(AsyncAdminCommandExecution.class.getName());

        void execute();

        @Override
        default void run() {
            try {
                LOG.log(TRACE, () -> "Command execution started. " + this);
                execute();
                LOG.log(TRACE, () -> "Command execution succeeded. " + this);
            } catch (Throwable t) {
                LOG.log(TRACE, () -> "Command execution failed. " + this, t);
                throw t;
            }
        }
    }
}
