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

import jakarta.inject.Inject;

import javax.security.auth.Subject;

import org.glassfish.api.admin.Job;
import org.glassfish.api.admin.JobCreator;
import org.glassfish.api.admin.ParameterMap;
import org.jvnet.hk2.annotations.Service;

/**
 * This service implements the <code>JobCreator</code> and is used for creating Jobs
 *
 * @author Bhakti Mehta
 */
@Service(name = "job-creator")
public class JobCreatorService implements JobCreator {

    @Inject
    private DefaultJobManagerFile defaultJobManagerFile;

    /**
     * This will create a new job with the name of command and a new unused id for the job
     *
     *
     * @param scope The scope of the command or null if there is no scope
     * @param name The name of the command
     * @return a newly created job
     */
    @Override
    public Job createJob(String id, String scope, String name, Subject subject, boolean isManagedJob, ParameterMap parameters) {
        if (!isManagedJob) {
            return new AdminCommandInstanceImpl(name, scope, subject, false, parameters);
        }
        AdminCommandInstanceImpl job = new AdminCommandInstanceImpl(id, name, scope, subject, true, parameters);
        job.setJobsFile(defaultJobManagerFile.getFile());
        return job;
    }
}
