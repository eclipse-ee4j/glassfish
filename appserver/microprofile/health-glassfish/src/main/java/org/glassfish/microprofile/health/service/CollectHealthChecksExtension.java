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
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.Startup;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.internal.api.Globals;
import org.glassfish.microprofile.health.HealthCheckInfo;
import org.glassfish.microprofile.health.HealthReporter;


public class CollectHealthChecksExtension implements Extension {
    private final HealthReporter service;
    private final Set<HealthCheckBeanAndKind> healthChecks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final InvocationManager invocationManager;

    record HealthCheckBeanAndKind(Bean<HealthCheck> bean, Set<HealthCheckInfo.Kind> kind) {
    }

    public CollectHealthChecksExtension() {
        ServiceLocator defaultBaseServiceLocator = Globals.getDefaultBaseServiceLocator();
        invocationManager = Globals.get(InvocationManager.class);
        HealthReporter healthReporterService = defaultBaseServiceLocator.getService(HealthReporter.class);
        if (healthReporterService == null) {
            ServiceLocatorUtilities.addClasses(defaultBaseServiceLocator, true, HealthReporter.class);
            healthReporterService = defaultBaseServiceLocator.getService(HealthReporter.class);
        }
        service = healthReporterService;
    }
    public <T> void processBeans(@Observes ProcessBean<T> beans) {
        Annotated annotated = beans.getAnnotated();
        boolean livenessPresent = annotated.isAnnotationPresent(Liveness.class);
        boolean readinessPresent = annotated.isAnnotationPresent(Readiness.class);
        boolean startupPresent = annotated.isAnnotationPresent(Startup.class);
        if (livenessPresent || readinessPresent || startupPresent) {
            Bean<T> bean = beans.getBean();
            if (bean.getTypes().contains(HealthCheck.class)) {
                Set<HealthCheckInfo.Kind> kinds = EnumSet.noneOf(HealthCheckInfo.Kind.class);
                if (livenessPresent) {
                    kinds.add(HealthCheckInfo.Kind.LIVE);
                }
                if (readinessPresent) {
                    kinds.add(HealthCheckInfo.Kind.READY);
                }
                if (startupPresent) {
                    kinds.add(HealthCheckInfo.Kind.STARTUP);
                }

                healthChecks.add(new HealthCheckBeanAndKind((Bean<HealthCheck>) bean, kinds));
            }
        }
    }

    public void afterDeploymentValidation(@Observes AfterDeploymentValidation adv, BeanManager beanManager) {
        healthChecks.forEach(healthCheckBeanAndKind -> {
            Bean<HealthCheck> bean = healthCheckBeanAndKind.bean();
            CreationalContext<HealthCheck> creationalContext = beanManager.createCreationalContext(bean);
            if (beanManager.getReference(bean, HealthCheck.class, creationalContext) instanceof HealthCheck healthCheck) {
                service.addHealthCheck(invocationManager.getCurrentInvocation().getAppName(),
                        new HealthCheckInfo(healthCheck, healthCheckBeanAndKind.kind()));
            }
        });
    }
}
