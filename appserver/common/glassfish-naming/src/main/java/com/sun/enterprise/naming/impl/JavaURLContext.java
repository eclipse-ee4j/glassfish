/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Hashtable;
import java.util.Objects;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.api.naming.ComponentNamingUtil;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP_ENV;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT_ENV;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_GLOBAL;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_MODULE_ENV;

/**
 * This class is a context implementation for the java:* namespaces.
 * The context determines the component id from the invocation manager
 * of the component that is invoking the method and then looks up the
 * object in that component's local namespace.
 */
public final class JavaURLContext implements Context, Cloneable {
    private static final Logger LOG = System.getLogger(JavaURLContext.class.getName());

    private static GlassfishNamingManagerImpl namingManager;

    private final SimpleJndiName myName;
    private final Hashtable<Object, Object> myEnv;
    // FIXME: Should not be here, causes cyclic dependency directly between these two classes.
    private final SerialContext serialContext;


    static void setNamingManager(GlassfishNamingManagerImpl mgr) {
        namingManager = mgr;
    }

    /**
     * Create a context with the specified environment.
     */
    public JavaURLContext(SimpleJndiName name) {
        this.myName = Objects.requireNonNull(name, "name");
        this.myEnv = getMyEnv(null);
        this.serialContext = null;
    }


    /**
     * Create a context with the specified name+environment.
     */
    public JavaURLContext(SimpleJndiName name, Hashtable<Object, Object> environment) {
        this.myName = Objects.requireNonNull(name, "name");
        this.myEnv = getMyEnv(environment);
        this.serialContext = null;
    }


    public JavaURLContext(Hashtable<Object, Object> environment, SerialContext serialContext) {
        this.myName = new SimpleJndiName("");
        this.myEnv = getMyEnv(environment);
        this.serialContext = serialContext;
    }


    /**
     * Create a context with the same name and env.
     */
    public JavaURLContext(JavaURLContext ctx) {
        this.myName = ctx.myName;
        this.myEnv = ctx.myEnv;
        this.serialContext = null;
    }


    public JavaURLContext(JavaURLContext ctx, SerialContext serialContext) {
        this.myName = ctx.myName;
        this.myEnv = ctx.myEnv;
        this.serialContext = serialContext;
    }


    /**
     * Lookup an object in the serial context.
     *
     * @return the object that is being looked up.
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public Object lookup(String name) throws NamingException {
        LOG.log(Level.TRACE, "lookup(name={0}); this={1}", name, this);
        if (name.isEmpty()) {

            // javadocs for Context.lookup: If name is empty, returns a new
            // instance of this context (which represents the same naming
            // context as this context, but its environment may be modified
            // independently and it may be accessed concurrently).
            return new JavaURLContext(myName, myEnv);
        }

        // Preconstructed exception collecting all tries which ended with an exception.
        final NamingException e = new NameNotFoundException("No object bound for " + name);
        final SimpleJndiName fullName = toFullName(name);
        LOG.log(Level.DEBUG, "Computed fullname={0} for name={1}", fullName, name);
        if (fullName == null) {
            // if this fails, there is no reason trying another.
            throw e;
        }
        {
            Object obj = tryNamingManager(fullName, e);
            if (obj != null) {
                return obj;
            }
        }
        final ServiceLocator services = Globals.getDefaultHabitat();
        final ProcessEnvironment processEnv = services.getService(ProcessEnvironment.class);
        if (fullName.isJavaApp() && processEnv.getProcessType() == ProcessType.ACC) {
            // This could either be an attempt by an app client to access a portable
            // remote session bean JNDI name via the java:app namespace or a lookup of
            // an application-defined java:app environment dependency.  Try them in
            // that order.
            final Context context = namingManager.getInitialContext();
            String appName = (String) context.lookup(JNDI_CTX_JAVA_APP + "AppName");
            if (!fullName.hasPrefix(JNDI_CTX_JAVA_APP_ENV) || !"java:app/env".equals(fullName.toString())) {
                // Translate the java:app name into the equivalent java:global name so that
                // the lookup will be resolved by the server.
                String globalName = JNDI_CTX_JAVA_GLOBAL + appName + '/' + fullName.removePrefix();
                Object obj = lookupOrCollectException(globalName, e, context::lookup);
                if (obj != null) {
                    return obj;
                }
            }
            ComponentNamingUtil util = services.getService(ComponentNamingUtil.class);
            SimpleJndiName internalGlobalJavaAppName = util.composeInternalGlobalJavaAppName(appName, fullName);
            Object obj = lookupOrCollectException(internalGlobalJavaAppName.toString(), e, context::lookup);
            if (obj != null) {
                return obj;
            }
        }
        throw e;
    }


    /**
     * Lookup a name in either the cosnaming or serial context.
     *
     * @return the object that is being looked up.
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public Object lookup(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return lookup(name.toString());
    }

    /**
     * Bind an object in the namespace. Binds the reference to the
     * actual object in either the cosnaming or serial context.
     *
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public void bind(String name, Object obj) throws NamingException {
        throw new NamingException("The namespace '" + myName + "' cannot be modified. Called for " + name);
    }

    /**
     * Bind an object in the namespace. Binds the reference to the
     * actual object in either the cosnaming or serial context.
     *
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public void bind(Name name, Object obj) throws NamingException {
        throw new NamingException("The namespace '" + myName + "' cannot be modified. Called for " + name);
    }

    /**
     * Rebind an object in the namespace. Rebinds the reference to the
     * actual object in either the cosnaming or serial context.
     *
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public void rebind(String name, Object obj) throws NamingException {
        throw new NamingException("The namespace '" + myName + "' cannot be modified. Called for " + name);
    }

    /**
     * Rebind an object in the namespace. Rebinds the reference to the
     * actual object in either the cosnaming or serial context.
     *
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        throw new NamingException("The namespace '" + myName + "' cannot be modified. Called for " + name);
    }

    /**
     * Unbind an object from the namespace.
     *
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public void unbind(String name) throws NamingException {
        throw new NamingException("The namespace '" + myName + "' cannot be modified. Called for " + name);
    }

    /**
     * Unbind an object from the namespace.
     *
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public void unbind(Name name) throws NamingException {
        throw new NamingException("The namespace '" + myName + "' cannot be modified. Called for " + name);
    }

    /**
     * The rename operation is not supported by this context. It throws
     * an OperationNotSupportedException.
     */
    @Override
    public void rename(String oldname, String newname) throws NamingException {
        throw new NamingException("The namespace '" + myName + "' cannot be modified. Called for " + oldname + " and " + newname);
    }

