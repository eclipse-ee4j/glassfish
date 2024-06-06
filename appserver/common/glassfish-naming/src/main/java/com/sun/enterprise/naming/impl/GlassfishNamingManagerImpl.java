/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2024 Payara Foundation and/or its affiliates
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

package com.sun.enterprise.naming.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.Serializable;
import java.lang.System.Logger;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.NamingManager;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.JNDIBinding;
import org.glassfish.api.naming.NamingObjectProxy;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.omg.CORBA.ORB;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.Objects.requireNonNull;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP_NS_ID;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT_NS_ID;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_MODULE;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_MODULE_NS_ID;


/**
 * This is the manager that handles all naming operations including
 * publishObject as well as binding environment props, resource and ejb
 * references in the namespace.
 */
@Service
@Singleton
public final class GlassfishNamingManagerImpl implements GlassfishNamingManager {

    public static final String IIOPOBJECT_FACTORY = "com.sun.enterprise.naming.util.IIOPObjectFactory";
    private static final Logger LOG = System.getLogger(GlassfishNamingManagerImpl.class.getName());

    @Inject
    private ServiceLocator serviceLocator;

    private final InitialContext initialContext;

    private final NameParser nameParser = new SerialNameParser();

    private final NamespacesMap<String> componentNamespaces;
    private final NamespacesMap<String> appNamespaces;
    private final NamespacesMap<AppModuleKey> moduleNamespaces;
    private final Map<String, ComponentIdInfo> componentIdInfo;

    private InvocationManager invMgr;

    // FIXME: cosContext has nothing to do with the rest of the class. It could be pushed to own class.
    private Context cosContext;

    /**
     * Create the naming manager. Creates a new initial context.
     *
     * @throws NamingException if the creation of the {@link InitialContext} instance failed.
     */
    public GlassfishNamingManagerImpl() throws NamingException {
        this(new InitialContext());
    }


    /**
     * Create the naming manager.
     */
    public GlassfishNamingManagerImpl(InitialContext initialContext) {
        this.initialContext = initialContext;
        this.componentNamespaces = new NamespacesMap<>(SimpleJndiName.JNDI_CTX_JAVA_COMPONENT);
        this.appNamespaces = new NamespacesMap<>(JNDI_CTX_JAVA_APP);
        this.moduleNamespaces = new NamespacesMap<>(SimpleJndiName.JNDI_CTX_JAVA_MODULE);
        this.componentIdInfo = new HashMap<>();
        JavaURLContext.setNamingManager(this);
    }


    // Used only for unit testing
    void setInvocationManager(final InvocationManager invMgr) {
        this.invMgr = invMgr;
    }


    /**
     * Get the initial naming context.
     */
    @Override
    public Context getInitialContext() {
        return initialContext;
    }


    public NameParser getNameParser() {
        return nameParser;
    }


    @Override
    public Remote initializeRemoteNamingSupport(ORB orb) throws NamingException {
        Remote remoteProvider;
        try {
            // Now that we have an ORB, initialize the CosNaming service
            // and set it on the server's naming service.
            Hashtable<Object, Object> cosNamingEnv = new Hashtable<>();
            cosNamingEnv.put("java.naming.factory.initial", "org.glassfish.jndi.cosnaming.CNCtxFactory");
            cosNamingEnv.put("java.naming.corba.orb", orb);
            cosContext = new InitialContext(cosNamingEnv);
            ProviderManager pm = ProviderManager.getProviderManager();

            // Initialize RemoteSerialProvider.  This allows access to the naming
            // service from clients.
            remoteProvider = pm.initRemoteProvider(orb);
        } catch(RemoteException re) {
            NamingException ne = new NamingException("Exception during remote naming initialization");
            ne.initCause(ne);
            throw ne;
        }

        return remoteProvider;
    }


    private Context getCosContext() {
        return requireNonNull(cosContext, "cosContext was not initialized by initializeRemoteNamingSupport method");
    }


    @Override
    public void publishObject(SimpleJndiName name, Object obj, boolean rebind) throws NamingException {
        publishObject(name.toName(), obj, rebind);
    }


    @Override
    public void publishObject(Name name, Object obj, boolean rebind) throws NamingException {
        LOG.log(DEBUG, "publishObject(name={0}, obj={1}, rebind={2})", name, obj, rebind);
        if (rebind) {
            initialContext.rebind(name, obj);
        } else {
            initialContext.bind(name, obj);
        }
    }


