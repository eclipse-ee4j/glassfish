/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.RestLogging;
import org.glassfish.admin.rest.results.OptionsResult;

/**
 * JSON provider for OptionsResult.
 *
 * @author Rajeshwar Patil
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class OptionsResultJsonProvider extends BaseProvider<OptionsResult> {
    private static final String NAME = "name";
    private static final String MESSAGE_PARAMETERS = "messageParameters";

    public OptionsResultJsonProvider() {
        super(OptionsResult.class, MediaType.APPLICATION_JSON_TYPE);
    }

    //get json representation for the given OptionsResult object
    @Override
    public String getContent(OptionsResult proxy) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(proxy.getName(), getRespresenationForMethodMetaData(proxy));
        } catch (JSONException ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        }
        return obj.toString();
    }

    public JSONArray getRespresenationForMethodMetaData(OptionsResult proxy) {
        JSONArray arr = new JSONArray();
        Set<String> methods = proxy.methods();
        Iterator<String> iterator = methods.iterator();
        String methodName;
        while (iterator.hasNext()) {
            try {
                methodName = iterator.next();
                MethodMetaData methodMetaData = proxy.getMethodMetaData(methodName);
                JSONObject method = new JSONObject();
                method.put(NAME, methodName);
                //                method.put(QUERY_PARAMETERS, getQueryParams(methodMetaData));
                method.put(MESSAGE_PARAMETERS, getMessageParams(methodMetaData));
                arr.put(method);
            } catch (JSONException ex) {
                RestLogging.restLogger.log(Level.SEVERE, null, ex);
            }
        }

        return arr;
    }

    //    //get json representation for the method query parameters
    //    private JSONObject getQueryParams(MethodMetaData methodMetaData) throws JSONException {
    //        JSONObject obj = new JSONObject();
    //        if (methodMetaData.sizeQueryParamMetaData() > 0) {
    //            Set<String> queryParams = methodMetaData.queryParams();
    //            Iterator<String> iterator = queryParams.iterator();
    //            String queryParam;
    //            while (iterator.hasNext()) {
    //                queryParam = iterator.next();
    //                ParameterMetaData parameterMetaData = methodMetaData.getQueryParamMetaData(queryParam);
    //                obj.put(queryParam, getParameter(parameterMetaData));
    //            }
    //        }
    //
    //        return obj;
    //    }

    private JSONObject getParameter(ParameterMetaData parameterMetaData) throws JSONException {
        JSONObject result = new JSONObject();
        Iterator<String> iterator = parameterMetaData.attributes().iterator();
        String attributeName;
        while (iterator.hasNext()) {
            attributeName = iterator.next();
            result.put(attributeName, parameterMetaData.getAttributeValue(attributeName));
        }
        return result;
    }

    private JSONObject getMessageParams(MethodMetaData methodMetaData) throws JSONException {
        JSONObject result = new JSONObject();
        if (methodMetaData.sizeParameterMetaData() > 0) {
            Set<String> parameters = methodMetaData.parameters();
            Iterator<String> iterator = parameters.iterator();
            String parameter;
            while (iterator.hasNext()) {
                parameter = iterator.next();
                ParameterMetaData parameterMetaData = methodMetaData.getParameterMetaData(parameter);
                result.put(parameter, getParameter(parameterMetaData));
            }
        }

        return result;
    }
}
