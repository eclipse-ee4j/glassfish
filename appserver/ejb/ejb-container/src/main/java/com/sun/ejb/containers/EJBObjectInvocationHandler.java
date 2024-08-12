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

package com.sun.ejb.containers;

import com.sun.ejb.EjbInvocation;
import com.sun.ejb.InvocationInfo;
import com.sun.ejb.containers.util.MethodMap;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.Utility;

import jakarta.ejb.EJBObject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.logging.Level;

/**
 * Handler for EJBObject invocations through EJBObject proxy.
 *
 *
 * @author Kenneth Saks
 */

public final class EJBObjectInvocationHandler
    extends EJBObjectImpl implements InvocationHandler {

    private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(EJBObjectInvocationHandler.class);

    // Cache reference to invocation info populated during container
    // initialization. This avoids the overhead of building the method
    // info each time a proxy is created.
    private MethodMap invocationInfoMap_;

    private Class remoteIntf_;

    /**
     * Constructor used for Remote Home view.
     */
    public EJBObjectInvocationHandler(MethodMap invocationInfoMap,
                                      Class remoteIntf)
        throws Exception {

        invocationInfoMap_ = invocationInfoMap;

        remoteIntf_ = remoteIntf;
        setIsRemoteHomeView(true);

        // NOTE : Container is not set on super-class until after
        // constructor is called.
    }

    /**
     * Constructor used for Remote Business view.
     */
    public EJBObjectInvocationHandler(MethodMap invocationInfoMap)
        throws Exception {

        invocationInfoMap_ = invocationInfoMap;

        setIsRemoteHomeView(false);

        // NOTE : Container is not set on super-class until after
        // constructor is called.
    }

    /**
     * This entry point is only used for the Remote Home view.
     */
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {

        return invoke(remoteIntf_, method, args);
    }

    Object invoke(Class clientInterface, Method method, Object[] args)
        throws Throwable {

        ClassLoader originalClassLoader = null;

        // NOTE : be careful with "args" parameter.  It is null
        //        if method signature has 0 arguments.
        try {
            container.onEnteringContainer();

            // In some cases(e.g. if the Home/Remote interfaces appear in
            // a parent of the application's classloader),
            // ServantLocator.preinvoke() will not be called by the
            // ORB, and therefore BaseContainer.externalPreInvoke will not have
            // been called for this invocation.  In those cases we need to set
            // the context classloader to the application's classloader before
            // proceeding. Otherwise, the context classloader could still
            // reflect the caller's class loader.

            if( Thread.currentThread().getContextClassLoader() !=
                getContainer().getClassLoader() ) {
                originalClassLoader = Utility.setContextClassLoader
                    (getContainer().getClassLoader());
            }

            Class methodClass = method.getDeclaringClass();
            if( methodClass == java.lang.Object.class ) {
                return InvocationHandlerUtil.invokeJavaObjectMethod
                    (this, method, args);
            }

            // Use optimized version of get that takes param count as an
            // argument.
            InvocationInfo invInfo = (InvocationInfo)
                invocationInfoMap_.get(method,
                                       ((args != null) ? args.length : 0) );

            if( invInfo == null ) {
                throw new RemoteException("Unknown Remote interface method :"
                                          + method);
            }

            if( (methodClass == jakarta.ejb.EJBObject.class) ||
                invInfo.ejbIntfOverride ) {
                return invokeEJBObjectMethod(method.getName(), args);
            } else if( invInfo.targetMethod1 == null ) {
                Object [] params = new Object[]
                    { invInfo.ejbName, "Remote", invInfo.method.toString() };
                String errorMsg = localStrings.getLocalString
                    ("ejb.bean_class_method_not_found", "", params);

                _logger.log(Level.SEVERE, "ejb.bean_class_method_not_found",
                       params);
                throw new RemoteException(errorMsg);
            }

            // Process application-specific method.

            Object returnValue = null;

            EjbInvocation inv = container.createEjbInvocation();

            inv.isRemote  = true;
            inv.isHome    = false;
            inv.isBusinessInterface = !isRemoteHomeView();
            inv.ejbObject = this;
            inv.method    = method;

            inv.clientInterface = clientInterface;

            // Set cached invocation params.  This will save additional lookups
            // in BaseContainer.
            inv.transactionAttribute = invInfo.txAttr;
            inv.invocationInfo = invInfo;
            inv.beanMethod = invInfo.targetMethod1;
            inv.methodParams = args;

            try {
                container.preInvoke(inv);
                returnValue = container.intercept(inv);
            } catch(InvocationTargetException ite) {
                inv.exception = ite.getCause();
                inv.exceptionFromBeanMethod = inv.exception;
            } catch(Throwable t) {
                inv.exception = t;
            } finally {
                container.postInvoke(inv);
                //purge ThreadLocals before the thread is returned to pool
                if (container.getSecurityManager() != null) {
                    container.getSecurityManager().resetPolicyContext();
                }
            }

            if (inv.exception != null) {
                InvocationHandlerUtil.throwRemoteException
                    (inv.exception, method.getExceptionTypes());
            }

            return returnValue;
        } finally {

            if( originalClassLoader != null ) {
                Utility.setContextClassLoader(originalClassLoader);
            }

            container.onLeavingContainer();
        }
    }


    private Object invokeEJBObjectMethod(String methodName, Object[] args)
        throws Exception
    {
        // Return value is null if target method returns void.
        Object returnValue = null;


        // NOTE : Might be worth optimizing this method check if it
        // turns out to be a bottleneck.  I don't think these methods
        // are called with the frequency that this would be an issue,
        // but it's worth considering.
        int methodIndex = -1;
        Exception caughtException = null;

        try {
            if( methodName.equals("getEJBHome") ) {

                methodIndex = container.EJBObject_getEJBHome;
                container.onEjbMethodStart(methodIndex);
                returnValue = super.getEJBHome();

            } else if( methodName.equals("getHandle") ) {

                methodIndex = container.EJBObject_getHandle;
                container.onEjbMethodStart(methodIndex);
                returnValue = super.getHandle();

            } else if( methodName.equals("getPrimaryKey") ) {

                methodIndex = container.EJBObject_getPrimaryKey;
                container.onEjbMethodStart(methodIndex);
                returnValue = super.getPrimaryKey();

            } else if( methodName.equals("isIdentical") ) {

                // boolean isIdentical(EJBObject)
                // Convert the param into an EJBObject.
                EJBObject other = (EJBObject) args[0];

                methodIndex = container.EJBObject_isIdentical;
                container.onEjbMethodStart(methodIndex);
                returnValue = super.isIdentical(other);

            } else if( methodName.equals("remove") ) {

                methodIndex = container.EJBObject_remove;
                container.onEjbMethodStart(methodIndex);
                super.remove();

            } else {

                throw new RemoteException("unknown EJBObject method = "
                                          + methodName);
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
