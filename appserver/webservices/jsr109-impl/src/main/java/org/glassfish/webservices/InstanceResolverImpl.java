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

/*
 * InstanceResolverImpl.java
 *
 * Created on May 29, 2007, 10:41 AM
 *
 * @author Mike Grogan
 */

package org.glassfish.webservices;
import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.ResourceInjector;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WSWebServiceContext;

import jakarta.xml.ws.Provider;
import jakarta.xml.ws.WebServiceException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public final class InstanceResolverImpl<T> extends InstanceResolver<T> {

    //delegate to this InstanceResolver
    private  InstanceResolver<T> resolver;
    private  T instance;
    private final Class<T> classtobeResolved;

    private WSWebServiceContext wsc;
    private WSEndpoint endpoint;

    private final InjectionManager injManager = WebServiceContractImpl.getInstance().getInjectionManager();

    public  InstanceResolverImpl(@NotNull Class<T> clasz) {
        this.classtobeResolved = clasz;
    }

    @Override
    public @NotNull T resolve(Packet request) {
        //See iss 9721
        //Injection and instantiation is now done lazily
        if (resolver == null) {
            try {
                //Bug18998101. inject() call below also calls @PostConstruct method.
                instance = injManager.createManagedObject(classtobeResolved, false);
            } catch (InjectionException e) {
                throw new WebServiceException(e);
            }
            resolver = InstanceResolver.createSingleton(instance);
            getResourceInjector(endpoint).inject(wsc, instance);
        }
        return resolver.resolve(request);
    }

    @Override
    public void start(WSWebServiceContext wsc, WSEndpoint endpoint) {
        this.wsc = wsc;
        this.endpoint = endpoint;
    }

    @Override
    public void dispose() {
        try {
            if(instance != null) {//instance can be null as it is created laziily
                injManager.destroyManagedObject(instance);
            }
        } catch (InjectionException e) {
            throw new WebServiceException(e);
        }
    }

    private ResourceInjector getResourceInjector(WSEndpoint endpoint) {
        ResourceInjector ri = endpoint.getContainer().getSPI(ResourceInjector.class);
        if (ri == null) {
            ri = ResourceInjector.STANDALONE;
        }
        return ri;
    }

     /**
     * Wraps this {@link InstanceResolver} into an {@link Invoker}.
     */
    public  //TODO - make this package private.  Cannot do it until this method is removed from base
        //       class com.sun.xml.ws.api.server.InstanceResolver
     @Override
     @NotNull Invoker createInvoker() {
        return new Invoker() {
            @Override
            public void start(@NotNull WSWebServiceContext wsc, @NotNull WSEndpoint endpoint) {
                InstanceResolverImpl.this.start(wsc,endpoint);
            }

            @Override
            public void dispose() {
                InstanceResolverImpl.this.dispose();
            }

            @Override
            public Object invoke(Packet p, Method m, Object... args) throws InvocationTargetException, IllegalAccessException {
                return m.invoke( resolve(p), args );
            }

            @Override
            public <T> T invokeProvider(@NotNull Packet p, T arg) {
                return ((Provider<T>)resolve(p)).invoke(arg);
            }

            @Override
            public String toString() {
                return "Default Invoker over "+InstanceResolverImpl.this.toString();
            }
        };
    }
}
