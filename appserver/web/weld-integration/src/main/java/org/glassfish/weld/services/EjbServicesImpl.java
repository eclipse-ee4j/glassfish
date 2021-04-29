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

package org.glassfish.weld.services;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.cdi.CDILoggerInfo;
import org.glassfish.ejb.api.EjbContainerServices;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.weld.ejb.EjbDescriptorImpl;
import org.glassfish.weld.ejb.SessionObjectReferenceImpl;
import org.jboss.weld.ejb.api.SessionObjectReference;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.ejb.spi.InterceptorBindings;

import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.PostActivate;
import jakarta.ejb.PrePassivate;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;

/**
 * An implementation of th <code>EJBServices</code> Weld SPI. The Weld implementation uses this SPI to resolve EJB and
 * register CDI Interceptors for EJBs.
 */
public class EjbServicesImpl implements EjbServices {
    private ServiceLocator services;
    private Logger logger = Logger.getLogger(EjbServicesImpl.class.getName());

    public EjbServicesImpl(ServiceLocator h) {
        services = h;
    }

    /**
     * Request a reference to an EJB session object from the container. If the EJB being resolved is a stateful session
     * bean, the container should ensure the session bean is created before this method returns.
     *
     * @param ejbDescriptor the ejb to resolve
     * @return a reference to the session object
     */
    @Override
    public SessionObjectReference resolveEjb(EjbDescriptor<?> ejbDescriptor) {

        SessionObjectReference sessionObj = null;

        // All we need to do is create a reference based on one of the beans'
        // client views, so just choose one and get its corresponding portable
        // JNDI name.
        String globalJndiName = getDefaultGlobalJndiName(ejbDescriptor);
        if (globalJndiName == null) {
            throw new IllegalArgumentException(
                    "Not enough type information to resolve ejb for " + " ejb name " + ejbDescriptor.getBeanClass());
        }
        try {

            InitialContext ic = new InitialContext();

            Object ejbRef = ic.lookup(globalJndiName);

            EjbContainerServices containerServices = services.getService(EjbContainerServices.class);

            sessionObj = new SessionObjectReferenceImpl(containerServices, ejbRef);

        } catch (NamingException ne) {
            throw new IllegalStateException("Error resolving session object reference for ejb name " + ejbDescriptor.getBeanClass()
                    + " and jndi name " + globalJndiName, ne);
        }

        return sessionObj;

    }

    private String getDefaultGlobalJndiName(EjbDescriptor<?> ejbDesc) {

        EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ((EjbDescriptorImpl<?>) ejbDesc).getEjbDescriptor();

        String clientView = null;

        if (sessionDesc.isLocalBean()) {
            clientView = sessionDesc.getEjbClassName();
        } else if (sessionDesc.getLocalBusinessClassNames().size() >= 1) {
            clientView = sessionDesc.getLocalBusinessClassNames().iterator().next();
        } else if (sessionDesc.getRemoteBusinessClassNames().size() >= 1) {
            clientView = sessionDesc.getRemoteBusinessClassNames().iterator().next();
        }

        return clientView != null ? sessionDesc.getPortableJndiName(clientView) : null;

    }

