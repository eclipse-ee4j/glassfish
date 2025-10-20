/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.lang.System.Logger;
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

import static java.lang.System.Logger.Level.DEBUG;

@Service
@Singleton
public class InvocationManagerImpl implements InvocationManager {
    private static final Logger LOG = System.getLogger(InvocationManagerImpl.class.getName());

    // This TLS variable stores an ArrayList.
    // The ArrayList contains ComponentInvocation objects which represent
    // the stack of invocations on this thread. Accesses to the ArrayList
    // don't need to be synchronized because each thread has its own ArrayList.
    private InheritableThreadLocal<InvocationArray<ComponentInvocation>> frames;

    private final ThreadLocal<Stack<ApplicationEnvironment>> applicationEnvironments = new ThreadLocal<>() {
        @Override
        protected Stack<ApplicationEnvironment> initialValue() {
            return new Stack<>();
        }
    };

    private final Map<ComponentInvocationType, List<RegisteredComponentInvocationHandler>> regCompInvHandlerMap = new HashMap<>();

    private final ComponentInvocationHandler[] invHandlers;

    public InvocationManagerImpl() {
        this(null);
    }

    @Inject
    private InvocationManagerImpl(@Optional IterableProvider<ComponentInvocationHandler> handlers) {
        LOG.log(DEBUG, "InvocationManagerImpl(handlers={0})", handlers);
        if (handlers == null) {
            invHandlers = null;
        } else {
            LinkedList<ComponentInvocationHandler> localHandlers = new LinkedList<>();
            for (ComponentInvocationHandler handler : handlers) {
                localHandlers.add(handler);
            }
            if (localHandlers.isEmpty()) {
                invHandlers = null;
            } else {
                invHandlers = localHandlers.toArray(ComponentInvocationHandler[]::new);
            }
        }

        frames = new InheritableThreadLocal<>() {
            @Override
            protected InvocationArray<ComponentInvocation> initialValue() {
                return new InvocationArray<>();
            }

            // if this is a thread created by user in servlet's service method
            // create a new ComponentInvocation with transaction
            // set to null and instance set to null
            // so that the resource won't be enlisted or registered
            @Override
            protected InvocationArray<ComponentInvocation> childValue(
                final InvocationArray<ComponentInvocation> parentValue) {
                // always creates a new ArrayList
                InvocationArray<ComponentInvocation> result = new InvocationArray<>();
                if (!parentValue.isEmpty() && parentValue.outsideStartup()) {
                    // get current invocation
                    ComponentInvocation parentInv = parentValue.get(parentValue.size() - 1);
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
    public <T extends ComponentInvocation> void preInvoke(T invocation) throws InvocationException {
        LOG.log(DEBUG, "preInvoke(invocation={0})", invocation);
        InvocationArray<ComponentInvocation> invocations = frames.get();
        if (invocation.getInvocationType() == ComponentInvocationType.SERVICE_STARTUP) {
            invocations.setInvocationAttribute(ComponentInvocationType.SERVICE_STARTUP);
            return;
        }

        int beforeSize = invocations.size();
        ComponentInvocation prevInv = beforeSize == 0 ? null : invocations.get(beforeSize - 1);

        // if ejb call EJBSecurityManager, for servlet call RealmAdapter
        ComponentInvocationType invType = invocation.getInvocationType();

        if (invHandlers != null) {
            for (ComponentInvocationHandler handler : invHandlers) {
                handler.beforePreInvoke(invType, prevInv, invocation);
            }
        }

        List<RegisteredComponentInvocationHandler> setCIH = regCompInvHandlerMap.get(invType);
        if (setCIH != null) {
            for (RegisteredComponentInvocationHandler element : setCIH) {
                element.getComponentInvocationHandler().beforePreInvoke(invType, prevInv, invocation);
            }
        }

        // push this invocation on the stack
        invocations.add(invocation);

        if (invHandlers != null) {
            for (ComponentInvocationHandler handler : invHandlers) {
                handler.afterPreInvoke(invType, prevInv, invocation);
            }
        }

        if (setCIH != null) {
            for (RegisteredComponentInvocationHandler element : setCIH) {
                element.getComponentInvocationHandler().afterPreInvoke(invType, prevInv, invocation);
            }
        }

    }

    @Override
    public <T extends ComponentInvocation> void postInvoke(T invocation) throws InvocationException {
        LOG.log(DEBUG, "postInvoke(invocation={0})", invocation);
        // Get this thread's ArrayList
        InvocationArray<ComponentInvocation> invocations = frames.get();
        if (invocation.getInvocationType() == ComponentInvocationType.SERVICE_STARTUP) {
            invocations.setInvocationAttribute(ComponentInvocationType.UN_INITIALIZED);
            return;
        }

        int beforeSize = invocations.size();
        if (beforeSize == 0) {
            throw new InvocationException("BeforeSize is null.");
        }

        ComponentInvocation prevInv = beforeSize > 1 ? invocations.get(beforeSize - 2) : null;
        ComponentInvocation curInv = invocations.get(beforeSize - 1);
        try {
            ComponentInvocationType invType = invocation.getInvocationType();
            if (invHandlers != null) {
                for (ComponentInvocationHandler handler : invHandlers) {
                    handler.beforePostInvoke(invType, prevInv, curInv);
                }
            }
            List<RegisteredComponentInvocationHandler> setCIH = regCompInvHandlerMap.get(invType);
            if (setCIH != null) {
                for (RegisteredComponentInvocationHandler element : setCIH) {
                    element.getComponentInvocationHandler().beforePostInvoke(invType, prevInv, curInv);
                }
            }
        } finally {
            // pop the stack
            invocations.remove(beforeSize - 1);

            if (invHandlers != null) {
                for (ComponentInvocationHandler handler : invHandlers) {
                    handler.afterPostInvoke(invocation.getInvocationType(), prevInv, invocation);
                }
            }

            ComponentInvocationType invType = invocation.getInvocationType();
            List<RegisteredComponentInvocationHandler> handlers = regCompInvHandlerMap.get(invType);
            if (handlers != null) {
                for (RegisteredComponentInvocationHandler handler : handlers) {
                    handler.getComponentInvocationHandler().afterPostInvoke(invType, prevInv, curInv);
                }
            }
        }
    }

    /**
     * return true iff no invocations on the stack for this thread
     */
    @Override
    public boolean isInvocationStackEmpty() {
        InvocationArray<ComponentInvocation> v = frames.get();
        return v == null || v.isEmpty();
    }

    /**
     * return the Invocation object of the component being called
     */
    @Override
    public <T extends ComponentInvocation> T getCurrentInvocation() {
        InvocationArray<ComponentInvocation> v = frames.get();
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
        InvocationArray<ComponentInvocation> v = frames.get();
        int i = v.size();
        if (i < 2) {
            return null;
        }
        return (T) v.get(i - 2);
    }

    @Override
    public List<ComponentInvocation> getAllInvocations() {
        return frames.get();
    }

    static class InvocationArray<T extends ComponentInvocation> extends java.util.ArrayList<T> {
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
        if (setRegCompInvHandlers.isEmpty()) {
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
