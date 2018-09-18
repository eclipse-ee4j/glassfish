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

package com.sun.enterprise.tools.verifier;


import java.util.Vector;
import java.util.logging.LogRecord;

/**
 * This class is responsible for collecting the result and error data
 *
 * @author Sudipto Ghosh
 */

public class ResultManager {

    public void add(Result result) {
        Result r = result;
        addToResult(r);
    }

    public void log(LogRecord logRecord) {
        error.add(logRecord);
    }

    public int getFailedCount() {
        return (failedResults == null) ? 0 : failedResults.size();
    }

    public int getWarningCount() {
        return (warningResults == null) ? 0 : warningResults.size();
    }

    public int getErrorCount() {
        return (error == null) ? 0 : error.size();
    }

    public Vector getFailedResults() {
        return failedResults;
    }

    public Vector getOkayResults() {
        return okayResults;
    }

    public Vector getWarningResults() {
        return warningResults;
    }

    public Vector getNaResults() {
        return naResults;
    }

    public Vector getError() {
        return error;
    }

    /**
     * add the result object to specific Vector based on the status.
     *
     * @param r
     */
    private void addToResult(Result r) {
        if (r.getStatus() == Result.FAILED) {
            failedResults.add(r);
        } else if (r.getStatus() == Result.PASSED) {
            okayResults.add(r);
        } else if (r.getStatus() == Result.WARNING) {
            warningResults.add(r);
        } else if ((r.getStatus() == Result.NOT_APPLICABLE) ||
                (r.getStatus() == Result.NOT_RUN) ||
                (r.getStatus() == Result.NOT_IMPLEMENTED)) {
            naResults.add(r);
        }
    }

    private Vector<Result> failedResults = new Vector<Result>();
    private Vector<Result> okayResults = new Vector<Result>();
    private Vector<Result> warningResults = new Vector<Result>();
    private Vector<Result> naResults = new Vector<Result>();
    private Vector<LogRecord> error = new Vector<LogRecord>();
}
