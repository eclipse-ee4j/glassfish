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

package org.glassfish.admin.rest.composite;

import org.glassfish.admin.rest.model.ResponseBody;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;

/**
 * @author tmoreau
 */
public abstract class ResponseBodyBuilderImpl implements ResponseBodyBuilder {

    public ResponseBody build(ActionReport report) {
        ExitCode exitCode = report.getActionExitCode();
        if (ExitCode.SUCCESS.equals(exitCode)) {
            ResponseBody rb = success(report);
            rb.setIncludeResourceLinks(includeResourceLinks());
            return rb;
        }
        final ResponseBody responseBody = new ResponseBody(includeResourceLinks());
        if (ExitCode.WARNING.equals(exitCode)) {
            responseBody.addWarning(report.getMessage());
        } else {
            responseBody.addFailure(report.getMessage());
        }
        return responseBody;
    }

    abstract protected boolean includeResourceLinks();

    abstract protected ResponseBody success(final ActionReport report);
}
