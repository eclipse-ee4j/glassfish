/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.deployment.admin;

import com.sun.enterprise.config.serverbeans.AppTenants;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationConfig;
import com.sun.enterprise.config.serverbeans.ApplicationExtension;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Resources;

import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.glassfish.api.deployment.DeployCommandParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * junit test to test ListComponentsCommand class
 */
public class ListComponentsCommandTest {

    private ListComponentsCommand lcc;

    @BeforeEach
    public void setup() {
        lcc = new ListComponentsCommand();
    }


    @Test
    @Disabled("FIXME: Reproduces NPE in ListComponentsCommand.getAppEngines")
    public void isApplicationOfThisTypeTest() throws Exception {
        ApplicationTest app = new ApplicationTest();
        Engine eng = new EngineTest();
        eng.setSniffer("web");
        List<Engine> engines = new ArrayList<>();
        engines.add(eng);
        List<Module> modules = new ArrayList<>();
        ModuleTest aModule = new ModuleTest();
        aModule.setEngines(engines);
        modules.add(aModule);
        app.setModules(modules);

        assertTrue(lcc.isApplicationOfThisType(app, "web"), "test app with sniffer engine=web");
        assertFalse(lcc.isApplicationOfThisType(app, "ejb"), "test app with sniffer engine=web");
    }


    @Test
    @Disabled("FIXME: Reproduces NPE in ListComponentsCommand.displaySnifferEngine")
    public void getSnifferEnginesTest() throws Exception {
        Engine eng1 = new EngineTest();
        eng1.setSniffer("web");
        Engine eng2 = new EngineTest();
        eng2.setSniffer("security");
        List<Engine> engines = new ArrayList<>();
        engines.add(eng1);
        engines.add(eng2);

        ApplicationTest app = new ApplicationTest();
        List<Module> modules = new ArrayList<>();
        ModuleTest aModule = new ModuleTest();
        aModule.setEngines(engines);
        modules.add(aModule);
        app.setModules(modules);
        String snifferEngines = lcc.getSnifferEngines(app.getModule().get(0), true);
        assertEquals("<web, security>", snifferEngines, "compare all sniffer engines");
    }

    public class RandomConfig implements ConfigBeanProxy {

        @Override
        public ConfigBeanProxy getParent() {
            throw new UnsupportedOperationException();
        }


        @Override
        public <T extends ConfigBeanProxy> T getParent(Class<T> type) {
            throw new UnsupportedOperationException();
        }


        public Property getProperty(String name) {
            throw new UnsupportedOperationException();
        }


        public String getPropertyValue(String name) {
            throw new UnsupportedOperationException();
        }


        public String getPropertyValue(String name, String defaultValue) {
            throw new UnsupportedOperationException();
        }


        @Override
        public <T extends ConfigBeanProxy> T createChild(Class<T> type) throws TransactionFailure {
            throw new UnsupportedOperationException();
        }


        @Override
        public ConfigBeanProxy deepCopy(ConfigBeanProxy parent) {
            throw new UnsupportedOperationException();
        }


        // hk2's Injectable class
        public void injectedInto(Object target) {
        }
    }

    public class ModuleTest extends RandomConfig implements Module {

        @Override
        public String getName() {
            return null;
        }


        @Override
        public void setName(String value) throws PropertyVetoException {
        }

        private List<Engine> engineList = null;

        @Override
        public List<Engine> getEngines() {
            return engineList;
        }


        @Override
        public Engine getEngine(String snifferType) {
            return null;
        }


        public void setEngines(List<Engine> engines) {
            this.engineList = engines;
        }


        @Override
        public List<Property> getProperty() {
            return null;
        }


        @Override
        public void setResources(Resources res) {
        }


        @Override
        public Resources getResources() {
            return null;
        }


