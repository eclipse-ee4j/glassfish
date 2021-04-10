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

package org.glassfish.weld.ejb;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.module.ejb.SessionBeanInterceptor;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;

import jakarta.interceptor.InvocationContext;

/**
 */
public class EjbDescriptorImpl<T> implements org.jboss.weld.ejb.spi.EjbDescriptor<T> {

    private EjbDescriptor ejbDesc;

    public EjbDescriptorImpl(EjbDescriptor ejbDescriptor) {
        ejbDesc = ejbDescriptor;

        if (ejbDesc.getType().equals(EjbSessionDescriptor.TYPE) || ejbDesc.getType().equals(EjbMessageBeanDescriptor.TYPE)) {
            // Before handling application-level interceptors that are defined using 299 metadata,
            // add the CDI impl-specific system-level interceptor that needs to be registered for ALL
            // EJB components.  At runtime, this sets up the appropriate request context for invocations that
            // do not originate via the web tier.  This interceptor must be registered *before*
            // any application-level interceptors in the chain, so add it in the framework interceptor
            // category within the ejb descriptor.
            EjbInterceptor systemLevelCDIInterceptor = createSystemLevelCDIInterceptor();
            ejbDesc.addFrameworkInterceptor(systemLevelCDIInterceptor);
        }
    }

    @Override
    public String getEjbName() {
        return ejbDesc.getName();
    }

