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

package org.glassfish.api.admin;

import java.util.Date;
import org.glassfish.api.admin.progress.ProgressStatusMirroringImpl;

/** Base interface of progress status implementation. Provides information
 * about overall progress.
 *
 * @author mmares
 */
public interface CommandProgress extends ProgressStatus {
    
    public static final String EVENT_PROGRESSSTATUS_CHANGE = "ProgressStatus/change";
    public static final String EVENT_PROGRESSSTATUS_STATE = "ProgressStatus/state";
    
    /** Timestamp of command complete event or {@code null} for running command
     */ 
    public Date getEndTime();
    
    /** Timestamp of command creation
     */
    public Date getStartTime();
    
    /** Creates child for mirroring (supplemental commands)
     */
    public ProgressStatusMirroringImpl createMirroringChild(int allocatedSteps);
    
    /** Unique id of this command
     */
    public String getId();
    
    public String getName();
    
    public float computeCompletePortion();
    
    public String getLastMessage();
    
    public void setEventBroker(AdminCommandEventBroker eventBroker);
    
    public int computeSumSteps();
    
    public boolean isSpinnerActive();
    
}
