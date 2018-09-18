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

package org.glassfish.api.admin;

import org.glassfish.api.ActionReport;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/** Represents running (or finished) command instance.
 *
 *
 * @author Martin Mares
 * @author Bhakti Mehta
 */
public interface Job extends AdminCommandState, Serializable {
    
    /** Command progress only if it is supported by command
     */
    public CommandProgress getCommandProgress();
    
    public void setCommandProgress(CommandProgress commandProgress);
    
    public void complete(ActionReport report, Payload.Outbound outbound);
    
    /** Change state to reverting. Command Can use it to send info about reverting
     * to Job management infrastructure.
     */
    public void revert();
    
    public AdminCommandEventBroker getEventBroker();

    public List<String> getSubjectUsernames();

    public String getName();

    public long getCommandExecutionDate();

    public Payload.Outbound getPayload();

    public File getJobsFile() ;

    public void setJobsFile(File jobsFile) ;

    public String getScope();

    public long getCommandCompletionDate();
    
    /** Job will be considered as retryable after fail. It means that checkpoint
     * will not be deleted and revert or continue can be decided by the user.
     */
    public void setFailToRetryable(boolean value);
    
    public ParameterMap getParameters();
    
}