    @Override
    public void publishCosNamingObject(SimpleJndiName name, Object obj, boolean rebind) throws NamingException {
        LOG.log(DEBUG, "publishCosNamingObject(name={0}, obj={1}, rebind={2})", name, obj, rebind);
        Name nameObj = name.toName();

        // Create any COS naming sub-contexts in name
        // that don't already exist.
        createSubContexts(nameObj, getCosContext());

        if (rebind) {
            getCosContext().rebind(nameObj, obj);
        } else {
            getCosContext().bind(nameObj, obj);
        }

        // Bind a reference to it in the SerialContext using
        // the same name. This is needed to allow standalone clients
        // to lookup the object using the same JNDI name.
        // It is also used from bindObjects while populating ejb-refs in
        // the java:comp namespace.
        StringRefAddr addr = new StringRefAddr("url", name.toString());
        Object serialObj = new Reference("reference", addr, IIOPOBJECT_FACTORY, null);
        publishObject(name, serialObj, rebind);

    }


    @Override
    public void unpublishObject(Name name) throws NamingException {
        LOG.log(DEBUG, "unpublishObject(name={0})", name);
        initialContext.unbind(name);
    }


    @Override
    public void unpublishObject(SimpleJndiName name) throws NamingException {
        LOG.log(DEBUG, "unpublishObject(name={0})", name);
        initialContext.unbind(name.toName());
    }


    @Override
    public void unpublishCosNamingObject(SimpleJndiName name) throws NamingException {
        LOG.log(DEBUG, "unpublishCosNamingObject(name={0})", name);
        try {
            getCosContext().unbind(name.toName());
        } catch (NamingException cne) {
            LOG.log(WARNING, "Error during CosNaming.unbind for name: " + name, cne);
        }
        initialContext.unbind(name.toString());
    }


    /**
     * Create any sub-contexts in name that don't already exist.
     *
     * @param name Name containing sub-contexts to create
     * @param rootCtx in which sub-contexts should be created
     * @throws Exception
     */
    private void createSubContexts(Name name, Context rootCtx) throws NamingException {
        int numSubContexts = name.size() - 1;
        Context currentCtx = rootCtx;

        for (int subCtxIndex = 0; subCtxIndex < numSubContexts; subCtxIndex++) {
            String subCtxName = name.get(subCtxIndex);
            try {
                final Object obj = currentCtx.lookup(subCtxName);
                if (obj == null) {
                    // Doesn't exist so create it.
                    currentCtx = currentCtx.createSubcontext(subCtxName);
                } else if (obj instanceof Context) {
                    // OK -- no need to create it.
                    currentCtx = (Context) obj;
                } else {
                    // Context name clashes with existing object.
                    throw new NameAlreadyBoundException(subCtxName);
                }
            } catch (NameNotFoundException e) {
                // Doesn't exist so create it.
                currentCtx = currentCtx.createSubcontext(subCtxName);
            }
        }
    }


    private JavaNamespace getComponentNamespace(String componentId) {
        // Note: HashMap is not synchronized. The namespace is populated
        // at deployment time by a single thread, and then on there are
        // no structural modifications (i.e. no keys added/removed).
        // So the namespace doesnt need to be synchronized.
        JavaNamespace namespace = componentNamespaces.get(componentId);
        if (namespace == null) {
            namespace = new JavaNamespace(componentId, "comp");
            componentNamespaces.put(componentId, namespace);
        }

        return namespace;
    }


    private JavaNamespace getModuleNamespace(AppModuleKey appModuleKey) throws NamingException {
        if (appModuleKey.getAppName() == null || appModuleKey.getModuleName() == null) {
            throw new NamingException("Invalid appModuleKey " + appModuleKey);
        }

        // Note: HashMap is not synchronized. The namespace is populated
        // at deployment time by a single thread, and then on there are
        // no structural modifications (i.e. no keys added/removed).
        // So the namespace doesnt need to be synchronized.
        JavaNamespace namespace = moduleNamespaces.get(appModuleKey);
        if (namespace == null) {
            namespace = new JavaNamespace(appModuleKey.toString(), "module");
            moduleNamespaces.put(appModuleKey, namespace);
        }

        return namespace;
    }