        @Override
        public Property addProperty(Property prprt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public Property lookupProperty(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public Property removeProperty(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public Property removeProperty(Property prprt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /** mock-up Application object */
    public class ApplicationTest extends RandomConfig implements Application {

        private List<Module> modules = null;

        @Override
        public String getName() {
            return "hello";
        }


        @Override
        public void setResources(Resources res) {
        }


        @Override
        public Resources getResources() {
            return null;
        }


        @Override
        public void setName(String value) throws PropertyVetoException {
        }


        @Override
        public String getContextRoot() {
            return "hello";
        }


        @Override
        public void setContextRoot(String value) throws PropertyVetoException {
        }


        @Override
        public String getLocation() {
            return "";
        }


        @Override
        public void setLocation(String value) throws PropertyVetoException {
        }


        @Override
        public String getObjectType() {
            return "";
        }


        @Override
        public void setObjectType(String value) throws PropertyVetoException {
        }


        @Override
        public String getEnabled() {
            return "";
        }


        @Override
        public void setEnabled(String value) throws PropertyVetoException {
        }


        @Override
        public String getLibraries() {
            return "";
        }


        @Override
        public void setLibraries(String value) throws PropertyVetoException {
        }


        @Override
        public String getAvailabilityEnabled() {
            return "";
        }


        @Override
        public void setAvailabilityEnabled(String value) throws PropertyVetoException {
        }


        @Override
        public String getAsyncReplication() {
            return "";
        }


        @Override
        public void setAsyncReplication(String value) throws PropertyVetoException {
        }


        @Override
        public String getDirectoryDeployed() {
            return "";
        }


        @Override
        public void setDirectoryDeployed(String value) throws PropertyVetoException {
        }


        @Override
        public String getDescription() {
            return "";
        }


        @Override
        public void setDescription(String value) throws PropertyVetoException {
        }


        @Override
        public String getDeploymentOrder() {
            return "100";
        }


        @Override
        public void setDeploymentOrder(String value) throws PropertyVetoException {
        }


        @Override
        public List<Engine> getEngine() {
            return null;
        }


        @Override
        public List<Property> getProperty() {
            return null;
        }


        public <T extends ApplicationConfig> T getApplicationConfig(Class<T> type) {
            return null;
        }


        public List<ApplicationConfig> getApplicationConfigs() {
            return null;
        }


        @Override
        public Map<String, Properties> getModulePropertiesMap() {
            return null;
        }


        @Override
        public Module getModule(String moduleName) {
            return null;
        }


        @Override
        public boolean isStandaloneModule() {
            return false;
        }


        @Override
        public boolean isLifecycleModule() {
            return false;
        }


        @Override
        public boolean containsSnifferType(String snifferType) {
            return false;
        }


        @Override
        public List<Module> getModule() {
            return modules;
        }


        public void setModules(List<Module> modules) {
            this.modules = modules;
        }


        @Override
        public Properties getDeployProperties() {
            return new Properties();
        }


        @Override
        public DeployCommandParameters getDeployParameters(ApplicationRef appRef) {
            return new DeployCommandParameters();
        }


        @Override
        public File application() {
            return null;
        }


        @Override
        public File deploymentPlan() {
            return null;
        }


        @Override
        public String archiveType() {
            return null;
        }


        @Override
        public AppTenants getAppTenants() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public void setAppTenants(AppTenants appTenants) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public List<ApplicationExtension> getExtensions() {
            return null;
        }


        @Override
        public <T extends ApplicationExtension> T getExtensionByType(Class<T> type) {
            return null;
        }


        @Override
        public <T extends ApplicationExtension> List<T> getExtensionsByType(Class<T> type) {
            return null;
        }


        @Override
        public Property addProperty(Property prprt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public Property lookupProperty(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public Property removeProperty(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public Property removeProperty(Property prprt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /** mock-up Engine object */
    public class EngineTest extends RandomConfig implements Engine {

        private String sniffer = "";

        @Override
        public String getSniffer() {
            return sniffer;
        }


        @Override
        public void setSniffer(String value) throws PropertyVetoException {
            sniffer = value;
        }


        @Override
        public String getDescription() {
            return "";
        }


        @Override
        public void setDescription(String value) {
        }


        @Override
        public List<Property> getProperty() {
            return null;
        }


        // config.serverbeans.Modules
        public String getName() {
            return "hello";
        }


        public void setName(String value) throws PropertyVetoException {
        }


        public ApplicationConfig getConfig() {
            return null;
        }


        public void setConfig(ApplicationConfig config) throws PropertyVetoException {
        }


        @Override
        public List<ApplicationConfig> getApplicationConfigs() {
            return Collections.EMPTY_LIST;
        }


        @Override
        public ApplicationConfig getApplicationConfig() {
            return null;
        }


        @Override
        public void setApplicationConfig(ApplicationConfig config) {
            // no-op for this test
        }


        @Override
        public <T extends ApplicationConfig> T newApplicationConfig(Class<T> configType) throws TransactionFailure {
            return null;
        }


        @Override
        public Property addProperty(Property prprt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public Property lookupProperty(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public Property removeProperty(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public Property removeProperty(Property prprt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
