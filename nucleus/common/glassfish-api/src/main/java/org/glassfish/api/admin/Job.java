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
import java.io.Serializable;
import java.util.List;

import org.glassfish.api.ActionReport;

/**
 * Represents command instance.
 *
 * @author Martin Mares
 * @author Bhakti Mehta
 */
public interface Job extends AdminCommandState, Serializable {

    /**
     * Sets the state.
     *
     * @param state must not be null
     */
    void setState(State state);

    /**
     * Command progress only if it is supported by command
     */
    CommandProgress getCommandProgress();

    void setCommandProgress(CommandProgress commandProgress);

    /**
     * Complete job with the given report and payload.
     *
     * @param report
     * @param outbound
     */
    void complete(ActionReport report, Payload.Outbound outbound);

    /**
     * Change state to reverting. Command Can use it to send info about reverting to Job management infrastructure.
     */
    void revert();

    AdminCommandEventBroker getEventBroker();

    List<String> getSubjectUsernames();

    long getCommandExecutionDate();

    Payload.Outbound getPayload();

    File getJobsFile();

    void setJobsFile(File jobsFile);

    String getScope();

    long getCommandCompletionDate();

    /**
     * Job will be considered as retryable after fail. It means that checkpoint will not be deleted and revert or continue
     * can be decided by the user.
     */
    void setFailToRetryable(boolean value);

    ParameterMap getParameters();

}
