/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;

import static com.sun.enterprise.naming.util.LogFacade.logger;

/**
 * Class to implement multiple level of subcontexts in SerialContext.
 * To use this class a new object of class InitialContext (env) should be instantiated.
 * <p>
 * The env i.e the Environment is initialised with SerialInitContextFactory.
 * An example for using this is in /test/subcontext
 */
public class TransientContext implements Context, Serializable {

    private static final long serialVersionUID = -674500209229911786L;

    private static NameParser myParser = new SerialNameParser();

    private Hashtable<Object, Object> myEnv;
    private final Map<String, Object> bindings = new BindingMap();

    // Issue 7067: lots of lookup failures in a heavily concurrent client.
    // So add a read/write lock, which allows unlimited concurrent readers,
    // and only imposes a global lock on relatively infrequent updates.
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();


    /**
     * Create a subcontext with the specified name.
     *
     * @return the created subcontext.
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public Context createSubcontext(String name) throws NamingException {
        return drillDownAndCreateSubcontext(name);
    }


    /**
     * Create a subcontext with the specified name.
     *
     * @return the created subcontext.
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public Context createSubcontext(Name name) throws NamingException {
        return createSubcontext(name.toString());
    }


    /**
     * Destroy the subcontext with the specified name.
     *
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public void destroySubcontext(String name) throws NamingException {
        drillDownAndDestroySubcontext(name);
    }


    /**
     * Destroy the subcontext with the specified name.
     *
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public void destroySubcontext(Name name) throws NamingException {
        destroySubcontext(name.toString());
    }


    /**
     * Handles making nested subcontexts
     * i.e. if you want abcd/efg/hij. It will go subcontext efg in abcd
     * (if not present already - it will create it) and then
     * make subcontext hij
     *
     * @return the created subcontext.
     * @throws NamingException if there is a Naming exception
     */
    private Context drillDownAndCreateSubcontext(String name) throws NamingException {
        lock.writeLock().lock();
        try {
            Name n = new CompositeName(name);
            if (n.size() <= 1) { // bottom
                if (bindings.containsKey(name)) {
                    throw new NameAlreadyBoundException("Subcontext " + name + " already present");
                }
                TransientContext ctx = new TransientContext();
                bindings.put(name, ctx);
                return ctx;
            }
            String suffix = n.getSuffix(1).toString();
            Context ctx;
            try {
                ctx = resolveContext(n.get(0));
            } catch (NameNotFoundException e) {
                ctx = new TransientContext();
            }
            Context subCtx = ctx.createSubcontext(suffix);
            bindings.put(n.get(0), ctx);
            return subCtx;
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Handles deleting nested subcontexts
     * i.e. if you want delete abcd/efg/hij. It will go subcontext efg in abcd
     * it will delete it) and then delete subcontext hij
     *
     * @throws NamingException if there is a naming exception
     */
    private void drillDownAndDestroySubcontext(String name) throws NamingException {
        lock.writeLock().lock();
        try {
            Name n = new CompositeName(name);
            if (n.size() < 1) {
                throw new InvalidNameException("Cannot destoy empty subcontext");
            }
            if (n.size() == 1) { // bottom
                if (bindings.containsKey(name)) {
                    bindings.remove(name);
                } else {
                    throw new NameNotFoundException("Subcontext: " + name + " not found");
                }
            } else {
                String suffix = n.getSuffix(1).toString();
                Context ctx; // the context to drill down from
                ctx = resolveContext(n.get(0));
                ctx.destroySubcontext(suffix);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Lookup the specified name.
     *
     * @return the object or context bound to the name.
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public Object lookup(String name) throws NamingException {
        lock.readLock().lock();
        try {
            Name n = new CompositeName(name);
            if (n.size() < 1) {
                throw new InvalidNameException("Cannot bind empty name");
            }
            if (n.size() == 1) {
                // bottom
                return doLookup(n.toString());
            }
            String suffix = n.getSuffix(1).toString();
            TransientContext ctx = resolveContext(n.get(0));
            return ctx.lookup(suffix);
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Lookup the specified name.
     *
     * @return the object or context bound to the name.
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }


    /**
     * Lookup the specified name in the current objects hashtable.
     *
     * @return the object or context bound to the name.
     * @throws NamingException if there is a naming exception.
     */
    private Object doLookup(String name) throws NamingException {
        Object answer = bindings.get(name);
        if (answer == null) {
            throw new NameNotFoundException(name + " not found");
        }
        return answer;
    }


    /**
     * Bind the object to the specified name.
     *
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public void bind(String name, Object obj) throws NamingException {
        lock.writeLock().lock();
        try {
            Name n = new CompositeName(name);
            if (n.size() < 1) {
                throw new InvalidNameException("Cannot bind empty name");
            }
            if (n.size() == 1) { // bottom
                doBindOrRebind(n.toString(), obj, false);
            } else {
                String suffix = n.getSuffix(1).toString();
                String subCtxName = n.get(0);
                Context ctx;
                try {
                    ctx = resolveContext(subCtxName);
                } catch (NameNotFoundException e) {
                    ctx = createSubcontext(subCtxName);
                }
                ctx.bind(suffix, obj);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Bind the object to the specified name.
     *
     * @throws NamingException if there is a naming exception.
     */
    @Override
    public void bind(Name name, Object obj) throws NamingException {
        bind(name.toString(), obj);
    }


    /**
     * Finds out if the subcontext specified is present in the current context
     *
     * @throws NamingException if there is a naming exception
     */
    private TransientContext resolveContext(String s) throws NamingException {
        TransientContext ctx;
        Object obj = bindings.get(s);
        if (obj == null) {
            throw new NameNotFoundException(s);
        }
        if (obj instanceof TransientContext) {
            ctx = (TransientContext) obj;
        } else {
            throw new NameAlreadyBoundException(s);
        }
        return ctx;
    }


    /**
     * Binds or rebinds the object specified by name
     *
     * @throws NamingException if there is a naming exception
     */
    private void doBindOrRebind(String name, Object obj, boolean rebind) throws NamingException {
        if (name.isEmpty()) {
            throw new InvalidNameException("Cannot bind empty name");
        }
        if (!rebind) {
            if (bindings.get(name) != null) {
                throw new NameAlreadyBoundException("Use rebind to override name " + name);
            }
        }
        bindings.put(name, obj);
    }


    /**
     * Rebinds the object specified by name
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public void rebind(String name, Object obj) throws NamingException {
        lock.writeLock().lock();
        try {
            Name jndiName = new CompositeName(name);
            if (jndiName.size() < 1) {
                throw new InvalidNameException("Cannot bind empty name");
            }
            if (jndiName.size() == 1) { // bottom
                doBindOrRebind(jndiName.toString(), obj, true);
            } else {
                String suffix = jndiName.getSuffix(1).toString();
                try {
                    resolveContext(jndiName.get(0)).rebind(suffix, obj);
                } catch (NameNotFoundException e) {
                    createSubcontext(jndiName.get(0)).rebind(suffix, obj);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Binds or rebinds the object specified by name
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        rebind(name.toString(), obj);
    }


    /**
     * Unbinds the object specified by name. Traverses down the context tree
     * and unbinds the object if required.
     *
     * @throws NamingException if there is a naming exception
     */
    private void doUnbind(String name) throws NamingException {
        if (name.isEmpty()) {
            throw new InvalidNameException("Cannot unbind empty name");
        }
        bindings.remove(name);
    }


    /**
     * Unbinds the object specified by name. Calls itself recursively to
     * traverse down the context tree and unbind the object.
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public void unbind(String name) throws NamingException {
        lock.writeLock().lock();
        try {
            Name n = new CompositeName(name);
            if (n.size() < 1) {
                throw new InvalidNameException("Cannot unbind empty name");
            }
            if (n.size() == 1) { // bottom
                doUnbind(n.toString());
            } else {
                String suffix = n.getSuffix(1).toString();
                TransientContext ctx = resolveContext(n.get(0));
                ctx.unbind(suffix);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Unbinds the object specified by name
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public void unbind(Name name) throws NamingException {
        unbind(name.toString());
    }


    /**
     * Rename the object specified by oldname to newname
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public void rename(Name oldname, Name newname) throws NamingException {
        rename(oldname.toString(), newname.toString());
    }


    /**
     * Rename the object specified by oldname to newname
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public void rename(String oldname, String newname) throws NamingException {
        if (oldname.isEmpty() || newname.isEmpty()) {
            throw new InvalidNameException("Cannot rename empty name");
        }

        lock.writeLock().lock();
        try {
            // Check if new name exists
            if (bindings.get(newname) != null) {
                throw new NameAlreadyBoundException(newname + " is already bound");
            }

            // Check if old name is bound
            Object oldBinding = bindings.remove(oldname);
            if (oldBinding == null) {
                throw new NameNotFoundException(oldname + " not bound");
            }

            bindings.put(newname, oldBinding);
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * list the objects stored by the current context
     */
    public Hashtable<Object, Object> list() {
        lock.readLock().lock();
        try {
            return new Hashtable<>(bindings);
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * List the objects specified by name.
     *
     * @throws NamingException if there is a naming exception
     */
    public Hashtable<Object, Object> listContext(String name) throws NamingException {
        lock.readLock().lock();
        try {
            if (logger.isLoggable(Level.FINE)) {
                print(bindings);
            }
            if (name.isEmpty()) {
                return new Hashtable<>(bindings);
            }

            Object target = lookup(name);
            if (target instanceof TransientContext) {
                return ((TransientContext) target).listContext("");
            }
            throw new NotContextException(name + " cannot be listed");
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * List the objects specified by name.
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return list(name.toString());
    }


    /**
     * List the objects specified by name.
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        lock.readLock().lock();
        try {
            if (logger.isLoggable(Level.FINE)) {
                print(bindings);
            }
            if (name.isEmpty()) {
                return new RepNames(new Hashtable<>(bindings));
            }

            Object target = lookup(name);
            if (target instanceof Context) {
                return ((Context) target).list("");
            }
            throw new NotContextException(name + " cannot be listed");
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * List the bindings of objects present in name.
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        lock.readLock().lock();
        try {
            if (name.isEmpty()) {
                return new RepBindings(new Hashtable<>(bindings));
            }

            Object target = lookup(name);
            if (target instanceof Context) {
                return ((Context) target).listBindings("");
            }
            throw new NotContextException(name + " cannot be listed");
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * List the binding of objects specified by name.
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return listBindings(name.toString());
    }


    /**
     * Lookup the name.
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public Object lookupLink(String name) throws NamingException {
        // This flat context does not treat links specially
        return lookup(name);
    }


    /**
     * Lookup name.
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public Object lookupLink(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return lookupLink(name.toString());
    }


    /**
     * List the NameParser specified by name.
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public NameParser getNameParser(String name) throws NamingException {
        return myParser;
    }


    /**
     * List the NameParser specified by name.
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return getNameParser(name.toString());
    }


    /**
     * Compose a new name specified by name and prefix.
     *
     * @return null
     * @throws NamingException if there is a naming exception
     */
    @Override
    public String composeName(String name, String prefix) throws NamingException {
        return null;
    }


    /**
     * Compose a new name specified by name and prefix.
     *
     * @return Name result of the concatenation
     * @throws NamingException if there is a naming exception
     */
    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result = (Name) (prefix.clone());
        result.addAll(name);
        return result;
    }


    /**
     * Add the property name and value to the environment.
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        lock.writeLock().lock();
        try {
            if (myEnv == null) {
                myEnv = new Hashtable<>(5, 0.75f);
            }
            return myEnv.put(propName, propVal);
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Remove property from the environment.
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        lock.writeLock().lock();
        try {
            if (myEnv == null) {
                return null;
            }
            return myEnv.remove(propName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * List the current environment.
     *
     * @throws NamingException if there is a naming exception
     */
    @Override
    public Hashtable<Object, Object> getEnvironment() throws NamingException {
        lock.writeLock().lock() ;
        try {
            if (myEnv == null) {
                myEnv = new Hashtable<>(3, 0.75f);
            }
            return myEnv;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Resets the environemnt to null and clears all bindings.
     * However the {@link TransientContext} instance can be used again.
     */
    @Override
    public void close() throws NamingException {
        myEnv = null;
        bindings.clear();
    }

    /**
     * Operation not supported.
     */
    @Override
    public String getNameInNamespace() throws NamingException {
        throw new OperationNotSupportedException("getNameInNamespace() not implemented");
    }

    /**
     * Print the current hashtable.  Should only be invoked for FINE Level logging.
     */
    private static void print(Map<String,Object> ht) {
        for (Map.Entry<String, Object> entry : ht.entrySet()) {
            Object value = entry.getValue();
            logger.log(Level.FINE, "[{0}, {1}:{2}]",
                    new Object[]{entry.getKey(), value, value.getClass().getName()});
        }
    }

    /** Class for enumerating name/class pairs */
    static class RepNames implements NamingEnumeration<NameClassPair> {

        private final Map<String, String> nameToClassName = new HashMap<>();
        private final Iterator<String> iter;

        RepNames(Hashtable<String, Object> bindings) {
            Set<String> names = new HashSet<>();
            for (Object str : bindings.keySet()) {
                names.add((String) str);
            }
            iter = names.iterator();
            for (Entry<String, Object> entry : bindings.entrySet()) {
                nameToClassName.put(entry.getKey(), entry.getValue().getClass().getName());
            }
        }


        @Override
        public boolean hasMoreElements() {
            return iter.hasNext();
        }

        @Override
        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        @Override
        public NameClassPair nextElement() {
            if (iter.hasNext()) {
                String name = iter.next();
                String className = nameToClassName.get(name) ;
                return new NameClassPair(name, className);
            }
            return null;
        }

        @Override
        public NameClassPair next() throws NamingException {
            return nextElement();
        }

        @Override
        public void close() {
            //no-op since no steps needed to free up resources
        }
    }

    /** Class for enumerating bmesindings */
    static class RepBindings implements NamingEnumeration<Binding> {
        Enumeration<String> names;
        Hashtable<String, Object> bindings;

        RepBindings(Hashtable<String, Object> bindings) {
            this.bindings = bindings;
            this.names = bindings.keys();
        }

        @Override
        public boolean hasMoreElements() {
            return names.hasMoreElements();
        }

        @Override
        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        @Override
        public Binding nextElement() {
            if (hasMoreElements()) {
                String name = names.nextElement();
                return new Binding(name, bindings.get(name));
            }
            return null;
        }

        @Override
        public Binding next() throws NamingException {
            return nextElement();
        }

        @Override
        public void close() {
            //no-op since no steps needed to free up resources
        }
    }

    /**
     * A map that excludes non-serializable values from serialization.
     */
    static class BindingMap extends HashMap<String, Object> {

        private static final long serialVersionUID = 1L;

        public Object writeReplace() throws ObjectStreamException {
            BindingMap bindingMap = (BindingMap) clone();
            // Skip non-serializable values for remote client
            bindingMap.entrySet().removeIf(binding -> !(binding.getValue() instanceof Serializable));
            return bindingMap;
        }
    }
}
