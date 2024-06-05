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
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.test.DolJunit5Extension;

import jakarta.inject.Inject;

import java.util.List;
import java.util.logging.Level;

import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.main.jul.handler.LogCollectorHandler;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.glassfish.tests.utils.junit.Classes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author David Matejcek
 */
@ExtendWith(DolJunit5Extension.class)
@Classes({Application.class})
class ApplicationValidatorTest {

    private static final java.util.logging.Logger LOG = DOLUtils.getDefaultLogger();

    @Inject
    private Application application;

    private LogCollectorHandler logCollector;

    @BeforeEach
    public void initLogger() {
        logCollector = new LogCollectorHandler(LOG);
        logCollector.setLevel(Level.WARNING);
    }


    @AfterEach
    public void resetLogger() {
        if (logCollector != null) {
            logCollector.close();
            logCollector = null;
        }
    }


    @Test
    void invalid() {
        ApplicationValidator validator = new ApplicationValidator();
        assertThrows(IllegalArgumentException.class, () -> validator.accept((BundleDescriptor) application));
    }


    @Test
    void valid_warWithLocalEjbIncluded() {
        application.setAppName("testAppName");
        application.setName("test");

        final FakeWebBundleDescriptor war = createWarBundle("test-war-name");
        final DataSourceDefinitionDescriptor ds = new DataSourceDefinitionDescriptor();
        ds.setName("java:app/jdbc/testdb");
        war.addResourceDescriptor(ds);

        final FakeEjbDescriptor bean1 = new FakeEjbDescriptor("Bean001");
        bean1.setLocalClassName("org.acme.FakeIface001");
        bean1.setLocalBean(true);
        final EjbReferenceDescriptor refBean1 = new EjbReferenceDescriptor("Bean001", null, bean1, true);
        war.addEjbReferenceDescriptor(refBean1);


        final FakeEjbDescriptor bean2 = new FakeEjbDescriptor("Bean002");
        bean2.setLocalClassName("org.acme.FakeIface002");
        bean2.setLocalBean(true);
        final EjbReferenceDescriptor refBean2 = new EjbReferenceDescriptor("Bean002", null, bean2, true);
        war.addEjbReferenceDescriptor(refBean2);

        application.addBundleDescriptor(war);

        final ApplicationValidator validator = new ApplicationValidator();
        assertDoesNotThrow(() -> validator.accept((BundleDescriptor) application));
    }


    @Test
    void duplicitJdbcResource() {
        application.setAppName("testAppName");
        application.setName("test");

        final DataSourceDefinitionDescriptor ds1 = new DataSourceDefinitionDescriptor();
        ds1.setName("java:app/jdbc/testdb");
        final DataSourceDefinitionDescriptor ds2 = new DataSourceDefinitionDescriptor();
        ds2.setName("java:app/jdbc/testdb");

        final FakeEjbBundleDescriptor ejbJar = new FakeEjbBundleDescriptor();
        ejbJar.setName("test-ejb-jar-name");
        ejbJar.setModuleDescriptor(createEjbJarModuleDescriptor(ejbJar));

        final FakeEjbBundleDescriptor ejbJar2 = new FakeEjbBundleDescriptor();
        ejbJar2.setName("test-ejb-jar2-name");
        ejbJar2.setModuleDescriptor(createEjbJarModuleDescriptor(ejbJar2));

        ejbJar.addResourceDescriptor(ds1);
        ejbJar2.addResourceDescriptor(ds2);

        application.addBundleDescriptor(ejbJar);
        application.addBundleDescriptor(ejbJar2);

        final ApplicationValidator validator = new ApplicationValidator();
        assertDoesNotThrow(() -> validator.accept((BundleDescriptor) application));
        List<GlassFishLogRecord> logs = logCollector.getAll();
        assertThat("Logs: " + logs, logs, hasSize(1));
        assertThat(logs.get(0).getMessage(),
            equalTo("JNDI name java:app/jdbc/testdb is declared by multiple modules of the application testAppName."
                + " Scopes: EBDLevel:testAppName#test-ejb-jar-name.jar, EBDLevel:testAppName#test-ejb-jar2-name.jar"));
    }


