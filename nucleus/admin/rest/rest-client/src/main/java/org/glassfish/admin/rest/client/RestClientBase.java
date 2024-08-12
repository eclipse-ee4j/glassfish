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

package org.glassfish.admin.rest.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.client.utils.Util;

/**
 *
 * @author jasonlee
 */
public abstract class RestClientBase {
    protected static final String RESPONSE_TYPE = MediaType.APPLICATION_JSON;
    protected Map<String, Object> entityValues = new HashMap<String, Object>();
    protected List<String> children;
    protected int status;
    protected String message;
    protected Client client;
    protected RestClientBase parent;

    private boolean initialized = false;
    private boolean isNew = false;

    protected RestClientBase(Client c, RestClientBase p) {
        client = c;
        parent = p;
    }

    protected RestClientBase getParent() {
        return parent;
    }

    protected String getRestUrl() {
        return getParent().getRestUrl() + getSegment();
    }

    protected abstract String getSegment();

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public boolean save() {
        Response response = client.target(getRestUrl()).request(RESPONSE_TYPE)
                .post(buildMultivalueMapEntity(entityValues, MediaType.MULTIPART_FORM_DATA_TYPE), Response.class);
        boolean success = isSuccess(response);

        if (!success) {
            status = response.getStatus();
            message = response.readEntity(String.class);
        } else {
            isNew = false;
        }

        return success;
    }

    public boolean delete() {
        Response response = client.target(getRestUrl()).request(RESPONSE_TYPE).delete(Response.class);
        boolean success = isSuccess(response);

        if (!success) {
            status = response.getStatus();
            message = response.readEntity(String.class);
        }

        return success;
    }

    public RestResponse execute(Method method, String endpoint, boolean needsMultiPart) {
        return execute(method, endpoint, new HashMap<String, Object>(), needsMultiPart);
    }

    public RestResponse execute(Method method, String endPoint, Map<String, Object> payload) {
        return execute(method, endPoint, payload, false);
    }

    public RestResponse execute(Method method, String endPoint, Map<String, Object> payload, boolean needsMultiPart) {
        final WebTarget target = client.target(getRestUrl() + endPoint);
        Response clientResponse;
        switch (method) {
        case POST: {
            //                 TODO - JERSEY2
            //                if (needsMultiPart) {
            //                    clientResponse = target
            //                            .request(RESPONSE_TYPE)
            //                            .post(buildFormDataMultipart(payload, MediaType.MULTIPART_FORM_DATA_TYPE), Response.class);
            //                } else
            {
                clientResponse = target.request(RESPONSE_TYPE).post(buildMultivalueMapEntity(payload, null), Response.class);

            }
            break;
        }
        case PUT: {
            //                TODO - JERSEY2
            //                if (needsMultiPart) {
            //                    clientResponse = target
            //                            .request(RESPONSE_TYPE)
            //                            .put(buildFormDataMultipart(payload, MediaType.MULTIPART_FORM_DATA_TYPE), Response.class);
            //                } else
            {
                clientResponse = target.request(RESPONSE_TYPE).put(buildMultivalueMapEntity(payload, null), Response.class);

            }
            break;
        }
        case DELETE: {
            //                addQueryParams(payload, target);
            clientResponse = targetWithQueryParams(target, buildMultivalueMap(payload)).request(RESPONSE_TYPE).delete(Response.class);
            break;
        }
        default: {
            //                addQueryParams(payload, target);
            clientResponse = targetWithQueryParams(target, buildMultivalueMap(payload)).request(RESPONSE_TYPE).get(Response.class);
        }
        }

        return new RestResponse(clientResponse);
    }

    private static WebTarget targetWithQueryParams(WebTarget target, MultivaluedMap<String, Object> paramMap) {
        for (Entry<String, List<Object>> param : paramMap.entrySet()) {
            target = target.queryParam(param.getKey(), param.getValue());
        }
        return target;
    }

    protected boolean isSuccess(Response response) {
        int responseStatus = response.getStatus();
        return ((responseStatus == 200) || (responseStatus == 201));
    }

    protected boolean isNew() {
        return isNew;
    }

    protected void setIsNew() {
        this.isNew = true;
    }

    protected synchronized void initialize() {
        if (!initialized) {
            Response clientResponse = client.target(getRestUrl()).request(RESPONSE_TYPE).get(Response.class);
            Map<String, Object> responseMap = Util.processJsonMap(clientResponse.readEntity(String.class));
            status = clientResponse.getStatus();

            getEntityValues(responseMap);
            getChildren(responseMap);
            initialized = true;
        }
    }

    protected <T> T getValue(String key, Class<T> clazz) {
        initialize();
        T retValue = null;
        Object value = entityValues.get(key);
        if ((value != null) && !(value.equals(JSONObject.NULL))) {
            retValue = (T) value;
        }
        return retValue;
    }

    protected <T> void setValue(String key, T value) {
        initialize();
        entityValues.put(key, value);
    }

