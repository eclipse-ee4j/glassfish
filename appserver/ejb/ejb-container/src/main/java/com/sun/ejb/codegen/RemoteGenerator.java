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
import java.io.*;
import java.util.*;
import com.sun.ejb.EJBUtils;

import static java.lang.reflect.Modifier.*;

import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.*;
import org.glassfish.pfl.dynamic.codegen.spi.Type ;

import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * This class is used to generate the RMI-IIOP version of a
 * remote business interface.
 */

public class RemoteGenerator extends Generator
    implements ClassGeneratorFactory {

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(RemoteGenerator.class);


    private Class businessInterface;
    private Method[] bizMethods;
    private String remoteInterfacePackageName;
    private String remoteInterfaceSimpleName;
    private String remoteInterfaceName;

    /**
     * Get the fully qualified name of the generated class.
     * Note: the remote/local implementation class is in the same package
     * as the bean class, NOT the remote/local interface.
     * @return the name of the generated class.
     */
    public String getGeneratedClass() {
        return remoteInterfaceName;
    }

    // For corba codegen infrastructure
    public String className() {
        return getGeneratedClass();
    }

    /**
     * Construct the Wrapper generator with the specified deployment
     * descriptor and class loader.
     * @exception GeneratorException
     */
    public RemoteGenerator(ClassLoader cl, String businessIntf) throws GeneratorException {
        super();

        try {
            businessInterface = cl.loadClass(businessIntf);
        } catch (ClassNotFoundException ex) {
            throw new InvalidBean(
                localStrings.getLocalString(
                    "generator.remote_interface_not_found",
                    "Remote interface not found "));
        }

        remoteInterfaceName = EJBUtils.getGeneratedRemoteIntfName
            (businessInterface.getName());

        remoteInterfacePackageName = getPackageName(remoteInterfaceName);
        remoteInterfaceSimpleName = getBaseName(remoteInterfaceName);

        bizMethods = removeDups(businessInterface.getMethods());

        // NOTE : no need to remove ejb object methods because EJBObject
        // is only visible through the RemoteHome view.
    }


    public void evaluate() {

        _clear();

        if (remoteInterfacePackageName != null) {
            _package(remoteInterfacePackageName);
        } else {
            // no-arg _package() call is required for default package
            _package();
        }

        _interface(PUBLIC, remoteInterfaceSimpleName,
                   _t("java.rmi.Remote"),
                   _t("com.sun.ejb.containers.RemoteBusinessObject"));

        for(int i = 0; i < bizMethods.length; i++) {
            printMethod(bizMethods[i]);
        }

        _end();

        _classGenerator() ;

        return;

    }


    private void printMethod(Method m) {
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

        exceptionList.add(_t("com.sun.ejb.containers.InternalEJBContainerException"));
        _method(PUBLIC | ABSTRACT, Type.type(m.getReturnType()), m.getName(), exceptionList);

        int i = 0;
        for(Class param : m.getParameterTypes()) {
            _arg(Type.type(param), "param" + i);
            i++;
        }

        _end();
    }


}
