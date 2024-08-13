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

import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.composite.RestModel;
import org.glassfish.admin.rest.utils.JsonUtil;

/**
 * Used to return a standard REST response body that contains a collection of entities
 *
 * @author tmoreau
 */
public class RestCollectionResponseBody<T extends RestModel> extends ResponseBody {

    private String collectionName;
    private UriInfo uriInfo;
    private List<T> items = new ArrayList<>();

    // If you want this object to automatically compute the links to the child entities,
    // then include the collection name (which becomes the 'rel' part of the link) and the
    // URI of the collection resource, then just call 'addItem(item, name)' for each child.
    // Otherwise pass in null for both, and call the appropriate addItem method
    // to either add a child without a link, or a child where you control the link.

    public RestCollectionResponseBody(UriInfo uriInfo, String collectionName) {
        super();
        setUriInfo(uriInfo);
        setCollectionName(collectionName);
    }

    public RestCollectionResponseBody(boolean includeResourceLinks, UriInfo uriInfo, String collectionName) {
        super(includeResourceLinks);
        setUriInfo(uriInfo);
        setCollectionName(collectionName);
    }

    public String getCollectionName() {
        return this.collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public UriInfo getUriInfo() {
        return this.uriInfo;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public List<T> getItems() {
        return this.items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public void addItem(T item, String name) {
        URI uri = (this.collectionName != null) ? this.uriInfo.getAbsolutePathBuilder().path("id").path(name).build() : null;
        addItem(item, name, uri);
    }

    public void addItem(T item, String name, URI uri) {
        addItem(item, this.collectionName, name, uri);
    }

    public void addItem(T item, String collectionName, String name, URI uri) {
        getItems().add(item);
        if (collectionName != null && uri != null) {
            addResourceLink(collectionName, name, uri);
        }
    }

    @Override
    protected void populateJson(JSONObject object) throws JSONException {
        super.populateJson(object);
        JSONArray array = new JSONArray();
        for (RestModel item : getItems()) {
            array.put(JsonUtil.getJsonObject(item));
        }
        object.put("items", array);
    }
}
