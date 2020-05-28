/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.webservices.monitoring;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This class encapsulates all information contained in the thread local
 * for servlet invocations to trace the messages.
 *
 * @author Jerome Dochez
 */
public class ThreadLocalInfo {

    private final String messageId;
    private final HttpServletRequest request;
    private MessageTraceImpl requestMessageTrace;

    public ThreadLocalInfo(String messageId, HttpServletRequest request) {
        this.messageId = messageId;
        this.request = request;
    }

    public String getMessageId() {
        return messageId;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequestMessageTrace(MessageTraceImpl trace) {
        requestMessageTrace = trace;
    }

    public MessageTraceImpl getRequestMessageTrace() {
        return requestMessageTrace;
    }

}
