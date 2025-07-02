/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.remote.reader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.glassfish.api.ActionReport;

/**
 * Temporary implementation. Copy of AcctionReporter. It is here until ActionReport refactoring will be complete.
 *
 * @author mmares
 */
//TODO: Remove when ActionReport refactoring will be done
public class CliActionReport extends ActionReport {

    private static final String EOL = System.getProperty("line.separator");

    protected Throwable exception;
    protected String actionDescription;
    protected List<CliActionReport> subActions = new ArrayList<>();
    protected ExitCode exitCode = ExitCode.SUCCESS;

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
    public void setActionDescription(String message) {
        this.actionDescription = message;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    @Override
    public void setFailureCause(Throwable t) {
        this.exception = t;
    }

    @Override
    public Throwable getFailureCause() {
        return exception;
    }

    @Override
    public ActionReport addSubActionsReport() {
        CliActionReport subAction = new CliActionReport();
        subActions.add(subAction);
        return subAction;
    }

    @Override
    public List<CliActionReport> getSubActionsReport() {
        return subActions;
    }

    @Override
    public void setActionExitCode(ExitCode exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public ExitCode getActionExitCode() {
        return exitCode;
    }

    @Override
    public String getContentType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasSuccesses() {
        return has(this, ExitCode.SUCCESS);
    }

    @Override
    public boolean hasWarnings() {
        return has(this, ExitCode.WARNING);
    }

    @Override
    public boolean hasFailures() {
        return has(this, ExitCode.FAILURE);
    }

    private static boolean has(CliActionReport ar, ExitCode value) {
        if (null != ar.exitCode && ar.exitCode.equals(value)) {
            return true;
        }
        Queue<CliActionReport> q = new LinkedList<>();
        q.addAll(ar.subActions);
        while (!q.isEmpty()) {
            CliActionReport lar = q.remove();
            ExitCode ec = lar.getActionExitCode();
            if (null != ec && ec.equals(value)) {
                return true;
            } else {
                q.addAll(lar.subActions);
            }
        }
        return false;
    }

    @Override
    public void writeReport(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void addIndent(int level, StringBuilder sb) {
        for (int i = 0; i < level; i++) {
            sb.append("    ");
        }
    }

    private void messageToString(int indentLevel, String id, MessagePart msg, StringBuilder sb) {
        if (msg == null) {
            return;
        }
        addIndent(indentLevel, sb);
        sb.append("MESSAGE - ").append(id).append(EOL);
        if (msg.getMessage() != null && !msg.getMessage().isEmpty()) {
            addIndent(indentLevel, sb);
            sb.append(" : ").append(msg.getMessage()).append(EOL);
        }
        if (msg.getChildrenType() != null) {
            addIndent(indentLevel, sb);
            sb.append(" childrenType: ").append(msg.getChildrenType()).append(EOL);
        }
        for (Map.Entry<Object, Object> entry : msg.getProps().entrySet()) {
            addIndent(indentLevel, sb);
            sb.append(" >").append(entry.getKey()).append(" = ").append(entry.getValue());
            sb.append(EOL);
        }
        if (msg.getChildren() != null) {
            int counter = 0;
            for (MessagePart child : msg.getChildren()) {
                messageToString(indentLevel + 1, id + ".M" + counter, child, sb);
                counter++;
            }
        }
    }

    private String toString(int indentLevel, String id, CliActionReport ar) {
        if (id == null) {
            id = "0";
        }
        StringBuilder r = new StringBuilder();
        addIndent(indentLevel, r);
        r.append("ACTION REPORT - ").append(id);
        r.append(" [").append(ar.getActionExitCode().name()).append(']').append(EOL);
        if (ar.getActionDescription() != null) {
            addIndent(indentLevel, r);
            r.append(" actionDescription: ").append(ar.getActionDescription()).append(EOL);
        }
        if (ar.getFailureCause() != null) {
            addIndent(indentLevel, r);
            r.append(" failure: ");
            String msg = ar.getFailureCause().getMessage();
            if (msg != null && !msg.isEmpty()) {
                r.append(msg).append(EOL);
            } else {
                r.append('[').append(ar.getFailureCause().getClass().getName());
                r.append(']').append(EOL);
            }
        }
        if (ar.getExtraProperties() != null) {
            for (Map.Entry<Object, Object> entry : ar.getExtraProperties().entrySet()) {
                addIndent(indentLevel, r);
                r.append(" >").append(entry.getKey()).append(" = ").append(entry.getValue());
                r.append(EOL);
            }
        }
        messageToString(indentLevel + 1, id + ".M0", ar.getTopMessagePart(), r);
        r.append(EOL);
        if (ar.getSubActionsReport() != null) {
            int counter = 0;
            for (CliActionReport sub : ar.getSubActionsReport()) {
                r.append(toString(indentLevel + 1, id + "." + counter, sub));
                counter++;
            }
        }
        return r.toString();
    }

    @Override
    public String toString() {
        return toString(0, "0", this);
    }
}
