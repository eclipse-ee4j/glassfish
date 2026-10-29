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

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

import org.eclipse.jnosql.mapping.core.repository.operations.CoreBaseRepositoryOperationProvider;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreDefaultMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreDeleteOperation;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreInsertOperation;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreProviderOperation;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreSaveOperation;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreUpdateOperation;
import org.eclipse.jnosql.mapping.core.repository.operations.DefaultBuiltInMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.operations.DefaultCustomRepositoryMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.operations.DefaultInfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.core.repository.operations.DefaultObjectMethodOperator;
import org.eclipse.jnosql.mapping.document.DefaultDocumentTemplate;
import org.eclipse.jnosql.mapping.document.DocumentTemplateProducer;
import org.eclipse.jnosql.mapping.document.configuration.DocumentManagerSupplier;
import org.eclipse.jnosql.mapping.reflection.DefaultEntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.ReflectionLifecycleEventHandler;
import org.eclipse.jnosql.mapping.reflection.repository.ReflectionRepositoriesMetadata;
import org.eclipse.jnosql.mapping.semistructured.DefaultEntityConverter;
import org.eclipse.jnosql.mapping.semistructured.DefaultEntityConverterFactory;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.eclipse.jnosql.mapping.semistructured.ProjectorConverter;
import org.eclipse.jnosql.mapping.semistructured.repository.SemiStructuredDeleteOperation;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredCountAllOperation;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredCountByOperation;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredCursorPaginationOperation;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredDeleteByOperation;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredExistsByOperation;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredFindAllOperation;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredFindByOperation;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredParameterBasedOperation;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredQueryBuilder;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredQueryOperation;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredRepositoryOperationProvider;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredRepositoryProducer;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredReturnType;
import org.glassfish.main.jnosql.jakartapersistence.GlassFishJakartaPersistenceClassScanner;

import static org.glassfish.main.jnosql.util.CdiExtensionUtil.addAnnotatedTypes;

/**
 * Registers the CDI beans of the JNoSQL libraries, which both NoSQL repositories and the JNoSQL
 * Jakarta Persistence driver use
 *
 * TODO - If classloader delegation is disabled, veto our beans if the app provides alternatives
 *
 * @author Ondro Mihalyi
 */
public class JNoSqlIntegrationExtension implements Extension {

    /*
     * The CDI beans of the JNoSQL libraries that GlassFish provides. An application that bundles these
     * libraries gets the beans from bean discovery; GlassFish adds them for applications that only bring
     * a NoSQL driver, or use Jakarta Data over Jakarta Persistence.
     */
    static final Class<?>[] JNOSQL_BEANS = {
            // jnosql-mapping-core
            CoreBaseRepositoryOperationProvider.class,
            CoreDefaultMethodOperator.class,
            CoreDeleteOperation.class,
            CoreInsertOperation.class,
            CoreProviderOperation.class,
            CoreSaveOperation.class,
            CoreUpdateOperation.class,
            DefaultBuiltInMethodOperator.class,
            DefaultCustomRepositoryMethodOperator.class,
            DefaultInfrastructureOperatorProvider.class,
            DefaultObjectMethodOperator.class,
            // jnosql-mapping-semistructured
            DefaultEntityConverter.class,
            DefaultEntityConverterFactory.class,
            EventPersistManager.class,
            ProjectorConverter.class,
            SemiStructuredDeleteOperation.class,
            SemistructuredCountAllOperation.class,
            SemistructuredCountByOperation.class,
            SemistructuredCursorPaginationOperation.class,
            SemistructuredDeleteByOperation.class,
            SemistructuredExistsByOperation.class,
            SemistructuredFindAllOperation.class,
            SemistructuredFindByOperation.class,
            SemistructuredParameterBasedOperation.class,
            SemistructuredQueryBuilder.class,
            SemistructuredQueryOperation.class,
            SemistructuredRepositoryOperationProvider.class,
            SemistructuredRepositoryProducer.class,
            SemistructuredReturnType.class,
            // jnosql-mapping-document
            DefaultDocumentTemplate.class,
            DocumentManagerSupplier.class,
            DocumentTemplateProducer.class,
            // jnosql-mapping-reflection
            DefaultEntitiesMetadata.class,
            ReflectionLifecycleEventHandler.class,
            ReflectionRepositoriesMetadata.class
    };

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery) {
        if (new GlassFishNoSqlClassScanner().isEnabled() || new GlassFishJakartaPersistenceClassScanner().isEnabled()) {
            addAnnotatedTypes(beforeBeanDiscovery, JNOSQL_BEANS);
        }
    }

}
