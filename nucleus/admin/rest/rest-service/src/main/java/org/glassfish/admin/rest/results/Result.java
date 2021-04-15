/*
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

    public Result() {
        __isError = false;
        __errorMessage = null;
    }

    /**
     * Returns name of the resource, this result object is for.
     */
    public String getName() {
        return __name;
    }

    /**
     * Returns true in case of error. Enables provider to generate error message or otherwise.
     */
    public boolean isError() {
        return __isError;
    }

    /**
     * Returns error message in case of an error.
     */
    public String getErrorMessage() {
        return __errorMessage;
    }

    /**
     * Sets status (error or success) of the response
     */
    public void setIsError(boolean isError) {
        __isError = isError;
    }

    /**
     * Sets error message of the response
     */
    public void setErrorMessage(String errorMessage) {
        __errorMessage = errorMessage;
    }

    boolean __isError;
    String __errorMessage;
    String __name;
}
