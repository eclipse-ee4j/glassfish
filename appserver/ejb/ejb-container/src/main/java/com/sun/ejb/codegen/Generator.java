/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021-2022 Contributors to the Eclipse Foundation
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

package com.sun.ejb.codegen;

import com.sun.ejb.ClassGenerator;
import com.sun.enterprise.loader.ASURLClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.pfl.dynamic.codegen.impl.ClassGeneratorImpl;
import org.glassfish.pfl.dynamic.codegen.impl.CodeGenerator;
import org.glassfish.pfl.dynamic.codegen.spi.ImportList;
import org.glassfish.pfl.dynamic.codegen.spi.Wrapper;

import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.DUMP_AFTER_SETUP_VISITOR;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.TRACE_BYTE_CODE_GENERATION;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.USE_ASM_VERIFIER;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._classGenerator;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._package;


/**
 * The base class for all code generators.
 */
public abstract class Generator {
    private static final Logger LOG = Logger.getLogger(Generator.class.getName());

    private final ClassLoader loader;


    /**
     * @param loader {@link ClassLoader} owning generated classes
     */
    public Generator(final ClassLoader loader) {
        this.loader = Objects.requireNonNull(loader);
    }

    protected abstract String getPackageName();

    /**
     * @return name of the generated class or interface
     */
    public abstract String getGeneratedClassName();

    /**
     * @return loadable class of the same package as {@link #getGeneratedClassName()}
     */
    protected abstract Class<?> getAnchorClass();

    /**
     * Calls {@link Wrapper} methods to configure the class definition.
     * The {@link Wrapper} uses {@link ThreadLocal} internally, so you should
     * always call {@link Wrapper#_clear()} in finally block after generation
     * to avoid leakages.
     */
    protected abstract void evaluate();


    /**
     * @return {@link ClassLoader} owning the generated class.
     */
    public ClassLoader getClassLoader() {
        return this.loader;
    }


    public Class<?> generate() throws IllegalAccessException {
        if (getPackageName() == null) {
            _package();
        } else {
            _package(getPackageName());
        }
        final ImportList imports = Wrapper._import();
        evaluate();
        final Properties props = new Properties();
        if (LOG.isLoggable(Level.FINEST)) {
            props.put(DUMP_AFTER_SETUP_VISITOR, "true");
            props.put(TRACE_BYTE_CODE_GENERATION, "true");
            props.put(USE_ASM_VERIFIER, "true");
            try {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final PrintStream ps = new PrintStream(baos);
                Wrapper._sourceCode(ps, props);
                LOG.finest(baos.toString());
            } catch (final Exception e) {
                LOG.log(Level.WARNING, "Exception generating src for logs", e);
            }
        }

        final ClassGeneratorImpl codeGenerator = (ClassGeneratorImpl) _classGenerator();
        final byte[] bytecode = CodeGenerator.generateBytecode(codeGenerator, getClassLoader(), imports, props, System.out);

        if (useMethodHandles()) {
            LOG.log(Level.FINEST, "Using MethodHandles to define {0}, anchorClass: {1}",
                new Object[] {getGeneratedClassName(), getAnchorClass()});
            final Lookup lookup = MethodHandles.privateLookupIn(getAnchorClass(), MethodHandles.lookup());
            return lookup.defineClass(bytecode);
        }

        LOG.log(Level.FINEST, "Using ClassGenerator to define {0}, loader: {1}",
            new Object[] {getGeneratedClassName(), getClassLoader()});
        if (System.getSecurityManager() == null) {
            return ClassGenerator.defineClass(getClassLoader(), getGeneratedClassName(), bytecode,
                getAnchorClass().getProtectionDomain());
        }
        final PrivilegedAction<Class<?>> action = () -> ClassGenerator.defineClass(getClassLoader(),
            getGeneratedClassName(), bytecode);
        return AccessController.doPrivileged(action);
    }

    private boolean useMethodHandles() {
        // The bootstrap CL used by embedded glassfish doesn't remember generated classes
        // Further ClassLoader.findClass calls will fail.
        System.out.println(
            "loader: " + loader + "\n"
            + "loader.parent: " + loader.getParent() + "\n"
            + "system CL: " + ClassLoader.getSystemClassLoader() + "\n"
            + "platform CL: " + ClassLoader.getPlatformClassLoader()
        );


        if (loader.getParent() == null || loader.getClass() == ASURLClassLoader.class) {
            return false;
        }
        return Objects.equals(getPackageName(), getAnchorClass().getPackageName());
    }


    /**
     * @return the package name or null.
     */
    public static String getPackageName(final String fullClassName) {
        final int dot = fullClassName.lastIndexOf('.');
        return dot == -1 ? null : fullClassName.substring(0, dot);
    }

    /**
     * @return simple class name (including wrapper class and dollar sign if it is internal class)
     */
    public static String getBaseName(final String fullClassName) {
        final int dot = fullClassName.lastIndexOf('.');
        return dot == -1 ? fullClassName : fullClassName.substring(dot + 1);
    }


    /**
     * Remove duplicates from method array.
     * <p>
     * Duplicates will arise if a class/intf and super-class/intf
     * define methods with the same signature. Potentially the
     * throws clauses of the methods may be different (note Java
     * requires that the superclass/intf method have a superset of the
     * exceptions in the derived method).
     *
     * @param methods
     * @return methods which can be generated in an interface
     */
    protected Method[] removeRedundantMethods(final Method[] methods) {
        final List<Method> nodups = new ArrayList<>();
        for (final Method method : methods) {
            boolean duplicationDetected = false;
            final List<Method> previousResult = new ArrayList<>(nodups);
            for (final Method alreadyProcessed : previousResult) {
                // m1 and m2 are duplicates if they have the same signature
                // (name and same parameters).
                if (!method.getName().equals(alreadyProcessed.getName())) {
                    continue;
                }
                if (!haveSameParams(method, alreadyProcessed)) {
                    continue;
                }
                duplicationDetected = true;
                // Select which of the duplicate methods to generate
                // code for: choose the one that is lower in the
                // inheritance hierarchy: this ensures that the generated
                // method will compile.
                if (alreadyProcessed.getDeclaringClass().isAssignableFrom(method.getDeclaringClass())) {
                    // alreadyProcessedMethod is a superclass/intf of method,
                    // so replace it with more concrete method
                    nodups.remove(alreadyProcessed);
                    nodups.add(method);
                }
                break;
            }

            if (!duplicationDetected) {
                nodups.add(method);
            }
        }
        return nodups.toArray(new Method[nodups.size()]);
    }


    private boolean haveSameParams(final Method method1, final Method method2) {
        final Class<?>[] m1parms = method1.getParameterTypes();
        final Class<?>[] m2parms = method2.getParameterTypes();
        if (m1parms.length != m2parms.length) {
            return false;
        }
        for (int i = 0; i < m2parms.length; i++) {
            if (m1parms[i] != m2parms[i]) {
                return false;
            }
        }
        return true;
    }
}