    @Override
    public <T> T lookupFromAppNamespace(String appName, SimpleJndiName name, Hashtable<?, ?> env) throws NamingException {
        LOG.log(TRACE, "lookupFromAppNamespace(appName={0}, name={1}, env)", appName, name);
        Map<SimpleJndiName, Object> namespace = getAppNamespace(appName);
        return lookupFromNamespace(name, namespace, env);
    }

    @Override
    public <T> T lookupFromModuleNamespace(String appName, String moduleName, SimpleJndiName name, Hashtable<?, ?> env)
        throws NamingException {
        LOG.log(TRACE, "lookupFromModuleNamespace(appName={0}, moduleName={1}, name={2}, env)", appName, moduleName,
            name);
        AppModuleKey appModuleKey = new AppModuleKey(appName, moduleName);
        Map<SimpleJndiName, Object> namespace = getModuleNamespace(appModuleKey);
        return lookupFromNamespace(name, namespace, env);
    }


    private <T> T getObjectInstance(SimpleJndiName name, Object obj, Hashtable<?, ?> env) throws Exception {
        LOG.log(DEBUG, "getObjectInstance(name={0}, obj, env={1})", name, env);
        if (env == null) {
            env = new Hashtable<>();
        }
        return (T) NamingManager.getObjectInstance(obj, name.toName(), null, env);
    }


    private JavaNamespace getAppNamespace(String appName) throws NamingException {
        LOG.log(TRACE, "getAppNamespace(appName={0})", appName);
        if (appName == null) {
            throw new NamingException("Null appName");
        }

        // Note: HashMap is not synchronized. The namespace is populated
        // at deployment time by a single thread, and then on there are
        // no structural modifications (i.e. no keys added/removed).
        // So the namespace doesnt need to be synchronized.
        JavaNamespace namespace = appNamespaces.get(appName);
        if (namespace == null) {
            namespace = new JavaNamespace(appName, "app");
            appNamespaces.put(appName, namespace);
        }

        return namespace;
    }


    private JavaNamespace getNamespace(SimpleJndiName logicalJndiName) throws NamingException {
        LOG.log(TRACE, "getNamespace(info, logicalJndiName={0})", logicalJndiName);
        final ComponentInvocation invocation = getComponentInvocation();
        if (logicalJndiName.isJavaModule()) {
            return getModuleNamespace(new AppModuleKey(invocation.getAppName(), invocation.getModuleName()));
        } else if (logicalJndiName.isJavaApp()) {
            return getAppNamespace(invocation.getAppName());
        } else {
            return getComponentNamespace(invocation.getComponentId());
        }
    }


    private JavaNamespace getNamespace(ComponentIdInfo info, SimpleJndiName logicalJndiName) throws NamingException {
        LOG.log(TRACE, "getNamespace(info, logicalJndiName={0})", logicalJndiName);
        if (logicalJndiName.isJavaModule()) {
            return getModuleNamespace(new AppModuleKey(info.appName, info.moduleName));
        } else if (logicalJndiName.isJavaApp()) {
            return getAppNamespace(info.appName);
        } else {
            return getComponentNamespace(info.componentId);
        }
    }


    /**
     * This method binds them in a java:namespace.
     */
    private void bindToNamespace(JavaNamespace namespace, SimpleJndiName jndiName, Object value,
        boolean force) throws NamingException {
        LOG.log(DEBUG, "bindToNamespace(namespace.name={0}, jndiName={1}, value={2}, force={3})", namespace.name,
            jndiName, value, force);

        if (force) {
            if (namespace.put(jndiName, value) != null) {
                LOG.log(WARNING, "Replaced existing binding for ''{0}'' in namespace ''{1}''", jndiName, namespace.name);
            }
        } else if (namespace.putIfAbsent(jndiName, value) != null) {
            LOG.log(TRACE, "The namespace already contains binding for ''{0}'' in namespace ''{1}'', ignoring request.",
                jndiName, namespace.name);
            return;
        }
        bindIntermediateContexts(namespace, jndiName);
    }


