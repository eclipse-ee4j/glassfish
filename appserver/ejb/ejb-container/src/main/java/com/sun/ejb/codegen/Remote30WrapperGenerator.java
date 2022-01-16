/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
import com.sun.ejb.containers.InternalRemoteException;
import com.sun.ejb.containers.RemoteBusinessWrapperBase;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.lang.reflect.Method;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.pfl.dynamic.codegen.spi.Expression;
import org.glassfish.pfl.dynamic.codegen.spi.Type;
import org.omg.CORBA.SystemException;

import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBTransactionRequiredException;
import jakarta.ejb.EJBTransactionRolledbackException;
import jakarta.ejb.NoSuchEJBException;
import jakarta.transaction.TransactionRequiredException;
import jakarta.transaction.TransactionRolledbackException;

import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.PUBLIC;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.*;

public final class Remote30WrapperGenerator extends Generator {

    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Remote30WrapperGenerator.class);
    private static final Logger LOG = Logger.getLogger(Remote30WrapperGenerator.class.getName());

    private final String remoteInterfaceName;
    private final Class<?> businessInterface;
    private final String remoteClientClassName;
    private final String remoteClientPackageName;
    private final String remoteClientSimpleName;
    private final Method[] methodsToGenerate;

    /**
     * Adds _Wrapper to the original name.
     *
     * @param businessIntf full class name
     */
    public static String getGeneratedRemoteWrapperName(String businessIntf) {
        String packageName = getPackageName(businessIntf);
        String simpleName = getBaseName(businessIntf);
        String generatedSimpleName = "_" + simpleName + "_Wrapper";
        return packageName == null ? generatedSimpleName : packageName + "." + generatedSimpleName;
    }

    /**
     * Construct the Wrapper generator with the specified deployment
     * descriptor and class loader.
     *
     * @param loader
     * @param businessIntfName must already exist and be loadable by the loader
     * @param remoteInterfaceName generated class will implement this
     *
     * @throws GeneratorException
     */
    public Remote30WrapperGenerator(ClassLoader loader, String businessIntfName, String remoteInterfaceName)
        throws GeneratorException {

        super(loader);
        this.remoteInterfaceName = remoteInterfaceName;

        try {
            businessInterface = loader.loadClass(businessIntfName);
        } catch (ClassNotFoundException ex) {
            throw new InvalidBean(localStrings.getLocalString(
                "generator.remote_interface_not_found",
                "Business interface " + businessIntfName + " not found "));
        }

        if (jakarta.ejb.EJBObject.class.isAssignableFrom(businessInterface)) {
            throw new GeneratorException("Invalid Remote Business Interface " + businessInterface
                + ". A Remote Business interface MUST not extend jakarta.ejb.EJBObject.");
        }

        remoteClientClassName = getGeneratedRemoteWrapperName(businessIntfName);
        remoteClientPackageName = getPackageName(remoteClientClassName);
        remoteClientSimpleName = getBaseName(remoteClientClassName);

        methodsToGenerate = removeRedundantMethods(businessInterface.getMethods());

        // NOTE : no need to remove ejb object methods because EJBObject
        // is only visible through the RemoteHome view.
    }

    @Override
    public String getGeneratedClassName() {
        return remoteClientClassName;
    }

    @Override
    public Class<?> getAnchorClass() {
        return businessInterface;
    }

    @Override
    public void evaluate() {

        _clear();

        _setClassLoader(loader);

        if (remoteClientPackageName != null) {
            _package(remoteClientPackageName);
        } else {
            // no-arg _package() call is required for default package
            _package();
        }

        _class(PUBLIC, remoteClientSimpleName,
               _t(RemoteBusinessWrapperBase.class.getName()),
               _t(businessInterface.getName()));

        _data(PRIVATE, _t(remoteInterfaceName), "delegate_");

        _constructor( PUBLIC ) ;
        _arg(_t(remoteInterfaceName), "stub");
        _arg(_String(), "busIntf");

        _body();
        _expr(_super(_s(_void(), _t(Remote.class.getName()), _String()), _v("stub"), _v("busIntf")));
        _assign(_v("delegate_"), _v("stub"));
        _end();

        for (Method method : methodsToGenerate) {
            printMethodImpl(method);
        }

        _end();

        try {
            Properties p = new Properties();
            p.put("Wrapper.DUMP_AFTER_SETUP_VISITOR", "true");
            p.put("Wrapper.TRACE_BYTE_CODE_GENERATION", "true");
            p.put("Wrapper.USE_ASM_VERIFIER", "true");
            _byteCode(loader, p);
        } catch(Exception e) {
            LOG.log(Level.WARNING, "Got exception when generating byte code", e);
        }

        _classGenerator();
    }


    private void printMethodImpl(Method m) {
        List<Type> exceptionList = new LinkedList<>();
        for (Class<?> exception : m.getExceptionTypes()) {
            exceptionList.add(Type.type(exception));
        }

        _method(PUBLIC, Type.type(m.getReturnType()), m.getName(), exceptionList);

        int i = 0;
        List<Type> expressionListTypes = new LinkedList<>();
        List<Expression> expressionList = new LinkedList<>();
        for (Class<?> param : m.getParameterTypes()) {
            String paramName = "param" + i;
            _arg(Type.type(param), paramName);
            i++;
            expressionListTypes.add(Type.type(param));
            expressionList.add(_v(paramName));
        }

        _body();

        _try();

        Class<?> returnType = m.getReturnType();

        if (returnType == void.class) {
            _expr(
                _call(_v("delegate_"), m.getName(), _s(Type.type(returnType), expressionListTypes), expressionList));
        } else {
            _return(
                _call(_v("delegate_"), m.getName(), _s(Type.type(returnType), expressionListTypes), expressionList));
        }

        boolean doExceptionTranslation = !Remote.class.isAssignableFrom(businessInterface);
        if (doExceptionTranslation) {
            _catch(_t(TransactionRolledbackException.class.getName()), "trex");

            _define(_t(RuntimeException.class.getName()), "r",
                _new(_t(EJBTransactionRolledbackException.class.getName()), _s(_void())));
            _expr(
                _call(
                    _v("r"), "initCause",
                    _s(_t(Throwable.class.getName()), _t(Throwable.class.getName())),
                    _v("trex")
                )
            );
            _throw(_v("r"));

            _catch(_t(TransactionRequiredException.class.getName()), "treqex");

            _define(_t(RuntimeException.class.getName()), "r",
                _new(_t(EJBTransactionRequiredException.class.getName()), _s(_void())));

            _expr(
                _call(
                    _v("r"), "initCause",
                    _s(_t(Throwable.class.getName()), _t(Throwable.class.getName())),
                    _v("treqex")
                )
            );
            _throw(_v("r"));

            _catch(_t(NoSuchObjectException.class.getName()), "nsoe");

            _define(_t(RuntimeException.class.getName()), "r",
                _new(_t(NoSuchEJBException.class.getName()), _s(_void())));
            _expr(
                _call(
                    _v("r"), "initCause",
                    _s(_t(Throwable.class.getName()), _t(Throwable.class.getName())),
                    _v("nsoe")
                )
            );
            _throw(_v("r"));

            _catch(_t(AccessException.class.getName()), "accex");

            _define(_t(RuntimeException.class.getName()), "r",
                _new(_t(EJBAccessException.class.getName()), _s(_void())));
            _expr(
                _call(
                    _v("r"), "initCause",
                    _s(_t(Throwable.class.getName()), _t(Throwable.class.getName())),
                    _v("accex")
                )
            );
            _throw(_v("r"));

            _catch(_t(InternalEJBContainerException.class.getName()), "iejbcEx");

            _throw(
                _cast(
                    _t(EJBException.class.getName()),
                    _call(_v("iejbcEx"), "getCause", _s(_t(Throwable.class.getName())))
                )
            );


            _catch(_t(RemoteException.class.getName()), "re");

            _throw( _new( _t(EJBException.class.getName()), _s(_void(), _t(Exception.class.getName())), _v("re")));

            _catch( _t(SystemException.class.getName()), "corbaSysEx");
            _define( _t(RuntimeException.class.getName()), "r", _new( _t(EJBException.class.getName()), _s(_void())));
            _expr(
                _call(
                    _v("r"), "initCause",
                    _s(_t(Throwable.class.getName()), _t(Throwable.class.getName())),
                    _v("corbaSysEx")
                )
            );
            _throw(_v("r"));

            _end();

        } else {
            _catch(_t(InternalEJBContainerException.class.getName()), "iejbcEx");
            _throw(
                _new( _t(InternalRemoteException.class.getName()),
                _s(_void(), _t(InternalEJBContainerException.class.getName())),
                _v("iejbcEx"))
            );
            _end();
        }

        _end();
    }
}
