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
package org.glassfish.main.jnosql.jakartapersistence.mapping.glassfishcontext;

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.DataRepository;
import jakarta.data.repository.Repository;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.persistence.Entity;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.main.jnosql.jakartapersistence.mapping.glassfishcontext.repositories.CombinedRepository;
import org.glassfish.main.jnosql.jakartapersistence.mapping.glassfishcontext.repositories.MyBasicRepository;
import org.glassfish.main.jnosql.jakartapersistence.mapping.glassfishcontext.repositories.MyCrudRepository;
import org.glassfish.main.jnosql.jakartapersistence.mapping.glassfishcontext.repositories.MyDataRepository;
import org.glassfish.main.jnosql.jakartapersistence.mapping.glassfishcontext.repositories.NoInterfaceRepository;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Ondro Mihalyi
 */
public class GlassFishJakartaPersistenceClassScannerTest {

    public GlassFishJakartaPersistenceClassScannerTest() {
    }

    @Test
    public void testVariousRepositories() throws URISyntaxException, IOException, InterruptedException {

        Set<Class<?>> allClasses = new HashSet<>();

        {
            final Set<Class<?>> apiClasses = Set.of(DataRepository.class, BasicRepository.class, CrudRepository.class,
                    Entity.class, Repository.class);
            allClasses.addAll(apiClasses);
        }

        {
            final Set<Class<?>> otherTestClasses = Set.of(MyEntity.class);
            allClasses.addAll(otherTestClasses);
        }

        final Set<Class<?>> standardRepositories = Set.of(
                MyDataRepository.class, MyBasicRepository.class, MyCrudRepository.class);

        final Set<Class<?>> combinedRepositories = Set.of(CombinedRepository.class);

        final Set<Class<?>> customRepositories = new HashSet<>();
        customRepositories.addAll(combinedRepositories);
        customRepositories.addAll(Set.of(NoInterfaceRepository.class));

        Set<Class<?>> allRepositories = new HashSet<>();
        allRepositories.addAll(standardRepositories);
        allRepositories.addAll(combinedRepositories);
        allRepositories.addAll(customRepositories);

        allClasses.addAll(combinedRepositories);
        allClasses.addAll(customRepositories);
        allClasses.addAll(standardRepositories);
        Types types = parseClasses(allClasses);

        configureCDI(types);
        final GlassFishJakartaPersistenceClassScanner scanner = new GlassFishJakartaPersistenceClassScanner();

        final Set<Class<?>> repositoriesStandardResult = scanner.repositoriesStandard();
        assertThat("Standard repositories", repositoriesStandardResult, is(equalTo(standardRepositories)));

        final Set<Class<?>> customRepositoriesResult = scanner.customRepositories();
        assertThat("Custom repositories", customRepositoriesResult, is(equalTo(customRepositories)));

        final Set<Class<?>> repositoriesResult = scanner.repositories();
        assertThat("Repositories", repositoriesResult, is(equalTo(allRepositories)));

        assertThat("Repositories = standard + custom", repositoriesStandardResult.size() + customRepositoriesResult.size(), is(equalTo(repositoriesResult.size())));
    }

    private void configureCDI(Types types) {
        CDI.setCDIProvider(() -> new MockCDI() {
            @Override
            public <U> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
                return new MockInstance<U>() {
                    @Override
                    public U get() {
                        if (subtype.isAssignableFrom(ApplicationContext.class)) {
                            return subtype.cast(new ApplicationContext(types));
                        }
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }
        });
    }

    public static Types parseClasses(Collection<Class<?>> classes) throws IOException {
        ParsingContext context = new ParsingContext.Builder().build();

        for (Class<?> clazz : classes) {
            // Get the bytecode
            String classPath = "/" + clazz.getName().replace('.', '/') + ".class";
            InputStream classBytes = clazz.getResourceAsStream(classPath);

            if (classBytes == null) {
                throw new IOException("Could not find bytecode for " + clazz.getName());
            }

            try {
                // Parse it
                ClassReader classReader = new ClassReader(classBytes);
                URI classUri = URI.create("test://memory/" + clazz.getSimpleName() + ".class");

                classReader.accept(
                        context.getClassVisitor(classUri, clazz.getSimpleName() + ".class", true),
                        ClassReader.SKIP_DEBUG
                );

            } finally {
                classBytes.close();
            }
        }

        // Return the parsed type
        return context.getTypes();

    }

    static class MockCDI extends CDI<Object> {

        @Override
        public BeanManager getBeanManager() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Instance<Object> select(Annotation... qualifiers) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <U> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <U> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isUnsatisfied() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isAmbiguous() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void destroy(Object instance) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Handle<Object> getHandle() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Iterable<? extends Handle<Object>> handles() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Iterator<Object> iterator() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object get() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    static class MockInstance<T> implements Instance<T> {

        @Override
        public Instance<T> select(Annotation... qualifiers) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isUnsatisfied() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isAmbiguous() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void destroy(T instance) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Handle<T> getHandle() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Iterable<? extends Handle<T>> handles() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Iterator<T> iterator() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public T get() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
