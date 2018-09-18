/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.deployment.client;

import javax.enterprise.deploy.spi.status.ProgressObject;

/**
 * This interface extends the JSR88 interface for providing 
 * deployment operations feedback and progress information. 
 * In particular, it allows to retrieve the complete JES 
 * deployment status object with all the phases information.
 *
 * @author Jerome Dochez
 */
public abstract class DFProgressObject implements ProgressObject {
    
    /** 
     * Once the progress object has reached a completed or 
     * failed state, this API will permit to retrieve the 
     * final status information for the deployment
     * @return the deployment status
     */
    public abstract DFDeploymentStatus getCompletedStatus();
    
    /**
     * Waits for the operation which this progress object is monitoring to 
     * complete.
     * @return the completed status
     */
    public DFDeploymentStatus waitFor() {
        DFDeploymentStatus status = null;
        do {
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException ie) {
                // Exception swallowed deliberately
            }
            status = getCompletedStatus();
        } while(status == null);
        return status;
    }
}