    @Override
    public void bindToComponentNamespace(String appName, String moduleName, String componentId,
        boolean treatComponentAsModule, Collection<? extends JNDIBinding> bindings) throws NamingException {
        LOG.log(DEBUG,
            "bindToComponentNamespace(appName={0}, moduleName={1}, componentId={2}, treatComponentAsModule={3}, bindings={4})",
            appName, moduleName, componentId, treatComponentAsModule, bindings);

        // These are null in rare cases, e.g. default web app.
        if (appName != null && moduleName != null) {
            ComponentIdInfo info = new ComponentIdInfo();
            info.appName = appName;
            info.moduleName = moduleName;
            info.componentId = componentId;
            info.treatComponentAsModule = treatComponentAsModule;
            componentIdInfo.put(componentId, info);
        }

        for (JNDIBinding binding : bindings) {
            final SimpleJndiName logicalJndiName;
            if (treatComponentAsModule && binding.getName().isJavaComponent()) {
                logicalJndiName = binding.getName().changePrefix(JNDI_CTX_JAVA_MODULE);
            } else {
                logicalJndiName = binding.getName();
            }

            final JavaNamespace namespace;
            if (logicalJndiName.isJavaComponent()) {
                namespace = getComponentNamespace(componentId);
            } else if (logicalJndiName.isJavaModule()) {
                namespace = getModuleNamespace(new AppModuleKey(appName, moduleName));
            } else if (logicalJndiName.isJavaApp()) {
                namespace = getAppNamespace(appName);
            } else {
                namespace = null;
            }

            if (namespace == null) {
                LOG.log(WARNING, "No namespace found for appName={0}, moduleName={1}, componentId={2}", appName,
                    moduleName, componentId);
                return;
            }
            bindToNamespace(namespace, logicalJndiName, binding.getValue(), false);
        }
    }


    @Override
    public void bindToModuleNamespace(String appName, String moduleName, Collection<? extends JNDIBinding> bindings)
        throws NamingException {
        LOG.log(TRACE, "bindToModuleNamespace(appName={0}, moduleName={1}, bindings={2})", appName, moduleName,
            bindings);
        AppModuleKey appModuleKey = new AppModuleKey(appName, moduleName);
        JavaNamespace namespace = getModuleNamespace(appModuleKey);
        for (JNDIBinding binding : bindings) {
            SimpleJndiName logicalJndiName = binding.getName();
            if (logicalJndiName.isJavaModule()) {
                bindToNamespace(namespace, logicalJndiName, binding.getValue(), true);
            }
        }
    }


    @Override
    public void bindToAppNamespace(String appName, Collection<? extends JNDIBinding> bindings) throws NamingException {
        LOG.log(TRACE, "bindToAppNamespace(appName={0}, bindings={1})", appName, bindings);
        JavaNamespace namespace = getAppNamespace(appName);
        for (JNDIBinding binding : bindings) {
            SimpleJndiName logicalJndiName = binding.getName();
            if (logicalJndiName.isJavaApp()) {
                bindToNamespace(namespace, logicalJndiName, binding.getValue(), true);
            }
        }
    }


    private void bindIntermediateContexts(JavaNamespace namespace, SimpleJndiName jndiName)
        throws NamingException {
        LOG.log(TRACE, "bindIntermediateContexts(namespace.name={0}, jndiName={1})", namespace.name, jndiName);
        // for each component of name, put an entry into namespace
        String partialName;
        if (jndiName.isJavaComponent()) {
            partialName = JNDI_CTX_JAVA_COMPONENT_NS_ID;
        } else if (jndiName.isJavaModule()) {
            partialName = JNDI_CTX_JAVA_MODULE_NS_ID;
        } else if (jndiName.isJavaApp()) {
            partialName = JNDI_CTX_JAVA_APP_NS_ID;
        } else {
            throw new NamingException("Invalid environment namespace name: " + jndiName);
        }

        String name = jndiName.toString().substring(partialName.length() + 1);
        StringTokenizer toks = new StringTokenizer(name, "/", false);
        StringBuilder sb = new StringBuilder();
        sb.append(partialName);
        while (toks.hasMoreTokens()) {
            String tok = toks.nextToken();
            sb.append('/').append(tok);
            final SimpleJndiName nsJndiName = new SimpleJndiName(sb.toString());
            if (namespace.get(nsJndiName) == null) {
                namespace.put(nsJndiName, new JavaURLContext(nsJndiName));
            }
        }
    }


    @Override
    public void unbindComponentObjects(String componentId) throws NamingException {
        LOG.log(DEBUG, "unbindComponentObjects(componentId={0})", componentId);
        // remove local namespace cache
        componentNamespaces.remove(componentId);
        componentIdInfo.remove(componentId);
    }

