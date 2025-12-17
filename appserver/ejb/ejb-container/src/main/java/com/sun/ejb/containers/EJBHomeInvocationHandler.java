/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.Utility;

import jakarta.ejb.EJBHome;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


/**
 * Handler for EJBHome invocations through EJBHome proxy.
 *
 * @author Kenneth Saks
 */
public class EJBHomeInvocationHandler extends EJBHomeImpl implements InvocationHandler {

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(EJBHomeInvocationHandler.class);

    private boolean isStatelessSession_;

    // Our associated proxy object.  Used when a caller needs EJBHome
    // but only has InvocationHandler.
    private EJBHome proxy_;

    private final Class homeIntfClass_;

    // Cache reference to invocation info.  There is one of these per
    // container.  It's populated during container initialization and
    // passed in when the InvocationHandler is created.  This avoids the
    // overhead of building the method info each time a Home proxy
    // is created.
    private MethodMap invocationInfoMap_;

    private final EjbContainerUtil ejbContainerUtil = EjbContainerUtilImpl.getInstance();


    protected EJBHomeInvocationHandler(EjbDescriptor ejbDescriptor, Class homeIntfClass) throws Exception {
        if (ejbDescriptor instanceof EjbSessionDescriptor) {
            isStatelessSession_ = ((EjbSessionDescriptor) ejbDescriptor).isStateless();
        } else {
            isStatelessSession_ = false;
        }

        homeIntfClass_ = homeIntfClass;

        // NOTE : Container is not set on super-class until after
        // constructor is called.
    }

    public void setProxy(EJBHome proxy) {
        proxy_ = proxy;
    }

    public void setMethodMap(MethodMap map) {
        invocationInfoMap_ = map;
    }

    @Override
    protected EJBHome getEJBHome() {
        return proxy_;
    }

    /**
     * Called by EJBHome proxy.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ClassLoader originalClassLoader = null;

        // NOTE : be careful with "args" parameter.  It is null
        //        if method signature has 0 arguments.
        try {
            getContainer().onEnteringContainer();

            // In some cases(e.g. if the Home/Remote interfaces appear in
            // a parent of the application's classloader),
            // ServantLocator.preinvoke() will not be called by the
            // ORB, and therefore BaseContainer.externalPreInvoke will not have
            // been called for this invocation.  In those cases we need to set
            // the context classloader to the application's classloader before
            // proceeding. Otherwise, the context classloader could still
            // reflect the caller's class loader.

            if (Thread.currentThread().getContextClassLoader() != getContainer().getClassLoader()) {
                originalClassLoader = Utility.setContextClassLoader(getContainer().getClassLoader());
            }

            Class<?> methodClass = method.getDeclaringClass();
            if (methodClass == Object.class) {
                return InvocationHandlerUtil.invokeJavaObjectMethod(this, method, args);
            } else if (invokeSpecialEJBHomeMethod(method, methodClass, args)) {
                return null;
            }

            // Use optimized version of get that takes param count as an argument.
            InvocationInfo invInfo = invocationInfoMap_.get(method, args == null ? 0 : args.length);

            if (invInfo == null) {
                throw new RemoteException("Unknown Home interface method: " + method);
            } else if (methodClass == EJBHome.class || invInfo.ejbIntfOverride) {
                return invokeEJBHomeMethod(method.getName(), args);
            } else if (GenericEJBHome.class.isAssignableFrom(methodClass)) {
                if (method.getName().equals("create")) {
                    // This is an internal creation request through the EJB 3.0
                    // client view, so just create an business object and return it
                    EJBObjectImpl busObjectImpl = createRemoteBusinessObjectImpl();
                    return busObjectImpl.getStub((String) args[0]);
                }
                EjbAsyncInvocationManager asyncManager = ((EjbContainerUtilImpl) ejbContainerUtil)
                    .getEjbAsyncInvocationManager();
                Long asyncTaskID = (Long) args[0];
                RemoteAsyncResult asyncResult = null;
                if (method.getName().equals("cancel")) {
                    asyncResult = asyncManager.remoteCancel(asyncTaskID);
                } else if (method.getName().equals("get")) {
                    asyncResult = asyncManager.remoteGet(asyncTaskID);
                } else if (method.getName().equals("isDone")) {
                    asyncResult = asyncManager.remoteIsDone(asyncTaskID);
                } else if (method.getName().equals("getWithTimeout")) {
                    Long timeout = (Long) args[1];
                    TimeUnit unit = TimeUnit.valueOf((String) args[2]);
                    asyncResult = asyncManager.remoteGetWithTimeout(asyncTaskID, timeout, unit);
                }
                return asyncResult;
            }

            // Process finder, create method, or home method.
            Object returnValue = null;

            final EJBObjectImpl ejbObjectImpl;
            if (invInfo.startsWithCreate) {
                ejbObjectImpl = createEJBObjectImpl();
                if (ejbObjectImpl != null) {
                    // Entity beans are created differently
                    returnValue = ejbObjectImpl.getStub();
                }
            } else {
                ejbObjectImpl = null;
            }

            if (!isStatelessSession_) {
                if (invInfo.targetMethod1 == null) {
                    _logger.log(Level.SEVERE, "ejb.bean_class_method_not_found",
                        new Object[] {invInfo.ejbName, "Home", invInfo.method.toString()});
                    // in exception use message without ID
                    String errorMsg = localStrings.getLocalString("ejb.bean_class_method_not_found", "",
                        new Object[] {invInfo.ejbName, "Home", invInfo.method.toString()});
                    throw new RemoteException(errorMsg);
                }

                BaseContainer container = (BaseContainer) getContainer();
                EjbInvocation inv = container.createEjbInvocation();

                inv.isRemote = true;
                inv.method  = method;
                inv.isHome  = true;

                inv.clientInterface = homeIntfClass_;

                // Set cached invocation params.  This will save
                // additional lookups in BaseContainer.

                inv.transactionAttribute = invInfo.txAttr;
                inv.invocationInfo = invInfo;

                if (ejbObjectImpl != null && invInfo.startsWithCreate) {
                    inv.ejbObject = ejbObjectImpl;
                }
                try {
                    container.preInvoke(inv);
                    if (invInfo.startsWithCreate) {
                        Object ejbCreateReturnValue = invokeTargetBeanMethod(container, invInfo.targetMethod1, inv,
                            inv.ejb, args);
                        postCreate(container, inv, invInfo, ejbCreateReturnValue, args);
                        if (inv.ejbObject != null) {
                            returnValue = ((EJBObjectImpl) inv.ejbObject).getStub();
                        }
                    } else if (invInfo.startsWithFindByPrimaryKey) {
                        returnValue = container.invokeFindByPrimaryKey(invInfo.targetMethod1, inv, args);
                    } else if ( invInfo.startsWithFind ) {
                        Object pKeys = invokeTargetBeanMethod(container, invInfo.targetMethod1, inv, inv.ejb, args);
                        returnValue = container.postFind(inv, pKeys, null);
                    } else {
                        returnValue = invokeTargetBeanMethod(container, invInfo.targetMethod1, inv, inv.ejb, args);
                    }
                } catch(InvocationTargetException ite) {
                    inv.exception = ite.getCause();
                } catch(Throwable c) {
                    inv.exception = c;
                } finally {
                    container.postInvoke(inv);
                }

                if (inv.exception != null) {
                    InvocationHandlerUtil.throwRemoteException(inv.exception, method.getExceptionTypes());
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

    /**
     * Default impl to be overridden in subclass if necessary
     * @return false
     */
    protected boolean invokeSpecialEJBHomeMethod(Method method, Class<?> methodClass, Object[] args) throws Exception {
        return false;
    }

