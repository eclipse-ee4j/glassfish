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

package org.glassfish.admin.rest.client.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.api.logging.LogHelper;

/**
 *
 * @author jdlee
 */
public class Util {

    public static Map<String, Object> processJsonMap(String json) {
        Map<String, Object> map;
        try {
            map = processJsonObject(new JSONObject(json));
        } catch (JSONException e) {
            map = new HashMap();
        }
        return map;
    }

    public static Map processJsonObject(JSONObject jo) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Iterator i = jo.keys();
            while (i.hasNext()) {
                String key = (String) i.next();
                Object value = jo.get(key);
                if (value instanceof JSONArray) {
                    map.put(key, processJsonArray((JSONArray) value));
                } else if (value instanceof JSONObject) {
                    map.put(key, processJsonObject((JSONObject) value));
                } else {
                    map.put(key, value);
                }
            }
        } catch (JSONException e) {
            LogHelper.log(RestClientLogging.logger, Level.SEVERE, RestClientLogging.REST_CLIENT_JSON_ERROR, e);
        }

        return map;
    }

    public static List processJsonArray(JSONArray ja) {
        List results = new ArrayList();

        try {
            for (int i = 0; i < ja.length(); i++) {
                Object entry = ja.get(i);
                if (entry instanceof JSONArray) {
                    results.add(processJsonArray((JSONArray) entry));
                } else if (entry instanceof JSONObject) {
                    results.add(processJsonObject((JSONObject) entry));
                } else {
                    results.add(entry);
                }
            }
        } catch (JSONException e) {
            LogHelper.log(RestClientLogging.logger, Level.SEVERE, RestClientLogging.REST_CLIENT_JSON_ERROR, e);
        }

        return results;
    }

}
