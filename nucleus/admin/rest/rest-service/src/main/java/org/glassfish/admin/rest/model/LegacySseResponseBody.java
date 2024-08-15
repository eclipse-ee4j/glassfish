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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.composite.RestModel;

/**
 *
 * @author jdlee
 */
public class LegacySseResponseBody<T extends RestModel> extends RestModelResponseBody<T> {

    private Map<String, List<String>> headers = new HashMap<String, List<String>>();

    public LegacySseResponseBody() {
        super();
    }

    public LegacySseResponseBody(boolean includeResourceLinks) {
        super(includeResourceLinks);
    }

    public LegacySseResponseBody<T> addHeader(String name, Object value) {
        if (value != null) {
            List<String> values = headers.get(name);
            if (values == null) {
                values = new ArrayList<String>();
                headers.put(name, values);
            }
            values.add(value.toString());
        }

        return this;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = super.toJson();

        if (!headers.isEmpty()) {
            JSONObject o = new JSONObject();
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                final String key = entry.getKey();
                for (String value : entry.getValue()) {
                    o.accumulate(key, value);
                }
            }
            json.accumulate("headers", o);
        }

        return json;
    }

}