    @Override
    public void unbindAppObjects(String appName) throws NamingException {
        LOG.log(DEBUG, "unbindAppObjects(appName={0})", appName);
        appNamespaces.remove(appName);
        Iterator<AppModuleKey> keys = moduleNamespaces.keySet().iterator();
        while (keys.hasNext()) {
            AppModuleKey key = keys.next();
            if (key.getAppName().equals(appName)) {
                keys.remove();
            }
        }
    }

    @Override
    public void unbindAppObject(String appName, SimpleJndiName name) throws NamingException {
        LOG.log(DEBUG, "unbindAppObject(appName={0}, name={1})", appName, name);
        Map<SimpleJndiName, Object> namespaces = appNamespaces.get(appName);
        if (namespaces != null) {
            namespaces.remove(name);
        }
    }

    @Override
    public void unbindModuleObject(String appName, String moduleName, SimpleJndiName name) throws NamingException {
        LOG.log(DEBUG, "unbindModuleObject(appName={0}, moduleName={1}, name={2})", appName, moduleName, name);
        AppModuleKey appModuleKey = new AppModuleKey(appName, moduleName);
        Map<SimpleJndiName, Object> namespaces = moduleNamespaces.get(appModuleKey);
        if (namespaces != null) {
            namespaces.remove(name);
        }
    }

    /**
     * Recreate a context for java:comp/env or one of its sub-contexts given the context name.
     */
    @Override
    public Context restoreJavaCompEnvContext(SimpleJndiName contextName) throws NamingException {
        if (contextName.hasJavaPrefix()) {
            return new JavaURLContext(contextName);
        }
        throw new NamingException("Invalid context name [" + contextName + "]. Name must start with java:");
    }


    // FIXME: resolve where to search by the name.
    @Override
    public <T> T lookup(SimpleJndiName name) throws NamingException {
        return (T) initialContext.lookup(name.toName());
    }


// FIXME: Wrong name, added dmatej, chooses where to search in.
    public <T> T lookupFromComponentNamespace(SimpleJndiName name) throws NamingException {
        final String componentId = getComponentId();
        return lookup(componentId, name, initialContext);
    }

    /**
     * This method is called from SerialContext class. The serialContext
     * instance that was created by the appclient's Main class is passed so that
     * stickiness is preserved. Called from javaURLContext.lookup, for java:comp
     * names.
     */
    public <T> T lookup(SimpleJndiName name, SerialContext serialContext) throws NamingException {
        // initialContext is used as ic in case of PE while
        // serialContext is used as ic in case of EE/SE
        // Get the component id and namespace to lookup
        final Context context = serialContext == null ? initialContext : serialContext;
        final String componentId = getComponentId();
        return lookup(componentId, name, context);
    }

    /**
     * Lookup object for a particular componentId and name.
     */
    @Override
    public <T> T lookup(String componentId, SimpleJndiName name) throws NamingException {
        return lookup(componentId, name, initialContext);
    }

    private <T> T lookup(String componentId, SimpleJndiName name, Context ctx) throws NamingException {
        LOG.log(DEBUG, "lookup(componentId={0}, name={1}, ctx={2})", componentId, name, ctx);
        final ComponentIdInfo info = componentIdInfo.get(componentId);
        LOG.log(TRACE, "Found componentIdInfo={0}", info);
        final boolean replaceName;
        final SimpleJndiName lookupName;
        final JavaNamespace namespace;
        if (info == null) {
            replaceName = false;
            lookupName = name;
            namespace = getNamespace(lookupName);
        } else {
            replaceName = info.treatComponentAsModule && name.isJavaComponent();
            lookupName = replaceName ? name.changePrefix(JNDI_CTX_JAVA_MODULE) : name;
            namespace = getNamespace(info, lookupName);
        }
        Object obj = namespace.get(lookupName);
        LOG.log(TRACE, "For {0} found object={1} in namespace.name={2}", lookupName, obj, namespace.name);
        if (obj == null) {
            throw new NameNotFoundException("No object bound to name " + lookupName + " in namespace " + namespace);
        }
        if (obj instanceof NamingObjectProxy) {
            NamingObjectProxy namingProxy = (NamingObjectProxy) obj;
            return namingProxy.create(ctx);
        } else if (obj instanceof Context) {
            // Need to preserve the original prefix so that further operations
            // on the context maintain the correct external view. In the case
            // of a replaced java:comp, create a new equivalent javaURLContext
            // and return that.
            if (replaceName) {
                obj = new JavaURLContext(name);
            }

            if (obj instanceof JavaURLContext) {
                if (ctx instanceof SerialContext) {
                    return (T) new JavaURLContext((JavaURLContext) obj, (SerialContext) ctx);
                }
                return (T) new JavaURLContext((JavaURLContext) obj);
            }
        }
        return (T) obj;
    }


