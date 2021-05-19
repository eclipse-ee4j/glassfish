/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.sse.api;

/**
 * Represents a Server-Sent Event data.
 *
 * <p>
 * {@code new ServerSentEventData().data("YHOO").data("+2").data("10)} would send
 * the following event (followed by blank line):
 * <pre>
 * data:YHOO
 * data:+2
 * data:10
 *
 * </pre>
 *
 * <p>
 * {@code new ServerSentEventData().comment("test stream")} would send the
 * following event (followed by blank line):
 * <pre>
 * :test stream
 *
 * </pre>
 *
 * <p>
 * {@code new ServerSentEventData().data("first event").id("1")} would send the
 * following event (followed by blank line):
 * <pre>
 * data:first event
 * id:1
 * </pre>
 *
 * <p>
 * {@code new ServerSentEventData().event("add").data("73857293")} would send the
 * following event (followed by blank line):
 * <pre>
 * event:add
 * data:73857293
 *
 * </pre>
 *
 * @author Jitendra Kotamraju
 */
public final class ServerSentEventData {

    private final StringBuilder strBuilder = new StringBuilder();

    public ServerSentEventData comment(String comment) {
        strBuilder.append(':');
        strBuilder.append(comment);
        strBuilder.append('\n');
        return this;
    }

    public ServerSentEventData data(String data) {
        strBuilder.append("data:");
        strBuilder.append(data);
        strBuilder.append('\n');
        return this;
    }

    public ServerSentEventData data() {
        strBuilder.append("data");
        strBuilder.append('\n');
        return this;
    }

    public ServerSentEventData event(String event) {
        strBuilder.append("event:");
        strBuilder.append(event);
        strBuilder.append('\n');
        return this;
    }

    public ServerSentEventData id(String id) {
        strBuilder.append("id:");
        strBuilder.append(id);
        strBuilder.append('\n');
        return this;
    }

    public ServerSentEventData id() {
        strBuilder.append("id");
        strBuilder.append('\n');
        return this;
    }

    public ServerSentEventData retry(int retry) {
        strBuilder.append("retry:");
        strBuilder.append(String.valueOf(retry));
        strBuilder.append('\n');
        return this;
    }

    @Override
    public String toString() {
        return strBuilder.toString();
    }
}
