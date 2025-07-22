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
import jakarta.persistence.Entity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Ondro Mihalyi
 */
public class GlassFishClassScannerTest {

    public GlassFishClassScannerTest() {
    }

    @Test
    public void testVariousRepositories() throws URISyntaxException, IOException, InterruptedException {

        Set<Class<?>> allClasses = new HashSet<>();
        Set<Class<?>> normalRepositories = new HashSet<>();

        final Set<Class<?>> apiClasses = Set.of(DataRepository.class, BasicRepository.class, CrudRepository.class,
                Entity.class, Repository.class);
        allClasses.addAll(apiClasses);

        final Set<Class<?>> otherTestClasses = Set.of(MyEntity.class);
        allClasses.addAll(otherTestClasses);

        final Set<Class<?>> standardRepositories = Set.of(
                MyDataRepository.class, MyBasicRepository.class, MyCrudRepository.class);
        allClasses.addAll(standardRepositories);
        normalRepositories.addAll(standardRepositories);

        final Set<Class<?>> combinedRepositories = Set.of(CombinedRepository.class);
        allClasses.addAll(combinedRepositories);
        normalRepositories.addAll(combinedRepositories);

        final Set<Class<?>> customRepositories = new HashSet<>();
        customRepositories.addAll(Set.of(NoInterfaceRepository.class));
        customRepositories.addAll(combinedRepositories);
        allClasses.addAll(customRepositories);

        Types types = parseClasses(allClasses);

        final GlassFishClassScanner scanner = new GlassFishClassScanner(types);

        final Set<Class<?>> repositoriesStandardResult = scanner.repositoriesStandard();
        assertTrue(repositoriesStandardResult.equals(standardRepositories), "Standard repositories: " + repositoriesStandardResult);
        final Set<Class<?>> customRepositoriesResult = scanner.customRepositories();
        assertTrue(customRepositoriesResult.equals(customRepositories), "Custom repositories: " + customRepositoriesResult);
        final Set<Class<?>> repositoriesResult = scanner.repositories();
        assertTrue(repositoriesResult.equals(normalRepositories), "Repositories: " + repositoriesResult);
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
}
