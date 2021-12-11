/*
 * Copyright (c) 2021 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.gf.ejb.internal;

import jakarta.ejb.Singleton;
import jakarta.ejb.Stateless;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import static org.glassfish.jersey.gf.ejb.internal.EjbClassUtilities.getRemoteAndLocalIfaces;

/**
 * Supplier to provide EJB components obtained via JNDI lookup.
 *
 * @param <T> The raw type of the bean
 */
class EjbSupplier<T> implements Supplier<T> {
    static final String JNDI_PREFIX_JAVA_MODULE = "java:module/";
    static final String JNDI_PREFIX_JAVA_APP = "java:app/";

    private static final Logger LOG = Logger.getLogger(EjbSupplier.class.getName());

    private final InitialContext ctx;
    private final Class<T> clazz;
    private final String beanName;
    private final EjbComponentProvider provider;

    EjbSupplier(final Class<T> rawType, final InitialContext ctx, final EjbComponentProvider ejbProvider) {
        this.clazz = rawType;
        this.ctx = ctx;
        this.provider = ejbProvider;
        this.beanName = getBeanName(rawType);
    }

    @Override
    public T get() {
        try {
            return lookup(clazz, beanName);
        } catch (final NamingException ex) {
            LOG.log(Level.SEVERE, "Could not find bean " + beanName + " of " + clazz, ex);
            return null;
        }
    }


    private static <T> String getBeanName(final Class<T> clazz) {
        final Stateless stateless = clazz.getAnnotation(Stateless.class);
        if (stateless != null) {
            if (stateless.name().isEmpty()) {
                return clazz.getSimpleName();
            }
            return stateless.name();
        }
        final Singleton singleton = clazz.getAnnotation(Singleton.class);
        if (singleton != null) {
            if (singleton.name().isEmpty()) {
                return clazz.getSimpleName();
            }
            return singleton.name();
        }
        return clazz.getSimpleName();
    }


    private T lookup(final Class<T> rawType, final String name) throws NamingException {
        try {
            return lookup(rawType, name, false);
        } catch (final NamingException ex) {
            LOG.log(Level.WARNING,
                "An instance of EJB class, " + rawType.getName()
                    + ", could not be looked up using simple form name."
                    + " Attempting to look up using the fully-qualified form name.",
                ex);
            return lookup(rawType, name, true);
        }
    }


    private T lookup(final Class<?> rawType, final String name, final boolean useRawTypeInJndiName)
        throws NamingException {
        final List<String> libNames = provider.getModuleNames();
        if (libNames.isEmpty()) {
            final String jndiName = toJndiName(
                JNDI_PREFIX_JAVA_MODULE, null, name, useRawTypeInJndiName ? rawType : null);
            return lookupTyped(jndiName);
        }
        NamingException exception = null;
        for (final String module : libNames) {
            final String jndiName = toJndiName(
                JNDI_PREFIX_JAVA_APP, module, name, useRawTypeInJndiName ? rawType : null);
            T result;
            try {
                result = lookupTyped(jndiName);
                if (result != null && isLookupInstanceValid(rawType, result)) {
                    return result;
                }
            } catch (final NamingException e) {
                exception = e;
            }
        }
        throw exception == null
            ? new NamingException("JNDI name " + name + " wasn't found in any of modules.")
            : exception;
    }


    private T lookupTyped(final String name) throws NamingException {
        final Object object = ctx.lookup(name);
        try {
            @SuppressWarnings("unchecked")
            final T typed = (T) object;
            return typed;
        } catch (final ClassCastException cause) {
            final NamingException e = new NamingException("The name " + name + " was found, but the type is incorrect.");
            e.initCause(cause);
            throw e;
        }
    }


    private static String toJndiName(final String prefix, final String contextName, final String name, final Class<?> rawType) {
        final StringBuilder jndiName = new StringBuilder(64);
        jndiName.append(prefix);
        if (contextName != null) {
            jndiName.append(contextName).append('/');
        }
        jndiName.append(name);
        if (rawType != null) {
            jndiName.append('!').append(rawType.getName());
        }
        return jndiName.toString();
    }


    private static boolean isLookupInstanceValid(final Class<?> rawType, final Object result) {
        return rawType.isInstance(result) || getRemoteAndLocalIfaces(rawType).stream()
            .filter(iface -> iface.isInstance(result)).findAny().isPresent();
    }
}
