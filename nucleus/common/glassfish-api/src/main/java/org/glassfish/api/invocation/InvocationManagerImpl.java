/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.invocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.glassfish.api.invocation.ComponentInvocation.ComponentInvocationType;
import org.glassfish.hk2.api.IterableProvider;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Service
@Singleton
public class InvocationManagerImpl implements InvocationManager {

    // This TLS variable stores an ArrayList.
    // The ArrayList contains ComponentInvocation objects which represent
    // the stack of invocations on this thread. Accesses to the ArrayList
    // dont need to be synchronized because each thread has its own ArrayList.
    private InheritableThreadLocal<InvocationArray<ComponentInvocation>> frames;

    private final ThreadLocal<Stack<ApplicationEnvironment>> applicationEnvironments = new ThreadLocal<>() {
        @Override
        protected Stack<ApplicationEnvironment> initialValue() {
            return new Stack<>();
        }

    };

    private Map<ComponentInvocationType, List<RegisteredComponentInvocationHandler>> regCompInvHandlerMap = new HashMap<>();

    private final ComponentInvocationHandler[] invHandlers;

    public InvocationManagerImpl() {
        this(null);
    }

    @Inject
    private InvocationManagerImpl(@Optional IterableProvider<ComponentInvocationHandler> handlers) {
        if (handlers == null) {
            invHandlers = null;
        } else {
            LinkedList<ComponentInvocationHandler> localHandlers = new LinkedList<>();
            for (ComponentInvocationHandler handler : handlers) {
                localHandlers.add(handler);
            }

            if (localHandlers.size() > 0) {
                invHandlers = localHandlers.toArray(new ComponentInvocationHandler[localHandlers.size()]);
            } else {
                invHandlers = null;
            }
        }

        frames = new InheritableThreadLocal<>() {
            @Override
            protected InvocationArray initialValue() {
                return new InvocationArray();
            }

            // if this is a thread created by user in servlet's service method
            // create a new ComponentInvocation with transaction
            // set to null and instance set to null
            // so that the resource won't be enlisted or registered
            @Override
            protected InvocationArray<ComponentInvocation> childValue(InvocationArray<ComponentInvocation> parentValue) {
                // always creates a new ArrayList
                InvocationArray<ComponentInvocation> result = new InvocationArray<>();
                InvocationArray<ComponentInvocation> v = parentValue;
                if (v.size() > 0 && v.outsideStartup()) {
                    // get current invocation
                    ComponentInvocation parentInv = v.get(v.size() - 1);
                    /*
                     * TODO: The following is ugly. The logic of what needs to be in the new ComponentInvocation should be with the
                     * respective container
                     */
                    if (parentInv.getInvocationType() == ComponentInvocationType.SERVLET_INVOCATION) {

                        ComponentInvocation inv = new ComponentInvocation();
                        inv.componentId = parentInv.getComponentId();
                        inv.setComponentInvocationType(parentInv.getInvocationType());
                        inv.instance = null;
                        inv.container = parentInv.getContainerContext();
                        inv.transaction = null;
                        result.add(inv);
                    } else if (parentInv.getInvocationType() != ComponentInvocationType.EJB_INVOCATION) {
                        // Push a copy of invocation onto the new result
                        // ArrayList
                        ComponentInvocation cpy = new ComponentInvocation();
                        cpy.componentId = parentInv.getComponentId();
                        cpy.setComponentInvocationType(parentInv.getInvocationType());
                        cpy.instance = parentInv.getInstance();
                        cpy.container = parentInv.getContainerContext();
                        cpy.transaction = parentInv.getTransaction();
                        result.add(cpy);
                    }

                }
                return result;
            }
        };
    }

    @Override
    public <T extends ComponentInvocation> void preInvoke(T inv) throws InvocationException {

        InvocationArray<ComponentInvocation> v = frames.get();
        if (inv.getInvocationType() == ComponentInvocationType.SERVICE_STARTUP) {
            v.setInvocationAttribute(ComponentInvocationType.SERVICE_STARTUP);
            return;
        }

        int beforeSize = v.size();
        ComponentInvocation prevInv = beforeSize > 0 ? v.get(beforeSize - 1) : null;

        // if ejb call EJBSecurityManager, for servlet call RealmAdapter
        ComponentInvocationType invType = inv.getInvocationType();

        if (invHandlers != null) {
            for (ComponentInvocationHandler handler : invHandlers) {
                handler.beforePreInvoke(invType, prevInv, inv);
            }
        }

        List<RegisteredComponentInvocationHandler> setCIH = regCompInvHandlerMap.get(invType);
        if (setCIH != null) {
            for (int i = 0; i < setCIH.size(); i++) {
                setCIH.get(i).getComponentInvocationHandler().beforePreInvoke(invType, prevInv, inv);
            }
        }

        // push this invocation on the stack
        v.add(inv);

        if (invHandlers != null) {
            for (ComponentInvocationHandler handler : invHandlers) {
                handler.afterPreInvoke(invType, prevInv, inv);
            }
        }

        if (setCIH != null) {
            for (int i = 0; i < setCIH.size(); i++) {
                setCIH.get(i).getComponentInvocationHandler().afterPreInvoke(invType, prevInv, inv);
            }
        }

    }

