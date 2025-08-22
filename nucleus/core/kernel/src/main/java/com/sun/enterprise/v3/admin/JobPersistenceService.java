/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import jakarta.inject.Singleton;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.api.admin.progress.JobInfo;
import org.glassfish.api.admin.progress.JobInfos;
import org.glassfish.api.admin.progress.JobPersistence;
import org.jvnet.hk2.annotations.Service;

/**
 * This service persists information for managed jobs to the file.
 * <p>
 * All API methods are synchronized as we don't have any other unique object
 * representing the file.
 *
 * @author Bhakti Mehta
 */
@Service(name = "job-persistence")
@Singleton
public class JobPersistenceService implements JobPersistence {

    private static final JAXBContext JAXB_CTX;

    static {
        try {
            JAXB_CTX = JAXBContext.newInstance(JobInfos.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to initializa JAXB for JobPersistenceService!", e);
        }
    }

    @Override
    public synchronized JobInfos load(File file) {
        try {
            if (file.exists()) {
                Unmarshaller unmarshaller = JAXB_CTX.createUnmarshaller();
                return (JobInfos) unmarshaller.unmarshal(file);
            }
            return new JobInfos();
        } catch (JAXBException e) {
            throw new RuntimeException("Error loading completed jobs from " + file, e);
        }
    }

    @Override
    public synchronized JobInfos add(JobInfo job) {
        final File jobsFile = job.getJobsFile();
        final JobInfos completedjobs = load(jobsFile);
        final List<JobInfo> jobs = completedjobs.getJobInfoList();
        if (contains(jobs, job)) {
            throw new IllegalArgumentException("Provided job is already contained in the file: " + job);
        }
        jobs.add(job);
        return persist(jobs, jobsFile);
    }

    @Override
    public synchronized JobInfos remove(JobInfo job) {
        final File jobsFile = job.getJobsFile();
        final JobInfos completedjobs = load(jobsFile);
        final List<JobInfo> oldList = completedjobs.getJobInfoList();
        final List<JobInfo> newList = new ArrayList<>(oldList.size());
        for (JobInfo jobInfo : oldList) {
            if (!jobInfo.jobId.equals(job.jobId)) {
                newList.add(jobInfo);
            }
        }
        return persist(newList, jobsFile);
    }

    private JobInfos persist(List<JobInfo> jobList, File jobsFile) {
        try {
            final JobInfos jobs = new JobInfos();
            jobs.setJobInfoList(jobList);
            Marshaller jaxbMarshaller = JAXB_CTX.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(jobs, jobsFile);
            return jobs;
        } catch (JAXBException e) {
            throw new RuntimeException("Error persisting job list.", e);
        }
    }

    private static boolean contains(List<JobInfo> jobs, JobInfo job) {
        for (JobInfo jobInfo : jobs) {
            if (jobInfo.jobId.equals(job.jobId)) {
                return true;
            }
        }
        return false;
    }
}
