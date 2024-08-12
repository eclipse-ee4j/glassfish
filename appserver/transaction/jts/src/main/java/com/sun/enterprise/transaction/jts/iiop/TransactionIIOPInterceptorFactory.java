/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.transaction.jts.iiop;

import com.sun.corba.ee.impl.txpoa.TSIdentificationImpl;
import com.sun.corba.ee.spi.legacy.interceptor.ORBInitInfoExt;
import com.sun.corba.ee.spi.logging.POASystemException;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.jts.CosTransactions.Configuration;
import com.sun.jts.CosTransactions.DefaultTransactionService;
import com.sun.jts.jta.TransactionServiceProperties;
import com.sun.jts.pi.InterceptorImpl;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.enterprise.iiop.api.IIOPInterceptorFactory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.pfl.basic.func.NullaryFunction;
import org.jvnet.hk2.annotations.Service;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INITIALIZE;
import org.omg.IOP.Codec;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

/**
 *
 * @author mvatkina
 */
@Service(name = "TransactionIIOPInterceptorFactory")
public class TransactionIIOPInterceptorFactory implements IIOPInterceptorFactory {

    // The log message bundle is in com.sun.jts package
    private static Logger _logger = LogDomains.getLogger(InterceptorImpl.class, LogDomains.TRANSACTION_LOGGER);

    private static Properties jtsProperties = new Properties();
    private static TSIdentificationImpl tsIdent = new TSIdentificationImpl();
    private static boolean txServiceInitialized = false;
    private InterceptorImpl interceptor = null;

    @Inject
    private ServiceLocator serviceLocator;
    @Inject
    private ProcessEnvironment processEnv;

    @Override
    public ClientRequestInterceptor createClientRequestInterceptor(ORBInitInfo info, Codec codec) {
        if (!txServiceInitialized) {
            createInterceptor(info, codec);
        }

        return interceptor;
    }

    @Override
    public ServerRequestInterceptor createServerRequestInterceptor(ORBInitInfo info, Codec codec) {
        if (!txServiceInitialized) {
            createInterceptor(info, codec);
        }

        return interceptor;
    }

    private void createInterceptor(ORBInitInfo info, Codec codec) {
        if (processEnv.getProcessType().isServer()) {
            try {
                System.setProperty(InterceptorImpl.CLIENT_POLICY_CHECKING, String.valueOf(false));
            } catch (Exception ex) {
                _logger.log(Level.WARNING, "iiop.readproperty_exception", ex);
            }

            initJTSProperties(true);
        } else {
            initJTSProperties(false);
        }

        try {
            // register JTS interceptors
            // first get hold of PICurrent to allocate a slot for JTS service.
            Current pic = (Current) info.resolve_initial_references("PICurrent");

            // allocate a PICurrent slotId for the transaction service.
            int[] slotIds = new int[2];
            slotIds[0] = info.allocate_slot_id();
            slotIds[1] = info.allocate_slot_id();

            interceptor = new InterceptorImpl(pic, codec, slotIds, null);
            // Get the ORB instance on which this interceptor is being
            // initialized
            com.sun.corba.ee.spi.orb.ORB theORB = ((ORBInitInfoExt) info).getORB();

            // Set ORB and TSIdentification: needed for app clients,
            // standalone clients.
            interceptor.setOrb(theORB);
            try {
                DefaultTransactionService jts = new DefaultTransactionService();
                jts.identify_ORB(theORB, tsIdent, jtsProperties);
                interceptor.setTSIdentification(tsIdent);

                // V2-XXX should jts.get_current() be called everytime
                // resolve_initial_references is called ??
                org.omg.CosTransactions.Current transactionCurrent = jts.get_current();

                theORB.getLocalResolver().register(ORBConstants.TRANSACTION_CURRENT_NAME,
                        NullaryFunction.Factory.makeConstant((org.omg.CORBA.Object) transactionCurrent));

                // the JTS PI use this to call the proprietary hooks
                theORB.getLocalResolver().register("TSIdentification",
                        NullaryFunction.Factory.makeConstant((org.omg.CORBA.Object) tsIdent));
                txServiceInitialized = true;
            } catch (Exception ex) {
                throw new INITIALIZE("JTS Exception: " + ex, POASystemException.JTS_INIT_ERROR, CompletionStatus.COMPLETED_MAYBE);
            }

            // Add IOR Interceptor only for OTS tagged components
            TxIORInterceptor iorInterceptor = new TxIORInterceptor(codec, serviceLocator);
            info.add_ior_interceptor(iorInterceptor);

        } catch (Exception e) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Exception registering JTS interceptors", e);
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    private void initJTSProperties(boolean isServer) {
        if (serviceLocator != null) {
            jtsProperties = TransactionServiceProperties.getJTSProperties(serviceLocator, true);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "++++ Server id: " + jtsProperties.getProperty(ORBConstants.ORB_SERVER_ID_PROPERTY));
            }
            if (isServer) {
                Configuration.setProperties(jtsProperties);
            }
        }
    }
}
