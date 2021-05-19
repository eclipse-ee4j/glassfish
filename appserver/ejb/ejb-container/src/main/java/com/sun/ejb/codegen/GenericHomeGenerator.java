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

package com.sun.ejb.codegen;


import com.sun.ejb.EJBUtils;

import com.sun.enterprise.util.LocalStringManagerImpl;

import static java.lang.reflect.Modifier.*;

import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.*;

/**
 * This class is used to generate a sub-interface of the
 * GenericEJBHome interface that will be loaded within each
 * application.
 */

public class GenericHomeGenerator extends Generator
    implements ClassGeneratorFactory {

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(GenericHomeGenerator.class);

    private String genericEJBHomeClassName;
    private ClassLoader loader;

    /**
     * Get the fully qualified name of the generated class.
     * @return the name of the generated class.
     */
    public String getGeneratedClass() {
        return genericEJBHomeClassName;
    }

    // For corba codegen infrastructure
    public String className() {
        return getGeneratedClass();
    }


    public GenericHomeGenerator(ClassLoader cl) throws GeneratorException {
        super();

        genericEJBHomeClassName = EJBUtils.getGenericEJBHomeClassName();
        loader = cl;
    }


    public void evaluate() {

        _clear();

        String packageName = getPackageName(genericEJBHomeClassName);
        String simpleName = getBaseName (genericEJBHomeClassName);

        _package(packageName);

        _interface(PUBLIC, simpleName,
                   _t("com.sun.ejb.containers.GenericEJBHome"));

        // Create method
        _method(PUBLIC | ABSTRACT, _t("java.rmi.Remote"),
                "create", _t("java.rmi.RemoteException"));

        _arg(_String(), "generatedBusinessIntf");

        _end();

        _classGenerator() ;

        return;
    }

}
