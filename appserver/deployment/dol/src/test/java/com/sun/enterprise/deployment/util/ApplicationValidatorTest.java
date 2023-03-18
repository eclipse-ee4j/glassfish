/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.DataSourceDefinitionDescriptor;
import com.sun.enterprise.deployment.EjbBeanDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.test.DolJunit5Extension;

import jakarta.inject.Inject;

import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.tests.utils.junit.Classes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author David Matejcek
 */
@ExtendWith(DolJunit5Extension.class)
@Classes({Application.class})
class ApplicationValidatorTest {

    @Inject
    private Application application;


    @Test
    void invalid() {
        ApplicationValidator validator = new ApplicationValidator();
        assertThrows(IllegalArgumentException.class, () -> validator.accept((BundleDescriptor) application));
    }


    @Test
    void valid_warWithEjbIncluded() {
        application.setAppName("testAppName");
        application.setName("test");

        final FakeWebBundleDescriptor war = new FakeWebBundleDescriptor();
        war.setName("test-war-name");
        final DataSourceDefinitionDescriptor ds = new DataSourceDefinitionDescriptor();
        ds.setName("java:app/jdbc/testdb");
        war.addResourceDescriptor(ds);
        application.addBundleDescriptor(war);

        final ApplicationValidator validator = new ApplicationValidator();
        assertDoesNotThrow(() -> validator.accept((BundleDescriptor) application));
    }

    @Test
    void invalid_warAndEjbJars() {
        application.setAppName("testAppName");
        application.setName("test");

        BundleDescriptor war = new FakeWebBundleDescriptor();
        war.setName("test-war-name");

        FakeEjbBundleDescriptor ejbJar = new FakeEjbBundleDescriptor();
        ejbJar.setName("test-ejb-jar-name");
        ModuleDescriptor<RootDeploymentDescriptor> ejbModDescriptor = new ModuleDescriptor<>();
        ejbModDescriptor.setModuleName("test-ejb-jar");
        ejbJar.setModuleDescriptor(ejbModDescriptor);
        DataSourceDefinitionDescriptor ds = new DataSourceDefinitionDescriptor();
        ds.setName("java:app/jdbc/testdb");
        ejbJar.addResourceDescriptor(ds);
        war.addBundleDescriptor(ejbJar);

        FakeEjbBundleDescriptor ejbJar2 = new FakeEjbBundleDescriptor();
        ejbJar2.setName("test-ejb-jar2-name");
        ModuleDescriptor<RootDeploymentDescriptor> ejbModDescriptor2 = new ModuleDescriptor<>();
        ejbModDescriptor2.setModuleName("test-ejb-jar2");
        ejbJar2.setModuleDescriptor(ejbModDescriptor2);
        DataSourceDefinitionDescriptor ds2 = new DataSourceDefinitionDescriptor();
        ds2.setName("java:comp/jdbc/testdb");
        ejbJar2.addResourceDescriptor(ds2);
        war.addBundleDescriptor(ejbJar2);
        application.addBundleDescriptor(war);

        ApplicationValidator validator = new ApplicationValidator();
        assertDoesNotThrow(() -> validator.accept((BundleDescriptor) application));
    }


    @Test
    void valid_warAndEjbJar() {
        application.setAppName("testAppName");
        application.setName("test");

        BundleDescriptor war = new FakeWebBundleDescriptor();
        war.setName("test-war-name");
        application.addBundleDescriptor(war);

        FakeEjbBundleDescriptor ejbJar = new FakeEjbBundleDescriptor();
        ejbJar.setName("test-ejb-jar-name");
        ModuleDescriptor<RootDeploymentDescriptor> ejbModDescriptor = new ModuleDescriptor<>();
        ejbModDescriptor.setModuleName("test-ejb-jar");
        ejbJar.setModuleDescriptor(ejbModDescriptor);
        DataSourceDefinitionDescriptor ds = new DataSourceDefinitionDescriptor();
        ds.setName("java:app/jdbc/testdb");
        ejbJar.addResourceDescriptor(ds);
        application.addBundleDescriptor(ejbJar);

        FakeEjbBundleDescriptor ejbJar2 = new FakeEjbBundleDescriptor();
        ejbJar2.setName("test-ejb-jar2-name");
        ModuleDescriptor<RootDeploymentDescriptor> ejbModDescriptor2 = new ModuleDescriptor<>();
        ejbModDescriptor2.setModuleName("test-ejb-jar2");
        ejbJar2.setModuleDescriptor(ejbModDescriptor2);
        DataSourceDefinitionDescriptor ds2 = new DataSourceDefinitionDescriptor();
        ds2.setName("java:app/jdbc/testdb");
        ejbJar2.addResourceDescriptor(ds2);
        application.addBundleDescriptor(ejbJar2);

        ApplicationValidator validator = new ApplicationValidator();
        assertDoesNotThrow(() -> validator.accept((BundleDescriptor) application));
    }


    private static final class FakeEjbDescriptor extends EjbBeanDescriptor {
        private EjbBundleDescriptor bundle;

        FakeEjbDescriptor(String ejbName) {
            setName(ejbName);
        }

        @Override
        public EjbBundleDescriptor getEjbBundleDescriptor() {
            return bundle;
        }

        @Override
        public void setEjbBundleDescriptor(EjbBundleDescriptor ejbBundleDescriptor) {
            this.bundle = ejbBundleDescriptor;
        }

        @Override
        public String getType() {
            return "fake";
        }
    }

    private static final class FakeEjbBundleDescriptor extends EjbBundleDescriptor {

        private static final long serialVersionUID = 1L;

        @Override
        public String getDefaultSpecVersion() {
            return "0.0";
        }


        @Override
        protected EjbDescriptor createDummyEjbDescriptor(String ejbName) {
            return new FakeEjbDescriptor(ejbName);
        }
    }

    private static final class FakeWebBundleDescriptor extends WebBundleDescriptor {

        private static final long serialVersionUID = 1L;

        @Override
        public void addJndiNameEnvironment(JndiNameEnvironment env) {
            throw new UnsupportedOperationException("Merging other descriptors is not supported");
        }


        @Override
        protected void addCommonWebBundleDescriptor(WebBundleDescriptor wbd, boolean defaultDescriptor) {
            throw new UnsupportedOperationException("Merging other descriptors is not supported");
        }


        @Override
        public String getDefaultSpecVersion() {
            return "0.0";
        }
    }
}
