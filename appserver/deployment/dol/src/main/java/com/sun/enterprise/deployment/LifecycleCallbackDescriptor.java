/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment;

import org.glassfish.deployment.common.Descriptor;

import java.lang.reflect.Method;

/**
 * Deployment object representing the lifecycle-callback.
 *
 * @author Shing Wai Chan
 */
public class LifecycleCallbackDescriptor extends Descriptor {

    private String lifecycleCallbackClass;
    private String lifecycleCallbackMethod;
    private String defaultLifecycleCallbackClass;
    private MetadataSource metadataSource = MetadataSource.XML;

    public enum CallbackType {

        AROUND_CONSTRUCT,
        POST_CONSTRUCT,
        PRE_DESTROY,
        PRE_PASSIVATE,
        POST_ACTIVATE

    }

    public void setLifecycleCallbackClass(String clazz) {
        lifecycleCallbackClass = clazz;
    }

    public String getLifecycleCallbackClass() {
        if (lifecycleCallbackClass == null ||
            lifecycleCallbackClass.trim().equals("")) {
            return defaultLifecycleCallbackClass;
        } else {
            return lifecycleCallbackClass;
        }
    }

    public void setDefaultLifecycleCallbackClass(String clazz) {
        defaultLifecycleCallbackClass = clazz;
    }

    public String getDefaultLifecycleCallbackClass() {
        return defaultLifecycleCallbackClass;
    }

    public void setLifecycleCallbackMethod(String method) {
        lifecycleCallbackMethod = method;
    }

    public String getLifecycleCallbackMethod() {
        return lifecycleCallbackMethod;
    }

    /**
     * Given a classloader, find the Method object corresponding to this
     * lifecycle callback.
     *
     * @throw Exception if no method found
     */
    public Method getLifecycleCallbackMethodObject(ClassLoader loader)
        throws Exception {

        Method method = null;

        if( getLifecycleCallbackClass() == null ) {
            throw new IllegalArgumentException("no lifecycle class defined");
        }

        // according to the ejb interceptors spec the around invoke and life cycle methods can be on the super class.
        Class clazz = loader.loadClass(getLifecycleCallbackClass());

        while ( method == null && ! clazz.equals( Object.class ) ) {
            for(Method next : clazz.getDeclaredMethods()) {
                if( next.getName().equals(lifecycleCallbackMethod) ) {
                    method = next;
                    break;
                }
            }
            if ( method == null ) {
                clazz = clazz.getSuperclass();
            }
        }

        if( method == null ) {
            throw new NoSuchMethodException("no method matching " + lifecycleCallbackMethod);
        }

        return method;
    }

    public MetadataSource getMetadataSource() {
        return metadataSource;
    }

    public void setMetadataSource(MetadataSource metadataSource) {
        this.metadataSource = metadataSource;
    }
}
