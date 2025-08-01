/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.v3.common;


import com.sun.enterprise.util.LocalStringManagerImpl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.glassfish.api.ActionReport;

/**
 * Superclass for common ActionReport extension.
 *
 * @author Jerome Dochez
 */
public abstract class ActionReporter extends ActionReport {

    public static final String EOL_MARKER = "%%%EOL%%%";

    private static final long serialVersionUID = -472074342932691854L;

    private Throwable exception;
    private String actionDescription;
    private ExitCode exitCode = ExitCode.SUCCESS;
    private final List<ActionReporter> subActions = Collections.synchronizedList(new ArrayList<>());


    public void setFailure() {
        setActionExitCode(ExitCode.FAILURE);
    }

    public boolean isFailure() {
        return getActionExitCode() == ExitCode.FAILURE;
    }

    public void setWarning() {
        setActionExitCode(ExitCode.WARNING);
    }

    public boolean isWarning() {
        return getActionExitCode() == ExitCode.WARNING;
    }

    public boolean isSuccess() {
        return getActionExitCode() == ExitCode.SUCCESS;
    }

    public void setSuccess() {
        setActionExitCode(ExitCode.SUCCESS);
    }

    @Override
    public final void setActionDescription(String message) {
        this.actionDescription = message;
    }

    public final String getActionDescription() {
        return actionDescription;
    }

    @Override
    public final void setFailureCause(Throwable t) {
        this.exception = t;
    }
    @Override
    public final Throwable getFailureCause() {
        return exception;
    }

    @Override
    public final ActionReport addSubActionsReport() {
        ActionReporter subAction;
        try {
            subAction = this.getClass().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to create subAction of type " + this.getClass().getName(), ex);
        }
        subActions.add(subAction);
        return subAction;
    }

    @Override
    public final List<ActionReporter> getSubActionsReport() {
        return subActions;
    }

    @Override
    public final void setActionExitCode(ExitCode exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public final ExitCode getActionExitCode() {
        return exitCode;
    }

    /**
     * Returns the content type to be used in sending the response back to
     * the client/caller.
     * <p>
     * This is the default type.  Specific subclasses of ActionReporter might
     * override the method to return a different valid type.
     * @return content type to be used in formatting the command response to the client
     */
    @Override
    public String getContentType() {
        return "text/html; charset=utf-8";
    }

    /** Returns combined messages. Meant mainly for long running
     *  operations where some of the intermediate steps can go wrong, although
     *  overall operation succeeds. Does nothing if either of the arguments are null.
     *  The traversal visits the message of current reporter first. The various
     *  parts of the message are separated by EOL_MARKERs.
     * <p>
     * Note: This method is a recursive implementation.
     * @param aReport a given (usually top-level) ActionReporter instance
     * @param sb StringBuilder instance that contains all the messages
     */
    public void getCombinedMessages(ActionReporter aReport, StringBuilder sb) {
        if (aReport == null || sb == null) {
            return;
        }
        String mainMsg = ""; //this is the message related to the topMessage
        String failMsg; //this is the message related to failure cause
        // Other code in the server may write something like report.setMessage(exception.getMessage())
        // and also set report.setFailureCause(exception). We need to avoid the duplicate message.
        if (aReport.getMessage() != null && !aReport.getMessage().isEmpty()) {
            mainMsg = aReport.getMessage();
            String format = "{0}";
            if (ActionReport.ExitCode.WARNING.equals(aReport.getActionExitCode())) {
                LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ActionReporter.class);
                format = localStrings.getLocalString("flag.message.as.warning", "Warning: {0}");
            }
            if (ActionReport.ExitCode.FAILURE.equals(aReport.getActionExitCode())) {
                LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ActionReporter.class);
                format = localStrings.getLocalString("flag.message.as.failure", "Failure: {0}");
            }
            if (sb.length() > 0) {
                sb.append(EOL_MARKER);
            }
            sb.append(MessageFormat.format(format,mainMsg));
        }
        if (aReport.getFailureCause() != null && aReport.getFailureCause().getMessage() != null && !aReport.getFailureCause().getMessage().isEmpty()) {
            failMsg = aReport.getFailureCause().getMessage();
            if (!failMsg.equals(mainMsg)) {
                if (sb.length() > 0) {
                    sb.append(EOL_MARKER);
                }
                sb.append(failMsg);
            }
        }
        for (ActionReporter sub : aReport.getSubActionsReport()) {
            getCombinedMessages(sub, sb);
        }
    }

    @Override
    public boolean hasSuccesses() {
        return has(this,ExitCode.SUCCESS);
    }

    @Override
    public boolean hasWarnings() {
        return has(this,ExitCode.WARNING);
    }

    @Override
    public boolean hasFailures() {
        return has(this,ExitCode.FAILURE);
    }

    private static boolean has(ActionReporter reporter, ExitCode value) {
        if (reporter.getActionExitCode() != null && reporter.getActionExitCode().equals(value)) {
            return true;
        }
        Queue<ActionReporter> workingCopy = new LinkedList<>();
        workingCopy.addAll(reporter.getSubActionsReport());
        while (!workingCopy.isEmpty()) {
            ActionReporter lar = workingCopy.remove();
            ExitCode ec = lar.getActionExitCode();
            if (ec != null && ec.equals(value)) {
                return true;
            }
            workingCopy.addAll(lar.getSubActionsReport());
        }
        return false;
    }
}
