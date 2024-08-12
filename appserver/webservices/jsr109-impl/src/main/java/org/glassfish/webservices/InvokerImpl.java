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

package org.glassfish.webservices;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.AsyncProvider;
import com.sun.xml.ws.api.server.AsyncProviderCallback;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WSWebServiceContext;

import jakarta.xml.ws.Provider;
import jakarta.xml.ws.WebServiceContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Implements JAXWS's Invoker interface to call the endpoint method
 */
public class InvokerImpl extends Invoker {
    protected final Invoker core;
    protected final Object invokeObject;
    protected final WebServiceContextImpl injectedWSCtxt;

    public InvokerImpl(Invoker core, Object inv, WebServiceContextImpl wsc) {
        this.core = core;
        this.injectedWSCtxt = wsc;
        this.invokeObject = inv;
    }

    private static final boolean jaxwsDirect=Boolean.getBoolean("com.sun.enterprise.webservice.jaxwsDirect");

    public void start(WSWebServiceContext wsc, WSEndpoint endpoint) {
        if(this.injectedWSCtxt != null) {
            injectedWSCtxt.setContextDelegate(wsc);
        }
        core.start(injectedWSCtxt, endpoint);
    }

    public void dispose() {
        core.dispose();
    }

    public Object invoke(Packet p, Method m, Object... args) throws InvocationTargetException, IllegalAccessException {
        if(jaxwsDirect)
            return core.invoke(p,m,args);
        Object ret = null;
        if(this.invokeObject != null) {
            ret = m.invoke(this.invokeObject, args);
        }
        return ret;
    }

    private static final Method invokeMethod;

    static {
        try {
            invokeMethod = Provider.class.getMethod("invoke",Object.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    public <T> T invokeProvider(Packet p, T arg) throws IllegalAccessException, InvocationTargetException {
        if(jaxwsDirect)
            return core.invokeProvider(p, arg);
        Object ret = null;
        if(this.invokeObject != null) {
            ret = invokeMethod.invoke(this.invokeObject, arg);
        }
        return (T)ret;

    }

    private static final Method asyncInvokeMethod;

    static {
        try {
            asyncInvokeMethod = AsyncProvider.class.getMethod("invoke",Object.class, AsyncProviderCallback.class, WebServiceContext.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    public <T> void invokeAsyncProvider(Packet p, T arg, AsyncProviderCallback cbak, WebServiceContext ctxt) throws IllegalAccessException, InvocationTargetException {
        if(jaxwsDirect)
            core.invokeAsyncProvider(p, arg, cbak, ctxt);
        if(this.invokeObject != null) {
            asyncInvokeMethod.invoke(this.invokeObject, arg);
        }

    }
}