    protected Map<String, String> getEntityMetadata(Map<String, Object> extraProperties) {
        Map<String, String> metadata = new HashMap<String, String>();
        List<Map<String, Object>> methods = (List<Map<String, Object>>) extraProperties.get("methods");

        for (Map<String, Object> entry : methods) {
            if ("POST".equals(entry.get("name"))) {
                Map<String, Map> params = (Map<String, Map>) entry.get("messageParameters");

                if (params != null) {
                    for (Map.Entry<String, Map> param : params.entrySet()) {
                        String paramName = param.getKey();
                        Map<String, String> md = (Map<String, String>) param.getValue();
                        metadata.put(paramName, md.get("type"));

                    }
                }
            }
        }

        return metadata;
    }

    protected void getEntityValues(Map<String, Object> responseMap) {
        entityValues = new HashMap<String, Object>();

        Map<String, Object> extraProperties = (Map<String, Object>) responseMap.get("extraProperties");
        if (extraProperties != null) {
            Map<String, String> entity = (Map) extraProperties.get("entity");

            if (entity != null) {
                Map<String, String> metadata = getEntityMetadata(extraProperties);
                for (Map.Entry<String, String> entry : entity.entrySet()) {
                    String type = metadata.get(entry.getKey());
                    Object value = null;
                    if ("int".equals(type)) {
                        value = Integer.parseInt(entry.getValue());
                    } else if ("boolean".equals(type)) {
                        value = Boolean.parseBoolean(entry.getValue());
                    } else {
                        value = entry.getValue();
                    }
                    entityValues.put(entry.getKey(), value);
                }
            }
        }
    }

    protected void getChildren(Map<String, Object> responseMap) {
        children = new ArrayList<String>();

        Map<String, Object> extraProperties = (Map<String, Object>) responseMap.get("extraProperties");
        if (extraProperties != null) {
            Map<String, String> childResources = (Map) extraProperties.get("childResources");

            if (childResources != null) {
                for (Map.Entry<String, String> child : childResources.entrySet()) {
                    children.add(child.getKey());
                }
            }
        }
    }

    /*
    protected void addQueryParams(Map<String, Object> payload, WebResource resource) {
        if ((payload != null) && !payload.isEmpty()) {
    //            resource.queryParams(buildMultivalueMap(payload));
        }
    }
    */

    //    TODO - JERSEY2
    //    protected Entity<FormDataMultiPart> buildFormDataMultipart(Map<String, Object> payload, MediaType type) {
    //        FormDataMultiPart formData = new FormDataMultiPart();
    //        Logger logger = Logger.getLogger(RestClientBase.class.getName());
    //        for (final Map.Entry<String, Object> entry : payload.entrySet()) {
    //            final Object value = entry.getValue();
    //            final String key = entry.getKey();
    //            if (value instanceof Collection) {
    //                for (Object obj : ((Collection) value)) {
    //                    try {
    //                        formData.field(key, obj, MediaType.TEXT_PLAIN_TYPE);
    //                    } catch (ClassCastException ex) {
    //                        if (logger.isLoggable(Level.FINEST)) {
    //                            logger.log(Level.FINEST, "Unable to add key (\"{0}\") w/ value (\"{1}\").",
    //                                new Object[]{key, obj});
    //                        }
    //
    //                        // Allow it to continue b/c this property most likely
    //                        // should have been excluded for this request
    //                    }
    //                }
    //            } else {
    //                //formData.putSingle(key, (value != null) ? value.toString() : value);
    //                try {
    //                    if (value instanceof File) {
    //                        formData.getBodyParts().add((new FileDataBodyPart(key, (File)value)));
    //                    } else {
    //                        formData.field(key, value, MediaType.TEXT_PLAIN_TYPE);
    //                    }
    //                } catch (ClassCastException ex) {
    //                    if (logger.isLoggable(Level.FINEST)) {
    //                        logger.log(Level.FINEST,
    //                                "Unable to add key (\"{0}\") w/ value (\"{1}\")." ,
    //                                new Object[]{key, value});
    //                    }
    //                    // Allow it to continue b/c this property most likely
    //                    // should have been excluded for this request
    //                }
    //            }
    //        }
    //        return Entity.entity(formData, type);
    //    }

    private Entity<MultivaluedMap<String, Object>> buildMultivalueMapEntity(Map<String, Object> payload, MediaType type) {
        return Entity.entity(buildMultivalueMap(payload), type);
    }

    private MultivaluedMap<String, Object> buildMultivalueMap(Map<String, Object> payload) {
        MultivaluedMap formData = new MultivaluedHashMap();
        for (final Map.Entry<String, Object> entry : payload.entrySet()) {
            Object value = entry.getValue();
            if (JSONObject.NULL.equals(value)) {
                value = null;
            } else if (value != null) {
                value = value.toString();
            }
            formData.add(entry.getKey(), value);
        }

        return formData;
    }

    public static enum Method {
        GET, PUT, POST, DELETE
    };
}
