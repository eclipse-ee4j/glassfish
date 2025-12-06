/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.main.jnosql.nosql;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.configurator.BeanConfigurator;
import jakarta.interceptor.Interceptor;

import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.glassfish.main.jnosql.jakartapersistence.JakartaPersistenceIntegrationExtension;
import org.glassfish.main.jnosql.nosql.metadata.NoSqlEntitiesMetadata;
import org.glassfish.main.jnosql.nosql.metadata.reflection.ReflectionGroupEntityMetadata;
import org.glassfish.main.jnosql.util.CdiExtensionUtil;

import static org.glassfish.main.jnosql.util.CdiExtensionUtil.addBean;

/**
 * Registers JNoSQL CDI beans that are needed for JNoSQL but not for Jakarta
 * Data over JPA
 *
 * TODO - veto JNoSQL CDI beans provided by the app if they conflict with beans
 * registered by this extension. If delegation is disabled, veto our beans
 * instead
 *
 * @author Ondro Mihalyi
 */
public class JNoSqlIntegrationExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(JakartaPersistenceIntegrationExtension.class.getName());

    /* Must be triggered before the JakartaPersistenceExtension from JNoSQL to register the GlassFishClassScanner
       before it's used there
     */
    void afterBeanDiscovery(@Observes @Priority(Interceptor.Priority.LIBRARY_BEFORE) AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {

        defineJNoSqlBeans(afterBeanDiscovery, beanManager);
    }

    /*
     * Exposes all beans we need from JNoSQL - defined in dependencies external to GlassFish
     * Exposes them as alternatives so that they override any beans defined in the app
     */
    private void defineJNoSqlBeans(AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {

        List<BeanConfigurator<?>> configurations = List.of(
                addBean(ReflectionGroupEntityMetadata.class, afterBeanDiscovery, beanManager),
                addBean(NoSqlEntitiesMetadata.class, afterBeanDiscovery, beanManager),
                addBean(GlassFishEntityConverter.class, afterBeanDiscovery, beanManager)
                        .types(EntityConverter.class),
                addBean(EventPersistManager.class, afterBeanDiscovery, beanManager),
                addBean(GlassFishDocumentTemplate.class, afterBeanDiscovery, beanManager)
                .types(DocumentTemplate.class)
                .qualifiers(Default.Literal.INSTANCE, DatabaseQualifier.ofDocument())
        );
        for (BeanConfigurator<?> configurator : configurations) {
            configurator
                    .scope(ApplicationScoped.class)
                    // enable as alternative to override beans in case they are added as application libraries
                    .alternative(true)
                    .priority(CdiExtensionUtil.INTEGRATION_BEANS_PRIORITY);
        }
    }

}