    public NamingEnumeration<NameClassPair> list(SimpleJndiName name) throws NamingException {
        ArrayList<SimpleJndiName> list = listNames(name);
        return new BindingsIterator<>(this, list.iterator(), true);
    }

    public NamingEnumeration<Binding> listBindings(SimpleJndiName name) throws NamingException {
        ArrayList<SimpleJndiName> list = listNames(name);
        return new BindingsIterator<>(this, list.iterator(), false);
    }

    private ArrayList<SimpleJndiName> listNames(SimpleJndiName name) throws NamingException {
        // Get the component id and namespace to lookup
        String componentId = getComponentId();
        ComponentIdInfo info = componentIdInfo.get(componentId);
        boolean replaceName = info != null && info.treatComponentAsModule && name.isJavaComponent();
        final SimpleJndiName logicalJndiName;
        if (replaceName) {
            logicalJndiName = name.changePrefix(JNDI_CTX_JAVA_MODULE);
        } else {
            logicalJndiName = name;
        }

        JavaNamespace namespace = info == null ? getComponentNamespace(componentId) : getNamespace(info, logicalJndiName);
        Object obj = namespace.get(logicalJndiName);
        if (obj == null) {
            throw new NameNotFoundException("No object bound to name " + name + " in namespace " + namespace);
        }

        if (!(obj instanceof JavaURLContext)) {
            throw new NotContextException(name + " cannot be listed");
        }

        // This iterates over all names in entire component namespace,
        // so its a little inefficient. The alternative is to store
        // a list of bindings in each javaURLContext instance.
        ArrayList<SimpleJndiName> list = new ArrayList<>();
        final String logicalNameWithSlash;
        if (logicalJndiName.hasSuffix("/")) {
            logicalNameWithSlash = logicalJndiName.toString();
        } else {
            logicalNameWithSlash = logicalJndiName + "/";
        }
        for (SimpleJndiName key : namespace.keySet()) {
            // Check if key begins with name and has only 1 component extra (i.e. no more slashes)
            // Make sure keys reflect the original prefix in the case of comp->module replacement
            // The search string itself is excluded from the returned list
            if (key.hasPrefix(logicalNameWithSlash) && key.toString().indexOf('/', logicalNameWithSlash.length()) == -1
                && !key.toString().equals(logicalNameWithSlash)) {
                SimpleJndiName toAdd = replaceName ? key.changePrefix(JNDI_CTX_JAVA_COMPONENT) : key;
                list.add(toAdd);
            }
        }
        return list;
    }

    /**
     * Get the component id from the Invocation Manager.
     *
     * @return the component id as a string.
     */
    private String getComponentId() throws NamingException {
        final ComponentInvocation invocation = getComponentInvocation();
        final String id = invocation.getComponentId();
        if (id == null) {
            throw new NamingException("Invocation exception: ComponentId is null");
        }
        return id;
    }


    private ComponentInvocation getComponentInvocation() throws NamingException {
        final ComponentInvocation invocation;
        if (invMgr == null) {
            invocation = serviceLocator.<InvocationManager> getService(InvocationManager.class).getCurrentInvocation();
        } else {
            invocation = invMgr.getCurrentInvocation();
        }

        if (invocation == null) {
            throw new NamingException("Invocation exception: Got null ComponentInvocation!");
        }
        return invocation;
    }


