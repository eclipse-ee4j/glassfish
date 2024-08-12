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

package org.glassfish.admin.rest.composite.metadata;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.Suspended;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.OptionsCapable;
import org.glassfish.admin.rest.composite.RestCollection;
import org.glassfish.admin.rest.composite.RestModel;
import org.glassfish.admin.rest.utils.Util;

/**
 * This class encapsulates the metadata for the specified REST resource method.
 *
 * @author jdlee
 */
public class RestMethodMetadata {
    private String httpMethod;
    private List<ParamMetadata> queryParameters = new ArrayList<ParamMetadata>();
    /**
     * The type of the incoming request body, if there is one.
     */
    private Type requestPayload;
    /**
     * The type of the response entity. If <code>isCollection</code> is true, this value reflects the type of the items in
     * the collection.
     */
    private Type returnPayload;
    /**
     * Specifies whether or not the return payload is a collection (in which case <code>returnPayload</code> gives the type
     * of the items in the collection) or not.
     */
    private boolean isCollection = false;
    private String path;
    private String produces[];
    private String consumes[];
    private OptionsCapable context;

    public RestMethodMetadata(OptionsCapable context, Method method, Annotation designator) {
        this.context = context;
        this.httpMethod = designator.getClass().getInterfaces()[0].getSimpleName();
        this.returnPayload = calculateReturnPayload(method);
        this.path = getPath(method);

        Consumes cann = getAnnotation(Consumes.class, method);
        Produces pann = getAnnotation(Produces.class, method);
        if (cann != null) {
            consumes = cann.value();
        }
        if (pann != null) {
            produces = pann.value();
        }

        processParameters(method);
    }

