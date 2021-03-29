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

package org.glassfish.admin.rest.model;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Message {
    public static enum Severity {
        SUCCESS, WARNING, FAILURE
    };

    private Severity severity;
    private String field;
    private String message;

    public Message(Severity severity, String message) {
        this.severity = severity;
        this.message = message;
    }

    public Message(Severity severity, String field, String message) {
        this.severity = severity;
        this.field = field;
        this.message = message;
    }

    public Severity getSeverity() {
        return this.severity;
    }

    public void setSeverity(Severity val) {
        this.severity = val;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String val) {
        this.message = val;
    }

    public String getField() {
        return this.field;
    }

    public void setField(String val) {
        this.field = val;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("message", getMessage());
        object.put("severity", getSeverity());
        String f = getField();
        if (f != null && f.length() > 0) {
            object.put("field", f);
        }
        return object;
    }
}
