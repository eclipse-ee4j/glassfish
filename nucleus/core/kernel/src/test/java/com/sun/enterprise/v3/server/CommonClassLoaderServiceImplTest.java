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

import com.sun.enterprise.module.bootstrap.StartupContext;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class CommonClassLoaderServiceImplTest {

    private int loadClassCalls;
    private int getResourceCalls;

    CommonClassLoaderServiceImpl commonCLService;
    MockServerEnvironment serverEnv;

    @BeforeEach
    public void setUp() {
        commonCLService = new CommonClassLoaderServiceImpl();
        commonCLService.acls = new APIClassLoaderServiceImpl() {
            @Override
            public ClassLoader getAPIClassLoader() {
                return new URLClassLoader(new URL[0], null);
            }

        };
        serverEnv = new MockServerEnvironment();
        commonCLService.env = serverEnv;
    }

    @Test
    public void testAddingUrlWithNoInitialUrls() throws MalformedURLException, ClassNotFoundException {
        commonCLService.postConstruct();

        final String classesPath = "target/test-additional-classes/";
        commonCLService.addToClassPath(new File(classesPath).toURI().toURL());

// we need to retrieve the classloader after adding URLs, otherwise we
        // would get its parent because of an optimization in the service
        final ClassLoader commonClassLoader = commonCLService.getCommonClassLoader();
        commonClassLoader.loadClass(CommonClassLoaderServiceImplTestAdditionalClass.class.getName());
        assertThat(commonCLService.getCommonClassPath(), containsString(new File(classesPath).getAbsolutePath()));
    }

    @Test
    public void testAddingUrlWithInitialUrl() throws MalformedURLException, ClassNotFoundException {
        final String domainDir = "target/test-domain";
        serverEnv.setInstanceRoot(new File(domainDir));
        commonCLService.postConstruct();
        // the classloader should already be the one we want, initialized with classes in domain/lib/classes
        final ClassLoader commonClassLoader = commonCLService.getCommonClassLoader();

        final String classesPath = "target/test-additional-classes/";
        commonCLService.addToClassPath(new File(classesPath).toURI().toURL());

        commonClassLoader.loadClass(CommonClassLoaderServiceImplTestAdditionalClass.class.getName());
        commonClassLoader.loadClass(CommonClassLoaderServiceImplTestDomainClass.class.getName());
        assertThat(commonCLService.getCommonClassPath(), containsString(new File(classesPath).getAbsolutePath()));
        assertThat(commonCLService.getCommonClassPath(), containsString(Path.of(domainDir,"lib","classes").toAbsolutePath().toString()));
    }

    static class MockServerEnvironment implements ServerEnvironment {

        File instanceRoot;

        @Override
        public File getInstanceRoot() {
            return instanceRoot;
        }

        public void setInstanceRoot(File instanceRoot) {
            this.instanceRoot = instanceRoot;
        }

        @Override
        public StartupContext getStartupContext() {
            return null;
        }

        @Override
        public File getConfigDirPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File getLibPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File getApplicationRepositoryPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File getApplicationStubPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File getApplicationCompileJspPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File getApplicationGeneratedXMLPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File getApplicationEJBStubPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File getApplicationPolicyFilePath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File getApplicationAltDDPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File getMasterPasswordFile() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File getJKS() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File getTrustStore() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Status getStatus() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public RuntimeType getRuntimeType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getInstanceName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isInstance() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isDas() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
