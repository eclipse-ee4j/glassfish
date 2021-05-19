/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.deployment.descriptor;

/**
 * Objects exhiniting this interface represent an error page and the exception type or
 * error code that will cause the redirect from the web container.
 *
 * @author Danny Coward
 */
public class ErrorPageDescriptor implements java.io.Serializable{
    private int errorCode = -1;  // none
    private String exceptionType;
    private String location;

    /**
     * The default constructor.
     */
    public ErrorPageDescriptor() {
    }


    /** Constructor for error code to error page mapping. */
    public ErrorPageDescriptor(int errorCode, String location) {
        this.errorCode = errorCode;
        this.location = location;
    }


    /** Constructor for Java exception type to error page mapping. */
    public ErrorPageDescriptor(String exceptionType, String location) {
        this.exceptionType = exceptionType;
        this.location = location;
    }


    /** Return the error code. -1 if none. */
    public int getErrorCode() {
        return this.errorCode;
    }


    /** Sets the error code. */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }


    /**
     * If there is an exception type, then the exception type is returned.
     * Otherwise, if the error code is not -1, then the error code is returned as a string.
     * If the error code is -1, then nul is returned.
     */
    public String getErrorSignifierAsString() {
        if ("".equals(this.getExceptionType())) {
            if (getErrorCode() == -1) {
                return null;
            } else {
                return String.valueOf(this.getErrorCode());
            }
        }
        return this.getExceptionType();
    }


    /** Sets the error code if the argument is parsable as an int, or the exception type else. */
    public void setErrorSignifierAsString(String errorSignifier) {
        try {
            int errorCode = Integer.parseInt(errorSignifier);
            this.setErrorCode(errorCode);
            this.setExceptionType(null);
            return;
        } catch (NumberFormatException nfe) {

        }
        this.setExceptionType(errorSignifier);
    }


    /** Return the exception type or the empty string if none. */
    public String getExceptionType() {
        if (this.exceptionType == null) {
            this.exceptionType = "";
        }
        return this.exceptionType;
    }


    /** Sets the exception type. */
    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }


    /** Return the page to map to */
    public String getLocation() {
        if (this.location == null) {
            this.location = "";
        }
        return this.location;
    }


    /** Set the page to map to */
    public void setLocation(String location) {
        this.location = location;
    }


    /** Appends a formatted version of my state as a String. */
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("ErrorPage ").append(this.getErrorCode())
            .append(" ").append(this.getExceptionType()).append(" ").append(this.getLocation());
    }

}
