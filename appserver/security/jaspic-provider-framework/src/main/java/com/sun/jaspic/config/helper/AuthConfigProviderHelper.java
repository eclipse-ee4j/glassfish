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

package com.sun.jaspic.config.helper;

import com.sun.jaspic.config.delegate.MessagePolicyDelegate;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.security.auth.callback.CallbackHandler;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.config.AuthConfigFactory;
import jakarta.security.auth.message.config.AuthConfigFactory.RegistrationContext;
import jakarta.security.auth.message.config.AuthConfigProvider;
import jakarta.security.auth.message.config.ClientAuthConfig;
import jakarta.security.auth.message.config.ServerAuthConfig;

/**
 *
 * @author Ron Monzillo
 */
public abstract class AuthConfigProviderHelper implements AuthConfigProvider {

    public static final String LAYER_NAME_KEY = "message.layer";
    public static final String ALL_LAYERS = "*";
    public static final String LOGGER_NAME_KEY = "logger.name";
    public static final String AUTH_MODULE_KEY = "auth.module.type";
    public static final String SERVER_AUTH_MODULE = "server.auth.module";
    public static final String CLIENT_AUTH_MODULE = "client.auth.module";
    private ReentrantReadWriteLock instanceReadWriteLock = new ReentrantReadWriteLock();
    private Lock rLock = instanceReadWriteLock.readLock();
    private Lock wLock = instanceReadWriteLock.writeLock();
    HashSet<String> selfRegistered;
    EpochCarrier epochCarrier;

    protected AuthConfigProviderHelper() {
        selfRegistered = new HashSet<String>();
        epochCarrier = new EpochCarrier();
    }

    protected final String getProperty(String key, String defaultValue) {
        String rvalue = defaultValue;
        Map<String, ?> properties = getProperties();
        if (properties != null && properties.containsKey(key)) {
            rvalue = (String) properties.get(key);
        }
        return rvalue;
    }

    protected String getLayer() {
        return getProperty(LAYER_NAME_KEY, ALL_LAYERS);
    }

    protected Class[] getModuleTypes() {
        Class[] rvalue = new Class[]{
            jakarta.security.auth.message.module.ServerAuthModule.class,
            jakarta.security.auth.message.module.ClientAuthModule.class
        };
        Map<String, ?> properties = getProperties();
        if (properties.containsKey(AUTH_MODULE_KEY)) {
            String keyValue = (String) properties.get(AUTH_MODULE_KEY);
            if (SERVER_AUTH_MODULE.equals(keyValue)) {
                rvalue = new Class[]{
                            jakarta.security.auth.message.module.ServerAuthModule.class
                        };
            } else if (CLIENT_AUTH_MODULE.equals(keyValue)) {
                rvalue = new Class[]{
                            jakarta.security.auth.message.module.ClientAuthModule.class
                        };
            }
        }
        return rvalue;
    }

    protected void oldSelfRegister() {
        if (getFactory() != null) {
            selfRegistered.clear();
            RegistrationContext[] contexts = getSelfRegistrationContexts();
            for (RegistrationContext r : contexts) {
                String id = getFactory().registerConfigProvider(this,
                        r.getMessageLayer(), r.getAppContext(),
                        r.getDescription());
                selfRegistered.add(id);
            }
        }
    }