    private <T> T getAnnotation(Class<T> annoClass, Method method) {
        T annotation = (T) method.getAnnotation((Class<Annotation>) annoClass);
        if (annotation == null) {
            annotation = (T) method.getDeclaringClass().getAnnotation((Class<Annotation>) annoClass);
        }

        return annotation;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public List<ParamMetadata> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(List<ParamMetadata> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public Type getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(Type requestPayload) {
        this.requestPayload = requestPayload;
    }

    public Type getReturnPayload() {
        return returnPayload;
    }

    public void setReturnPayload(Type returnPayload) {
        this.returnPayload = returnPayload;
    }

    public boolean getIsCollection() {
        return isCollection;
    }

    public void setIsCollection(boolean isCollection) {
        this.isCollection = isCollection;
    }

    @Override
    public String toString() {
        try {
            return toJson().toString();
        } catch (JSONException ex) {
            return ex.getMessage();
        }
    }

    /**
     * Build and return a JSON object representing the metadata for the resource method
     *
     * @return
     * @throws JSONException
     */
    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        if (path != null) {
            o.put("path", path);
        }

        JSONObject queryParamJson = new JSONObject();
        for (ParamMetadata pmd : queryParameters) {
            queryParamJson.put(pmd.getName(), pmd.toJson());
        }

        if (consumes != null) {
            JSONArray array = new JSONArray();
            for (String type : consumes) {
                array.put(type);
            }
            o.put("accepts", array);
        }
        if (produces != null) {
            JSONArray array = new JSONArray();
            for (String type : produces) {
                array.put(type);
            }
            o.put("produces", array);
        }

        o.put("queryParams", queryParamJson);

        if (requestPayload != null) {
            JSONObject requestProps = new JSONObject();
            requestProps.put("isCollection", isCollection);
            requestProps.put("dataType", getTypeString(requestPayload));
            requestProps.put("properties", getProperties(requestPayload));
            o.put("request", requestProps);
        }

        if (returnPayload != null) {
            JSONObject returnProps = new JSONObject();
            returnProps.put("isCollection", isCollection);
            returnProps.put("dataType", getTypeString(returnPayload));
            returnProps.put("properties", getProperties(returnPayload));
            o.put("response", returnProps);
        }

        return o;
    }

    /**
     * Get the return type of the method. If the return type is a generic type, then extract the first generic type from the
     * declaration via reflection. This method does not take into account types with multiple generic types, but, given the
     * style guide, this should not be an issue. If the style guide is modified to allow non-RestModel and
     * non-RestCollection returns (for example), this may need to be modified.
     *
     * @param method
     * @return
     */
    private Type calculateReturnPayload(Method method) {
        final Type grt = method.getGenericReturnType();
        Type value = method.getReturnType();
        if (ParameterizedType.class.isAssignableFrom(grt.getClass())) {
            final ParameterizedType pt = (ParameterizedType) grt;
            if (RestCollection.class.isAssignableFrom((Class) pt.getRawType())) {
                isCollection = true;
                value = Util.getFirstGenericType(grt);
            } else if (RestModel.class.isAssignableFrom((Class) pt.getRawType())) {
                value = Util.getFirstGenericType(grt);
            }
        }

        return value;
    }

    private Type calculateParameterType(Type paramType) {
        Type type;

        if (Util.isGenericType(paramType)) {
            ParameterizedType pt = (ParameterizedType) paramType;
            Class<?> first = Util.getFirstGenericType(paramType);
            if (RestModel.class.isAssignableFrom(first) || RestCollection.class.isAssignableFrom(first)) {
                //            if ((first instanceof RestModel.class) || (first instanceof RestCollection.class)) {
                type = first;
            } else {
                type = pt;
            }
        } else {
            type = paramType;
        }

        return type;
    }

    private String getPath(Method method) {
        Path p = method.getAnnotation(Path.class);
        if (p != null) {
            return p.value();
        } else {
            return null;
        }
    }

    /**
     * Process the parameters for the method. Any parameter marked <code>@PathParam</code> is ignored, since JAX-RS handles
     * setting that value, meaning its presence need not be exposed to the client. Any parameter marked with
     * <code>@QueryParameter</code> is stored in the <code>queryParameter</code> list. Anything left is considered the type
     * of the request body. There should be only one of these.
     *
     * @param method
     */
    private void processParameters(Method method) {
        Type[] paramTypes = method.getGenericParameterTypes();
        Annotation[][] paramAnnos = method.getParameterAnnotations();
        int paramCount = paramTypes.length;

        for (int i = 0; i < paramCount; i++) {
            boolean processed = false;
            boolean isPathParam = false;
            Type paramType = paramTypes[i];
            for (Annotation annotation : paramAnnos[i]) {
                processed = (annotation instanceof Suspended) || (annotation instanceof PathParam);

                if (annotation instanceof QueryParam) {
                    queryParameters.add(new ParamMetadata(context, paramType, ((QueryParam) annotation).value(), paramAnnos[i]));
                    processed = true;
                }
            }
            if (!processed && !isPathParam) {
                requestPayload = calculateParameterType(paramType);
            }
        }
    }

    /**
     * This method will analyze the getters of the given class to determine its properties. Currently, for simplicity's
     * sake, only getters are checked.
     *
     * @param type
     * @return
     * @throws JSONException
     */
    private JSONObject getProperties(Type type) throws JSONException {
        JSONObject props = new JSONObject();
        Class<?> clazz;
        Map<String, ParamMetadata> map = new HashMap<String, ParamMetadata>();
        if (Util.isGenericType(type)) {
            ParameterizedType pt = (ParameterizedType) type;
            clazz = (Class<?>) pt.getRawType();
            //            if (RestModel.class.isAssignableFrom(clazz) || RestCollection.class.isAssignableFrom(clazz)) {
            //                Object model = CompositeUtil.instance().getModel(clazz);
            //                clazz = model.getClass();
            //            }
        } else {
            clazz = (Class<?>) type;
        }

        if (RestModel.class.isAssignableFrom(clazz) || RestCollection.class.isAssignableFrom(clazz)) {
            for (Method m : clazz.getMethods()) {
                String methodName = m.getName();
                if (methodName.startsWith("get")) {
                    String propertyName = methodName.substring(3, 4).toLowerCase(Locale.getDefault()) + methodName.substring(4);
                    map.put(propertyName, new ParamMetadata(context, m.getGenericReturnType(), propertyName, m.getAnnotations()));
                }
            }

            for (Map.Entry<String, ParamMetadata> entry : map.entrySet()) {
                props.put(entry.getKey(), entry.getValue().toJson());
            }
        }

        return props;
    }

    protected String getTypeString(Type clazz) {
        StringBuilder sb = new StringBuilder();
        if (Util.isGenericType(clazz)) {
            ParameterizedType pt = (ParameterizedType) clazz;
            sb.append(((Class<?>) pt.getRawType()).getSimpleName());
            Type[] typeArgs = pt.getActualTypeArguments();
            if ((typeArgs != null) && (typeArgs.length >= 1)) {
                String sep = "";
                sb.append("<");
                for (Type arg : typeArgs) {
                    sb.append(sep).append(((Class<?>) arg).getSimpleName());
                    sep = ",";
                }
                sb.append(">");
            }
        } else {
            sb.append(((Class<?>) clazz).getSimpleName());
        }
        return sb.toString();
    }
}
