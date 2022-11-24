/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.remote.reader;

import com.sun.enterprise.admin.util.AdminLoggerInfo;
import com.sun.enterprise.util.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.MessagePart;

/**
 * Reads ActionReport from JSON format.
 *
 * @author mmares
 */
public final class ActionReportJsonProprietaryReader implements ProprietaryReader<ActionReport> {

    static class LoggerRef {
        private static final Logger logger = AdminLoggerInfo.getLogger();
    }

    @Override
    public boolean isReadable(final Class<?> type, final String mimetype) {
        return type.isAssignableFrom(ActionReport.class);
    }

    public ActionReport readFrom(final HttpURLConnection urlConnection) throws IOException {
        return readFrom(urlConnection.getInputStream(), urlConnection.getContentType());
    }

    @Override
    public ActionReport readFrom(final InputStream is, final String contentType) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            FileUtils.copy(is, baos);
        } finally {
            is.close();
        }
        String str = baos.toString(StandardCharsets.UTF_8);
        try {
            JSONObject json = new JSONObject(str);
            CliActionReport result = new CliActionReport();
            fillActionReport(result, json);
            return result;
        } catch (JSONException ex) {
            LoggerRef.logger.log(Level.SEVERE, AdminLoggerInfo.mUnexpectedException, ex);
            throw new IOException(ex);
        }
    }

    public static void fillActionReport(final ActionReport ar, final JSONObject json) throws JSONException {
        ar.setActionExitCode(ActionReport.ExitCode.valueOf(json.getString("exit_code")));
        ar.setActionDescription(json.optString("command"));
        String failure = json.optString("failure_cause");
        if (failure != null && !failure.isEmpty()) {
            ar.setFailureCause(new Exception(failure));
        }
        ar.setExtraProperties((Properties) extractMap(json.optJSONObject("extraProperties"), new Properties()));
        ar.getTopMessagePart().setMessage(json.optString("top_message", json.optString("message")));
        Properties props = (Properties) extractMap(json.optJSONObject("properties"), new Properties());
        for (Map.Entry entry : props.entrySet()) {
            ar.getTopMessagePart().addProperty(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        //Sub messages
        fillSubMessages(ar.getTopMessagePart(), json.optJSONArray("children"));
        //Sub action reports
        JSONArray subJsons = json.optJSONArray("subReports");
        if (subJsons != null) {
            for (int i = 0; i < subJsons.length(); i++) {
                JSONObject subJson = subJsons.getJSONObject(i);
                fillActionReport(ar.addSubActionsReport(), subJson);
            }
        }
    }

    private static void fillSubMessages(final ActionReport.MessagePart mp, final JSONArray json) throws JSONException {
        if (json == null) {
            return;
        }
        for (int i = 0; i < json.length(); i++) {
            JSONObject subJson = json.getJSONObject(i);
            MessagePart child = mp.addChild();
            child.setMessage(subJson.optString("message"));
            Properties props = (Properties) extractMap(subJson.optJSONObject("properties"), new Properties());
            for (Map.Entry entry : props.entrySet()) {
                child.addProperty(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
            fillSubMessages(child, subJson.optJSONArray("children"));
        }
    }

    //    private void fillMessage(ActionReport.MessagePart mp, JSONObject json) throws JSONException {
    //        mp.setMessage(json.optString("value"));
    //        mp.setChildrenType(json.optString("children-type"));
    //        Properties props = extractProperties("properties", json);
    //        for (String key : props.stringPropertyNames()) {
    //            mp.addProperty(key, props.getProperty(key));
    //        }
    //        JSONArray subJsons = extractArray("messages", json);
    //        for (int i = 0; i < subJsons.length(); i++) {
    //            JSONObject subJson = subJsons.getJSONObject(i);
    //            fillMessage(mp.addChild(), subJson);
    //        }
    //    }

    private static Object extractGeneral(final Object obj) throws JSONException {
        if (obj == null) {
            return null;
        }
        if (obj instanceof JSONObject) {
            return extractMap((JSONObject) obj, null);
        } else if (obj instanceof JSONArray) {
            return extractCollection((JSONArray) obj, null);
        } else {
            return obj;
        }
    }

    private static Map extractMap(final JSONObject json, Map preferredResult) throws JSONException {
        if (json == null) {
            return preferredResult;
        }
        if (preferredResult == null) {
            preferredResult = new HashMap();
        }
        Iterator keys = json.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            preferredResult.put(key, extractGeneral(json.get(key)));
        }
        return preferredResult;
    }

    private static Collection extractCollection(final JSONArray array, Collection preferredResult) throws JSONException {
        if (array == null) {
            return preferredResult;
        }
        if (preferredResult == null) {
            preferredResult = new ArrayList(array.length());
        }
        for (int i = 0; i < array.length(); i++) {
            preferredResult.add(extractGeneral(array.get(i)));
        }
        return preferredResult;
    }

    //    private Properties extractProperties(final String key, final JSONObject json) throws JSONException {
    //        Properties result = new Properties();
    //        JSONArray array = extractArray(key, json);
    //        for (int i = 0; i < array.length(); i++) {
    //            JSONObject entry = array.getJSONObject(i);
    //            Iterator keys = entry.keys();
    //            while (keys.hasNext()) {
    //                String inKey = (String) keys.next();
    //                result.put(inKey, entry.getString(key));
    //            }
    //        }
    //        return result;
    //    }
    //
    //    private JSONArray extractArray(final String key, final JSONObject json) {
    //        Object res = json.opt(key);
    //        if (res == null) {
    //            return new JSONArray();
    //        }
    //        if (res instanceof JSONArray) {
    //            return (JSONArray) res;
    //        } else {
    //            JSONArray result = new JSONArray();
    //            result.put(res);
    //            return result;
    //        }
    //    }

}
