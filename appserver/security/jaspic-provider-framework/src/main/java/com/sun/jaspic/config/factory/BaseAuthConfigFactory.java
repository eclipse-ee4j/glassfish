/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

package com.sun.jaspic.config.factory;

import com.sun.jaspic.config.factory.singlemodule.DefaultAuthConfigProvider;
import com.sun.jaspic.config.helper.JASPICLogManager;

import jakarta.security.auth.message.config.AuthConfigFactory;
import jakarta.security.auth.message.config.AuthConfigProvider;
import jakarta.security.auth.message.config.RegistrationListener;
import jakarta.security.auth.message.module.ServerAuthModule;
import jakarta.servlet.ServletContext;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class implements methods in the abstract class AuthConfigFactory.
 * @author  Shing Wai Chan
 */
public abstract class BaseAuthConfigFactory extends AuthConfigFactory {

    private static final Logger LOG = Logger.getLogger(JASPICLogManager.LOGGER, JASPICLogManager.BUNDLE);

    private static final String CONTEXT_REGISTRATION_ID = "org.glassfish.security.message.registrationId";

    private static final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    public static final Lock rLock = rwLock.readLock();
    public static final Lock wLock = rwLock.writeLock();

    private static Map<String, AuthConfigProvider> id2ProviderMap;
    private static Map<String, RegistrationContext> id2RegisContextMap;
    private static Map<String, List<RegistrationListener>> id2RegisListenersMap;
    private static Map<AuthConfigProvider, List<String>> provider2IdsMap;

    protected static final String CONF_FILE_NAME = "auth.conf";

    abstract protected RegStoreFileParser getRegStore();

    /**
     * Get a registered AuthConfigProvider from the factory.
     *
     * Get the provider of ServerAuthConfig and/or
     * ClientAuthConfig objects registered for the identified message
     * layer and application context.
     *
     * @param layer a String identifying the message layer
     *        for which the registered AuthConfigProvider is
     *          to be returned. This argument may be null.
     *
     * @param appContext a String that identifies the application messaging
     *          context for which the registered AuthConfigProvider
     *          is to be returned. This argument may be null.
     *
     * @param listener the RegistrationListener whose
     *          <code>notify</code> method is to be invoked
     *          if the corresponding registration is unregistered or
     *          replaced. The value of this argument may be null.
     *
     * @return the implementation of the AuthConfigProvider interface
     *          registered at the factory for the layer and appContext
     *          or null if no AuthConfigProvider is selected.
     *
     * <p>All factories shall employ the following precedence rules to select
     * the registered AuthConfigProvider that matches (via matchConstructors) the
     * layer and appContext arguments:
     *<ul>
     * <li> The provider that is specifically registered for both the
     * corresponding message layer and appContext
     * shall be selected.
     * <li> if no provider is selected according to the preceding rule,
     * the provider specifically registered for the
     * corresponding appContext and for all message layers
     * shall be selected.
     * <li> if no provider is selected according to the preceding rules,
     * the provider specifically registered for the
     * corresponding message layer and for all appContexts
     * shall be selected.
     * <li> if no provider is selected according to the preceding rules,
     * the provider registered for all message layers and for all
     * appContexts shall be selected.
     * <li> if no provider is selected according to the preceding rules,
     * the factory shall terminate its search for a registered provider.
     *</ul>
     */
    @Override
    public AuthConfigProvider getConfigProvider(String layer, String appContext, RegistrationListener listener) {
        AuthConfigProvider provider = null;
        if (listener == null) {
            rLock.lock();
            try {
                provider = getConfigProviderUnderLock(layer,appContext,null);
            } finally {
                rLock.unlock();
            }
        } else {
            wLock.lock();
            try {
                provider = getConfigProviderUnderLock(layer,appContext,listener);
            } finally {
                wLock.unlock();
            }
        }
        return provider;
    }

