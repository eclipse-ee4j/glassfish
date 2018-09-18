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
  * DeploymentStatusImplWithError.java
  *
  * Created on August 13, 2004, 8:54 AM
  */

package org.glassfish.deployapi;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.shared.CommandType;

/**
  *Simple implementation of DeploymentStatus intended to describe an exception that
  *occurred during a DeploymentManager method invocation.
  * @author  tjquinn
  */
public class DeploymentStatusImplWithError extends DeploymentStatusImpl {
    
    /** Records the error, if any, associated with this status object */
    private Throwable cause = null;
    
    /** Creates a new instance of DeploymentStatusImplWithError */
    public DeploymentStatusImplWithError() {
    }
    
    /** Creates a new instance of DeploymentStatusImplWithError */
    public DeploymentStatusImplWithError(CommandType commandType, Throwable cause) {
        super();
        initCause(cause);
        setState(StateType.FAILED);
        setCommand(commandType);
    }
    
    /**
     *Assigns the cause for this status.
     *@param Throwable that describes the error to be reported
     */
    public void initCause(Throwable cause) {
        this.cause = cause;
        setMessage(cause.getMessage());
    }
    
    /**
     *Returns the cause for this status.
     *@return Throwable that describes the error associated with this status
     */
    public Throwable getCause() {
        return cause;
    }
    
    /**
     *Displays the status as a string, including stack trace information if error is present.
     *@return String describing the status, including stack trace info from the error (if present).
     */
    public String toString() {
        StringBuffer result = new StringBuffer(super.toString());
        if (cause != null) {
            String lineSep = System.getProperty("line.separator");
            result.append(lineSep).append("Cause: ");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);
            cause.printStackTrace(pw);
            pw.close();
            result.append(baos.toString());
        }
        return result.toString();
    }
}
