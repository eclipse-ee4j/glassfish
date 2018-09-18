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

/*
  * ProgressObjectIimplWithError.java
  *
  * Created on August 13, 2004, 9:53 AM
  */

package org.glassfish.deployapi;

import java.util.Vector;
import java.util.Iterator;

import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.shared.StateType;

/**
  *Implements a progress object primarily intended to report an error during a DeploymentManager
  *method invocation.
  * @author  tjquinn
  */
public class SimpleProgressObjectImpl implements ProgressObject {
    
    /** Records registered listeners */
    private Vector listeners = new Vector();

    /**
     *Records all events delivered so late-registering listeners will be informed of all past events
     *as well as future ones.
     */
    protected Vector deliveredEvents = new Vector();
    
    /** Records the deployment status.  Normally of type DeploymentStatusWithError */
    private DeploymentStatus deploymentStatus = null;
    
    /** Creates a new instance of ProgressObjectIimplWithError */
    public SimpleProgressObjectImpl(DeploymentStatus deploymentStatus) {
        this.deploymentStatus = deploymentStatus;
    }

    /**
     *Registers a listener for progress events.
     *@param new progress listener
     */
    public void addProgressListener(javax.enterprise.deploy.spi.status.ProgressListener progressListener) {
	synchronized (listeners) {
            listeners.add(progressListener);
	    if (deliveredEvents.size() > 0) {
	        for (Iterator i = deliveredEvents.iterator(); i.hasNext();) {
		    progressListener.handleProgressEvent((ProgressEvent)i.next());
	        }
	    }
	}
    }    
    
    public void cancel() throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException {
        throw new OperationUnsupportedException("cancel not supported");
    }
    
    public javax.enterprise.deploy.spi.status.ClientConfiguration getClientConfiguration(javax.enterprise.deploy.spi.TargetModuleID targetModuleID) {
        return null;
    }
    
    public javax.enterprise.deploy.spi.status.DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }
    
    public javax.enterprise.deploy.spi.TargetModuleID[] getResultTargetModuleIDs() {
        return new javax.enterprise.deploy.spi.TargetModuleID[0];
    }
    
    public boolean isCancelSupported() {
        return false;
    }
    
    public boolean isStopSupported() {
        return false;
    }

    /**
     *Unregister a previously-registered event listener.
     *@param the listener to unregister
     */
    public void removeProgressListener(javax.enterprise.deploy.spi.status.ProgressListener progressListener) {
	synchronized (listeners) {
            listeners.remove(progressListener);
	}
    }
    
    public void stop() throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException {
        throw new OperationUnsupportedException("stop not supported");
    }
    
    /**
     * Notifies all listeners that have registered interest for ProgressEvent notification. 
     */
    protected void fireProgressEvent(ProgressEvent progressEvent) {
	Vector currentListeners = null;
        synchronized (listeners) {
            currentListeners = (Vector) listeners.clone();
            deliveredEvents.add(progressEvent);
        }

        for (Iterator listenersItr = currentListeners.iterator(); listenersItr.hasNext();) {
            ((ProgressListener)listenersItr.next()).handleProgressEvent(progressEvent);
        }
    }


}
