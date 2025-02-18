/*
 * Copyright (c) 2024 Contributors to Eclipse Foundation.
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
package org.glassfish.microprofile.health.service;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBean;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.Startup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.internal.api.Globals;
import org.glassfish.microprofile.health.HealthReporter;


public class CollectHealthChecksExtension implements Extension {

    private final HealthReporter service;
    private final Set<Bean<HealthCheck>> healthChecks = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public CollectHealthChecksExtension() {
        ServiceLocator defaultBaseServiceLocator = Globals.getDefaultBaseServiceLocator();
        HealthReporter healthReporterService = defaultBaseServiceLocator.getService(HealthReporter.class);
        if (healthReporterService == null) {
            ServiceLocatorUtilities.addClasses(defaultBaseServiceLocator, true, HealthReporter.class);
            healthReporterService = defaultBaseServiceLocator.getService(HealthReporter.class);
        }
        service = healthReporterService;
    }
    public <T> void processBeans(@Observes ProcessBean<T> beans) {
        Annotated annotated = beans.getAnnotated();
        if (annotated.isAnnotationPresent(Liveness.class) ||
                annotated.isAnnotationPresent(Readiness.class) ||
                annotated.isAnnotationPresent(Startup.class)) {
            if (beans.getBean().getTypes().contains(HealthCheck.class)) {
                healthChecks.add((Bean<HealthCheck>) beans.getBean());
            }
        }
    }

    public void afterDeploymentValidation(@Observes AfterDeploymentValidation adv, BeanManager beanManager) {
        healthChecks.forEach(bean -> {
            CreationalContext<HealthCheck> creationalContext = beanManager.createCreationalContext(bean);
            service.addHealthCheck("", bean.create(creationalContext));
        });
    }



}
