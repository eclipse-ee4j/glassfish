/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.util.StringUtils.ok;

/**
 *
 * @author Byron Nevins
 */
@Service(name = "plain")
@PerLookup
public final class PlainTextActionReporter extends ActionReporter {

    private transient PrintWriter writer;
    private static final String INDENT = "    ";
    private final StringBuffer message = new StringBuffer();

    @Override
    public void writeReport(OutputStream os) throws IOException {
        // The caller will read MAGIC and the next characters for success/failure
        // everything after the HEADER_END is good data
        writer = new PrintWriter(os);
        writer.print("PlainTextActionReporter");
        if (isFailure()) {
            writer.print("FAILURE");
            Throwable failureCause = getFailureCause();
            if (failureCause != null) {
                writer.print(failureCause);
            }
        } else {
            writer.print("SUCCESS");
        }

        StringBuilder finalOutput = new StringBuilder();
        getCombinedMessages(this, finalOutput);
        String outs = finalOutput.toString();

        if (!ok(outs)) {
            // we want at least one line of output.  Otherwise RemoteResponseManager
            // will consider this an error.  It is NOT an error there just is no data to report.
            LocalStringManagerImpl localStrings = new LocalStringManagerImpl(PlainTextActionReporter.class);
            writer.print(localStrings.getLocalString("get.mon.no.data", "No monitoring data to report."));
            writer.print("\n"); // forces an error to manifest constructor
        } else {
            writer.print(outs);
        }

        writer.flush();
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    /**
     * Append the string to the internal buffer -- not to the internal message string!
     * @param s the string to append
     */
    @Override
    public void appendMessage(String s) {
        message.append(s);
    }

    /**
     * Append the string to the internal buffer and add a linefeed like 'println'
     * @param s the string to append
     */
    public void appendMessageln(String s) {
        message.append(s).append('\n');
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        this.message.delete(0, this.message.length());
        appendMessage(message);
    }

    @Override
    public String getMessage() {
        return message.toString();
    }

    @Override
    public void getCombinedMessages(ActionReporter aReport, StringBuilder out) {
        if(aReport == null || !(aReport instanceof PlainTextActionReporter) ) {
            throw new RuntimeException("Internal Error: Sub reports are different types than parent report.");
        }
        // guaranteed safe above.
        PlainTextActionReporter ptr = (PlainTextActionReporter) aReport;
        String s = ptr.getOutputData();

        if (ok(s)) {
            if (out.length() > 0) {
                out.append('\n');
            }
            out.append(s);
        }

        for (ActionReporter ar : aReport.getSubActionsReport()) {
            getCombinedMessages(ar, out);
        }
    }

    private String getOutputData() {
        if (superSimple(getTopMessagePart())) {
            return simpleGetOutputData();
        }
        return notSoSimpleGetOutputData();
    }

    private boolean superSimple(MessagePart part) {
        // this is mainly here for backward compatability for when this Reporter
        // only wrote out the main message.
        List<MessagePart> list = part.getChildren();
        Properties props = part.getProps();
        boolean hasChildren = (list != null && !list.isEmpty());
        boolean hasProps = (props != null && props.size() > 0);

        // return true if we are very very simple!
        return !hasProps && !hasChildren;
    }

    private String simpleGetOutputData() {
        StringBuilder out = new StringBuilder();
        String tm = getTopMessagePart().getMessage();
        String body = message.toString();

        if (ok(tm) && !ok(body)) {
            body = tm;
        }

        if (ok(body)) {
            out.append(body);
        }

        return out.toString();
    }

    private String notSoSimpleGetOutputData() {
        StringBuilder out = new StringBuilder();

        if (ok(getActionDescription())) {
            out.append("Description: ").append(getActionDescription());
        }

        write("", getTopMessagePart(), out);
        return out.toString();
    }

    private void write(String indent, MessagePart part, StringBuilder out) {
        out.append(indent).append(part.getMessage()).append('\n');
        write(indent + INDENT, part.getProps(), out);

        for (MessagePart child :
                part.getChildren()) {
            write(indent + INDENT, child, out);
        }
    }

    private void write(String indent, Properties props, StringBuilder out) {
        if (props == null || props.size() <= 0) {
            return;
        }

        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = "" + entry.getKey();
            String val = "" + entry.getValue();
            out.append(indent).append('[').append(key).append('=').append(val).append("\n");
        }
    }
}
