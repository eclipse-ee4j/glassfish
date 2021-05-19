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

package com.oracle.hk2.devtest.cdi.extension;

import java.io.File;
import java.io.IOException;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.BeforeShutdown;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.inject.spi.ProcessObserverMethod;
import jakarta.enterprise.inject.spi.ProcessProducer;
import jakarta.enterprise.inject.spi.ProcessProducerField;
import jakarta.enterprise.inject.spi.ProcessProducerMethod;
import jakarta.enterprise.inject.spi.ProcessSessionBean;
import jakarta.inject.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 * This extension is used to ensure that the ServiceLocator
 * is availble via JNDI in all of the extension callbacks
 *
 * @author jwells
 *
 */
public class CDIExtension implements Extension {
    private final static String FILE_PREFIX = "destroyed-";
    private final static String FILE_POSTFIX = ".txt";
    private final static String JNDI_APP_NAME = "java:app/AppName";
    private final static String JNDI_LOCATOR_NAME = "java:app/hk2/ServiceLocator";

    private File createDestructionFileObject() {
        try {
            Context context = new InitialContext();

            String appName = (String) context.lookup(JNDI_APP_NAME);

            return new File(FILE_PREFIX + appName + FILE_POSTFIX);
        }
        catch (NamingException ne) {
            return null;
        }
    }

    private ServiceLocator getServiceLocator() {
        try {
            Context context = new InitialContext();

            return (ServiceLocator) context.lookup(JNDI_LOCATOR_NAME);
        }
        catch (NamingException ne) {
            return null;
        }

    }

    /**
     * This method will ensure that the file which indicates that the
     * application has shut down properly has been removed and then
     * adds the HK2 service to the system
     *
     * @param beforeBeanDiscovery
     */
    @SuppressWarnings("unused")
    private void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery) {
        File destructoFile = createDestructionFileObject();
        if (destructoFile == null) return;

        if (destructoFile.exists()) {
            if (destructoFile.delete() == false) return;
        }

        ServiceLocator locator = getServiceLocator();
        if (locator == null) return;

        Descriptor d = BuilderHelper.link(HK2ExtensionVerifier.class).
                in(Singleton.class.getName()).
                andLoadWith(new HK2LoaderImpl()).
                build();

        // Just having the service present is enough for the first callback
        ServiceLocatorUtilities.addOneDescriptor(locator, d);
    }

    /**
     * This method will ensure that the file which indicates that the
     * application has shut down properly has been removed and then
     * adds the HK2 service to the system
     *
     * @param beforeBeanDiscovery
     */
    @SuppressWarnings("unused")
    private void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery) {
        ServiceLocator locator = getServiceLocator();
        if (locator == null) return;

        HK2ExtensionVerifier verifier = locator.getService(HK2ExtensionVerifier.class);
        verifier.afterBeanDiscoveryCalled();
    }

    @SuppressWarnings("unused")
    private void afterDeploymentValidation(@Observes AfterDeploymentValidation afterDeploymentValidation) {
        ServiceLocator locator = getServiceLocator();
        if (locator == null) return;

        HK2ExtensionVerifier verifier = locator.getService(HK2ExtensionVerifier.class);
        verifier.afterDeploymentValidationCalled();
    }

    /**
     * This one is a little different, as it cannot use the application to
     * communicate success or failure.  Instead it writes out a file that
     * the test will look for after the application has been undeployed
     *
     * @param beforeShutdown
     */
    @SuppressWarnings("unused")
    private void beforeShutdown(@Observes BeforeShutdown beforeShutdown) {
        ServiceLocator locator = getServiceLocator();
        if (locator == null) return;

        File destructoFile = createDestructionFileObject();
        try {
            destructoFile.createNewFile();
        }
        catch (IOException ioe) {
            System.err.println("ERROR:  Failed to create file " + destructoFile.getAbsolutePath());
            ioe.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> processAnnotatedType) {
        ServiceLocator locator = getServiceLocator();
        if (locator == null) return;

        HK2ExtensionVerifier verifier = locator.getService(HK2ExtensionVerifier.class);
        verifier.processAnnotatedTypeCalled();
    }

    @SuppressWarnings("unused")
    private <T> void processInjectionTarget(@Observes ProcessInjectionTarget<T> processInjectionTarget) {
        ServiceLocator locator = getServiceLocator();
        if (locator == null) return;

        HK2ExtensionVerifier verifier = locator.getService(HK2ExtensionVerifier.class);
        verifier.processInjectionTargetCalled();
    }

    @SuppressWarnings("unused")
    private <T, X> void processProducer(@Observes ProcessProducer<T, X> processProducer) {
        ServiceLocator locator = getServiceLocator();
        if (locator == null) return;

        HK2ExtensionVerifier verifier = locator.getService(HK2ExtensionVerifier.class);
        verifier.processProducerCalled();
    }

    @SuppressWarnings("unused")
    private <T> void processManagedBean(@Observes ProcessManagedBean<T> processManagedBean) {
        ServiceLocator locator = getServiceLocator();
        if (locator == null) return;

        HK2ExtensionVerifier verifier = locator.getService(HK2ExtensionVerifier.class);
        verifier.processManagedBeanCalled();
    }

    @SuppressWarnings("unused")
    private <T> void processSessionBean(@Observes ProcessSessionBean<T> processSessionBean) {
        ServiceLocator locator = getServiceLocator();
        if (locator == null) return;

        HK2ExtensionVerifier verifier = locator.getService(HK2ExtensionVerifier.class);
        verifier.processSessionBeanCalled();
    }

    @SuppressWarnings("unused")
    private <T, X> void processProducerMethod(@Observes ProcessProducerMethod<T, X> processProducerMethod) {
        ServiceLocator locator = getServiceLocator();
        if (locator == null) return;

        HK2ExtensionVerifier verifier = locator.getService(HK2ExtensionVerifier.class);
        verifier.processProducerMethodCalled();
    }

    @SuppressWarnings("unused")
    private <T, X> void processProducerField(@Observes ProcessProducerField<T, X> processProducerField) {
        ServiceLocator locator = getServiceLocator();
        if (locator == null) return;

        HK2ExtensionVerifier verifier = locator.getService(HK2ExtensionVerifier.class);
        verifier.processProducerFieldCalled();
    }

    @SuppressWarnings("unused")
    private <T, X> void processObserverMethod(@Observes ProcessObserverMethod<T, X> processObserverMethod) {
        ServiceLocator locator = getServiceLocator();
        if (locator == null) return;

        HK2ExtensionVerifier verifier = locator.getService(HK2ExtensionVerifier.class);
        verifier.processObserverMethodCalled();
    }

    private class HK2LoaderImpl implements HK2Loader {
        private final ClassLoader loader = getClass().getClassLoader();

        @Override
        public Class<?> loadClass(String className) throws MultiException {
            try {
                return loader.loadClass(className);
            }
            catch (Throwable th) {
                throw new MultiException(th);
            }
        }

    }

}
