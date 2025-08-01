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

package org.glassfish.api.admin.progress;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import java.io.File;

/**
 * This class contains information related to a job which will be persisted to jobs.xml
 *
 * @author Bhakti Mehta
 */
@XmlType
public class JobInfo {

    @XmlElement
    public String jobId;
    @XmlElement
    public String jobName;
    @XmlElement
    public long commandExecutionDate;
    @XmlElement
    public String message;
    @XmlElement
    public String state;
    @XmlElement
    public String exitCode;
    @XmlElement
    public String user;
    @XmlTransient
    public File jobFile;
    @XmlElement
    public long commandCompletionDate;

    public JobInfo(String jobId, String jobName, long commandStartDate, String exitCode, String user, String message, File jobsFileLoc, String state,
            long commandCompletionDate) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.commandExecutionDate = commandStartDate;
        this.exitCode = exitCode;
        this.user = user;
        this.message = message;
        this.jobFile = jobsFileLoc;
        this.state = state;
        this.commandCompletionDate = commandCompletionDate;
    }

    public JobInfo() {
    }

    public File getJobsFile() {
        return jobFile;
    }

    public void setJobsFile(File jobsFile) {
        this.jobFile = jobsFile;
    }

    @Override
    public String toString() {
        return "Job[id=" + jobId + ", name=" + jobName + ", exitCode=" + exitCode + "]";
    }
}
