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

import java.util.Hashtable;
import java.util.Objects;
import java.util.logging.Level;

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

import static com.sun.enterprise.naming.util.LogFacade.logger;
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
    private static GlassfishNamingManagerImpl namingManager;

    private final SimpleJndiName myName;
    private final Hashtable<Object, Object> myEnv;
    private final SerialContext serialContext;


    static void setNamingManager(GlassfishNamingManagerImpl mgr) {
        namingManager = mgr;
    }

    /**
     * Create a context with the specified environment.
     */
    public JavaURLContext(SimpleJndiName name) {
        this.myEnv = getMyEnv(null);
        this.myName = Objects.requireNonNull(name, "name");
        this.serialContext = null;
    }


    /**
     * Create a context with the specified environment.
     */
    public JavaURLContext(Hashtable<Object, Object> environment) throws NamingException {
        this.myEnv = getMyEnv(environment);
        this.myName = new SimpleJndiName("");
        this.serialContext = null;
    }


    /**
     * Create a context with the specified name+environment.
     * Called only from GlassfishNamingManager.
     */
    public JavaURLContext(SimpleJndiName name, Hashtable<Object, Object> environment) throws NamingException {
        this.myEnv = getMyEnv(environment);
        this.myName = Objects.requireNonNull(name, "name");
        this.serialContext = null;
    }


    /**
     * this constructor is called from SerialContext class
     */
    public JavaURLContext(Hashtable<Object, Object> environment, SerialContext serialContext) throws NamingException {
        this.myEnv = getMyEnv(environment);
        this.myName = new SimpleJndiName("");
        this.serialContext = serialContext;
    }


    public JavaURLContext(JavaURLContext ctx, SerialContext sctx) {
        this.myName = ctx.myName;
        this.myEnv = ctx.myEnv;
        this.serialContext = sctx;
    }


    /**
     * Lookup an object in the serial context.
     *
     * @return the object that is being looked up.
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public Object lookup(String name) throws NamingException {
        if (name.isEmpty()) {

            // javadocs for Context.lookup: If name is empty, returns a new
            // instance of this context (which represents the same naming
            // context as this context, but its environment may be modified
            // independently and it may be accessed concurrently).
            return new JavaURLContext(myName, myEnv);
        }

        final SimpleJndiName fullName;
        if (myName.isEmpty()) {
            fullName = new SimpleJndiName(name);
        } else if (myName.toString().equals(JNDI_CTX_JAVA)) {
            fullName = new SimpleJndiName(myName + name);
        } else {
            fullName = new SimpleJndiName(myName + "/" + name);
        }

        try {
            Object obj = null;
            // If we know for sure it's an entry within an environment namespace
            if (isLookingUpEnv(fullName)) {
                // refers to a dependency defined by the application
                obj = namingManager.lookup(fullName, serialContext);
            } else {
                // It's either an application-defined dependency in a java:
                // namespace or a special EE platform object.
                // Check for EE platform objects first to prevent overriding.
                obj = NamedNamingObjectManager.tryNamedProxies(fullName);
                if (obj == null) {
                    obj = namingManager.lookup(fullName, serialContext);
                }
            }

            if (obj == null) {
                throw new NamingException("No object found for " + name);
            }

            return obj;
        } catch (NamingException ex) {

            ServiceLocator services = Globals.getDefaultHabitat();
            ProcessEnvironment processEnv = services.getService(ProcessEnvironment.class);
            if (fullName.isJavaApp() && processEnv.getProcessType() == ProcessType.ACC) {

                // This could either be an attempt by an app client to access a portable
                // remote session bean JNDI name via the java:app namespace or a lookup of
                // an application-defined java:app environment dependency.  Try them in
                // that order.

                Context ic = namingManager.getInitialContext();
                String appName = (String) namingManager.getInitialContext().lookup(JNDI_CTX_JAVA_APP + "AppName");

                Object obj = null;

                if (!fullName.hasPrefix(JNDI_CTX_JAVA_APP_ENV) || !"java:app/env".equals(fullName.toString())) {
                    try {
                        // Translate the java:app name into the equivalent java:global name so that
                        // the lookup will be resolved by the server.
                        obj = ic.lookup(JNDI_CTX_JAVA_GLOBAL + appName + "/" + fullName.removePrefix(JNDI_CTX_JAVA_APP));
                    } catch (NamingException e) {
                        logger.log(Level.FINE, "Trying global version of java:app ejb lookup", e);
                    }
                }

                if (obj == null) {
                    ComponentNamingUtil util = services.getService(ComponentNamingUtil.class);
                    SimpleJndiName internalGlobalJavaAppName = util.composeInternalGlobalJavaAppName(appName, fullName);
                    obj = ic.lookup(internalGlobalJavaAppName.toString());
                }

                if (obj == null) {
                    throw new NamingException("No object found for " + name);
                }

                return obj;

            }

            throw ex;
        } catch (Exception ex) {
            throw (NamingException) (new NameNotFoundException("No object bound for " + fullName)).initCause(ex);
        }
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
        myEnv.clear();
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


    private boolean isLookingUpEnv(SimpleJndiName jndiName) {
        final String fullName = jndiName.toString();
        if (fullName.startsWith(JNDI_CTX_JAVA_COMPONENT_ENV) || fullName.startsWith(JNDI_CTX_JAVA_MODULE_ENV)
            || fullName.startsWith(JNDI_CTX_JAVA_APP_ENV)) {
            return true;
        } else if ("java:comp/env".equals(fullName) || "java:module/env".equals(fullName)
            || "java:app/env".equals(fullName)) {
            return true;
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    private static Hashtable<Object, Object> getMyEnv(Hashtable<Object, Object> environment) {
        return environment == null ? new Hashtable<>() : (Hashtable<Object, Object>) environment.clone();
    }
}
