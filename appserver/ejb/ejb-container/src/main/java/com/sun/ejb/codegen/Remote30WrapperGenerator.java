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

import java.lang.reflect.Method;
import java.util.*;


import static java.lang.reflect.Modifier.*;

import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.*;
import org.glassfish.pfl.dynamic.codegen.spi.Type ;
import org.glassfish.pfl.dynamic.codegen.spi.Expression ;

import com.sun.ejb.EJBUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;

public class Remote30WrapperGenerator extends Generator
    implements ClassGeneratorFactory {

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Remote30WrapperGenerator.class);

    private String remoteInterfaceName;
    private Class businessInterface;
    private String remoteClientClassName;
    private String remoteClientPackageName;
    private String remoteClientSimpleName;
    private Method[] bizMethods;

    private ClassLoader loader;

    public String getGeneratedClass() {
        return remoteClientClassName;
    }

    // For corba codegen infrastructure
    public String className() {
        return getGeneratedClass();
    }


    /**
     * Construct the Wrapper generator with the specified deployment
     * descriptor and class loader.
     *
     * @exception GeneratorException
     */
    public Remote30WrapperGenerator(ClassLoader cl, String businessIntfName, String remoteIntfName)
        throws GeneratorException {
        super();

        remoteInterfaceName = remoteIntfName;
        loader = cl;

        try {
            this.businessInterface = cl.loadClass(businessIntfName);
        } catch (ClassNotFoundException ex) {
            throw new InvalidBean(
            localStrings.getLocalString(
            "generator.remote_interface_not_found",
            "Business interface " + businessInterface + " not found "));
        }

        if (jakarta.ejb.EJBObject.class.isAssignableFrom(businessInterface)) {
            throw new GeneratorException("Invalid Remote Business Interface " +
                 businessInterface + ". A Remote Business interface MUST " +
                 "not extend jakarta.ejb.EJBObject.");
        }

        remoteClientClassName = EJBUtils.
            getGeneratedRemoteWrapperName(businessInterface.getName());

        remoteClientPackageName = getPackageName(remoteClientClassName);
        remoteClientSimpleName = getBaseName(remoteClientClassName);

        bizMethods = removeDups(businessInterface.getMethods());

        // NOTE : no need to remove ejb object methods because EJBObject
        // is only visible through the RemoteHome view.

    }


    public void evaluate() {

        _clear();

        if (remoteClientPackageName != null) {
            _package(remoteClientPackageName);
        } else {
            // no-arg _package() call is required for default package
            _package();
        }

        _class(PUBLIC, remoteClientSimpleName,
               _t("com.sun.ejb.containers.RemoteBusinessWrapperBase"),
               _t(businessInterface.getName()));

        _data(PRIVATE, _t(remoteInterfaceName), "delegate_");

        _constructor( PUBLIC ) ;
        _arg(_t(remoteInterfaceName), "stub");
        _arg(_String(), "busIntf");

        _body();
        _expr(_super(_s(_void(), _t("java.rmi.Remote"), _String()), _v("stub"), _v("busIntf")));
        _assign(_v("delegate_"), _v("stub"));
        _end();

        for(int i = 0; i < bizMethods.length; i++) {
            printMethodImpl(bizMethods[i]);
        }

        _end();

        try {
            java.util.Properties p = new java.util.Properties();
            p.put("Wrapper.DUMP_AFTER_SETUP_VISITOR", "true");
            p.put("Wrapper.TRACE_BYTE_CODE_GENERATION", "true");
            p.put("Wrapper.USE_ASM_VERIFIER", "true");
            _byteCode(loader, p);
        } catch(Exception e) {
            System.out.println("Got exception when generating byte code");
            e.printStackTrace();
        }

        _classGenerator() ;

        return;
    }


    private void printMethodImpl(Method m) {
        List<Type> exceptionList = new LinkedList<Type>();
        for (Class exception : m.getExceptionTypes()) {
            exceptionList.add(Type.type(exception));
        }

        _method(PUBLIC, Type.type(m.getReturnType()), m.getName(), exceptionList);

        int i = 0;
        List<Type> expressionListTypes = new LinkedList<Type>();
        List<Expression> expressionList = new LinkedList<Expression>();
        for (Class param : m.getParameterTypes()) {
            String paramName = "param" + i;
            _arg(Type.type(param), paramName);
            i++;
            expressionListTypes.add(Type.type(param));
            expressionList.add(_v(paramName));
        }

        _body();

        _try();

        Class returnType = m.getReturnType();

        if (returnType == void.class) {
            _expr(
                _call(_v("delegate_"), m.getName(), _s(Type.type(returnType), expressionListTypes), expressionList));
        } else {
            _return(
                _call(_v("delegate_"), m.getName(), _s(Type.type(returnType), expressionListTypes), expressionList));
        }

        boolean doExceptionTranslation = !java.rmi.Remote.class.isAssignableFrom(businessInterface);
        if (doExceptionTranslation) {

            _catch(_t("jakarta.transaction.TransactionRolledbackException"), "trex");

            _define( _t("java.lang.RuntimeException"), "r",
                _new( _t("jakarta.ejb.EJBTransactionRolledbackException"), _s(_void())));
            _expr(
                _call( _v("r"), "initCause", _s(_t("java.lang.Throwable"), _t("java.lang.Throwable")), _v("trex"))
            );
            _throw(_v("r"));

            _catch(_t("jakarta.transaction.TransactionRequiredException"), "treqex");

            _define( _t("java.lang.RuntimeException"), "r",
                _new( _t("jakarta.ejb.EJBTransactionRequiredException"), _s(_void())));
            _expr(
                _call( _v("r"), "initCause", _s(_t("java.lang.Throwable"), _t("java.lang.Throwable")), _v("treqex"))
            );
            _throw(_v("r"));

            _catch(_t("java.rmi.NoSuchObjectException"), "nsoe");

            _define( _t("java.lang.RuntimeException"), "r",
                _new( _t("jakarta.ejb.NoSuchEJBException"), _s(_void())));
            _expr(
                _call( _v("r"), "initCause", _s(_t("java.lang.Throwable"), _t("java.lang.Throwable")), _v("nsoe"))
            );
            _throw(_v("r"));

            _catch(_t("java.rmi.AccessException"), "accex");

            _define( _t("java.lang.RuntimeException"), "r",
                _new( _t("jakarta.ejb.EJBAccessException"), _s(_void())));
            _expr(
                _call( _v("r"), "initCause", _s(_t("java.lang.Throwable"), _t("java.lang.Throwable")), _v("accex"))
            );
            _throw(_v("r"));

            _catch(_t("com.sun.ejb.containers.InternalEJBContainerException"), "iejbcEx");

            // This wraps an EJBException. Pull out the cause and throw
            // it as is.
            //_define( _t("java.lang.Throwable"), "r", _null());


            // _throw(_cast(_t("jakarta.ejb.EJBException"), _v("r")));
            _throw(
                _cast(_t("jakarta.ejb.EJBException"), _call( _v("iejbcEx"), "getCause", _s(_t("java.lang.Throwable"))))
            );


            _catch(_t("java.rmi.RemoteException"), "re");

            _throw( _new( _t("jakarta.ejb.EJBException"), _s(_void(), _t("java.lang.Exception")), _v("re")));

            _catch( _t("org.omg.CORBA.SystemException"), "corbaSysEx");
            _define( _t("java.lang.RuntimeException"), "r", _new( _t("jakarta.ejb.EJBException"), _s(_void())));
            _expr(
                _call( _v("r"), "initCause", _s(_t("java.lang.Throwable"), _t("java.lang.Throwable")), _v("corbaSysEx"))
            );
            _throw(_v("r"));

            _end();

        } else {
            _catch(_t("com.sun.ejb.containers.InternalEJBContainerException"), "iejbcEx");
            _throw(
                _new( _t("com.sun.ejb.containers.InternalRemoteException"),
                _s(_void(), _t("com.sun.ejb.containers.InternalEJBContainerException")),
                _v("iejbcEx"))
            );
            _end();
        }

        _end();
    }
}