    public EjbDescriptor getEjbDescriptor() {
        return ejbDesc;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getBeanClass() {
        @SuppressWarnings("rawtypes")
        Class beanClassType = null;
        try {

            beanClassType = ejbDesc.getEjbBundleDescriptor().getClassLoader().loadClass(ejbDesc.getEjbClassName());

        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

        return beanClassType;
    }

    /**
     * Gets the local business interfaces of the EJB
     *
     * @return An iterator over the local business interfaces
     */
    @Override
    public Collection<BusinessInterfaceDescriptor<?>> getLocalBusinessInterfaces() {

        Set<BusinessInterfaceDescriptor<?>> localBusIntfs = new HashSet<>();

        if (ejbDesc.getType().equals(EjbSessionDescriptor.TYPE)) {

            EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ejbDesc;
            Set<String> localNames = sessionDesc.getLocalBusinessClassNames();

            // Add superinterfaces that are also marked as Local
            //JJS: 4/2/13 Removing superinterfaces to track down cdi tck failures.
            // http://java.net/jira/browse/GLASSFISH-19970
            //            Set<String> extraNames = new HashSet<String>();
            //            for(String local : localNames) {
            //                try {
            //                    Class<?> localClass = sessionDesc.getEjbBundleDescriptor().getClassLoader().loadClass(local);
            //                    addIfLocal(localClass.getInterfaces(), extraNames);
            //                } catch(ClassNotFoundException e) {
            //                    throw new IllegalStateException(e);
            //                }
            //            }
            //
            //            localNames.addAll(extraNames);

            // Include the no-interface Local view
            if (sessionDesc.isLocalBean()) {
                localNames.add(sessionDesc.getEjbClassName());
            }

            for (String local : localNames) {
                try {

                    Class<?> localClass = sessionDesc.getEjbBundleDescriptor().getClassLoader().loadClass(local);
                    @SuppressWarnings({ "rawtypes", "unchecked" })
                    BusinessInterfaceDescriptor<?> busIntfDesc = new BusinessInterfaceDescriptorImpl(localClass);
                    localBusIntfs.add(busIntfDesc);

                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        return localBusIntfs;
    }

    /**
     * Get the remove methods of the EJB
     *
     * @return An iterator over the remove methods
     */
    @Override
    public Collection<Method> getRemoveMethods() {
        Set<Method> removeMethods = new HashSet<>();

        if (ejbDesc.getType().equals(EjbSessionDescriptor.TYPE)) {
            EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ejbDesc;
            if (sessionDesc.isStateful() && sessionDesc.hasRemoveMethods()) {

                for (MethodDescriptor mDesc : sessionDesc.getRemoveMethodDescriptors()) {
                    Method m = mDesc.getMethod(ejbDesc);
                    if (m == null) {
                        throw new IllegalStateException("Can't resolve remove method " + mDesc + " For EJB " + sessionDesc.getName());
                    }
                    removeMethods.add(m);

                }

            }
        }

        return removeMethods;

    }

    /**
     * Indicates if the bean is stateless
     *
     * @return True if stateless, false otherwise
     */
    @Override
    public boolean isStateless() {
        return ejbDesc.getType().equals(EjbSessionDescriptor.TYPE) && ((EjbSessionDescriptor) ejbDesc).isStateless();
    }

    /**
     * Indicates if the bean is a EJB 3.1 Singleton
     *
     * @return True if the bean is a singleton, false otherwise
     */
    @Override
    public boolean isSingleton() {
        return ejbDesc.getType().equals(EjbSessionDescriptor.TYPE) && ((EjbSessionDescriptor) ejbDesc).isSingleton();
    }

    /**
     * Indicates if the EJB is stateful
     *
     * @return True if the bean is stateful, false otherwise
     */
    @Override
    public boolean isStateful() {
        return ejbDesc.getType().equals(EjbSessionDescriptor.TYPE) && ((EjbSessionDescriptor) ejbDesc).isStateful();
    }

    /**
     * Indicates if the EJB is and MDB
     *
     * @return True if the bean is an MDB, false otherwise
     */
    @Override
    public boolean isMessageDriven() {
        return ejbDesc.getType().equals(EjbMessageBeanDescriptor.TYPE);
    }

    @Override
    public boolean isPassivationCapable() {
        if (ejbDesc instanceof EjbSessionDescriptor) {
            EjbSessionDescriptor ejbSessionDescriptor = (EjbSessionDescriptor) ejbDesc;
            if (ejbSessionDescriptor.isStateful() && ejbSessionDescriptor.isPassivationCapable()) {
                return true;
            }
        }

        return false;
    }

    /*  enabled for debugging
    public int hashCode() {
        return getEjbName().hashCode();
    }

    public boolean equals(Object o) {

        boolean equal = false;

        if( (o != null) && (o instanceof EjbDescriptorImpl) ) {

            equal = getEjbName().equals( ((EjbDescriptorImpl)o).getEjbName() );

        }

        return equal;

    }
    */

    //JJS: 4/2/13 Removing superinterfaces to track down cdi tck failures.
    // http://java.net/jira/browse/GLASSFISH-19970
    //    private void addIfLocal(Class<?>[] interfaces, Set<String> names) {
    //        for(Class<?> next : interfaces) {
    //            if( next.getAnnotation(Local.class) != null ) {
    //                names.add(next.getName());
    //            }
    //            addIfLocal(next.getInterfaces(), names);
    //        }
    //    }
    //
    //    private void addIfRemote(Class<?>[] interfaces, Set<String> names) {
    //        for(Class<?> next : interfaces) {
    //            if( next.getAnnotation(Remote.class) != null ) {
    //                names.add(next.getName());
    //            }
    //            addIfRemote(next.getInterfaces(), names);
    //        }
    //    }

    private EjbInterceptor createSystemLevelCDIInterceptor() {

        EjbInterceptor interceptor = new EjbInterceptor();

        Class<SessionBeanInterceptor> interceptorClass = SessionBeanInterceptor.class;

        String interceptorName = interceptorClass.getName();

        interceptor.setInterceptorClass(interceptorClass);

        // @@@ SessionBeanInterceptor.class no longer has @AroundInvoke annotation so
        // we have to look for method explicitly.
        try {
            Method aroundInvokeMethod = interceptorClass.getMethod("aroundInvoke", InvocationContext.class);

            if (aroundInvokeMethod != null) {
                LifecycleCallbackDescriptor aroundInvokeDesc = new LifecycleCallbackDescriptor();
                aroundInvokeDesc.setLifecycleCallbackClass(interceptorName);
                aroundInvokeDesc.setLifecycleCallbackMethod(aroundInvokeMethod.getName());
                interceptor.addAroundInvokeDescriptor(aroundInvokeDesc);

                // TODO BUG -- Existing SessionBeanInterceptor does not define an @AroundTimeout method.
                // Until that's fixed, work around it by adding the method marked @AroundInvoke as an
                // @AroundTimeout.
                LifecycleCallbackDescriptor aroundTimeoutDesc = new LifecycleCallbackDescriptor();
                aroundTimeoutDesc.setLifecycleCallbackClass(interceptorName);
                aroundTimeoutDesc.setLifecycleCallbackMethod(aroundInvokeMethod.getName());
                interceptor.addAroundTimeoutDescriptor(aroundTimeoutDesc);

                // SessionBeanInterceptor does not define an @PostConstruct method so use the aroundInvoke method
                LifecycleCallbackDescriptor postConstructDesc = new LifecycleCallbackDescriptor();
                postConstructDesc.setLifecycleCallbackClass(interceptorName);
                postConstructDesc.setLifecycleCallbackMethod(aroundInvokeMethod.getName());
                interceptor.addPostConstructDescriptor(postConstructDesc);
            }
        } catch (NoSuchMethodException nsme) {
            throw new RuntimeException("Cannot find weld EJB interceptor aroundInvoke method");
        }

        return interceptor;

    }

    @Override
    public Collection<BusinessInterfaceDescriptor<?>> getRemoteBusinessInterfaces() {
        Set<BusinessInterfaceDescriptor<?>> remoteBusIntfs = new HashSet<>();

        if (ejbDesc.getType().equals(EjbSessionDescriptor.TYPE)) {

            EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ejbDesc;
            Set<String> remoteNames = sessionDesc.getRemoteBusinessClassNames();

            // Add superinterfaces that are also marked as Local
            //JJS: 4/2/13 Removing superinterfaces to track down cdi tck failures.
            // http://java.net/jira/browse/GLASSFISH-19970
            //            Set<String> extraNames = new HashSet<String>();
            //            for(String local : remoteNames) {
            //                try {
            //                    Class<?> remoteClass = sessionDesc.getEjbBundleDescriptor().getClassLoader().loadClass(local);
            //                    addIfRemote(remoteClass.getInterfaces(), extraNames);
            //                } catch(ClassNotFoundException e) {
            //                    throw new IllegalStateException(e);
            //                }
            //            }
            //
            //            remoteNames.addAll(extraNames);
            //JJS: 5/17/17 According to ejb spec 3.0 4.6.6 the remote name cannot be the local
            // Include the no-interface Local view
            //if( sessionDesc.isLocalBean() ) {
            //    remoteNames.add(sessionDesc.getEjbClassName());
            //}

            for (String remote : remoteNames) {
                try {

                    Class<?> remoteClass = sessionDesc.getEjbBundleDescriptor().getClassLoader().loadClass(remote);
                    @SuppressWarnings({ "rawtypes", "unchecked" })
                    BusinessInterfaceDescriptor<?> busIntfDesc = new BusinessInterfaceDescriptorImpl(remoteClass);
                    remoteBusIntfs.add(busIntfDesc);

                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        return remoteBusIntfs;
    }

    @Override
    public String toString() {
        return ejbDesc.getEjbClassName();
    }
}