    /**
     * Default impl to be overridden in subclass if necessary.
     * Doesn't do anything.
     */
    protected void postCreate(Container container, EjbInvocation inv, InvocationInfo invInfo, Object primaryKey,
        Object[] args) throws Throwable {
    }


    /**
     * Allow subclasses to execute a protected method in BaseContainer
     */
    protected Object invokeTargetBeanMethod(BaseContainer container, Method beanClassMethod, EjbInvocation inv,
        Object target, Object[] params) throws Throwable {
        return container.invokeTargetBeanMethod(beanClassMethod, inv, target, params);
    }


    private Object invokeEJBHomeMethod(String methodName, Object[] args) throws Exception {
        // Return value is null if target method returns void.
        final Object returnValue;

        // NOTE : Might be worth optimizing this method check if it
        // turns out to be a bottleneck.  I don't think these methods
        // are called with the frequency that this would be an issue,
        // but it's worth considering.

        int methodIndex = -1;
        Exception caughtException = null;
        BaseContainer container = (BaseContainer) getContainer();
        try {
            if (methodName.equals("getEJBMetaData")) {
                methodIndex = BaseContainer.EJBHome_getEJBMetaData;
                container.onEjbMethodStart(methodIndex);
                returnValue = super.getEJBMetaData();
            } else if (methodName.equals("getHomeHandle")) {
                methodIndex = BaseContainer.EJBHome_getHomeHandle;
                container.onEjbMethodStart(methodIndex);
                returnValue = super.getHomeHandle();
            } else if (methodName.equals("remove")) {
                if (args[0] instanceof jakarta.ejb.Handle) {
                    methodIndex = BaseContainer.EJBHome_remove_Handle;
                    container.onEjbMethodStart(methodIndex);
                    super.remove((jakarta.ejb.Handle) args[0]);
                } else {
                    methodIndex = BaseContainer.EJBHome_remove_Pkey;
                    container.onEjbMethodStart(methodIndex);
                    super.remove(args[0]);
                }
                returnValue = null;
            } else {
                throw new RemoteException("unknown EJBHome method = " + methodName);
            }
        } catch (Exception ex) {
            caughtException = ex;
            throw ex;
        } finally {
            if (methodIndex != -1) {
                container.onEjbMethodEnd(methodIndex, caughtException);
            }
        }
        return returnValue;
    }
}
