/*
 * Copyright (c) 2021, 2026 Contributors to the Eclipse Foundation
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
import com.sun.jts.CosTransactions.Configuration;
import com.sun.jts.CosTransactions.DefaultTransactionService;
import com.sun.jts.jta.TransactionServiceProperties;
import com.sun.jts.pi.InterceptorImpl;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

import java.lang.System.Logger;
import java.util.Properties;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.enterprise.iiop.api.IIOPInterceptorFactory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.main.jdke.props.SystemProperties;
import org.glassfish.pfl.basic.func.NullaryFunction;
import org.jvnet.hk2.annotations.Service;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INITIALIZE;
import org.omg.IOP.Codec;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import static com.sun.corba.ee.spi.misc.ORBConstants.ORB_SERVER_ID_PROPERTY;
import static com.sun.corba.ee.spi.misc.ORBConstants.TRANSACTION_CURRENT_NAME;
import static java.lang.System.Logger.Level.DEBUG;

/**
 *
 * @author mvatkina
 */
@Service(name = "TransactionIIOPInterceptorFactory")
@Singleton
public class TransactionIIOPInterceptorFactory implements IIOPInterceptorFactory {

    private static final Logger LOG = System.getLogger(TransactionIIOPInterceptorFactory.class.getName());

    private final TSIdentificationImpl tsIdent = new TSIdentificationImpl();
    private Properties jtsProperties = new Properties();
    private boolean txServiceInitialized;
    private InterceptorImpl interceptor;

    @Inject
    private ServiceLocator serviceLocator;
    @Inject
    private ProcessEnvironment processEnv;

    @PostConstruct
    private void initJTSProperties() {
        if (processEnv.getProcessType().isServer()) {
            SystemProperties.setProperty(InterceptorImpl.CLIENT_POLICY_CHECKING, Boolean.FALSE.toString(), true);
            jtsProperties = TransactionServiceProperties.getJTSProperties(serviceLocator, true);
            Configuration.setProperties(jtsProperties);
        } else {
            jtsProperties = TransactionServiceProperties.getJTSProperties(serviceLocator, true);
        }
        LOG.log(DEBUG, () -> "Server id: " + jtsProperties.getProperty(ORB_SERVER_ID_PROPERTY));
    }

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

            // Set ORB and TSIdentification: needed for app clients, standalone clients.
            InterceptorImpl.setOrb(theORB);
            try {
                DefaultTransactionService jts = new DefaultTransactionService();
                jts.identify_ORB(theORB, tsIdent, jtsProperties);
                interceptor.setTSIdentification(tsIdent);

                // resolve_initial_references is called ??
                org.omg.CosTransactions.Current transactionCurrent = jts.get_current();

                theORB.getLocalResolver().register(TRANSACTION_CURRENT_NAME,
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
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