    protected void selfRegister() {
        if (getFactory() != null) {
            wLock.lock();
            try {
                RegistrationContext[] contexts = getSelfRegistrationContexts();
                if (!selfRegistered.isEmpty()) {
                    HashSet<String> toBeUnregistered = new HashSet<String>();
                    // get the current self-registrations
                    String[] regID = getFactory().getRegistrationIDs(this);
                    for (String i : regID) {
                        if (selfRegistered.contains(i)) {
                            RegistrationContext c = getFactory().getRegistrationContext(i);
                            if (c != null && !c.isPersistent()) {
                                toBeUnregistered.add(i);
                            }
                        }
                    }
                    // remove self-registrations that already exist and should continue
                    for (String i : toBeUnregistered) {
                        RegistrationContext r = getFactory().getRegistrationContext(i);
                        for (int j = 0; j < contexts.length; j++) {
                            if (contextsAreEqual(contexts[j], r)) {
                                toBeUnregistered.remove(i);
                                contexts[j] = null;
                            }
                        }
                    }
                    // unregister those that should not continue to exist
                    for (String i : toBeUnregistered) {
                        selfRegistered.remove(i);
                        getFactory().removeRegistration(i);
                    }
                }
                // add new self-segistrations
                for (RegistrationContext r : contexts) {
                    if (r != null) {
                        String id = getFactory().registerConfigProvider(this,
                                r.getMessageLayer(), r.getAppContext(),
                                r.getDescription());
                        selfRegistered.add(id);
                    }
                }
            } finally {
                wLock.unlock();
            }

        }
    }

    protected CallbackHandler getClientCallbackHandler(CallbackHandler cbh)
            throws AuthException {
        if (cbh == null) {
            AuthException ae = new AuthException("AuthConfigProvider does not support null Client Callbackhandler");
            ae.initCause(new UnsupportedOperationException());
            throw ae;
        }
        return cbh;
    }

    protected CallbackHandler getServerCallbackHandler(CallbackHandler cbh) throws
            AuthException {
        if (cbh == null) {
            AuthException ae = new AuthException("AuthConfigProvider does not support null Server Callbackhandler");
            ae.initCause(new UnsupportedOperationException());
            throw ae;
        }
        return cbh;
    }

    public ClientAuthConfig getClientAuthConfig(String layer, String appContext,
            CallbackHandler cbh) throws AuthException {
        return new ClientAuthConfigHelper(getLoggerName(), epochCarrier,
                getAuthContextHelper(appContext, true),
                getMessagePolicyDelegate(appContext),
                layer, appContext,
                getClientCallbackHandler(cbh));
    }

    public ServerAuthConfig getServerAuthConfig(String layer, String appContext,
            CallbackHandler cbh) throws AuthException {
        return new ServerAuthConfigHelper(getLoggerName(), epochCarrier,
                getAuthContextHelper(appContext, true),
                getMessagePolicyDelegate(appContext),
                layer, appContext,
                getServerCallbackHandler(cbh));
    }

    public boolean contextsAreEqual(RegistrationContext a, RegistrationContext b) {
        if (a == null || b == null) {
            return false;
        } else if (a.isPersistent() != b.isPersistent()) {
            return false;
        } else if (!a.getAppContext().equals(b.getAppContext())) {
            return false;
        } else if (!a.getMessageLayer().equals(b.getMessageLayer())) {
            return false;
        } else if (!a.getDescription().equals(b.getDescription())) {
            return false;
        }
        return true;
    }

    /**
     * to be called by refresh on provider subclass, and after subclass impl.
     * has reloaded its underlying configuration system.
     * Note: Spec is silent as to whether self-registrations should be reprocessed.
     */
    public void oldRefresh() {
        if (getFactory() != null) {
            String[] regID = getFactory().getRegistrationIDs(this);
            for (String i : regID) {
                if (selfRegistered.contains(i)) {
                    RegistrationContext c = getFactory().getRegistrationContext(i);
                    if (c != null && !c.isPersistent()) {
                        getFactory().removeRegistration(i);
                    }
                }
            }
        }
        epochCarrier.increment();
        selfRegister();
    }

    public void refresh() {
        epochCarrier.increment();
        selfRegister();
    }

    public String getLoggerName() {
        return getProperty(LOGGER_NAME_KEY, AuthConfigProviderHelper.class.getName());
    }

    public abstract Map<String, ?> getProperties();

    public abstract AuthConfigFactory getFactory();

    public abstract RegistrationContext[] getSelfRegistrationContexts();

    public abstract AuthContextHelper getAuthContextHelper(String appContext,
            boolean returnNullContexts) throws AuthException;

    public abstract MessagePolicyDelegate getMessagePolicyDelegate(String appContext) throws AuthException;
}