    @Test
    void sameJdbcResource() {
        application.setAppName("testAppName");
        application.setName("test");

        final DataSourceDefinitionDescriptor ds = new DataSourceDefinitionDescriptor();
        ds.setName("java:app/jdbc/testdb");

        final FakeEjbBundleDescriptor ejbJar = new FakeEjbBundleDescriptor();
        ejbJar.setName("test-ejb-jar-name");
        ejbJar.setModuleDescriptor(createEjbJarModuleDescriptor(ejbJar));

        final FakeEjbBundleDescriptor ejbJar2 = new FakeEjbBundleDescriptor();
        ejbJar2.setName("test-ejb-jar2-name");
        ejbJar2.setModuleDescriptor(createEjbJarModuleDescriptor(ejbJar2));

        ejbJar.addResourceDescriptor(ds);
        ejbJar2.addResourceDescriptor(ds);
        application.addBundleDescriptor(ejbJar);
        application.addBundleDescriptor(ejbJar2);

        final ApplicationValidator validator = new ApplicationValidator();
        assertDoesNotThrow(() -> validator.accept((BundleDescriptor) application));
        List<GlassFishLogRecord> logs = logCollector.getAll();
        assertThat("Logs: " + logs, logs, hasSize(0));
    }


    @Test
    void conflictingJdbcResourceScopes() {
        application.setAppName("testAppName");
        application.setName("test");

        final DataSourceDefinitionDescriptor ds = new DataSourceDefinitionDescriptor();
        ds.setName("java:app/jdbc/testdb");
        final DataSourceDefinitionDescriptor ds2 = new DataSourceDefinitionDescriptor();
        ds2.setName("java:comp/jdbc/testdb");

        final FakeEjbBundleDescriptor ejbJar = new FakeEjbBundleDescriptor();
        ejbJar.setName("test-ejb-jar-name");
        ejbJar.setModuleDescriptor(createEjbJarModuleDescriptor(ejbJar));
        ejbJar.addResourceDescriptor(ds);

        final FakeEjbBundleDescriptor ejbJar2 = new FakeEjbBundleDescriptor();
        ejbJar2.setName("test-ejb-jar2-name");
        ejbJar2.setModuleDescriptor(createEjbJarModuleDescriptor(ejbJar2));
        ejbJar2.addResourceDescriptor(ds2);

        application.addBundleDescriptor(ejbJar);
        application.addBundleDescriptor(ejbJar2);

        final ApplicationValidator validator = new ApplicationValidator();
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> validator.accept((BundleDescriptor) application));
        assertThat(exception.getMessage(), equalTo(
            "Application validation fails for given application testAppName for jndi-name java:comp/jdbc/testdb"));
        List<GlassFishLogRecord> logs = logCollector.getAll();
        assertThat("Logs: " + logs, logs, hasSize(1));
        assertThat(logs.get(0).getMessage(),
            equalTo("DEP0005:Deployment failed due to the invalid scope EBDLevel:testAppName#test-ejb-jar2-name.jar"
                + " defined for jndi-name: java:comp/jdbc/testdb"));
    }


    private ModuleDescriptor<RootDeploymentDescriptor> createEjbJarModuleDescriptor(final FakeEjbBundleDescriptor ejbJar) {
        final ModuleDescriptor<RootDeploymentDescriptor> module = new ModuleDescriptor<>();
        module.setDescriptor(ejbJar);
        module.setModuleName(ejbJar.getName());
        module.setModuleType(DOLUtils.ejbType());
        module.setArchiveUri(ejbJar.getName() + ".jar");
        return module;
    }


    private FakeWebBundleDescriptor createWarBundle(final String name) {
        final FakeWebBundleDescriptor war = new FakeWebBundleDescriptor();
        war.setName(name);
        final ModuleDescriptor<RootDeploymentDescriptor> module = createWarModuleDescriptor(war);
        war.setModuleDescriptor(module);
        return war;
    }


    private ModuleDescriptor<RootDeploymentDescriptor> createWarModuleDescriptor(final FakeWebBundleDescriptor war) {
        final ModuleDescriptor<RootDeploymentDescriptor> warModule = new ModuleDescriptor<>();
        warModule.setDescriptor(war);
        warModule.setModuleName(war.getName());
        warModule.setModuleType(DOLUtils.warType());
        warModule.setArchiveUri(war.getName() + ".war");
        warModule.setContextRoot("/");
        return warModule;
    }


    private static final class FakeEjbDescriptor extends EjbBeanDescriptor {
        private static final long serialVersionUID = 1L;
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
