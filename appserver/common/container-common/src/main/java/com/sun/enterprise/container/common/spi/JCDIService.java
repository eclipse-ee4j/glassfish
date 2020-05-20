/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.container.common.spi;


import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import org.jvnet.hk2.annotations.Contract;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionTarget;
import javax.naming.NamingException;
import jakarta.servlet.ServletContext;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 */
@Contract
public interface JCDIService {

    public boolean isCurrentModuleJCDIEnabled();

    public boolean isJCDIEnabled(BundleDescriptor bundle);

    public boolean isCDIScoped(Class<?> clazz);

    public void setELResolver(ServletContext servletContext) throws NamingException;

    public <T> JCDIInjectionContext<T> createManagedObject(Class<T> managedClass, BundleDescriptor bundle);
    public <T> JCDIInjectionContext<T> createManagedObject(Class<T> managedClass, BundleDescriptor bundle,
                                                    boolean invokePostConstruct);

    public void injectManagedObject(Object managedObject, BundleDescriptor bundle);

    /**
     * Create an inteceptor instance for an ejb.
     * @param interceptorClass The interceptor class.
     * @param ejbDesc The ejb descriptor of the ejb for which the interceptor is created.
     * @param ejbContext The ejb context.
     * @param ejbInterceptors All of the ejb interceptors for the ejb.
     *
     * @return The interceptor instance.
     */
    <T> T createInterceptorInstance( Class<T> interceptorClass,
                                     EjbDescriptor ejbDesc,
                                     JCDIService.JCDIInjectionContext ejbContext,
                                     Set<EjbInterceptor> ejbInterceptors );

    /**
     * Create an ejb via CDI.
     *
     * @param ejbDesc The ejb descriptor
     * @param ejbInfo Information about the ejb.  Entries are the com.sun.ejb.containers.BaseContainer
     *                and com.sun.ejb.containers.EJBContextImpl
     * @return The created EJB.
     */
    <T> JCDIInjectionContext<T> createJCDIInjectionContext(EjbDescriptor ejbDesc, Map<Class, Object> ejbInfo);

    //todo: This should be removed as it is not used.
    public <T> JCDIInjectionContext<T> createJCDIInjectionContext(EjbDescriptor ejbDesc, T instance, Map<Class, Object> ejbInfo);

    public <T> void injectEJBInstance(JCDIInjectionContext<T> injectionCtx);

    /**
     * Create an empty JCDIInjectionContext.
     * @return The empty JCDIInjectionContext.
     */
    JCDIInjectionContext createEmptyJCDIInjectionContext();

    public interface JCDIInjectionContext<T> {
        /**
         * @return The instance associated with this context.
         */
        T getInstance();

        /**
         * Set the instance on this context
         * @param instance The instance to set.
         */
        void setInstance( T instance );

        void cleanup(boolean callPreDestroy);

        /**
         * @return The injection target.
         */
        InjectionTarget<T> getInjectionTarget();

        /**
         * Set the injection target.
         * @param injectionTarget The injection target to set.
         */
        void setInjectionTarget( InjectionTarget<T> injectionTarget );

        /**
         * @return The creational context.
         */
        CreationalContext<T> getCreationalContext();

        /**
         * Set the creational context.
         * @param creationalContext The creational context.
         */
        void setCreationalContext( CreationalContext<T> creationalContext );

        /**
         * Add a dependent context to this context so that the dependent
         * context can be cleaned up when this one is.
         *
         * @param dependentContext The dependenct context.
         */
        void addDependentContext( JCDIInjectionContext dependentContext );

        /**
         * @return The dependent contexts.
         */
        Collection<JCDIInjectionContext> getDependentContexts();

        /**
         * Create the EJB and perform constructor injection, if applicable.  This should only happen when the
         * last interceptor method in the AroundConstruct interceptor chain invokes the InvocationContext.proceed
         * method. If the InvocationContext.proceed method is not invoked by an interceptor method,
         * the target instance will not be created.
         */
        T createEjbAfterAroundConstruct();
    }

}
