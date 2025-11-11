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
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.interceptor.Interceptor;

import java.util.logging.Logger;

import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.glassfish.main.jnosql.jakartapersistence.JakartaPersistenceIntegrationExtension;
import org.glassfish.main.jnosql.nosql.metadata.NoSqlEntitiesMetadata;

import static org.glassfish.main.jnosql.util.CdiExtensionUtil.addBean;

/**
 * Registers JNoSQL CDI beans that are needed for JNoSQL but not for Jakarta Data over JPA
 *
 * TODO - veto JNoSQL CDI beans provided by the app if they conflict with beans registered by this extension.
 * If delegation is disabled, veto our beans instead
 * @author Ondro Mihalyi
 */
public class JNoSqlIntegrationExtension implements Extension {
    private static final Logger LOGGER = Logger.getLogger(JakartaPersistenceIntegrationExtension.class.getName());

    /* Must be triggered before the JakartaPersistenceExtension from JNoSQL to register the GlassFishClassScanner
       before it's used there
     */
    void afterBeanDiscovery(@Observes @Priority(Interceptor.Priority.LIBRARY_BEFORE) AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {

        addBean(NoSqlEntitiesMetadata.class, afterBeanDiscovery, beanManager)
                .types(EntitiesMetadata.class)
                .scope(ApplicationScoped.class);

        addBean(GlassFishEntityConverter.class, afterBeanDiscovery, beanManager)
                .types(EntityConverter.class)
                .scope(ApplicationScoped.class);

        addBean(EventPersistManager.class, afterBeanDiscovery, beanManager)
                .scope(ApplicationScoped.class);
    }

}
