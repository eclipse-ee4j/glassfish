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

package org.glassfish.admin.rest.provider;

import com.sun.enterprise.v3.common.ActionReporter;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.utils.JsonUtil;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.api.ActionReport.MessagePart;

/**
 * @author Ludovic Champenois
 * @author Jason Lee
 * @author mmares
 */
@Provider
@Produces({ MediaType.APPLICATION_JSON, "application/x-javascript" })
public class ActionReportJsonProvider extends BaseProvider<ActionReporter> {

    public ActionReportJsonProvider() {
        super(ActionReporter.class, MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    protected boolean isGivenTypeWritable(Class<?> type, Type genericType) {
        return desiredType.isAssignableFrom(type);
    }

    @Override
    public String getContent(ActionReporter ar) {
        String JSONP = getCallBackJSONP();
        try {
            JSONObject result = processReport(ar);
            int indent = getFormattingIndentLevel();
            if (indent > -1) {
                if (JSONP == null) {
                    return result.toString(indent);
                } else {
                    return JSONP + "(" + result.toString(indent) + ")";
                }
            } else {
                if (JSONP == null) {
                    return result.toString();
                } else {
                    return JSONP + "(" + result.toString() + ")";
                }
            }
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected JSONObject processReport(ActionReporter ar) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("message",
                (ar instanceof RestActionReporter) ? ((RestActionReporter) ar).getCombinedMessage() : decodeEol(ar.getMessage()));
        result.put("command", ar.getActionDescription());
        result.put("exit_code", ar.getActionExitCode());

        Properties properties = ar.getTopMessagePart().getProps();
        if ((properties != null) && (!properties.isEmpty())) {
            result.put("properties", properties);
        }

        Properties extraProperties = ar.getExtraProperties();
        if ((extraProperties != null) && (!extraProperties.isEmpty())) {
            result.put("extraProperties", getExtraProperties(result, extraProperties));
        }

        List<MessagePart> children = ar.getTopMessagePart().getChildren();
        if ((children != null) && (!children.isEmpty())) {
            result.put("children", processChildren(children));
        }

        List<ActionReporter> subReports = ar.getSubActionsReport();
        if ((subReports != null) && (!subReports.isEmpty())) {
            result.put("subReports", processSubReports(subReports));
        }

        return result;
    }

    protected JSONArray processChildren(List<MessagePart> parts) throws JSONException {
        JSONArray array = new JSONArray();

        for (MessagePart part : parts) {
            JSONObject object = new JSONObject();
            object.put("message", decodeEol(part.getMessage()));
            object.put("properties", part.getProps());
            List<MessagePart> children = part.getChildren();
            if (children.size() > 0) {
                object.put("children", processChildren(part.getChildren()));
            }
            array.put(object);
        }

        return array;
    }

    protected JSONArray processSubReports(List<ActionReporter> subReports) throws JSONException {
        JSONArray array = new JSONArray();

        for (ActionReporter subReport : subReports) {
            array.put(processReport(subReport));
        }

        return array;
    }

    protected JSONObject getExtraProperties(JSONObject object, Properties props) throws JSONException {
        JSONObject extraProperties = new JSONObject();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = entry.getKey().toString();
            Object value = JsonUtil.getJsonObject(entry.getValue());
            extraProperties.put(key, value);
        }

        return extraProperties;
    }

    protected <T> T getFieldValue(final ActionReporter ar, final String name, final T type) {
        return AccessController.doPrivileged(new PrivilegedAction<T>() {
            @Override
            public T run() {
                T value = null;
                try {
                    final Class<?> clazz = ar.getClass().getSuperclass();
                    final Field field = clazz.getDeclaredField(name);
                    field.setAccessible(true);
                    value = (T) field.get(ar);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                return value;
            }
        });
    }

    protected String decodeEol(String str) {
        if (str == null) {
            return str;
        }
        return str.replace(ActionReporter.EOL_MARKER, "\n");
    }
}