    /**
     * The rename operation is not supported by this context. It throws
     * an OperationNotSupportedException.
     */
    @Override
    public void rename(Name oldname, Name newname) throws NamingException {
        throw new NamingException("The namespace '" + myName + "' cannot be modified. Called for " + oldname + " and " + newname);
    }

    /**
     * The destroySubcontext operation is not supported by this context.
     * It throws an OperationNotSupportedException.
     */
    @Override
    public void destroySubcontext(String name) throws NamingException {
        throw new NamingException("The namespace '" + myName + "' cannot be modified. Called for " + name);
    }

    /**
     * The destroySubcontext operation is not supported by this context.
     * It throws an OperationNotSupportedException.
     */
    @Override
    public void destroySubcontext(Name name) throws NamingException {
        throw new NamingException("The namespace '" + myName + "' cannot be modified. Called for " + name);
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        throw new NamingException("The namespace '" + myName + "' cannot be modified. Called for " + name);
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        throw new NamingException("The namespace '" + myName + "' cannot be modified. Called for " + name);
    }


    /**
     * Lists the contents of a context or subcontext. The operation is
     * delegated to the serial context.
     *
     * @return an enumeration of the contents of the context.
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        if (name.isEmpty()) {
            // listing this context
            if (namingManager == null) {
                throw new NamingException();
            }
            return namingManager.list(myName);
        }

        // Check if 'name' names a context
        Object target = lookup(name);
        if (target instanceof Context) {
            return ((Context) target).list("");
        }
        throw new NotContextException(name + " cannot be listed");
    }

    /**
     * Lists the contents of a context or subcontext. The operation is
     * delegated to the serial context.
     *
     * @return an enumeration of the contents of the context.
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return list(name.toString());
    }


    /**
     * Lists the bindings of a context or subcontext. The operation is
     * delegated to the serial context.
     *
     * @return an enumeration of the bindings of the context.
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        if (name.isEmpty()) {
            // listing this context
            if (namingManager == null) {
                throw new NamingException();
            }
            return namingManager.listBindings(myName);
        }

        // Perhaps 'name' names a context
        Object target = lookup(name);
        if (target instanceof Context) {
            return ((Context) target).listBindings("");
        }
        throw new NotContextException(name + " cannot be listed");
    }

    /**
     * Lists the bindings of a context or subcontext. The operation is
     * delegated to the serial context.
     *
     * @return an enumeration of the bindings of the context.
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return listBindings(name.toString());
    }

    /**
     * This context does not treat links specially. A lookup operation is
     * performed.
     */
    @Override
    public Object lookupLink(String name) throws NamingException {
        // This flat context does not treat links specially
        return lookup(name);
    }

