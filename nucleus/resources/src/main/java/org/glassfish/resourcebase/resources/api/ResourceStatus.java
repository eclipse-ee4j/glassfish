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

package org.glassfish.resourcebase.resources.api;

/**
 *
 * @author PRASHANTH ABBAGANI
 * 
 * Holds the status code/msg (including the exception) of the appropriate resource
 * create/delete functionality
 */
public class ResourceStatus {

    public final static int SUCCESS = 0;
    public final static int FAILURE = 1;
    public final static int WARNING = 2;

    int status = SUCCESS;
    String message;
    Throwable exception;
    boolean alreadyExists = false;
    
    public ResourceStatus(int status, String message) {
        this.status = status;
        this.message = message;
    }
    
    public ResourceStatus(int status, String message, boolean alreadyExists) {
        this.status = status;
        this.message = message;
        this.alreadyExists = alreadyExists;
    }
    
    public int getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public boolean isAlreadyExists() {
        return alreadyExists;
    }
    
    public Throwable getException(){
        return exception;
    }
    
    public void setException (Throwable t) {
        exception = t;
    }
}
