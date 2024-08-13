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

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MultivaluedHashMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.OptionsCapable;

/**
 *
 * @author jdlee
 */
public class RestResourceMetadata {
    private MultivaluedHashMap<String, RestMethodMetadata> resourceMethods = new MultivaluedHashMap<String, RestMethodMetadata>();
    private List<String> subResources = new ArrayList<String>();
    private OptionsCapable context;

    public RestResourceMetadata(OptionsCapable context) {
        this.context = context;
        processClass();
    }

    public MultivaluedHashMap<String, RestMethodMetadata> getResourceMethods() {
        return resourceMethods;
    }

    public void setResourceMethods(MultivaluedHashMap<String, RestMethodMetadata> resourceMethods) {
        this.resourceMethods = resourceMethods;
    }

    public List<String> getSubResources() {
        return subResources;
    }

    public void setSubResources(List<String> subResources) {
        this.subResources = subResources;
    }

    private void processClass() {
        for (Method m : context.getClass().getMethods()) {
            Annotation designator = getMethodDesignator(m);

            if (designator != null) {
                final String httpMethod = designator.annotationType().getSimpleName();
                RestMethodMetadata rmm = new RestMethodMetadata(context, m, designator);
                //                if (resourceMethods.containsKey(httpMethod)) {
                //                    throw new RuntimeException("Multiple " + httpMethod + " methods found on resource: " +
                //                            context.getClass().getName());
                //                }
                resourceMethods.add(httpMethod, rmm);
            }

            final Path path = m.getAnnotation(Path.class);
            if (path != null) {
                subResources.add(context.getUriInfo().getAbsolutePathBuilder().build().toASCIIString() + "/" + path.value());
            }
        }

        Collections.sort(subResources);
    }

    private Annotation getMethodDesignator(Method method) {
        Annotation a = method.getAnnotation(GET.class);
        if (a == null) {
            a = method.getAnnotation(POST.class);
            if (a == null) {
                a = method.getAnnotation(DELETE.class);
                if (a == null) {
                    a = method.getAnnotation(OPTIONS.class);
                }
            }
        }

        return a;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();

        if (!resourceMethods.isEmpty()) {
            final JSONObject methods = new JSONObject();
            for (String key : resourceMethods.keySet()) {
                for (RestMethodMetadata rmm : resourceMethods.get(key)) {
                    methods.accumulate(key, rmm.toJson());
                }
            }

            o.accumulate("resourceMethods", methods);
        }

        if (!subResources.isEmpty()) {
            o.put("subResources", subResources);
        }

        return o;
    }
}
