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

package com.sun.enterprise.container.common.spi.util;


import com.sun.enterprise.deployment.InterceptorDescriptor;

import java.util.*;

import java.lang.reflect.Method;

/**
 */

public class InterceptorInfo {

    private List aroundConstructInterceptors = new LinkedList();
    private List postConstructInterceptors = new LinkedList();
    private List preDestroyInterceptors = new LinkedList();

    private Map<Method, List> aroundInvokeChains = new HashMap<Method, List>();

    private Set<String> interceptorClassNames = new HashSet<String>();

    // True if a system interceptor needs to be added dynamically
    private boolean supportRuntimeDelegate;

    private Object targetObjectInstance;
    private Class targetClass;

    private boolean hasTargetClassAroundInvoke = false;

    public void setTargetObjectInstance(Object instance) {
        targetObjectInstance = instance;
    }

    public Object getTargetObjectInstance() {
        return targetObjectInstance;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public Class getTargetClass() {
        return this.targetClass;
    }

    public void setAroundConstructInterceptors(List interceptors) {
        aroundConstructInterceptors = interceptors;
    }

    public List getAroundConstructInterceptors() {
        return new LinkedList(aroundConstructInterceptors);
    }

    public void setPostConstructInterceptors(List interceptors) {
        postConstructInterceptors = interceptors;
    }

    public List getPostConstructInterceptors() {
        return new LinkedList(postConstructInterceptors);
    }

    public void setPreDestroyInterceptors(List interceptors) {
        preDestroyInterceptors = interceptors;
    }

    public List getPreDestroyInterceptors() {
        return new LinkedList(preDestroyInterceptors);
    }

    public void setInterceptorClassNames(Set<String> names) {
        interceptorClassNames = new HashSet<String>(names);
    }

    public Set<String> getInterceptorClassNames() {
        return interceptorClassNames;
    }

    public void setAroundInvokeInterceptorChains(Map<Method, List> chains) {
        aroundInvokeChains = new HashMap<Method, List>(chains);
    }


    public void setHasTargetClassAroundInvoke(boolean flag) {
        hasTargetClassAroundInvoke = flag;
    }

    public boolean getHasTargetClassAroundInvoke() {
        return hasTargetClassAroundInvoke;
    }


    public List getAroundInvokeInterceptors(Method m) {

        return aroundInvokeChains.get(m);
    }

    public boolean getSupportRuntimeDelegate() {
        return supportRuntimeDelegate;
    }

    public void setSupportRuntimeDelegate(boolean flag) {
        supportRuntimeDelegate = flag;
    }
}
