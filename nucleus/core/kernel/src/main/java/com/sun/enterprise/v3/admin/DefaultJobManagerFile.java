/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.File;

import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.annotations.Service;

/**
 * Manages the default jobsFile.
 * Jobs may have own files.
 */
@Service(name = "default-job-manager-file")
@Singleton
public class DefaultJobManagerFile {

    private final String JOBS_FILE = "jobs.xml";

    @Inject
    private ServerEnvironment serverEnvironment;

    private File jobsFile;


    @PostConstruct
    private void postConstruct() {
        this.jobsFile = new File(serverEnvironment.getConfigDirPath(), JOBS_FILE);
    }

    /**
     * This is used to get the jobs file for a job
     *
     * @return the location of the job file
     */
    public File getFile() {
        return this.jobsFile;
    }
}
