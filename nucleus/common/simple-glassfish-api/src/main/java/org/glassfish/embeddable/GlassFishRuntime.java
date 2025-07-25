/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.embeddable;

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.embeddable.spi.RuntimeBuilder;

/**
 * This is the entry point API to bootstrap GlassFish.
 *
 * <p/>A GlassFishRuntime represents just the runtime environment,
 * i.e., no active services yet. e.g., there won't be any web container
 * started just by creating a GlassFishRuntime object.
 *
 * <p/> The services will be activated when GlassFish instance is
 * started by doing omething like:
 *
 * <pre>
 *      GlassFishRuntime runtime = GlassFishRuntime.bootstrap(); // no active services
 *      GlassFish glassfish = runtime.newGlassFish();
 *      glassfish.start(); // active services.
 * </pre>
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author bhavanishankar@dev.java.net
 */
public abstract class GlassFishRuntime {

    private static final Logger logger = Logger.getLogger(GlassFishRuntime.class.getPackage().getName());


    /**
     * Singleton
     */
    private static GlassFishRuntime me;

    /**
     * Default constructor, doesn't do anything
     */
    protected GlassFishRuntime() {
        // reduced visibility
    }

    /**
     * Bootstrap a GlassFishRuntime with default {@link BootstrapProperties}.
     *
     * @return Bootstrapped GlassFishRuntime
     * @throws GlassFishException if the GlassFishRuntime is already bootstrapped.
     */
    public static GlassFishRuntime bootstrap() throws GlassFishException {
        return bootstrap(new BootstrapProperties(), GlassFishRuntime.class.getClassLoader());
    }

    /**
     * Bootstrap GlassFish runtime based on runtime configuration passed in the bootstrapProperties object.
     * This is a convenience method. Calling this method is same as
     * calling {@link #bootstrap(BootstrapProperties , ClassLoader)} with null as second argument.
     *
     * @param bootstrapProperties BootstrapProperties used to setup the runtime
     * @throws GlassFishException
     */
    public static GlassFishRuntime bootstrap(BootstrapProperties bootstrapProperties) throws GlassFishException {
        return bootstrap(bootstrapProperties, GlassFishRuntime.class.getClassLoader());
    }

    /**
     * Bootstrap GlassFish runtime based on runtime configuration passed in the bootstrapProperties object.
     * Calling this method twice will throw a GlassFishException
     *
     * @param bootstrapProperties BootstrapProperties used to setup the runtime
     * @param cl      ClassLoader used as parent loader by GlassFish modules. If null is passed, the class loader
     *                of this class is used.
     * @return a bootstrapped runtime that can now be used to create new GlassFish instances
     * @throws GlassFishException
     */
    public static GlassFishRuntime bootstrap(BootstrapProperties bootstrapProperties, ClassLoader cl) throws GlassFishException {
        return _bootstrap(bootstrapProperties, cl);
    }

    /**
     * Shuts down the Runtime and dispose off all the GlassFish objects
     * created via this Runtime
     *
     * @throws GlassFishException
     */
    public abstract void shutdown() throws GlassFishException;

    /**
     * Create a new instance of GlassFish with default {@link org.glassfish.embeddable.GlassFishProperties}
     *
     * @return New GlassFish instance.
     * @throws GlassFishException If at all fails to create a new GlassFish instance.
     */
    public GlassFish newGlassFish() throws GlassFishException {
        return newGlassFish(new GlassFishProperties());
    }

    /**
     * Creates a new instance of GlassFish.
     *
     * @param glassfishProperties GlassFishProperties used to setup the GlassFish instance
     * @return newly instantiated GlassFish object. It will be in {@link GlassFish.Status#INIT} state.
     * @throws GlassFishException
     */
    public abstract GlassFish newGlassFish(GlassFishProperties glassfishProperties) throws GlassFishException;


    private synchronized static GlassFishRuntime _bootstrap(BootstrapProperties bootstrapProperties, ClassLoader cl) throws GlassFishException {
        if (me != null) {
            throw new GlassFishException("Already bootstrapped", null);
        }
        ClassLoader classLoader = cl == null ? GlassFishRuntime.class.getClassLoader() : cl;
        RuntimeBuilder runtimeBuilder = getRuntimeBuilder(bootstrapProperties, classLoader);
        me = runtimeBuilder.build(bootstrapProperties, classLoader);
        return me;
    }

    protected synchronized static void shutdownInternal() throws GlassFishException {
        if (me == null) {
            throw new GlassFishException("Already shutdown", null);
        }
        me = null;
        System.out.println("Completed shutdown of GlassFish runtime");
    }


    private static RuntimeBuilder getRuntimeBuilder(BootstrapProperties bootstrapProperties, ClassLoader classLoader)
        throws GlassFishException {
        logger.logp(Level.FINE, "GlassFishRuntime", "getRuntimeBuilder", "classloader={0}", classLoader);
        for (RuntimeBuilder builder : ServiceLoader.load(RuntimeBuilder.class, classLoader)) {
            try {
                logger.logp(Level.FINE, "GlassFishRuntime", "getRuntimeBuilder", "builder = {0}", builder);
                if (builder.handles(bootstrapProperties)) {
                    return builder;
                }
            } catch (ServiceConfigurationError sce) {
                // Ignore the exception and move ahead to the next builder.
                logger.logp(Level.FINE, "GlassFishRuntime", "getRuntimeBuilder", "Ignoring", sce);
            } catch (NoClassDefFoundError ncdfe) {
                // On IBM JDK, we seem to be getting NoClassDefFoundError instead of ServiceConfigurationError
                // when OSgiRuntimeBuilder is not able to be loaded in non-OSGi mode because of absence of
                // OSGi classes in classpath. So, we need to catch it and ignore.
                logger.logp(Level.FINE, "GlassFishRuntime", "getRuntimeBuilder", "Ignoring", ncdfe);
            }
        }
        throw new GlassFishException(
            "No runtime builder available for this configuration: " + bootstrapProperties.getProperties(), null);
    }

}
