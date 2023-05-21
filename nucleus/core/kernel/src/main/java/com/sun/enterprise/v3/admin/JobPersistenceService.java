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

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.glassfish.api.admin.progress.JobInfo;
import org.glassfish.api.admin.progress.JobInfos;
import org.glassfish.api.admin.progress.JobPersistence;
import org.glassfish.kernel.KernelLoggerInfo;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * This service persists information for managed jobs to the file
 * 
 * @author Bhakti Mehta
 */
@Service(name = "job-persistence")
public class JobPersistenceService implements JobPersistence {

    protected Marshaller jaxbMarshaller;

    protected Unmarshaller jaxbUnmarshaller;

    protected JobInfos jobInfos;

    @Inject
    private JobManagerService jobManager;

    protected JAXBContext jaxbContext;

    protected final static Logger logger = KernelLoggerInfo.getLogger();

    protected static final LocalStringManagerImpl adminStrings = new LocalStringManagerImpl(JobPersistenceService.class);

    @Override
    public void persist(Object obj) {
        JobInfo jobInfo = (JobInfo) obj;

        jobInfos = jobManager.getCompletedJobs(jobManager.getJobsFile());

        doPersist(jobInfos, jobInfo);

    }

    public void doPersist(JobInfos jobInfos, JobInfo jobInfo) {
        File file = jobInfo.getJobsFile();
        synchronized (file) {

            if (jobInfos == null) {
                jobInfos = new JobInfos();
            }

            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(JobInfos.class);
                jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                CopyOnWriteArrayList<JobInfo> jobList = new CopyOnWriteArrayList<>(jobInfos.getJobInfoList());
                jobInfos.setJobInfoList(jobList);
                jobList.add(jobInfo);
                jaxbMarshaller.marshal(jobInfos, file);
                jobManager.addToCompletedJobs(new CompletedJob(jobInfo.jobId, jobInfo.commandCompletionDate, jobInfo.getJobsFile()));
                jobManager.purgeJob(jobInfo.jobId);

            } catch (JAXBException e) {
                throw new RuntimeException(adminStrings.getLocalString("error.persisting.jobs", "Error while persisting jobs",
                        jobInfo.jobId, e.getLocalizedMessage()), e);

            }
        }
    }

}