    /**
     * Registers within the factory, a provider
     * of ServerAuthConfig and/or ClientAuthConfig objects for a
     * message layer and application context identifier.
     *
     * <P>At most one registration may exist within the factory for a
     * given combination of message layer
     * and appContext. Any pre-existing
     * registration with identical values for layer and appContext is replaced
     * by a subsequent registration. When replacement occurs, the registration
     * identifier, layer, and appContext identifier remain unchanged,
     * and the AuthConfigProvider (with initialization properties) and
     * description are replaced.
     *
     *<p>Within the lifetime of its Java process, a factory must assign unique
     * registration identifiers to registrations, and must never
     * assign a previously used registration identifier to a registration
     * whose message layer and or appContext identifier differ from
     * the previous use.
     *
     * <p>Programmatic registrations performed via this method must update
     * (according to the replacement rules described above), the persistent
     * declarative representation of provider registrations employed by the
     * factory constructor.
     *
     * @param className the fully qualified name of an AuthConfigProvider
     *          implementation class. This argument must not be null.
     *
     * @param properties a Map object containing the initialization
     *          properties to be passed to the provider constructor.
     *          This argument may be null. When this argument is not null,
     *          all the values and keys occuring in the Map must be of
     *          type String.
     *
     * @param layer a String identifying the message layer
     *        for which the provider will be registered at the factory.
     *          A null value may be passed as an argument for this parameter,
     *          in which case, the provider is registered at all layers.
     *
     * @param appContext a String value that may be used by a runtime
     *          to request a configuration object from this provider.
     *          A null value may be passed as an argument for this parameter,
     *          in which case, the provider is registered for all
     *          configuration ids (at the indicated layers).
     *
     * @param description a text String describing the provider.
     *          this value may be null.
     *
     * @return a String identifier assigned by
     *          the factory to the provider registration, and that may be
     *          used to remove the registration from the provider.
     *
     * @exception SecurityException if the caller does not have
     *        permission to register a provider at the factory.
     */
    @Override
    public String registerConfigProvider(String className, Map properties, String layer, String appContext,
        String description) {
        //XXX factory must check permission
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthConfigFactory.providerRegistrationSecurityPermission);
        }
        //XXX do we need doPrivilege here
        AuthConfigProvider provider = _constructProvider(className, properties, null);
        return _register(provider, properties, layer, appContext, description, true);
    }

    @Override
    public String registerConfigProvider(AuthConfigProvider provider,
            String layer, String appContext, String description) {
        //XXX factory must check permission
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthConfigFactory.providerRegistrationSecurityPermission);
        }
        return _register(provider, null, layer, appContext, description, false);
    }

    /**
     * Remove the identified provider registration from the factory
     * and invoke any listeners associated with the removed registration.
     *
     * @param registrationID a String that identifies a provider registration
     *          at the factory
     *
     * @return true if there was a registration with the specified identifier
     *          and it was removed. Return false if the registraionID was
     *          invalid.
     *
     * @exception SecurityException if the caller does not have
     *        permission to unregister the provider at the factory.
     *
     */
    @Override
    public boolean removeRegistration(String registrationID) {
        //XXX factory must check permission
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthConfigFactory.providerRegistrationSecurityPermission);
        }
        return _unRegister(registrationID);
    }

    /**
     * Disassociate the listener from all the provider
     * registrations whose layer and appContext values are matched
     * by the corresponding arguments to this method.
     *
     * @param listener the RegistrationListener to be detached.
     *
     * @param layer a String identifying the message layer or null.
     *
     * @param appContext a String value identifying the application context
     *          or null.
     *
     * @return an array of String values where each value identifies a
     *          provider registration from which the listener was removed.
     *          This method never returns null; it returns an empty array if
     *          the listener was not removed from any registrations.
     *
     * @exception SecurityException if the caller does not have
     *        permission to detach the listener from the factory.
     *
     */
    @Override
    public String[] detachListener(RegistrationListener listener, String layer, String appContext) {
        //XXX factory must check permission
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthConfigFactory.providerRegistrationSecurityPermission);
        }
        ArrayList<String> list = new ArrayList<>();
        String regisID = getRegistrationID(layer, appContext);
        wLock.lock();
        try {
            Set<String> targets = id2RegisListenersMap.keySet();
            for (String targetID : targets) {
                if (regIdImplies(regisID, targetID)) {
                    List<RegistrationListener> listeners =
                            id2RegisListenersMap.get(targetID);
                    if (listeners != null && listeners.remove(listener)) {
                        list.add(targetID);
                    }
                }
            }
        } finally {
            wLock.unlock();
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Get the registration identifiers for all registrations of the
     * provider instance at the factory.
     *
     * @param provider the AuthConfigurationProvider whose registration
     *          identifiers are to be returned. This argument may be
     *          null, in which case, it indicates that the the id's of
     *          all active registration within the factory are returned.
     *
     * @return an array of String values where each value identifies a
     * provider registration at the factory. This method never returns null;
     * it returns an empty array when their are no registrations at the
     * factory for the identified provider.
     */
    @Override
    public String[] getRegistrationIDs(AuthConfigProvider provider) {
        rLock.lock();
        try {
            Collection<String> regisIDs = null;
            if (provider != null) {
                regisIDs = provider2IdsMap.get(provider);
            } else {
                Collection<List<String>> collList = provider2IdsMap.values();
                if (collList != null) {
                    regisIDs = new HashSet<>();
                    for (List<String> listIds : collList) {
                         if (listIds != null) {
                             regisIDs.addAll(listIds);
                         }
                    }
                }
            }
            return ((regisIDs != null) ? regisIDs.toArray(new String[regisIDs.size()]) : new String[0]);
        } finally {
            rLock.unlock();
        }
    }

    /**
     * Get the the registration context for the identified registration.
     *
     * @param registrationID a String that identifies a provider registration
     *          at the factory
     *
     * @return a RegistrationContext or null. When a Non-null value is
     * returned, it is a copy of the registration context corresponding to the
     * registration. Null is returned when the registration identifier does
     * not correspond to an active registration
     */
    @Override
    public RegistrationContext getRegistrationContext(String registrationID) {
        rLock.lock();
        try {
            return id2RegisContextMap.get(registrationID);
        } finally {
            rLock.unlock();
        }
    }

   /**
     * Cause the factory to reprocess its persistent declarative
     * representation of provider registrations.
     *
     * <p> A factory should only replace an existing registration when
     * a change of provider implementation class or initialization
     * properties has occurred.
     *
     * @exception SecurityException if the caller does not have permission
     *        to refresh the factory.
     */
    @Override
    public void refresh() {
        //XXX factory must check permission
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthConfigFactory.providerRegistrationSecurityPermission);
        }
        Map<String, List<RegistrationListener>> preExistingListenersMap;
        wLock.lock();
        try {
            preExistingListenersMap = id2RegisListenersMap;
            _loadFactory();
        } finally {
            wLock.unlock();
        }

        // notify pre-existing listeners after (re)loading factory
        if (preExistingListenersMap != null) {
            notifyListeners(preExistingListenersMap);
        }
    }

    /**
     * Gets the app context ID from the servlet context.
     *
     * <p>
     * The app context ID is the ID that Jakarta Authentication associates with the given application.
     * In this case that given application is the web application corresponding to the
     * ServletContext.
     *
     * @param context the servlet context for which to obtain the Jakarta Authentication app context ID
     * @return the app context ID for the web application corresponding to the given context
     */
    public static String getAppContextID(ServletContext context) {
        return context.getVirtualServerName() + " " + context.getContextPath();
    }

    @Override
    public String registerServerAuthModule(ServerAuthModule serverAuthModule, Object context) {
        if (!(context instanceof ServletContext)) {
            return null;
        }

        ServletContext servletContext = (ServletContext) context;

        // Register the factory-factory-factory for the SAM
        String registrationId = AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return registerConfigProvider(
                        new DefaultAuthConfigProvider(serverAuthModule),
                        "HttpServlet",
                        getAppContextID(servletContext),
                        "Default single SAM authentication config provider");
            }
        });

        // Remember the registration ID returned by the factory, so we can unregister the JASPIC module when the web module
        // is undeployed. JASPIC being the low level API that it is won't do this automatically.
        servletContext.setAttribute(CONTEXT_REGISTRATION_ID, registrationId);

        return registrationId;
    }

    @Override
    public void removeServerAuthModule(Object context) {
        if (!(context instanceof ServletContext)) {
            return;
        }

        ServletContext servletContext = (ServletContext) context;

        String registrationId = (String) servletContext.getAttribute(CONTEXT_REGISTRATION_ID);
        if (!isEmpty(registrationId)) {
            AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return removeRegistration(registrationId);
                }
            });
        }
    }

    private static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    private AuthConfigProvider getConfigProviderUnderLock(String layer, String appContext,
        RegistrationListener listener) {
        AuthConfigProvider provider = null;
        String regisID = getRegistrationID(layer, appContext);
        String matchedID = null;
        boolean providerFound = false;
        if (id2ProviderMap.containsKey(regisID)) {
            provider = id2ProviderMap.get(regisID);
            providerFound = true;
        }
        if (!providerFound) {
            matchedID = getRegistrationID(null, appContext);
            if (id2ProviderMap.containsKey(matchedID)) {
                provider = id2ProviderMap.get(matchedID);
                providerFound = true;
            }
        }
        if (!providerFound) {
            matchedID = getRegistrationID(layer, null);
            if (id2ProviderMap.containsKey(matchedID)) {
                provider = id2ProviderMap.get(matchedID);
                providerFound = true;
            }
        }
        if (!providerFound) {
            matchedID = getRegistrationID(null, null);
            if (id2ProviderMap.containsKey(matchedID)) {
                provider = id2ProviderMap.get(matchedID);
            }
        }
        if (listener != null) {
            List<RegistrationListener> listeners = id2RegisListenersMap.get(regisID);
            if (listeners == null) {
                listeners = new ArrayList<>();
                id2RegisListenersMap.put(regisID, listeners);
            }
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }

        return provider;
    }


    private static String getRegistrationID(String layer, String appContext) {
        String regisID = null;

        // __0                          (null, null)
        // __1<appContext>              (null, appContext)
        // __2<layer>                   (layer, null)
        // __3<nn>_<layer><appContext>  (layer, appContext)

        if (layer != null) {
            regisID = (appContext != null) ?
                "__3" + layer.length() + "_" + layer + appContext :
                "__2" + layer;
        } else {
            regisID = (appContext != null) ?
                "__1" + appContext :
                "__0";
        }
        return regisID;
    }

    /**
     * This API decomposes the given regisID into layer and appContext.
     * @param regisID
     * @return a String array with layer and appContext
     */
    private static String[] decomposeRegisID(String regisID) {
        String layer = null;
        String appContext = null;
        if (regisID.equals("__0")) {
            // null, null
        } else if (regisID.startsWith("__1")) {
            appContext = (regisID.length() == 3)?
                   "" : regisID.substring(3);
        } else if (regisID.startsWith("__2")) {
            layer = (regisID.length() == 3)?
                   "" : regisID.substring(3);
        } else if (regisID.startsWith("__3")) {
            int ind = regisID.indexOf('_', 3);
            if (regisID.length() > 3 && ind > 0) {
                String numberString = regisID.substring(3, ind);
                int n;
                try {
                    n = Integer.parseInt(numberString);
                } catch (Exception ex) {
                    throw new IllegalArgumentException();
                }
                layer = regisID.substring(ind + 1, ind + 1 + n);
                appContext = regisID.substring(ind + 1 + n);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }

        return new String[]{layer, appContext};
    }

    private static AuthConfigProvider _constructProvider(String className, Map properties, AuthConfigFactory factory) {
        //XXX do we need doPrivilege here
        AuthConfigProvider provider = null;
        if (className != null) {
            try {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Class c = Class.forName(className, true, loader);
                Constructor<AuthConfigProvider> constr = c.getConstructor(Map.class, AuthConfigFactory.class);
                provider = constr.newInstance(new Object[] {properties, factory});
            } catch (Throwable t) {
                LOG.log(Level.WARNING, JASPICLogManager.MSG_UNABLE_LOAD_PROVIDER + className , t);
            }
        }
        return provider;
    }

    //XXX need to update persistent state and notify effected listeners
    private String _register(AuthConfigProvider provider,
            Map<String, Object> properties,
            String layer,
            String appContext,
            String description,
            boolean persistent) {

        String regisID = getRegistrationID(layer, appContext);
        RegistrationContext rc =
                new RegistrationContextImpl(layer, appContext, description, persistent);
        RegistrationContext prevRegisContext = null;
        Map<String, List<RegistrationListener>> listenerMap;
        wLock.lock();
        try {
            prevRegisContext = id2RegisContextMap.get(regisID);
            AuthConfigProvider prevProvider = id2ProviderMap.get(regisID);

            // handle the persistence first - so that any exceptions occur before
            // the actual registration happens
            if (persistent) {
                _storeRegistration(regisID, rc, provider, properties);
            } else if (prevRegisContext != null && prevRegisContext.isPersistent()) {
                _deleteStoredRegistration(regisID, prevRegisContext);
            }

            boolean wasRegistered = id2ProviderMap.containsKey(regisID);

            if (wasRegistered) {
                List<String> prevRegisIDs = provider2IdsMap.get(prevProvider);
                prevRegisIDs.remove(regisID);
                if (prevRegisIDs.isEmpty()) {
                    provider2IdsMap.remove(prevProvider);
                }
            }

            id2ProviderMap.put(regisID, provider);
            id2RegisContextMap.put(regisID, rc);

            List<String> regisIDs = provider2IdsMap.get(provider);
            if (regisIDs == null) {
                regisIDs = new ArrayList<>();
                provider2IdsMap.put(provider, regisIDs);
            }

            if (!regisIDs.contains(regisID)) {
                regisIDs.add(regisID);
            }

            listenerMap = getEffectedListeners(regisID);

        } finally {
            wLock.unlock();
        }

        // outside wLock to prevent dead lock
        notifyListeners(listenerMap);

        return regisID;
    }

    //XXX need to update persistent state and notify effected listeners
    private boolean _unRegister(String regisID) {
        boolean rvalue = false;
        RegistrationContext rc = null;
        Map<String, List<RegistrationListener>> listenerMap;
        wLock.lock();
        try {
            rc = id2RegisContextMap.remove(regisID);
            rvalue = id2ProviderMap.containsKey(regisID);
            AuthConfigProvider provider = id2ProviderMap.remove(regisID);
            List<String> regisIDs = provider2IdsMap.get(provider);
            if (regisIDs != null) {
                regisIDs.remove(regisID);
            }
            if (regisIDs == null || regisIDs.isEmpty()) {
                provider2IdsMap.remove(provider);
            }
            if (!rvalue) {
                return false;
            }
            listenerMap = getEffectedListeners(regisID);
            if (rc != null && rc.isPersistent()) {
                    _deleteStoredRegistration(regisID, rc);
            }
        } finally {
            wLock.unlock();
        }

        // outside wLock to prevent dead lock
        notifyListeners(listenerMap);
        return rvalue;
    }

// the following methods implement the factory's persistence layer
    protected void _loadFactory() {
        try {
            initializeMaps();

            List<EntryInfo> entryList = getRegStore().getPersistedEntries();

            for (EntryInfo info : entryList) {
                if (info.isConstructorEntry()) {
                    _constructProvider(info.getClassName(), info.getProperties(), this);
                } else {
                    boolean first = true;
                    AuthConfigProvider p = null;
                    List<RegistrationContext> contexts = (info.getRegContexts());
                    for (RegistrationContext ctx : contexts) {
                        if (first) {
                            p = _constructProvider(info.getClassName(), info.getProperties(), null);
                        }
                        _loadRegistration(p, ctx.getMessageLayer(), ctx.getAppContext(), ctx.getDescription());
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, JASPICLogManager.MSG_LOADER_FAILURE, e);
        }
    }

    /**
     * Initialize the static maps in a static method
     */
    private static void initializeMaps() {
        id2ProviderMap = new HashMap<>();
        id2RegisContextMap = new HashMap<>();
        id2RegisListenersMap = new HashMap<>();
        provider2IdsMap = new HashMap<>();
    }

    private static String _loadRegistration(AuthConfigProvider provider,
            String layer,
            String appContext,
            String description) {

        RegistrationContext rc = new RegistrationContextImpl(layer, appContext, description, true);
        String regisID = getRegistrationID(layer, appContext);
        id2RegisContextMap.get(regisID);
        AuthConfigProvider prevProvider = id2ProviderMap.get(regisID);
        boolean wasRegistered = id2ProviderMap.containsKey(regisID);
        if (wasRegistered) {
            List<String> prevRegisIDs = provider2IdsMap.get(prevProvider);
            prevRegisIDs.remove(regisID);
            if (prevRegisIDs.isEmpty()) {
                provider2IdsMap.remove(prevProvider);
            }
        }

        id2ProviderMap.put(regisID, provider);
        id2RegisContextMap.put(regisID, rc);

        List<String> regisIDs = provider2IdsMap.get(provider);
        if (regisIDs == null) {
            regisIDs = new ArrayList<>();
            provider2IdsMap.put(provider, regisIDs);
        }

        if (!regisIDs.contains(regisID)) {
            regisIDs.add(regisID);
        }

        return regisID;
    }


    private void _storeRegistration(String regId, RegistrationContext ctx, AuthConfigProvider p, Map properties) {

        String className = null;
        if (p != null) {
            className = p.getClass().getName();
        }
        if (propertiesContainAnyNonStringValues(properties)) {
            throw new IllegalArgumentException("AuthConfigProvider cannot be registered - properties must all be of type String.");
        }
        if (ctx.isPersistent()) {
            getRegStore().store(className, ctx, properties);
        }
    }


    private boolean propertiesContainAnyNonStringValues(Map<String,Object> props) {
        if (props != null) {
            for(Map.Entry<String, Object> entry : props.entrySet()) {
                if (!(entry.getValue() instanceof String)) {
                    return true;
                }
            }
        }
        return false;
    }


    private void _deleteStoredRegistration(String regId, RegistrationContext ctx) {
        if (ctx.isPersistent()) {
            getRegStore().delete(ctx);
        }
    }

    private static boolean regIdImplies(String reference, String target) {

        boolean rvalue = true;

        String[] refID = decomposeRegisID(reference);
        String[] targetID = decomposeRegisID(target);

        if (refID[0] != null && !refID[0].equals(targetID[0])) {
            rvalue = false;
        } else if (refID[1] != null && !refID[1].equals(targetID[1])) {
            rvalue = false;
        }
        return rvalue;
    }

    /* will return some extra listeners. iow, effected listeners could be reduced
     * by removing any associated with a provider registration id that is
     * more specific than the one being added or removed.l
     */
    private static Map<String, List<RegistrationListener>> getEffectedListeners(String regisID) {
        Map<String, List<RegistrationListener>> effectedListeners = new HashMap<>();
        Set<String> listenerRegistrations = new HashSet<>(id2RegisListenersMap.keySet());

        for (String listenerID : listenerRegistrations) {
            if (regIdImplies(regisID, listenerID)) {
                if (!effectedListeners.containsKey(listenerID)) {
                    effectedListeners.put(listenerID, new ArrayList<>());
                }
                effectedListeners.get(listenerID).addAll(id2RegisListenersMap.remove(listenerID));
            }
        }
        return effectedListeners;
    }

    private static void notifyListeners(Map<String, List<RegistrationListener>> map) {
        Set<Map.Entry<String, List<RegistrationListener>>> entrySet = map.entrySet();
        for (Map.Entry<String, List<RegistrationListener>> entry : entrySet) {
            List<RegistrationListener> listeners = map.get(entry.getKey());

            if (listeners != null && listeners.size() > 0) {
                String[] dIds = decomposeRegisID(entry.getKey());

                for (RegistrationListener listener : listeners) {
                    listener.notify(dIds[0], dIds[1]);
                }
            }
        }
    }
}
