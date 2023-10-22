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

import com.sun.ejb.containers.InternalEJBContainerException;
import com.sun.ejb.containers.RemoteBusinessObject;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.lang.reflect.Method;
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
 * This class is used to generate the RMI-IIOP version of a remote business interface.
 */
public final class RemoteGenerator extends Generator {

    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(RemoteGenerator.class);

    private final Class<?> businessInterface;
    private final Method[] methodsToGenerate;
    private final String remoteInterfacePackageName;
    private final String remoteInterfaceSimpleName;
    private final String remoteInterfaceName;


    /**
     * Adds _Remote to the original name.
     *
     * @param businessIntf full class name
     */
    public static String getGeneratedRemoteIntfName(String businessIntf) {
        String packageName = getPackageName(businessIntf);
        String simpleName = getBaseName(businessIntf);
        String generatedSimpleName = "_" + simpleName + "_Remote";
        return getFullClassName(packageName, generatedSimpleName);
    }


    /**
     * Construct the Wrapper generator with the specified deployment
     * descriptor and class loader.
     *
     * @param classLoader the class loader
     * @param businessIntf the full qualified class name
     * @throws GeneratorException if the businessInterface doesn't exist.
     */
    public RemoteGenerator(ClassLoader classLoader, String businessIntf) throws GeneratorException {
        super(classLoader);
        try {
            businessInterface = classLoader.loadClass(businessIntf);
        } catch (ClassNotFoundException ex) {
            throw new GeneratorException(
                localStrings.getLocalString("generator.remote_interface_not_found", "Remote interface not found "));
        }

        remoteInterfaceName = getGeneratedRemoteIntfName(businessInterface.getName());
        remoteInterfacePackageName = getPackageName(remoteInterfaceName);
        remoteInterfaceSimpleName = getBaseName(remoteInterfaceName);

        methodsToGenerate = removeRedundantMethods(businessInterface.getMethods());

        // NOTE : no need to remove ejb object methods because EJBObject
        // is only visible through the RemoteHome view.
    }

    @Override
    public String getPackageName() {
        return this.remoteInterfacePackageName;
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
        return remoteInterfaceName;
    }

    @Override
    public Class<?> getAnchorClass() {
        return businessInterface;
    }

    @Override
    public void defineClassBody() {
        _interface(PUBLIC, remoteInterfaceSimpleName,
            _t(java.rmi.Remote.class.getName()),
            _t(RemoteBusinessObject.class.getName())
        );

        for (Method method : methodsToGenerate) {
            printMethod(method);
        }

        _end();
    }


    private void printMethod(Method m) {
        boolean throwsRemoteException = false;
        List<Type> exceptionList = new LinkedList<>();
        for (Class<?> exception : m.getExceptionTypes()) {
            exceptionList.add(Type.type(exception));
            if (exception.getName().equals("java.rmi.RemoteException")) {
                throwsRemoteException = true;
            }
        }
        if (!throwsRemoteException) {
            exceptionList.add(_t("java.rmi.RemoteException"));
        }

        exceptionList.add(_t(InternalEJBContainerException.class.getName()));
        _method(PUBLIC | ABSTRACT, Type.type(m.getReturnType()), m.getName(), exceptionList);

        int i = 0;
        for (Class<?> param : m.getParameterTypes()) {
            _arg(Type.type(param), "param" + i);
            i++;
        }

        _end();
    }
}
