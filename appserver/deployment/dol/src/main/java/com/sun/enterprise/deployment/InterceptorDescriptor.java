/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * Contains information about 1 Java EE interceptor.
 */

public class InterceptorDescriptor extends JndiEnvironmentRefsGroupDescriptor
{
    private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(InterceptorDescriptor.class);

    private static final Logger _logger = DOLUtils.getDefaultLogger();

    private Set<LifecycleCallbackDescriptor> aroundInvokeDescriptors;
    private Set<LifecycleCallbackDescriptor> aroundTimeoutDescriptors;
    private String interceptorClassName;

    private Class<?> interceptorClass;


    // true if the AroundInvoke/AroundTimeout/Callback methods for this
    // descriptor were defined on the bean class itself (or one of its
    // super-classes).  false if the methods are defined
    // on a separate interceptor class (or one of its super-classes).
    private boolean fromBeanClass = false;

    public String getInterceptorClassName() {
        return interceptorClassName;
    }

    public void setInterceptorClassName(String className) {
        interceptorClassName = className;
    }


    public Class getInterceptorClass() {
        return interceptorClass;
    }

    // Should ONLY be used for system-level interceptors whose class
    // is loaded by something other than the application class-loader
    public void setInterceptorClass(Class c) {
        interceptorClass = c;
        setInterceptorClassName(c.getName());
    }


    public Set<LifecycleCallbackDescriptor> getAroundInvokeDescriptors() {
        if (aroundInvokeDescriptors == null) {
            aroundInvokeDescriptors =
                new HashSet<LifecycleCallbackDescriptor>();
        }
        return aroundInvokeDescriptors;
    }

    /**
     * Some clients need the AroundInvoke methods for this inheritance
     * hierarchy in the spec-defined "least derived --> most derived" order.
     */
    public List<LifecycleCallbackDescriptor> getOrderedAroundInvokeDescriptors
        (ClassLoader loader) throws Exception {

        return orderDescriptors(getAroundInvokeDescriptors(), loader);

    }

    public Set<LifecycleCallbackDescriptor> getAroundTimeoutDescriptors() {
        if (aroundTimeoutDescriptors == null) {
            aroundTimeoutDescriptors =
                new HashSet<LifecycleCallbackDescriptor>();
        }
        return aroundTimeoutDescriptors;
    }

    /**
     * Some clients need the AroundTimeout methods for this inheritance
     * hierarchy in the spec-defined "least derived --> most derived" order.
     */
    public List<LifecycleCallbackDescriptor> getOrderedAroundTimeoutDescriptors
        (ClassLoader loader) throws Exception {

        return orderDescriptors(getAroundTimeoutDescriptors(), loader);

    }

    public void setFromBeanClass(boolean flag) {
        fromBeanClass = flag;
    }

    public boolean getFromBeanClass() {
        return fromBeanClass;
    }

    public void addAroundInvokeDescriptor(LifecycleCallbackDescriptor aroundInvokeDesc) {
        Set<LifecycleCallbackDescriptor> aroundInvokeDescs =
            getAroundInvokeDescriptors();

        if (!knownLifecycleCallbackDescriptor(aroundInvokeDesc, aroundInvokeDescs)) {
            aroundInvokeDescs.add(aroundInvokeDesc);
        }
    }

    public void addAroundInvokeDescriptors(
        Set<LifecycleCallbackDescriptor> aroundInvokes) {
        for (LifecycleCallbackDescriptor ai : aroundInvokes) {
            addAroundInvokeDescriptor(ai);
        }
    }

    public boolean hasAroundInvokeDescriptor() {
        return (getAroundInvokeDescriptors().size() > 0);
    }

    public void addAroundTimeoutDescriptor(LifecycleCallbackDescriptor aroundTimeoutDesc) {
        Set<LifecycleCallbackDescriptor> aroundTimeoutDescs =
            getAroundTimeoutDescriptors();

        if (!knownLifecycleCallbackDescriptor(aroundTimeoutDesc, aroundTimeoutDescs)) {
            aroundTimeoutDescs.add(aroundTimeoutDesc);
        }
    }

    private boolean knownLifecycleCallbackDescriptor(
            LifecycleCallbackDescriptor desc, Set<LifecycleCallbackDescriptor> descs) {
        boolean found = false;

        for (LifecycleCallbackDescriptor ai : descs) {
            if ((desc.getLifecycleCallbackClass() != null) &&
                    desc.getLifecycleCallbackClass().equals(
                    ai.getLifecycleCallbackClass())) {
                found = true;
            }
        }

        return found;
    }

    public void addAroundTimeoutDescriptors(
        Set<LifecycleCallbackDescriptor> aroundTimeouts) {
        for (LifecycleCallbackDescriptor ai : aroundTimeouts) {
            addAroundTimeoutDescriptor(ai);
        }
    }

    public boolean hasAroundTimeoutDescriptor() {
        return (getAroundTimeoutDescriptors().size() > 0);
    }

    /**
     * Some clients need the Callback methods for this inheritance
     * hierarchy in the spec-defined "least derived --> most derived" order.
     */
    public List<LifecycleCallbackDescriptor> getOrderedCallbackDescriptors
        (CallbackType type, ClassLoader loader) throws Exception {

        return orderDescriptors(getCallbackDescriptors(type), loader);
    }

    public void addAroundConstructDescriptor(LifecycleCallbackDescriptor lcDesc) {
        addCallbackDescriptor(CallbackType.AROUND_CONSTRUCT, lcDesc);
    }

    public void addPostActivateDescriptor(LifecycleCallbackDescriptor lcDesc) {
        addCallbackDescriptor(CallbackType.POST_ACTIVATE, lcDesc);
    }

    public void addPrePassivateDescriptor(LifecycleCallbackDescriptor lcDesc) {
        addCallbackDescriptor(CallbackType.PRE_PASSIVATE, lcDesc);
    }

    /**
     * Order a set of lifecycle method descriptors for a particular
     * inheritance hierarchy with highest precedence assigned to the
     * least derived class.
     */
    private List<LifecycleCallbackDescriptor> orderDescriptors
        (Set<LifecycleCallbackDescriptor> lcds, ClassLoader loader)
        throws Exception
    {

        LinkedList<LifecycleCallbackDescriptor> orderedDescs =
            new LinkedList<LifecycleCallbackDescriptor>();

        Map<String, LifecycleCallbackDescriptor> map =
            new HashMap<String, LifecycleCallbackDescriptor>();

        for(LifecycleCallbackDescriptor next : lcds) {
            map.put(next.getLifecycleCallbackClass(), next);
        }

        Class<?> nextClass = interceptorClass != null? interceptorClass : loader.loadClass(getInterceptorClassName());

        while((nextClass != Object.class) && (nextClass != null)) {
            String nextClassName = nextClass.getName();
            if( map.containsKey(nextClassName) ) {
                LifecycleCallbackDescriptor lcd = map.get(nextClassName);
                orderedDescs.addFirst(lcd);
            }

            nextClass = nextClass.getSuperclass();
        }


        return orderedDescs;

    }

    @Override
    public String toString() {
        return "InterceptorDescriptor class = " + getInterceptorClassName();
    }
}
