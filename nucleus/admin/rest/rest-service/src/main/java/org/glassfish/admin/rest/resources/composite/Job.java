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

package org.glassfish.admin.rest.resources.composite;

import org.glassfish.admin.rest.composite.RestModel;
import org.jvnet.hk2.annotations.Service;

/**
 * This model holds information for detached jobs
 * @author jdlee
 */
@Service
public interface Job extends RestModel {
    /**
     * The ID of this job
     */
    String getJobId();
    void setJobId(String jobid);

    /**
     * Command being executed 
     */
    String getJobName();
    void setJobName(String jobName);

    /**
     *  The date and time the job was executed 
     */
    String getExecutionDate();
    void setExecutionDate(String executionDate);

    /**
     *  The date and time the job was completed 
     */
    String getCompletionDate();
    void setCompletionDate(String completionDate);

    /**
     * The message, if any, from the command 
     */
    String getMessage();
    void setMessage(String message);

    /**
     * Completion code for this job, if completed 
     */
    String getExitCode();
    void setExitCode(String exitCode);

    /**
     * The user who executed the command 
     */
    String getUser();
    void setUser(String user);

    /**
     * The current state of the command's execution
     */
    String getJobState();
    void setJobState(String state);
}
