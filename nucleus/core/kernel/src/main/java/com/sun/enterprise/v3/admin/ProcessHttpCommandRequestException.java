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

package com.sun.enterprise.v3.admin;

import org.glassfish.api.ActionReport;
import org.glassfish.grizzly.http.util.HttpStatus;

/**
 * Inform, that HttpCommandExecution has problem. Must change status code.
 *
 * @author mmares
 */
public class ProcessHttpCommandRequestException extends Exception {

    private ActionReport report;
    private HttpStatus responseStatus = HttpStatus.OK_200;

    /**
     * Constructs an instance of <code>InvalidPreconditionException</code> with the specified detail message.
     *
     * @param report Report with result
     */
    public ProcessHttpCommandRequestException(ActionReport report) {
        this(report, null);
    }

    /**
     * Constructs an instance of <code>InvalidPreconditionException</code> with the specified detail message.
     *
     * @param responseStatus HttpResponse status code
     */
    public ProcessHttpCommandRequestException(HttpStatus responseStatus) {
        this(null, responseStatus);
    }

    /**
     * Constructs an instance of <code>InvalidPreconditionException</code> with the specified detail message.
     *
     * @param report Report with result
     * @param responseStatus HttpResponse status code
     */
    public ProcessHttpCommandRequestException(ActionReport report, HttpStatus responseStatus) {
        super();
        this.report = report;
        if (responseStatus != null) {
            this.responseStatus = responseStatus;
        }
    }

    public ActionReport getReport() {
        return report;
    }

    public HttpStatus getResponseStatus() {
        return responseStatus;
    }

}
