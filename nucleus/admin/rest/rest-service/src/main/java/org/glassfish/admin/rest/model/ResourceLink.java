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

package org.glassfish.admin.rest.model;

import java.net.URI;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Describes a link from one resource to another resource. Used to return links in response bodies.
 *
 * @author tmoreau
 */
public class ResourceLink {

    private String rel;
    private String title;
    private URI uri;

    public ResourceLink(String rel, String title, URI uri) {
        this.rel = rel;
        this.title = title;
        this.uri = uri;
    }

    public ResourceLink(String rel, URI uri) {
        this.rel = rel;
        this.uri = uri;
    }

    public URI getURI() {
        return this.uri;
    }

    public void setURI(URI val) {
        this.uri = val;
    }

    public String getRelationship() {
        return this.rel;
    }

    public void setRelationship(String val) {
        this.rel = val;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String val) {
        this.title = val;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("rel", getRelationship());
        object.put("uri", getURI().toASCIIString());
        String t = getTitle();
        if (t != null && t.length() > 0) {
            object.put("title", t);
        }
        return object;
    }
}