    /**
     * This context does not treat links specially. A lookup operation is
     * performed.
     */
    @Override
    public Object lookupLink(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return lookupLink(name.toString());
    }


    /**
     * Return the name parser for the specified name.
     *
     * @return the NameParser instance.
     * @throws NamingException if there is an exception.
     */
    @Override
    public NameParser getNameParser(String name) throws NamingException {
        if (namingManager == null) {
            throw new NamingException("Naming manager wasn't set!");
        }
        return namingManager.getNameParser();
    }

    /**
     * Return the name parser for the specified name.
     *
     * @return the NameParser instance.
     * @throws NamingException if there is an exception.
     */
    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return getNameParser(name.toString());
    }


    @Override
    public String composeName(String name, String prefix) throws NamingException {
        Name result = composeName(new CompositeName(name), new CompositeName(prefix));
        return result.toString();
    }


    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result = (Name) prefix.clone();
        result.addAll(name);
        return result;
    }


    /**
     * Add a property to the environment.
     */
    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return myEnv.put(propName, propVal);
    }


    /**
     * Remove a property from the environment.
     */
    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        return myEnv.remove(propName);
    }


    /**
     * Get the context's environment.
     */
    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return myEnv;
    }


    /**
     * New JNDI 1.2 operation.
     */
    @Override
    public void close() throws NamingException {
        this.myEnv.clear();
    }

    /**
     * @return my name, never null.
     */
    public SimpleJndiName getName() {
        return this.myName;
    }


    /**
     * Return the name of this context within the namespace.
     * The name can be passed as an argument to new InitialContext().lookup() to retrieve this
     * context.
     */
    @Override
    public String getNameInNamespace() throws NamingException {
        return myName.toString();
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + myName + ']';
    }


    private SimpleJndiName toFullName(String name) {
        if (!name.startsWith(JNDI_CTX_JAVA) && name.indexOf(':') != -1) {
            // this is probably some generic JNDI name like jdbc:derby: or http://... etc.,
            // not compatible with java contexts.
            return null;
        }
        if (myName.isEmpty()) {
            return new SimpleJndiName(name);
        } else if (myName.toString().equals(JNDI_CTX_JAVA)) {
            return new SimpleJndiName(myName + name);
        } else {
            return new SimpleJndiName(myName + "/" + name);
        }
    }


    private Object tryNamingManager(final SimpleJndiName fullName, NamingException collector) throws NamingException {
        // If we know for sure it's an entry within an environment namespace it might be a proxy.
        if (!isAnyJavaEnvJndiName(fullName)) {
            Object obj = lookupOrCollectException(fullName, collector, NamedNamingObjectManager::tryNamedProxies);
            if (obj != null) {
                return obj;
            }
        }
        return lookupOrCollectException(fullName, collector, n -> namingManager.lookup(n, serialContext));
    }


    private static boolean isAnyJavaEnvJndiName(SimpleJndiName jndiName) {
        if (jndiName == null || jndiName.isEmpty()) {
            return false;
        }
        if (jndiName.hasPrefix(JNDI_CTX_JAVA_COMPONENT_ENV) || jndiName.hasPrefix(JNDI_CTX_JAVA_MODULE_ENV)
            || jndiName.hasPrefix(JNDI_CTX_JAVA_APP_ENV)) {
            return true;
        } else if (jndiName.toString().equals("java:comp/env") || jndiName.toString().equals("java:module/env")
            || jndiName.toString().equals("java:app/env")) {
            return true;
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    private static Hashtable<Object, Object> getMyEnv(Hashtable<Object, Object> environment) {
        return environment == null ? new Hashtable<>() : (Hashtable<Object, Object>) environment.clone();
    }


    private static <N> Object lookupOrCollectException(final N jndiName, final NamingException collector,
        final NamingFunction<N> lookup) {
        try {
            return lookup.applyName(jndiName);
        } catch (NamingException e) {
            collector.addSuppressed(e);
            return null;
        }
    }


    @FunctionalInterface
    private interface NamingFunction<N> {
        Object applyName(N jndiName) throws NamingException;
    }
}
