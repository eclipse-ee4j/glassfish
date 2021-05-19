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

package com.sun.ejb.codegen;


import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.*;
import org.glassfish.pfl.dynamic.codegen.spi.Type ;

import java.util.logging.Logger;

import jakarta.jws.WebMethod;

import static java.lang.reflect.Modifier.*;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;

/**
 * This class is responsible for generating the SEI when it is not packaged
 * by the application.
 *
 * @author Jerome Dochez
 */
public class ServiceInterfaceGenerator extends Generator
    implements ClassGeneratorFactory {

    private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(ServiceInterfaceGenerator.class);
    private static Logger _logger=null;
    static{
       _logger=LogDomains.getLogger(ServiceInterfaceGenerator.class, LogDomains.DPL_LOGGER);
    }

    Class sib=null;
    String serviceIntfName;
    String packageName;
    String serviceIntfSimpleName;
    Method[] intfMethods;

   /**
     * Construct the Wrapper generator with the specified deployment
     * descriptor and class loader.
     * @exception GeneratorException.
     */
    public ServiceInterfaceGenerator(ClassLoader cl, Class sib)
        throws GeneratorException, ClassNotFoundException
    {
        super();

        this.sib = sib;
        serviceIntfSimpleName = getServiceIntfName();

        packageName = getPackageName();
        serviceIntfName = packageName + "." + serviceIntfSimpleName;

        intfMethods = calculateMethods(sib, removeDups(sib.getMethods()));

        // NOTE : no need to remove ejb object methods because EJBObject
        // is only visible through the RemoteHome view.
    }

    public String getServiceIntfName() {
        String serviceIntfSimpleName = sib.getSimpleName();
        if (serviceIntfSimpleName.endsWith("EJB")) {
            return serviceIntfSimpleName.substring(0, serviceIntfSimpleName.length()-3);
        } else {
            return serviceIntfSimpleName+"SEI";
        }
    }

    public String getPackageName() {
        return sib.getPackage().getName()+".internal.jaxws";
    }

    /**
     * Get the fully qualified name of the generated class.
     * Note: the remote/local implementation class is in the same package
     * as the bean class, NOT the remote/local interface.
     * @return the name of the generated class.
     */
    public String getGeneratedClass() {
        return serviceIntfName;
    }

    // For corba codegen infrastructure
    public String className() {
        return getGeneratedClass();
    }

    private Method[] calculateMethods(Class sib, Method[] initialList) {

        // we start by assuming the @WebMethod was NOT used on this class
        boolean webMethodAnnotationUsed = false;
        List<Method> list = new ArrayList<Method>();

        for (Method m : initialList) {
            WebMethod wm = m.getAnnotation(WebMethod.class);
            if ( (wm != null) && !webMethodAnnotationUsed) {
                webMethodAnnotationUsed=true;
                // reset the list, this is the first annotated method we find
                list.clear();
            }
            if (wm!=null) {
                list.add(m);
            } else {
                if (!webMethodAnnotationUsed && !m.getDeclaringClass().equals(java.lang.Object.class)) {
                    list.add(m);
                }
            }
        }
        return list.toArray(new Method[list.size()]);
    }

    public void evaluate() {

        _clear();

        if (packageName != null) {
            _package(packageName);
        }

        _interface(PUBLIC, serviceIntfSimpleName);

        for(int i = 0; i < intfMethods.length; i++) {
            printMethod(intfMethods[i]);
        }

        _end();

        return;

    }


    private void printMethod(Method m)
    {

        boolean throwsRemoteException = false;
        List<Type> exceptionList = new LinkedList<Type>();
        for(Class exception : m.getExceptionTypes()) {
            exceptionList.add(Type.type(exception));
            if( exception.getName().equals("java.rmi.RemoteException") ) {
                throwsRemoteException = true;
            }
    }
        if( !throwsRemoteException ) {
            exceptionList.add(_t("java.rmi.RemoteException"));
        }

        _method( PUBLIC | ABSTRACT, Type.type(m.getReturnType()),
                 m.getName(), exceptionList);

        int i = 0;

        for(Class param : m.getParameterTypes()) {
            _arg(Type.type(param), "param" + i);
            i++;
        }

        _end();
    }

}
