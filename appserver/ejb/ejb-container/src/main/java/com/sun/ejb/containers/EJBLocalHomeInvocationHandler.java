/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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

package com.sun.ejb.containers;

import com.sun.ejb.Container;
import com.sun.ejb.EjbInvocation;
import com.sun.ejb.InvocationInfo;
import com.sun.ejb.containers.util.MethodMap;
import com.sun.enterprise.container.common.spi.util.IndirectlySerializable;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.Utility;

import jakarta.ejb.EJBException;
import jakarta.ejb.EJBLocalHome;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Handler for EJBLocalHome invocations through EJBLocalHome proxy.
 *
 * @author Kenneth Saks
 */
public class EJBLocalHomeInvocationHandler extends EJBLocalHomeImpl implements InvocationHandler {

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(EJBLocalHomeInvocationHandler.class);

    private boolean isStatelessSession_;

    // Our associated proxy object. Used when a caller needs EJBLocalObject
    // but only has InvocationHandler.
    private EJBLocalHome proxy_;

    private final Class localHomeIntfClass_;

    // Cache reference to invocation info. There is one of these per
    // container. It's populated during container initialization and
    // passed in when the InvocationHandler is created. This avoids the
    // overhead of building the method info each time a LocalHome proxy
    // is created.
    private MethodMap invocationInfoMap_;

    protected EJBLocalHomeInvocationHandler(EjbDescriptor ejbDescriptor, Class localHomeIntf) throws Exception {
        if (ejbDescriptor instanceof EjbSessionDescriptor) {
            isStatelessSession_ = ((EjbSessionDescriptor) ejbDescriptor).isStateless();
        } else {
            isStatelessSession_ = false;
        }

        localHomeIntfClass_ = localHomeIntf;

        // NOTE : Container is not set on super-class until after
        // constructor is called.
    }

    public void setMethodMap(MethodMap map) {
        invocationInfoMap_ = map;
    }

    public void setProxy(EJBLocalHome proxy) {
        proxy_ = proxy;
    }

    @Override
    protected EJBLocalHome getEJBLocalHome() {
        return proxy_;
    }

