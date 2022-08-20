/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class InjectionInfo {

    private List<InjectionCapable> injectionResources;

    // Name of post-consturct method.  Set to null if there is no post-construct
    // method for this class.
    private String postConstructMethodName;

    // Cached post-construct reflection object method.  Only
    // set at runtime.
    private transient Method postConstructMethod;

    // Name of pre-destroy method.  Set to null if there is no pre-destroy
    // method for this class.
    private String preDestroyMethodName;

    // Cached pre-destroy reflection object method.  Only
    // set at runtime.
    private transient Method preDestroyMethod;

    private String className;

    public InjectionInfo() {
    }


    public InjectionInfo(String cName, String postmName, String premName, List<InjectionCapable> resources) {
        className = cName;
        postConstructMethodName = postmName;
        preDestroyMethodName = premName;
        injectionResources = resources;
    }

    public List<InjectionCapable> getInjectionResources() {
        if (injectionResources == null) {
            injectionResources = new LinkedList<>();
        }
        return injectionResources;
    }

    public void setInjectionResources(List<InjectionCapable> resources) {
        injectionResources = resources;
    }

    public String getPostConstructMethodName() {
        return postConstructMethodName;
    }

    public void setPostConstructMethodName(String methodName) {
        postConstructMethodName = methodName;
    }

    public Method getPostConstructMethod() {
        return postConstructMethod;
    }

    public void setPostConstructMethod(Method method) {
        postConstructMethod = method;
    }

    public String getPreDestroyMethodName() {
        return preDestroyMethodName;
    }

    public Method getPreDestroyMethod() {
        return preDestroyMethod;
    }

    public void setPreDestroyMethod(Method method) {
        preDestroyMethod = method;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String name) {
        className = name;
    }
}
