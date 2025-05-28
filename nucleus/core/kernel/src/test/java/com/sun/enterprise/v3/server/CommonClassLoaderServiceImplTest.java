/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation
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
package com.sun.enterprise.v3.server;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.main.core.kernel.test.KernelJUnitExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Note: The CommonClassLoaderServiceImpl is stateful, so every change affects other tests.
 *
 * @author Ondro Mihaliy
 * @author David Matejcek
 */
@ExtendWith(KernelJUnitExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class CommonClassLoaderServiceImplTest {
    private static final Logger LOG = System.getLogger(CommonClassLoaderServiceImplTest.class.getName());

    @TempDir
    private static File tmpDir;
    private static Path tmpClassesDir;

    @Inject
    private CommonClassLoaderServiceImpl commonCLService;

    @Inject
    private ServerEnvironment env;

    private Path libClasses;


    @BeforeAll
    static void initAdditionalClassesDir() throws Exception {
        tmpClassesDir = new File(tmpDir, "additional classes dir").toPath();
        copyToAdditionalClasses(CommonClassLoaderServiceImplTestAdditionalClass.class, tmpClassesDir);
    }

    @BeforeEach
    void init() throws Exception {
        commonCLService.acls = new APIClassLoaderServiceImpl() {

            @Override
            public ClassLoader getAPIClassLoader() {
                return new URLClassLoader(new URL[0], null);
            }

        };
        libClasses = env.getInstanceRoot().toPath().resolve(Path.of("lib", "classes"));
    }


    @Test
    @Order(10)
    void testAddingEmptyPath() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> commonCLService.addToClassPath(new URL("file://")));
        String classpath = commonCLService.getCommonClassPath();
        assertThat(classpath, emptyString());
    }


    /**
     * We need to retrieve the classloader after adding URLs, otherwise we
     * would get its parent because of an optimization in the service
     */
    @Test
    @Order(20)
    void testAddingUrlWithNoInitialUrls() throws Exception {
        commonCLService.postConstruct();

        final ClassLoader origClassLoader = commonCLService.getCommonClassLoader();
        commonCLService.addToClassPath(tmpClassesDir.toUri().toURL());
        String classpath = commonCLService.getCommonClassPath();
        LOG.log(INFO, "classpath: {0}", classpath);
        assertThat(classpath, containsString(tmpClassesDir.toString()));

        final ClassLoader commonClassLoader = commonCLService.getCommonClassLoader();
        assertNotSame(origClassLoader, commonClassLoader);
        assertNotNull(commonClassLoader.loadClass(CommonClassLoaderServiceImplTestAdditionalClass.class.getName()));
    }


    /**
     * The classloader should already be the one we want, initialized with classes in
     * domain/lib/classes
     */
    @Test
    @Order(30)
    void testAddingUrlWithInitialUrl() throws Exception {
        copyToAdditionalClasses(CommonClassLoaderServiceImplTestDomainClass.class, libClasses);
        commonCLService.postConstruct();

        // Don't move this line below commonCLService.addToClassPath()
        final ClassLoader commonClassLoader = commonCLService.getCommonClassLoader();
        commonCLService.addToClassPath(tmpClassesDir.toUri().toURL());
        String[] classpath = commonCLService.getCommonClassPath().split(File.pathSeparator);
        LOG.log(INFO, "classpath: {0}", Arrays.toString(classpath));
        assertThat(classpath, arrayContaining(libClasses.toString(), tmpClassesDir.toString()));

        assertNotNull(commonClassLoader.loadClass(CommonClassLoaderServiceImplTestAdditionalClass.class.getName()));
        assertNotNull(commonClassLoader.loadClass(CommonClassLoaderServiceImplTestDomainClass.class.getName()));
    }


    private static void copyToAdditionalClasses(Class<?> sourceClass, Path targetClasses) throws IOException {
        String relativePath = sourceClass.getName().replace('.', File.separatorChar) + ".class";
        try (InputStream in = sourceClass.getClassLoader().getResourceAsStream(relativePath)) {
            Path targetPath = targetClasses.resolve(relativePath);
            Files.createDirectories(targetPath.getParent());
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