    @Override
    public <T extends ComponentInvocation> void postInvoke(T inv) throws InvocationException {

        // Get this thread's ArrayList
        InvocationArray<ComponentInvocation> v = frames.get();
        if (inv.getInvocationType() == ComponentInvocationType.SERVICE_STARTUP) {
            v.setInvocationAttribute(ComponentInvocationType.UN_INITIALIZED);
            return;
        }

        int beforeSize = v.size();
        if (beforeSize == 0) {
            throw new InvocationException();
        }

        ComponentInvocation prevInv = beforeSize > 1 ? v.get(beforeSize - 2) : null;
        ComponentInvocation curInv = v.get(beforeSize - 1);

        try {
            ComponentInvocationType invType = inv.getInvocationType();

            if (invHandlers != null) {
                for (ComponentInvocationHandler handler : invHandlers) {
                    handler.beforePostInvoke(invType, prevInv, curInv);
                }
            }

            List<RegisteredComponentInvocationHandler> setCIH = regCompInvHandlerMap.get(invType);
            if (setCIH != null) {
                for (int i = 0; i < setCIH.size(); i++) {
                    setCIH.get(i).getComponentInvocationHandler().beforePostInvoke(invType, prevInv, curInv);
                }
            }

        } finally {
            // pop the stack
            v.remove(beforeSize - 1);

            if (invHandlers != null) {
                for (ComponentInvocationHandler handler : invHandlers) {
                    handler.afterPostInvoke(inv.getInvocationType(), prevInv, inv);
                }
            }

            ComponentInvocationType invType = inv.getInvocationType();

            List<RegisteredComponentInvocationHandler> setCIH = regCompInvHandlerMap.get(invType);
            if (setCIH != null) {
                for (int i = 0; i < setCIH.size(); i++) {
                    setCIH.get(i).getComponentInvocationHandler().afterPostInvoke(invType, prevInv, curInv);
                }
            }

        }

    }

    /**
     * return true iff no invocations on the stack for this thread
     */
    @Override
    public boolean isInvocationStackEmpty() {
        ArrayList v = frames.get();
        return v == null || v.size() == 0;
    }

    /**
     * return the Invocation object of the component being called
     */
    @Override
    public <T extends ComponentInvocation> T getCurrentInvocation() {
        ArrayList v = frames.get();
        int size = v.size();
        if (size == 0) {
            return null;
        }
        return (T) v.get(size - 1);
    }

    /**
     * return the Inovcation object of the caller return null if none exist (e.g. caller is from another VM)
     */
    @Override
    public <T extends ComponentInvocation> T getPreviousInvocation() throws InvocationException {

        ArrayList v = frames.get();
        int i = v.size();
        if (i < 2) {
            return null;
        }
        return (T) v.get(i - 2);
    }

    @Override
    public List getAllInvocations() {
        return frames.get();
    }

    static class InvocationArray<T extends ComponentInvocation> extends java.util.ArrayList<T> {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private ComponentInvocationType invocationAttribute;

        public void setInvocationAttribute(ComponentInvocationType attribute) {
            this.invocationAttribute = attribute;
        }

        public ComponentInvocationType getInvocationAttribute() {
            return invocationAttribute;
        }

        public boolean outsideStartup() {
            return getInvocationAttribute() != ComponentInvocationType.SERVICE_STARTUP;
        }
    }

    @Override
    public void registerComponentInvocationHandler(ComponentInvocationType type, RegisteredComponentInvocationHandler handler) {
        List<RegisteredComponentInvocationHandler> setRegCompInvHandlers = regCompInvHandlerMap.get(type);
        if (setRegCompInvHandlers == null) {
            setRegCompInvHandlers = new ArrayList<>();
            regCompInvHandlerMap.put(type, setRegCompInvHandlers);
        }
        if (setRegCompInvHandlers.size() == 0) {
            setRegCompInvHandlers.add(handler);
        }
    }

    @Override
    public void pushAppEnvironment(ApplicationEnvironment env) {
        Stack<ApplicationEnvironment> stack = applicationEnvironments.get();

        stack.push(env);
    }

    @Override
    public ApplicationEnvironment peekAppEnvironment() {
        Stack<ApplicationEnvironment> stack = applicationEnvironments.get();

        if (stack.isEmpty()) {
            return null;
        }

        return stack.peek();
    }

    @Override
    public void popAppEnvironment() {
        Stack<ApplicationEnvironment> stack = applicationEnvironments.get();

        if (!stack.isEmpty()) {
            stack.pop();
        }
    }
}
