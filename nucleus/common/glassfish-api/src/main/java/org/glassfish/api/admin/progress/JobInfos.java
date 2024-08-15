/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains information of all the jobs which will be persisted to jobs.xml
 *
 * @author Bhakti Mehta
 */
@XmlRootElement(name = "jobs")
public class JobInfos {
    private List<JobInfo> jobInfoList;

    @XmlElement(name = "job")
    public List<JobInfo> getJobInfoList() {

        return jobInfoList;
    }

    public void setJobInfoList(List<JobInfo> jobInfoList) {
        this.jobInfoList = jobInfoList;
    }

    public JobInfos() {
        jobInfoList = new ArrayList<>();
    }
}
