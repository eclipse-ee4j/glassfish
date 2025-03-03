/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
import java.nio.file.Path;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.main.core.kernel.test.KernelJUnitExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.stringContainsInOrder;

@ExtendWith(KernelJUnitExtension.class)
public class CommonClassLoaderServiceImplTest {

    @Inject
    private CommonClassLoaderServiceImpl commonCLService;

    @Inject
    private ServerEnvironment env;

    @Test
    public void testAddingUrlWithNoInitialUrls() throws Exception {
        final String classesPath = "target/test-additional-classes/";
        commonCLService.addToClassPath(new File(classesPath).toURI().toURL());

        // we need to retrieve the classloader after adding URLs, otherwise we
        // would get its parent because of an optimization in the service
        final ClassLoader commonClassLoader = commonCLService.getCommonClassLoader();
        commonClassLoader.loadClass(CommonClassLoaderServiceImplTestAdditionalClass.class.getName());
        assertThat(commonCLService.getCommonClassPath(), containsString(new File(classesPath).getAbsolutePath()));
    }

    @Test
    public void testAddingUrlWithInitialUrl() throws Exception {
        // the classloader should already be the one we want, initialized with classes in domain/lib/classes
        final ClassLoader commonClassLoader = commonCLService.getCommonClassLoader();

        final String classesPath = "target/test-additional-classes/";
        commonCLService.addToClassPath(new File(classesPath).toURI().toURL());

        commonClassLoader.loadClass(CommonClassLoaderServiceImplTestAdditionalClass.class.getName());
        commonClassLoader.loadClass(CommonClassLoaderServiceImplTestDomainClass.class.getName());
        assertThat(commonCLService.getCommonClassPath(),
            stringContainsInOrder(
                env.getInstanceRoot().toPath().resolve(Path.of("lib", "classes")).toString(),
                new File(classesPath).getAbsolutePath()));
    }
}
