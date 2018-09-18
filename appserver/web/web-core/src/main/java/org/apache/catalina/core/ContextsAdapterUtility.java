/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.apache.catalina.core;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

/**
 * Utility class to adapt:
 * {@link Context} to {@link org.glassfish.grizzly.http.server.naming.NamingContext} and
 * {@link DirContext} to {@link org.glassfish.grizzly.http.server.naming.DirContext}.
 */
public class ContextsAdapterUtility {

    /**
     * Wraps {@link Context} and returns corresponding Grizzly
     * {@link org.glassfish.grizzly.http.server.naming.NamingContext}.
     *
     * @param namingContext {@link Context} to wrap.
     * @return {@link org.glassfish.grizzly.http.server.naming.NamingContext}
     */
    public static org.glassfish.grizzly.http.server.naming.NamingContext wrap(
            final Context namingContext) {
        if (namingContext == null) {
            return null;
        }
        
        return new NamingContextAdapter(namingContext);
    }

    /**
     * Unwraps Grizzly
     * {@link org.glassfish.grizzly.http.server.naming.NamingContext} and returns
     * internal {@link Context}.
     *
     * @param grizzlyNamingContext {@link org.glassfish.grizzly.http.server.naming.NamingContext}
     * @return {@link Context}
     * @throws IllegalArgumentException if passed Grizzly
     * {@link final org.glassfish.grizzly.http.server.naming.NamingContext} is
     * of unknown type (wasn't wrapped by this utility class).
     */
    public static Context unwrap(
            final org.glassfish.grizzly.http.server.naming.NamingContext grizzlyNamingContext) {
        if (grizzlyNamingContext == null) {
            return null;
        }
        
        if (!(grizzlyNamingContext instanceof NamingContextAdapter)) {
            throw new IllegalArgumentException("Unknown NamingContext type: " +
                    grizzlyNamingContext.getClass().getName());
        }
        return ((NamingContextAdapter) grizzlyNamingContext).getJmxNamingContext();
    }
    
    /**
     * Wraps {@link DirContext} and returns corresponding Grizzly
     * {@link org.glassfish.grizzly.http.server.naming.DirContext}.
     *
     * @param dirContext {@link DirContext} to wrap.
     * @return {@link org.glassfish.grizzly.http.server.naming.DirContext}
     */
    public static org.glassfish.grizzly.http.server.naming.DirContext wrap(
            final DirContext dirContext) {
        if (dirContext == null) {
            return null;
        }
        
        return new DirContextAdapter(dirContext);
    }

    /**
     * Unwraps Grizzly
     * {@link org.glassfish.grizzly.http.server.naming.DirContext} and returns
     * internal {@link DirContext}.
     *
     * @param grizzlyDirContext {@link org.glassfish.grizzly.http.server.naming.DirContext}
     * @return {@link DirContext}
     * @throws IllegalArgumentException if passed Grizzly
     * {@link final org.glassfish.grizzly.http.server.naming.DirContext} is not
     * of unknown type (wasn't wrapped by this utility class).
     */
    public static DirContext unwrap(
            final org.glassfish.grizzly.http.server.naming.DirContext grizzlyDirContext) {
        
        if (grizzlyDirContext == null) {
            return null;
        }
        
        if (!(grizzlyDirContext instanceof DirContextAdapter)) {
            throw new IllegalArgumentException("Unknown DirContext type: " +
                    grizzlyDirContext.getClass().getName());
        }
        return ((DirContextAdapter) grizzlyDirContext).getJmxDirContext();
    }

    private static Object wrapIfNeeded(final Object resource) {
        if (resource == null) {
            return null;
        } else if (resource instanceof DirContext) {
            return wrap((DirContext) resource);
        } else if (resource instanceof Context) {
            return wrap((Context) resource);
        }

        return resource;
    }
    
    private static class NamingContextAdapter
            implements org.glassfish.grizzly.http.server.naming.DirContext {
        private final Context jmxNamingContext;

        private NamingContextAdapter(final Context jmxNamingContext) {
            this.jmxNamingContext = jmxNamingContext;
        }

        public Context getJmxNamingContext() {
            return jmxNamingContext;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object lookup(final String pathStr)
                throws org.glassfish.grizzly.http.server.naming.NamingException {
            try {
                return wrapIfNeeded(jmxNamingContext.lookup(pathStr));
            } catch (NamingException e) {
                throw new org.glassfish.grizzly.http.server.naming.NamingException(e);
            }
        }
    }
    
    private static class DirContextAdapter
            implements org.glassfish.grizzly.http.server.naming.DirContext {
        private final DirContext jmxDirContext;

        private DirContextAdapter(final DirContext jmxDirContext) {
            this.jmxDirContext = jmxDirContext;
        }

        public DirContext getJmxDirContext() {
            return jmxDirContext;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object lookup(final String pathStr)
                throws org.glassfish.grizzly.http.server.naming.NamingException {
            try {
                return wrapIfNeeded(jmxDirContext.lookup(pathStr));
            } catch (NamingException e) {
                throw new org.glassfish.grizzly.http.server.naming.NamingException(e);
            }
        }
    }
}
