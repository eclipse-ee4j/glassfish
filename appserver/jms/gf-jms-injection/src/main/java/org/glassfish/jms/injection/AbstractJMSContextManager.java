/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jms.injection;

import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.annotation.PreDestroy;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;

/**
 * This bean has a map to store JMSContext instances based on the injection point, that makes sure in one class, the
 * injected JMSContext beans of different injection point will not share the same request/trasaction scoped JMSContext
 * instance in a request/transaction.
 */
public abstract class AbstractJMSContextManager implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(InjectableJMSContext.JMS_INJECTION_LOGGER);
    private final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(AbstractJMSContextManager.class);

    protected final Map<String, JMSContextEntry> contexts;

    public AbstractJMSContextManager() {
        contexts = new HashMap<>();
    }

    protected JMSContext createContext(String ipId, JMSContextMetadata metadata, ConnectionFactory connectionFactory) {
        int sessionMode = metadata.getSessionMode();
        String userName = metadata.getUserName();
        JMSContext context = null;
        if (userName == null) {
            context = connectionFactory.createContext(sessionMode);
        } else {
            String password = metadata.getPassword();
            context = connectionFactory.createContext(userName, password, sessionMode);
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, localStrings.getLocalString("JMSContext.impl.create",
                    "Created new JMSContext instance associated with id {0}: {1}.", ipId, context.toString()));
        }
        return context;
    }

    public synchronized JMSContext getContext(String ipId, String id, JMSContextMetadata metadata, ConnectionFactory connectionFactory) {
        JMSContextEntry contextEntry = contexts.get(id);
        JMSContext context = null;
        if (contextEntry == null) {
            context = createContext(ipId, metadata, connectionFactory);
            ServiceLocator serviceLocator = Globals.get(ServiceLocator.class);
            InvocationManager invMgr = serviceLocator.getService(InvocationManager.class);
            contexts.put(id, new JMSContextEntry(ipId, context, invMgr.getCurrentInvocation()));
        } else {
            context = contextEntry.getCtx();
        }
        return context;
    }

    public JMSContext getContext(String id) {
        JMSContextEntry entry = contexts.get(id);
        JMSContext context = null;
        if (entry != null) {
            context = entry.getCtx();
        }
        return context;
    }

    // Close and remove the JMSContext instances
    @PreDestroy
    public synchronized void cleanup() {
        ServiceLocator serviceLocator = Globals.get(ServiceLocator.class);
        InvocationManager invMgr = serviceLocator.getService(InvocationManager.class);
        ComponentInvocation currentInv = invMgr.getCurrentInvocation();

        for (Entry<String, JMSContextEntry> entry : contexts.entrySet()) {
            JMSContextEntry contextEntry = entry.getValue();
            String ipId = contextEntry.getInjectionPointId();
            JMSContext context = contextEntry.getCtx();
            if (context != null) {
                ComponentInvocation inv = contextEntry.getComponentInvocation();
                if (inv != null && currentInv != inv) {
                    invMgr.preInvoke(inv);
                }
                try {
                    context.close();
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, localStrings.getLocalString("JMSContext.impl.close",
                                "Closed JMSContext instance associated with id {0}: {1}.", ipId, context.toString()));
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, localStrings.getLocalString("JMSContext.impl.close.failure",
                            "Failed to close JMSContext instance associated with id {0}: {1}.", ipId, context.toString()), e);
                } finally {
                    if (inv != null && currentInv != inv) {
                        invMgr.postInvoke(inv);
                    }
                }
            }
        }
        contexts.clear();
    }

    public abstract String getType();
}