    private <T> T lookupFromNamespace(SimpleJndiName name, Map<SimpleJndiName, Object> namespace, Hashtable<?, ?> env)
        throws NamingException {
        final Object objectOrProxyOrRef = namespace.get(name);
        if (objectOrProxyOrRef == null) {
            throw new NameNotFoundException("No object bound to name " + name);
        }
        if (objectOrProxyOrRef instanceof NamingObjectProxy) {
            NamingObjectProxy namingProxy = (NamingObjectProxy) objectOrProxyOrRef;
            return namingProxy.create(env == null || env.isEmpty() ? initialContext : new InitialContext(env));
        } else if (objectOrProxyOrRef instanceof Reference) {
            try {
                return getObjectInstance(name, objectOrProxyOrRef, env);
            } catch (Exception e) {
                LOG.log(DEBUG, () -> "Unable to get Object instance from Reference for name [" + name + "]. "
                    + "Hence returning the Reference object " + objectOrProxyOrRef, e);
            }
        }
        return (T) objectOrProxyOrRef;
    }

    private static class AppModuleKey implements Serializable {

        private static final long serialVersionUID = -6080646413719869870L;
        private final String app;
        private final String module;

        private AppModuleKey(String appName, String moduleName) {
            app = appName;
            module = moduleName;
        }


        private String getAppName() {
            return app;
        }


        private String getModuleName() {
            return module;
        }


        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof AppModuleKey) {
                AppModuleKey other = (AppModuleKey) o;
                return app.equals(other.app) && module.equals(other.module);
            }
            return false;
        }


        @Override
        public int hashCode() {
            return app.hashCode();
        }


        @Override
        public String toString() {
            return app + '/' + module;
        }
    }

    private static class ComponentIdInfo {

        String appName;
        String moduleName;
        String componentId;
        boolean treatComponentAsModule;

        @Override
        public String toString() {
            return "ComponentIdInfo[appName=" + appName + ", module=" + moduleName + ", componentId=" + componentId
                + ", treatComponentAsModule=" + treatComponentAsModule + ']';
        }
    }

    private static class BindingsIterator<T> implements NamingEnumeration<T> {
        private final GlassfishNamingManagerImpl nm;
        private final Iterator<SimpleJndiName> names;
        private final boolean producesNamesOnly;

        BindingsIterator(GlassfishNamingManagerImpl nm, Iterator<SimpleJndiName> names, boolean producesNamesOnly) {
            this.nm = nm;
            this.names = names;
            this.producesNamesOnly = producesNamesOnly;
        }

        @Override
        public boolean hasMoreElements() {
            return names.hasNext();
        }

        @Override
        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        @Override
        public T nextElement() {
            if (!names.hasNext()) {
                return null;
            }
            try {
                SimpleJndiName name = names.next();
                Object obj = nm.lookupFromComponentNamespace(name);
                @SuppressWarnings("unchecked")
                T next = producesNamesOnly
                    ? (T) (new NameClassPair(name.toString(), getClass().getName()))
                    : (T) (new Binding(name.toString(), obj));
                return next;
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public T next() throws NamingException {
            return nextElement();
        }

        @Override
        public void close() {
            //no-op since no steps needed to free up resources
        }
    }

    // FIXME: no synchronization or concurrency, is it alright?
    private static class NamespacesMap<K extends Serializable> extends HashMap<K, JavaNamespace> {
        private static final long serialVersionUID = 7921214769289453089L;
        private final String name;

        private NamespacesMap(String name) {
            this.name = name;
        }


        @Override
        public String toString() {
            return "NamespacesMap[name=" + name + ", keys=" + keySet() + ']';
        }
    }


    private static class JavaNamespace extends HashMap<SimpleJndiName, Object> {
        private static final long serialVersionUID = 8493699306782159175L;
        private final String name;

        private JavaNamespace(String name, String subContextName) {
            this.name = name;

            SimpleJndiName javaJndi = new SimpleJndiName(JNDI_CTX_JAVA);
            JavaURLContext javaContext = new JavaURLContext(javaJndi);
            put(javaJndi, javaContext);
            put(new SimpleJndiName(javaJndi + "/"), javaContext);

            JavaURLContext subContext = new JavaURLContext(new SimpleJndiName(javaJndi + subContextName));
            put(subContext.getName(), subContext);
            SimpleJndiName subContextJndi = new SimpleJndiName(subContext.getName() + "/");
            put(subContextJndi, subContext);

            JavaURLContext envContext = new JavaURLContext(new SimpleJndiName(subContextJndi + "env"));
            put(envContext.getName(), envContext);
            put(new SimpleJndiName(envContext.getName() + "/"), envContext);
        }


        @Override
        public String toString() {
            return "JavaNamespace[name=" + name + ", keys=" + keySet() + ']';
        }
    }
}