    /**
     * Called by EJBLocalHome proxy.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        ClassLoader originalClassLoader = null;

        // NOTE : be careful with "args" parameter. It is null
        // if method signature has 0 arguments.
        try {
            getContainer().onEnteringContainer();

            // In some cases(e.g. CDI + OSGi combination) ClassLoader
            // is accessed from the current Thread. In those cases we need to set
            // the context classloader to the application's classloader before
            // proceeding. Otherwise, the context classloader could still
            // reflect the caller's class loader.

            if (Thread.currentThread().getContextClassLoader() != getContainer().getClassLoader()) {
                originalClassLoader = Utility.setContextClassLoader(getContainer().getClassLoader());
            }

            Class<?> methodClass = method.getDeclaringClass();
            if (methodClass == Object.class) {
                return InvocationHandlerUtil.invokeJavaObjectMethod(this, method, args);
            } else if (methodClass == IndirectlySerializable.class) {
                return this.getSerializableObjectFactory();
            } else if (handleSpecialEJBLocalHomeMethod(method, methodClass)) {
                return invokeSpecialEJBLocalHomeMethod(method, methodClass, args);
            }

            // Use optimized version of get that takes param count as an argument.
            InvocationInfo invInfo = invocationInfoMap_.get(method, args == null ? 0 : args.length);
            if (invInfo == null) {
                throw new IllegalStateException("Unknown method :" + method);
            }

            if (methodClass == EJBLocalHome.class || invInfo.ejbIntfOverride) {
                // There is only one method on jakarta.ejb.EJBLocalHome
                super.remove(args[0]);
                return null;
            } else if (methodClass == GenericEJBLocalHome.class) {
                // This is a creation request through the EJB 3.0
                // client view, so just create a local business object and return it.
                EJBLocalObjectImpl localImpl = createEJBLocalBusinessObjectImpl((String) args[0]);
                return localImpl.getClientObject((String) args[0]);
            }

            // Process finder, create method, or home method.
            Object returnValue = null;

            final EJBLocalObjectImpl localObjectImpl;
            if (invInfo.startsWithCreate) {
                localObjectImpl = createEJBLocalObjectImpl();
                if (localObjectImpl != null) {
                    returnValue = localObjectImpl.getClientObject();
                }
            } else {
                localObjectImpl = null;
            }

            if (!isStatelessSession_) {
                if (invInfo.targetMethod1 == null) {
                    Object[] params = new Object[] { invInfo.ejbName, "LocalHome", invInfo.method.toString() };
                    String errorMsg = localStrings.getLocalString("ejb.bean_class_method_not_found", "", params);
                    throw new EJBException(errorMsg);
                }

                EjbInvocation inv = getContainer().createEjbInvocation();

                inv.isLocal = true;
                inv.isHome = true;
                inv.method = method;

                inv.clientInterface = localHomeIntfClass_;

                // Set cached invocation params. This will save additional lookups
                // in BaseContainer.
                inv.transactionAttribute = invInfo.txAttr;
                inv.invocationInfo = invInfo;

                if (localObjectImpl != null && invInfo.startsWithCreate) {
                    inv.ejbObject = localObjectImpl;
                }

                final BaseContainer container = getContainer();
                try {
                    container.preInvoke(inv);

                    if (invInfo.startsWithCreate) {
                        Object ejbCreateReturnValue = invokeTargetBeanMethod(container, invInfo.targetMethod1, inv, inv.ejb, args);
                        postCreate(container, inv, invInfo, ejbCreateReturnValue, args);
                        if (inv.ejbObject != null) {
                            returnValue = ((EJBLocalObjectImpl) inv.ejbObject).getClientObject();
                        }
                    } else if (invInfo.startsWithFindByPrimaryKey) {
                        returnValue = container.invokeFindByPrimaryKey(invInfo.targetMethod1, inv, args);
                    } else if (invInfo.startsWithFind) {
                        Object pKeys = invokeTargetBeanMethod(container, invInfo.targetMethod1, inv, inv.ejb, args);
                        returnValue = container.postFind(inv, pKeys, null);
                    } else {
                        returnValue = invokeTargetBeanMethod(container, invInfo.targetMethod1, inv, inv.ejb, args);
                    }
                } catch (InvocationTargetException ite) {
                    inv.exception = ite.getCause();
                } catch (Throwable c) {
                    inv.exception = c;
                } finally {
                    container.postInvoke(inv);
                }

                if (inv.exception != null) {
                    InvocationHandlerUtil.throwLocalException(inv.exception, method.getExceptionTypes());
                }
            }

            return returnValue;
        } finally {
            if (originalClassLoader != null) {
                Utility.setContextClassLoader(originalClassLoader);
            }
            getContainer().onLeavingContainer();
        }
    }

    // default impl to be overridden in subclasses if special invoke is necessary
    protected boolean handleSpecialEJBLocalHomeMethod(Method method, Class methodClass) {
        return false;
    }

    // default impl to be overridden in subclasses if special invoke is necessary
    protected Object invokeSpecialEJBLocalHomeMethod(Method method, Class methodClass, Object[] args) throws Throwable {
        return null;
    }

    // default impl to be overridden in subclass if necessary
    protected void postCreate(Container container, EjbInvocation inv, InvocationInfo invInfo, Object primaryKey, Object[] args)
            throws Throwable {
    }

    /**
     * Allow subclasses to execute a protected method in BaseContainer
     */
    protected Object invokeTargetBeanMethod(BaseContainer container, Method beanClassMethod, EjbInvocation inv, Object target, Object[] params) throws Throwable {
        return container.invokeTargetBeanMethod(beanClassMethod, inv, target, params);
    }

}
