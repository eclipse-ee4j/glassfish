/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.results;

/**
 * Response information object. Information used by provider to generate the appropriate output.
 *
 * @author Rajeshwar Patil
 */
public class Result {

    private final String name;
    private boolean isError;
    private String errorMessage;

    public Result(String name, boolean isError, String errorMessage) {
        this.name = name;
        this.isError = isError;
        this.errorMessage = errorMessage;
    }

    /**
     * Returns name of the resource, this result object is for.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns true in case of error. Enables provider to generate error message or otherwise.
     */
    public boolean isError() {
        return isError;
    }

    /**
     * Sets status (error or success) of the response
     */
    public void setIsError(boolean isError) {
        this.isError = isError;
    }

    /**
     * Returns error message in case of an error.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets error message of the response
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
