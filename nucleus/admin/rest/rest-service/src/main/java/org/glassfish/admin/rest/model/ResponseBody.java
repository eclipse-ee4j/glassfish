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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class ResponseBody {
    public static final String EVENT_NAME = "response/body";
    private List<Message> messages = new ArrayList<Message>();
    private boolean includeResourceLinks = true;
    private List<ResourceLink> links = new ArrayList<ResourceLink>();

    public ResponseBody() {
    }

    public ResponseBody(boolean includeResourceLinks) {
        setIncludeResourceLinks(includeResourceLinks);
    }

    public ResponseBody(URI parentUri) {
        addParentResourceLink(parentUri);
    }

    public ResponseBody(boolean includeResourceLinks, URI parentUri) {
        setIncludeResourceLinks(includeResourceLinks);
        addParentResourceLink(parentUri);
    }

    public void setIncludeResourceLinks(boolean includeResourceLinks) {
        this.includeResourceLinks = includeResourceLinks;
    }

    public List<Message> getMessages() {
        return this.messages;
    }

    public void setMessages(List<Message> val) {
        this.messages = val;
    }

    public ResponseBody addSuccess(String message) {
        return addMessage(Message.Severity.SUCCESS, message);
    }

    public ResponseBody addWarning(String message) {
        return addMessage(Message.Severity.WARNING, message);
    }

    public ResponseBody addFailure(Throwable t) {
        for (; t != null; t = t.getCause()) {
            addFailure(t.getLocalizedMessage());
        }
        return this;
    }

    public ResponseBody addFailure(String message) {
        return addMessage(Message.Severity.FAILURE, message);
    }

    public ResponseBody addFailure(String field, String message) {
        return addMessage(Message.Severity.FAILURE, field, message);
    }

    public ResponseBody addMessage(Message.Severity severity, String field, String message) {
        return add(new Message(severity, field, message));
    }

    public ResponseBody addMessage(Message.Severity severity, String message) {
        return add(new Message(severity, message));
    }

    public ResponseBody add(Message message) {
        getMessages().add(message);
        return this;
    }

    public List<ResourceLink> getResourceLinks() {
        return this.links;
    }

    public void setResourceLinks(List<ResourceLink> val) {
        this.links = val;
    }

    public ResponseBody addParentResourceLink(URI uri) {
        if (uri == null) {
            return this;
        }
        return addResourceLink("parent", uri);
    }

    public void addActionResourceLink(String action, URI uri) {
        addResourceLink("action", action, uri);
    }

    public ResponseBody addResourceLink(String rel, URI uri) {
        return add(new ResourceLink(rel, uri));
    }

    public ResponseBody addResourceLink(String rel, String title, URI uri) {
        return add(new ResourceLink(rel, title, uri));
    }

    public ResponseBody add(ResourceLink link) {
        getResourceLinks().add(link);
        return this;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        populateJson(object);
        return object;
    }

    protected void populateJson(JSONObject object) throws JSONException {
        if (!getMessages().isEmpty()) {
            JSONArray array = new JSONArray();
            for (Message message : getMessages()) {
                array.put(message.toJson());
            }
            object.put("messages", array);
        }
        if (includeResourceLinks) {
            if (!getResourceLinks().isEmpty()) {
                JSONArray array = new JSONArray();
                for (ResourceLink link : getResourceLinks()) {
                    array.put(link.toJson());
                }
                object.put("resources", array);
            }
        }
    }
}
