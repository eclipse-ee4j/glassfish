/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.jdke;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

/**
 * This class collects missing gaps in JDK's public API, which are needed.
 */
public final class JavaApiGaps {

    @SuppressWarnings("removal")
    private static final SecurityManager SECURITY_MANAGER = System.getSecurityManager();
    private static final String FIELD_INITCTX_FACTORY_BUILDER = "initctx_factory_builder";


    private JavaApiGaps() {
        // Prevent instantiation
    }


    /**
     * Sets the InitialContextFactoryBuilder in NamingManager if it was not set yet.
     * <p>
     * This is used to reset the {@link InitialContextFactoryBuilder} when necessary, because the
     * class doesn't have any suitable public method for that.
     *
     * @param builder the InitialContextFactoryBuilder to set
     *
     * @throws NamingException
     * @throws IllegalStateException if the builder was already set
     */
    public static void setInitialContextFactoryBuilder(InitialContextFactoryBuilder builder) throws NamingException {
        NamingAction action = () -> NamingManager.setInitialContextFactoryBuilder(builder);
        if (SECURITY_MANAGER == null) {
            action.execute();
            return;
        }
        doPrivilegedNamingAction(action);
    }


    /**
     * Sets the InitialContextFactoryBuilder in NamingManager to null.
     * <p>
     * This is used to reset the {@link InitialContextFactoryBuilder} when necessary, because the
     * class doesn't have any suitable public method for that.
     * @throws NamingException
     */
    public static void unsetInitialContextFactoryBuilder() throws NamingException {
        NamingAction action = () -> set(NamingManager.class, FIELD_INITCTX_FACTORY_BUILDER, null);
        if (SECURITY_MANAGER == null) {
            action.execute();
            return;
        }
        doPrivilegedNamingAction(action);
    }


    private static void set(Class<?> type, String fieldName, Object value) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                "Reflection to the field " + type.getCanonicalName() + "." + fieldName + " failed.", e);
        }
    }


    private static void doPrivilegedNamingAction(NamingAction action) throws NamingException {
        try {
            AccessController.doPrivileged(action);
        } catch (PrivilegedActionException e) {
            Throwable cause = e.getCause();
            // Runtime and Naming exceptions same as if there would be no security manager
            if (cause instanceof NamingException) {
                throw (NamingException) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("Failed to execute privileged naming action.", e);
        }
    }


    @FunctionalInterface
    private interface NamingAction extends PrivilegedExceptionAction<Void> {
        @Override
        default Void run() throws PrivilegedActionException {
            try {
                execute();
                return null;
            } catch (Exception e) {
                throw new PrivilegedActionException(e);
            }
        }

        void execute() throws NamingException;
    }
}
