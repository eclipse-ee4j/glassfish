/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.naming;

import com.sun.enterprise.naming.impl.SerialInitContextFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.InitRunLevel;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.main.jdke.JavaApiGaps;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.naming.util.LogFacade.logger;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * This is both a init run level service as well as our implementation of {@link InitialContextFactoryBuilder}. When GlassFish
 * starts up, this startup service configures NamingManager with appropriate builder by calling
 * {@link javax.naming.spi.NamingManager#setInitialContextFactoryBuilder}. Once the builder is setup, when ever new
 * InitialContext() is called, builder can either instantiate {@link SerialInitContextFactory}, which is our implementation of
 * {@link InitialContextFactory}, or any user specified InitialContextFactory class. While loading user specified class, it first
 * uses Thread's context class loader and then CommonClassLoader.
 *
 * Please note that this is setup as an init level service to ensure that JNDI subsystem is setup before applications are loaded.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
@RunLevel(value = InitRunLevel.VAL, mode = RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
public class GlassFishNamingBuilder implements InitialContextFactoryBuilder {

    @LogMessageInfo(message = "Failed to load {0} using CommonClassLoader")
    public static final String FAILED_TO_LOAD_CLASS = "AS-NAMING-00001";

    @LogMessageInfo(message = "Fall back to INITIAL_CONTEXT_FACTORY {0}")
    private static final String FALL_BACK_INITIAL_CONTEXT_FACTORY = "AS-NAMING-00008";

    /**
     * We use a naming builder in order to enable use of JNDI in OSGi context, because the builder gives us desired hooks to create
     * appserver specific initial context without having to rely on thread's context class loader which is a unknown quantity in osgi
     * environment. Use of a builder can lead to some probelamatic scenarios as discussed in issue #11997, so we allow user to
     * disable it if they want. Having such configuration option is more of a workaround than a fix, but I have not been able to find
     * a better solution so far.
     */
    private static final String ALLOW_JNDI_FROM_OSGI = "com.sun.enterprise.naming.allowJndiLookupFromOSGi";

    @Inject
    private ServerContext serverContext;


    @PostConstruct
    public void postConstruct() throws NamingException {
        if (isUsingBuilder()) {
            JavaApiGaps.setInitialContextFactoryBuilder(GlassFishNamingBuilder.this);
        }
    }

    @PreDestroy
    public void preDestroy() throws NamingException {
        if (isUsingBuilder()) {
            JavaApiGaps.unsetInitialContextFactoryBuilder();
        }
    }

    @Override
    public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException {
        if (environment != null) {
            // As per the documentation of Context.INITIAL_CONTEXT_FACTORY,
            // it represents a fully qualified class name.
            String className = (String) environment.get(Context.INITIAL_CONTEXT_FACTORY);
            if (className != null) {
                try {
                    return (InitialContextFactory) (loadClass(className).getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    if (className.startsWith("weblogic.jndi")) {
                        logger.log(INFO, FALL_BACK_INITIAL_CONTEXT_FACTORY,
                            "com.sun.enterprise.naming.impl.SerialInitContextFactory");
                    } else {
                        NoInitialContextException ne = new NoInitialContextException("Cannot instantiate class: " + className);
                        ne.setRootCause(e);
                        throw ne;
                    }
                }
            }
        }
        // default case
        return new SerialInitContextFactory();
    }

    private Class<?> loadClass(String className) throws ClassNotFoundException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            return Class.forName(className, true, contextClassLoader);
        } catch (ClassNotFoundException e) {
            // Not a significant error.  Try with common class loader instead.
            logger.logp(FINE, "GlassFishNamingBuilder", "loadClass", "Failed to load {0} using thread context class loader {1}",
                new Object[] { className, contextClassLoader });

            // Try using CommonClassLoader.
            ClassLoader commonClassLoader = serverContext.getCommonClassLoader();
            if (contextClassLoader != commonClassLoader) {
                try {
                    return Class.forName(className, true, commonClassLoader);
                } catch (ClassNotFoundException e2) {
                    logger.logp(WARNING, "GlassFishNamingBuilder", "loadClass", FAILED_TO_LOAD_CLASS, new Object[] { className });
                    throw e2;
                }
            }

            throw e;
        }
    }

    /**
     * @return true if we are using NamingBuilder, else false.
     */
    private Boolean isUsingBuilder() {
        // We are using a system property, because NamingBuilder is a JDK wide singleton.
        return Boolean.valueOf(System.getProperty(ALLOW_JNDI_FROM_OSGI, "true"));
    }

}
