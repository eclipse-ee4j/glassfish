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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.composite.RestModel;
import org.glassfish.admin.rest.utils.JsonUtil;

/**
 * Used to return a standard REST response body that contains a single entity
 *
 * @author tmoreau
 */
public class RestModelResponseBody<T extends RestModel> extends ResponseBody {
    private T entity;

    public RestModelResponseBody() {
        super();
    }

    public RestModelResponseBody(boolean includeResourceLinks) {
        super(includeResourceLinks);
    }

    public T getEntity() {
        return entity;
    }

    public RestModelResponseBody<T> setEntity(T entity) {
        this.entity = entity;
        return this;
    }

    @Override
    protected void populateJson(JSONObject object) throws JSONException {
        super.populateJson(object);
        if (getEntity() != null) {
            object.put("item", JsonUtil.getJsonObject(getEntity()));
        }
    }
}