    @Override
    public void registerInterceptors(EjbDescriptor<?> ejbDesc, InterceptorBindings interceptorBindings) {
        //no need to continue with this work around as jboss has provided the resolution for https://issues.jboss.org/browse/WELD-2184

        // Work around bug that ejbDesc might be internal 299 descriptor.
        /* if( ejbDesc instanceof org.jboss.weld.module.ejb.InternalEjbDescriptor ) {
            ejbDesc = ((org.jboss.weld.module.ejb.InternalEjbDescriptor<?>)ejbDesc).delegate();
        }*/

        com.sun.enterprise.deployment.EjbDescriptor glassfishEjbDesc = ((EjbDescriptorImpl<?>) ejbDesc).getEjbDescriptor();

        // Convert to EjbInterceptor
        // First create master list of EjbInterceptor descriptors
        for (Interceptor<?> next : interceptorBindings.getAllInterceptors()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, CDILoggerInfo.TRYING_TO_REGISTER_INTERCEPTOR, new Object[] { next });
            }
            // Add interceptor to list all interceptors in ejb descriptor
            if (!glassfishEjbDesc.hasInterceptorClass(next.getBeanClass().getName())) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, CDILoggerInfo.ADDING_INTERCEPTOR_FOR_EJB,
                            new Object[] { next.getBeanClass().getName(), glassfishEjbDesc.getEjbClassName() });
                }
                EjbInterceptor ejbInt = makeEjbInterceptor(next, glassfishEjbDesc.getEjbBundleDescriptor());
                glassfishEjbDesc.addInterceptorClass(ejbInt);
            }
        }

        // Create ordered list of EjbInterceptor for each lifecycle interception type and append to
        // EjbDescriptor.   299 interceptors are always added after any interceptors defined via
        // EJB-defined metadata, so the ordering will be correct since all the ejb interceptors
        // have already been processed.
        List<EjbInterceptor> aroundConstructChain = makeInterceptorChain(InterceptionType.AROUND_CONSTRUCT,
                interceptorBindings.getLifecycleInterceptors(InterceptionType.AROUND_CONSTRUCT), glassfishEjbDesc);
        glassfishEjbDesc.appendToInterceptorChain(aroundConstructChain);

        List<EjbInterceptor> postConstructChain = makeInterceptorChain(InterceptionType.POST_CONSTRUCT,
                interceptorBindings.getLifecycleInterceptors(InterceptionType.POST_CONSTRUCT), glassfishEjbDesc);
        glassfishEjbDesc.appendToInterceptorChain(postConstructChain);

        List<EjbInterceptor> preDestroyChain = makeInterceptorChain(InterceptionType.PRE_DESTROY,
                interceptorBindings.getLifecycleInterceptors(InterceptionType.PRE_DESTROY), glassfishEjbDesc);
        glassfishEjbDesc.appendToInterceptorChain(preDestroyChain);

        List<EjbInterceptor> prePassivateChain = makeInterceptorChain(InterceptionType.PRE_PASSIVATE,
                interceptorBindings.getLifecycleInterceptors(InterceptionType.PRE_PASSIVATE), glassfishEjbDesc);
        glassfishEjbDesc.appendToInterceptorChain(prePassivateChain);

        List<EjbInterceptor> postActivateChain = makeInterceptorChain(InterceptionType.POST_ACTIVATE,
                interceptorBindings.getLifecycleInterceptors(InterceptionType.POST_ACTIVATE), glassfishEjbDesc);
        glassfishEjbDesc.appendToInterceptorChain(postActivateChain);

        // 299-provided list is organized as per-method.  Append each method chain to EjbDescriptor.

        Class<?> ejbBeanClass = null;

        try {
            ClassLoader cl = glassfishEjbDesc.getEjbBundleDescriptor().getClassLoader();
            ejbBeanClass = cl.loadClass(glassfishEjbDesc.getEjbClassName());
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalStateException("Cannot load bean class " + glassfishEjbDesc.getEjbClassName(), cnfe);
        }

        Class<?> ejbBeanSuperClass = ejbBeanClass;
        while (!ejbBeanSuperClass.equals(java.lang.Object.class)) {
            for (Method m : ejbBeanSuperClass.getDeclaredMethods()) {
                if (!methodOverridden(ejbBeanClass, m)) {
                    List<EjbInterceptor> aroundInvokeChain = makeInterceptorChain(InterceptionType.AROUND_INVOKE,
                            interceptorBindings.getMethodInterceptors(InterceptionType.AROUND_INVOKE, m), glassfishEjbDesc);
                    glassfishEjbDesc.addMethodLevelChain(aroundInvokeChain, m, true);

                    List<EjbInterceptor> aroundTimeoutChain = makeInterceptorChain(InterceptionType.AROUND_TIMEOUT,
                            interceptorBindings.getMethodInterceptors(InterceptionType.AROUND_TIMEOUT, m), glassfishEjbDesc);
                    glassfishEjbDesc.addMethodLevelChain(aroundTimeoutChain, m, false);
                }
            }
            ejbBeanSuperClass = ejbBeanSuperClass.getSuperclass();
        }

    }

    // Section 4.2. Inheritance of member-level metadata of CDI spec states:
    //   If X declares a non-static method x() annotated with an interceptor binding type Z then Y inherits
    //   the binding if and only if neither Y nor any intermediate class that is a subclass of X and a
    //   superclass of Y overrides the method x(). (This behavior is defined by the Common Annotations
    //   for the Java Platform specification.)
    //
    // So look at a method on a class.  If the method is overridden (not if it over rides) then we skip it
    // because it will have already been processed to see if it should be intercepted.
    private boolean methodOverridden(Class beanClass, Method methodOfCurrentClass) {
        String methodName = methodOfCurrentClass.getName();
        Class[] methodParams = methodOfCurrentClass.getParameterTypes();
        Class declaringClass = methodOfCurrentClass.getDeclaringClass();

        try {
            Method method = beanClass.getMethod(methodName, methodParams);
            return !method.getDeclaringClass().equals(declaringClass);
        } catch (NoSuchMethodException ignored) {
        }
        return false;
    }

    private List<EjbInterceptor> makeInterceptorChain(InterceptionType interceptionType, List<Interceptor<?>> lifecycleList,
            com.sun.enterprise.deployment.EjbDescriptor ejbDesc) {

        List<EjbInterceptor> ejbInterceptorList = new LinkedList<>();

        if (lifecycleList == null) {
            return ejbInterceptorList;
        }

        for (Interceptor<?> next : lifecycleList) {
            EjbInterceptor ejbInt = makeEjbInterceptor(next, ejbDesc.getEjbBundleDescriptor());
            Class interceptorClass = next.getBeanClass();
            while (interceptorClass != null && !interceptorClass.equals(Object.class)) {
                String methodName = getInterceptorMethod(interceptorClass, getInterceptorAnnotationType(interceptionType));
                if (methodName != null) {
                    LifecycleCallbackDescriptor lifecycleDesc = new LifecycleCallbackDescriptor();

                    lifecycleDesc.setLifecycleCallbackClass(interceptorClass.getName());
                    lifecycleDesc.setLifecycleCallbackMethod(methodName);
                    switch (interceptionType) {
                    case POST_CONSTRUCT:
                        ejbInt.addPostConstructDescriptor(lifecycleDesc);
                        break;
                    case PRE_DESTROY:
                        ejbInt.addPreDestroyDescriptor(lifecycleDesc);
                        break;
                    case PRE_PASSIVATE:
                        ejbInt.addPrePassivateDescriptor(lifecycleDesc);
                        break;
                    case POST_ACTIVATE:
                        ejbInt.addPostActivateDescriptor(lifecycleDesc);
                        break;
                    case AROUND_INVOKE:
                        ejbInt.addAroundInvokeDescriptor(lifecycleDesc);
                        break;
                    case AROUND_TIMEOUT:
                        ejbInt.addAroundTimeoutDescriptor(lifecycleDesc);
                        break;
                    case AROUND_CONSTRUCT:
                        ejbInt.addAroundConstructDescriptor(lifecycleDesc);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid lifecycle interception type " + interceptionType);
                    }
                }

                interceptorClass = interceptorClass.getSuperclass();
            }

            ejbInterceptorList.add(ejbInt);
        }

        return ejbInterceptorList;
    }

    private Class<?> getInterceptorAnnotationType(InterceptionType interceptionType) {

        switch (interceptionType) {
        case AROUND_CONSTRUCT:
            return AroundConstruct.class;
        case POST_CONSTRUCT:
            return PostConstruct.class;
        case PRE_DESTROY:
            return PreDestroy.class;
        case PRE_PASSIVATE:
            return PrePassivate.class;
        case POST_ACTIVATE:
            return PostActivate.class;
        case AROUND_INVOKE:
            return AroundInvoke.class;
        case AROUND_TIMEOUT:
            return AroundTimeout.class;
        }

        throw new IllegalArgumentException("Invalid interception type " + interceptionType);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private String getInterceptorMethod(Class interceptorClass, Class annotation) {

        for (Method next : interceptorClass.getDeclaredMethods()) {
            if (next.getAnnotation(annotation) != null) {
                return next.getName();
            }
        }

        return null;
    }

    private EjbInterceptor makeEjbInterceptor(Interceptor<?> interceptor, EjbBundleDescriptor bundle) {

        EjbInterceptor ejbInt = new EjbInterceptor();
        ejbInt.setBundleDescriptor(bundle);
        ejbInt.setInterceptorClass(interceptor.getBeanClass());
        ejbInt.setCDIInterceptor(true);
        ejbInt.setInterceptor(interceptor);

        return ejbInt;
    }

    @Override
    public void cleanup() {
        //Nothing to do here.
    }

}
