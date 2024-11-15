/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.cdi.hk2;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;
import jakarta.inject.Singleton;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;

import static org.glassfish.cdi.hk2.HK2IntegrationUtilities.convertInjectionPointToInjectee;

/**
 * A portable CDI extension, which is the touch-point for hk2 integration with CDI
 *
 * This extension is registered via META-INF/services/jakarta.enterprise.inject.spi.Extension
 *
 * @author jwells
 *
 */
public class HK2IntegrationExtension implements Extension {
    private final HashMap<Long, ActiveDescriptor<?>> foundWithHK2 = new HashMap<>();
    private final ServiceLocator locator = HK2IntegrationUtilities.getApplicationServiceLocator();
    protected static final Logger logger = Logger.getLogger("org.glassfish.cdi");
    /**
     * Called by CDI, gathers up all of the injection points known to hk2.
     *
     * @param pit The injection target even from CDI
     */
    @SuppressWarnings("unused")
    private <T> void injectionTargetObserver(@Observes ProcessInjectionTarget<T> pit) {
        if (locator == null) {
            return;
        }

        InjectionTarget<?> injectionTarget = pit.getInjectionTarget();
        Set<InjectionPoint> injectionPoints = injectionTarget.getInjectionPoints();

        for (InjectionPoint injectionPoint : injectionPoints) {
            logger.fine("Processing injection point " + injectionPoint);
            Injectee injectee = convertInjectionPointToInjectee(injectionPoint);

            ActiveDescriptor<?> descriptor = locator.getInjecteeDescriptor(injectee);
            if (descriptor == null || descriptor.getServiceId() == null) {
                logger.fine("Could not find descriptor for " + injectee);
                continue;
            }

            // using a map removes duplicates
            foundWithHK2.put(descriptor.getServiceId(), descriptor);
        }
    }

    /**
     * Called by CDI after going through all of the injection points. For each service known to hk2, adds a CDI bean.
     *
     * @param afterBeanDiscovery This is used just to mark the type of the event
     */
    @SuppressWarnings({ "unused", "unchecked", "rawtypes" })
    private void afterDiscoveryObserver(@Observes AfterBeanDiscovery afterBeanDiscovery) {
        if (locator == null) {
            return;
        }

        HashSet<Class<? extends Annotation>> customScopes = new HashSet<>();

        for (ActiveDescriptor<?> descriptor : foundWithHK2.values()) {
            afterBeanDiscovery.addBean(new HK2CDIBean(locator, descriptor));

            customScopes.add(descriptor.getScopeAnnotation());
        }

        customScopes.remove(PerLookup.class);
        customScopes.remove(Singleton.class);

        List<org.glassfish.hk2.api.Context> hk2Contexts = locator.getAllServices(org.glassfish.hk2.api.Context.class);

        for (org.glassfish.hk2.api.Context hk2Context : hk2Contexts) {
            if (!customScopes.contains(hk2Context.getScope())) {
                continue;
            }

            afterBeanDiscovery.addContext(new HK2ContextBridge(hk2Context));
        }

        foundWithHK2.clear(); // No need to keep these references around
    }

    /**
     * Called by CDI after it has been completely validated. Will add the JIT resolver to HK2 with the BeanManager
     *
     * @param event This is just to mark the type of the event
     * @param manager The manager that will be used to get references
     */
    @SuppressWarnings("unused")
    private void afterDeploymentValidation(@Observes AfterDeploymentValidation event) {
        if (locator == null) {
            return;
        }

        DynamicConfiguration config = locator.getService(DynamicConfigurationService.class).createDynamicConfiguration();

        config.addActiveDescriptor(CDISecondChanceResolver.class);
        config.addActiveDescriptor(CDIContextBridge.class);

        config.commit();
    }

    @Override
    public String toString() {
        return "HK2IntegrationExtension(" + locator + "," + System.identityHashCode(this) + ")";
    }
}
