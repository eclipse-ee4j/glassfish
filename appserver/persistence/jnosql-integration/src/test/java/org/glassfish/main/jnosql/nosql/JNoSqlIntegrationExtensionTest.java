/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.junit.jupiter.api.Test;

import static org.glassfish.main.jnosql.util.LibraryBeans.applicationScopedClassesIn;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * An application that bundles JNoSQL gets its beans from bean discovery. GlassFish lists them instead,
 * so a bean that a JNoSQL upgrade adds or renames has to be added to that list as well.
 */
class JNoSqlIntegrationExtensionTest {

    /** A class from each JNoSQL library that GlassFish provides */
    private static final Class<?>[] LIBRARIES = {
            Converters.class, EntityConverter.class, DocumentTemplate.class, Reflections.class};

    /** Registered by the Jakarta Persistence integration, or not used as a bean */
    private static final Set<Class<?>> NOT_REGISTERED = Set.of(Converters.class, Reflections.class);

    @Test
    void registersTheBeansOfTheJNoSqlLibraries() throws Exception {
        Set<Class<?>> beans = new HashSet<>();
        for (Class<?> library : LIBRARIES) {
            beans.addAll(applicationScopedClassesIn(library));
        }
        beans.removeAll(NOT_REGISTERED);

        assertThat(Set.of(JNoSqlIntegrationExtension.JNOSQL_BEANS), equalTo(beans));
    }
}
