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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.api.admin.JobLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * This service will look for completed jobs from the jobs.xml files and load the information
 *
 * @author Bhakti Mehta
 */
@Service(name = "job-locator")
public class JobLocatorService implements JobLocator {

    protected Set<File> jobFiles = Collections.synchronizedSet(new HashSet<File>());

    @Override
    public Set<File> locateJobXmlFiles() {
        return jobFiles;
    }

    public void addFile(File file) {
        jobFiles.add(file);
    }
}
