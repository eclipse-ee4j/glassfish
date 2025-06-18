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

import java.io.File;

/**
 * This class stores some data about long running jobs which have completed
 * It stores job id, the time of completion, the location of the jobs file
 * @author Bhakti Mehta
 */
public class CompletedJob {

    private final String id;
    private final long  completionTime;
    private final File jobsFile;

    public CompletedJob(String id, long completionTime, File jobsFile) {
        this.completionTime = completionTime;
        this.jobsFile = jobsFile;
        this.id = id;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public String getId() {
        return id;
    }

    public File getJobsFile() {
        return jobsFile;
    }

    @Override
    public String toString() {
        return "CompletedJob[id=" + id + ", completitionTime=" + completionTime + ", jobsFile=" + jobsFile + "]";
    }
}
