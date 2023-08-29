/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation.
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

package com.sun.ejb.codegen;

import jakarta.jws.WebMethod;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.glassfish.pfl.dynamic.codegen.spi.Type ;

import static java.lang.reflect.Modifier.ABSTRACT;
import static java.lang.reflect.Modifier.PUBLIC;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._arg;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._end;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._interface;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._method;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._t;

/**
 * This class is responsible for generating the SEI when it is not packaged
 * by the application.
 *
 * @author Jerome Dochez
 */
public class ServiceInterfaceGenerator extends Generator {

    private final Class<?> ejbClass;
    private final String packageName;
    private final String serviceIntfName;
    private final String serviceIntfSimpleName;
    private final Method[] intfMethods;

    /**
     * Construct the Wrapper generator with the specified deployment
     * descriptor and class loader.
     *
     * @param loader {@link ClassLoader} owning generated classes
     * @param ejbClass the wrapped class
     */
    public ServiceInterfaceGenerator(final ClassLoader loader, final Class<?> ejbClass) {
        super(loader);
        this.ejbClass = ejbClass;
        packageName = getPackageName(ejbClass.getName());
        serviceIntfSimpleName = getServiceIntfName(ejbClass);
        serviceIntfName = getFullClassName(packageName, serviceIntfSimpleName);
        intfMethods = calculateMethods(ejbClass, removeRedundantMethods(ejbClass.getMethods()));

        // NOTE : no need to remove ejb object methods because EJBObject
        // is only visible through the RemoteHome view.
    }

    @Override
    public String getPackageName() {
        return this.packageName;
    }

    /**
     * Get the fully qualified name of the generated class.
     * <p>
     * Note: the remote/local implementation class is in the same package
     * as the bean class, NOT the remote/local interface.
     *
     * @return the name of the generated class.
     */
    @Override
    public String getGeneratedClassName() {
        return serviceIntfName;
    }


    @Override
    public Class<?> getAnchorClass() {
        return ejbClass;
    }


    @Override
    public void defineClassBody() {
        _interface(PUBLIC, serviceIntfSimpleName);

        for (Method intfMethod : intfMethods) {
            printMethod(intfMethod);
        }

        _end();
    }


    private void printMethod(Method m) {
        boolean throwsRemoteException = false;
        List<Type> exceptionList = new LinkedList<>();
        for (Class<?> exception : m.getExceptionTypes()) {
            exceptionList.add(Type.type(exception));
            if (exception.getName().equals(RemoteException.class.getName())) {
                throwsRemoteException = true;
            }
        }
        if (!throwsRemoteException) {
            exceptionList.add(_t(RemoteException.class.getName()));
        }

        _method(PUBLIC | ABSTRACT, Type.type(m.getReturnType()), m.getName(), exceptionList);

        int i = 0;
        for (Class<?> param : m.getParameterTypes()) {
            _arg(Type.type(param), "param" + i);
            i++;
        }

        _end();
    }


    private static String getServiceIntfName(Class<?> ejbClass) {
        String serviceIntfSimpleName = ejbClass.getSimpleName();
        if (serviceIntfSimpleName.endsWith("EJB")) {
            return serviceIntfSimpleName.substring(0, serviceIntfSimpleName.length() - 3) + "_GeneratedSEI";
        }
        return serviceIntfSimpleName + "_GeneratedSEI";
    }


    private static Method[] calculateMethods(Class sib, Method[] initialList) {
        // we start by assuming the @WebMethod was NOT used on this class
        boolean webMethodAnnotationUsed = false;
        List<Method> list = new ArrayList<>();

        for (Method m : initialList) {
            WebMethod wm = m.getAnnotation(WebMethod.class);
            if (wm != null && !webMethodAnnotationUsed) {
                webMethodAnnotationUsed = true;
                // reset the list, this is the first annotated method we find
                list.clear();
            }
            if (wm != null) {
                list.add(m);
            } else {
                if (!webMethodAnnotationUsed && !m.getDeclaringClass().equals(Object.class)) {
                    list.add(m);
                }
            }
        }
        return list.toArray(new Method[list.size()]);
    }
}
