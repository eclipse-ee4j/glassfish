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

package org.glassfish.deployment.versioning;

import com.sun.enterprise.config.serverbeans.AppTenants;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationExtension;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Resources;

import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.glassfish.api.deployment.DeployCommandParameters;
import org.junit.jupiter.api.Test;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Romain GRECOURT - SERLI (romain.grecourt@serli.com)
 */
public class VersioningUtilsTest {

    private static final String APPLICATION_NAME = "foo";

    // the list of all foo versions
    private static final List<String> FOO_VERSIONS = List.of(
        // ALPHA versions
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "ALPHA-1.0.0",
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "ALPHA-1.0.1",
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "ALPHA-1.0.2",
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "ALPHA-1.1.0",
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "ALPHA-1.1.1",
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "ALPHA-1.1.2",
        // BETA versions
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "BETA-1.0.0",
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "BETA-1.0.1",
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "BETA-1.0.2",
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "BETA-1.1.0",
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "BETA-1.1.1",
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "BETA-1.1.2",
        // RC versions
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.0.0",
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.0.1",
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.0.2",
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.1.0",
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.1.1",
        APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.1.2"
    );

    /**
     * Test of {@link VersioningUtils#getUntaggedName(String)}
     *
     * Check the extraction of untagged names from different application names
     * as version identifier, version expression or untagged application name.
     */
    @Test
    public void testGetUntaggedName() throws Exception {
        // test an application name that contains a version expression
        // application name : foo:RC-*
        final String expression = APPLICATION_NAME
                + VersioningUtils.EXPRESSION_SEPARATOR
                + "RC-" + VersioningUtils.EXPRESSION_WILDCARD;
        assertEquals(APPLICATION_NAME, VersioningUtils.getUntaggedName(expression));

        // test an application name that contains a version identifier
        // application name : foo:RC-1.0.0
        assertEquals(APPLICATION_NAME,
            VersioningUtils.getUntaggedName(APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.0.0"));

        // test an application name that is an untagged version name
        // application name : foo
        assertEquals(APPLICATION_NAME, VersioningUtils.getUntaggedName(APPLICATION_NAME));

        // test an application name containing a critical pattern
        // application name : foo:
        assertThrows(VersioningSyntaxException.class,
            () -> VersioningUtils.getUntaggedName(APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR));
    }

    /**
     * Test of {@link VersioningUtils#getExpression(String)}
     *
     * Check the extraction of version expression / identifier from different
     * application names.
     */
    @Test
    public void testGetExpression() throws Exception {
        // test an application name containing a critical pattern
        // application name : foo:
        assertThrows(VersioningSyntaxException.class,
            () -> VersioningUtils.getExpression(APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR));

        // test an application name containing a critical pattern
        // application name : foo:RC-1;0.0
        final String expression = APPLICATION_NAME
                + VersioningUtils.EXPRESSION_SEPARATOR
                + "RC-1"
                + VersioningUtils.EXPRESSION_SEPARATOR
                + "0.0";
        assertEquals("RC-1:0.0", VersioningUtils.getExpression(expression));
    }

    /**
     * Test of {@link VersioningUtils#getVersions(String, List)}
     *
     * Check the extraction of a set of version(s) from a set of applications.
     */
    @Test
    public void testGetVersions() throws Exception {
        // the set of applications
        final List<Application> listApplications = new ArrayList<>();
        listApplications.add(new ApplicationTest(APPLICATION_NAME));
        listApplications
            .add(new ApplicationTest(APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "BETA-1.0.0"));
        listApplications.add(new ApplicationTest(APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.0.0"));
        listApplications.add(new ApplicationTest(APPLICATION_NAME + "_RC-1.0.0"));
        listApplications.add(new ApplicationTest(APPLICATION_NAME + ";RC-1.0.0"));
        listApplications.add(new ApplicationTest(APPLICATION_NAME + ".RC-1.0.0"));
        listApplications.add(new ApplicationTest(APPLICATION_NAME + "-RC-1.0.0"));
        listApplications.add(new ApplicationTest(APPLICATION_NAME + APPLICATION_NAME));

        // the expected set of versions
        final List<String> expResult = new ArrayList<>();
        expResult.add(APPLICATION_NAME);
        expResult.add(APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "BETA-1.0.0");
        expResult.add(APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.0.0");

        final List<String> result = VersioningUtils.getVersions(APPLICATION_NAME, listApplications);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@link VersioningUtils#matchExpression(List, String)}
     * TEST TYPE 1 : expression matching all the versions
     *
     * Check the matching of version expression over a set of version
     */
    @Test
    public void testMatchExpression() throws Exception {
        // application name foo:*
        String expression = APPLICATION_NAME
            + VersioningUtils.EXPRESSION_SEPARATOR
            + VersioningUtils.EXPRESSION_WILDCARD;

        assertEquals(FOO_VERSIONS, VersioningUtils.matchExpression(FOO_VERSIONS, expression));

        // application name foo:******
        expression = APPLICATION_NAME
            + VersioningUtils.EXPRESSION_SEPARATOR
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD;

        assertEquals(FOO_VERSIONS, VersioningUtils.matchExpression(FOO_VERSIONS, expression));
    }

    /**
     * Test of {@link VersioningUtils#matchExpression(List, String)}
     * TEST TYPE 2 : expression matching all the RC versions
     *
     * Check the matching of version expression over a set of version
     */
    @Test
    public void testMatchExpression_RC() throws Exception {
        // the expected set of matched version is all the versions
        final List<String> expResult = List.of(
            APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.0.0",
            APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.0.1",
            APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.0.2",
            APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.1.0",
            APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.1.1",
            APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.1.2"
        );

        // application name foo:RC*
        final String expressionFooRCAny = APPLICATION_NAME
            + VersioningUtils.EXPRESSION_SEPARATOR + "RC"
            + VersioningUtils.EXPRESSION_WILDCARD;
        assertEquals(expResult, VersioningUtils.matchExpression(FOO_VERSIONS, expressionFooRCAny));

        // application name foo:*RC*
        final String expressionFooAnyRCAny = APPLICATION_NAME
            + VersioningUtils.EXPRESSION_SEPARATOR
            + VersioningUtils.EXPRESSION_WILDCARD + "RC"
            + VersioningUtils.EXPRESSION_WILDCARD;
        assertEquals(expResult, VersioningUtils.matchExpression(FOO_VERSIONS, expressionFooAnyRCAny));

        // application name foo:***RC***
        final String expression = APPLICATION_NAME
            + VersioningUtils.EXPRESSION_SEPARATOR
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD + "RC"
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD;
        assertEquals(expResult, VersioningUtils.matchExpression(FOO_VERSIONS, expression));
    }

    /**
     * Test of {@link VersioningUtils#matchExpression(List, String)}
     * TEST TYPE 3 : expression matching all the 1.0.2 versions
     *
     * Check the matching of version expression over a set of version
     */
    @Test
    public void testMatchExpression_102() throws Exception {
        final List<String> expResult = List.of(
            APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "ALPHA-1.0.2",
            APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "BETA-1.0.2",
            APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "RC-1.0.2"
            );

        // application name foo:*-1.0.2
        final String expression1 = APPLICATION_NAME
            + VersioningUtils.EXPRESSION_SEPARATOR
            + VersioningUtils.EXPRESSION_WILDCARD + "-1.0.2";
        assertEquals(expResult, VersioningUtils.matchExpression(FOO_VERSIONS, expression1));

        // application name foo:***1.0.2***
        final String expression2 = APPLICATION_NAME
            + VersioningUtils.EXPRESSION_SEPARATOR
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD + "-1.0.2"
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD;
        assertEquals(expResult, VersioningUtils.matchExpression(FOO_VERSIONS, expression2));

        // application name foo:***1*0*2***
        final String expression3 = APPLICATION_NAME
            + VersioningUtils.EXPRESSION_SEPARATOR
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD + "1"
            + VersioningUtils.EXPRESSION_WILDCARD + "0"
            + VersioningUtils.EXPRESSION_WILDCARD + "2"
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD
            + VersioningUtils.EXPRESSION_WILDCARD;
        assertEquals(expResult, VersioningUtils.matchExpression(FOO_VERSIONS, expression3));
    }

    /**
     * Test of {@link VersioningUtils#matchExpression(List, String)}
     * TEST TYPE 4 : identifier as expression
     *
     * Check the matching of version expression over a set of version
     */
    @Test
    public void testMatchExpression_asterisks() throws Exception {
        final List<String> expResult = List.of(APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "ALPHA-1.0.2");

        // application name foo:ALPHA-1.0.2
        String expression = APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "ALPHA-1.0.2";
        assertEquals(expResult, VersioningUtils.matchExpression(FOO_VERSIONS, expression));

        final List<String> listVersion = new ArrayList<>();
        listVersion.add(APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "abc-1");
        listVersion.add(APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "abc-2");
        listVersion.add(APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "abc-3");
        listVersion.add(APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "bac-4");
        listVersion.add(APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "cab-5");
        listVersion.add(APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "cba-6");

        expression = APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "a*";
        assertThat(VersioningUtils.matchExpression(listVersion, expression), hasSize(3));

        expression = APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "*a";
        assertThat(VersioningUtils.matchExpression(listVersion, expression), hasSize(0));

        expression = APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "a****1";
        assertThat(VersioningUtils.matchExpression(listVersion, expression), hasSize(1));

        expression = APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "*-*";
        assertThat(VersioningUtils.matchExpression(listVersion, expression), hasSize(6));

        expression = APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "*-4";
        assertThat(VersioningUtils.matchExpression(listVersion, expression), hasSize(1));

        expression = APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "b*";
        assertThat(VersioningUtils.matchExpression(listVersion, expression), hasSize(1));

        expression = APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + "b*";
        assertThat(VersioningUtils.matchExpression(listVersion, expression), hasSize(1));
    }

    /**
     * Test of getIdentifier method, of class VersioningUtils.
     */
    @Test
    public void testGetIdentifier() throws Exception {
        // check for getIdentifier with and without '*'
        assertDoesNotThrow(
            () -> VersioningUtils.checkIdentifier("foo" + VersioningUtils.EXPRESSION_SEPARATOR + "BETA-1"));

        assertThrows(VersioningException.class,
            () -> VersioningUtils.checkIdentifier("foo" + VersioningUtils.EXPRESSION_SEPARATOR + "BETA-*"));
     }

    /**
     * Test of getRepositoryName method, of class VersioningUtils.
     */
    @Test
    public void testGetRepositoryName() throws Exception {
        {
            String versionIdentifier = "RC-1.0.0";
            String appName = APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + versionIdentifier;
            String expectedResult = APPLICATION_NAME + VersioningUtils.REPOSITORY_DASH + versionIdentifier;
            String result = VersioningUtils.getRepositoryName(appName);
            assertEquals(expectedResult, result);
        }
        {
            String versionIdentifier = "RC:1.0.0";
            String appName = APPLICATION_NAME + VersioningUtils.EXPRESSION_SEPARATOR + versionIdentifier;
            String expectedResult = APPLICATION_NAME + VersioningUtils.REPOSITORY_DASH
                + versionIdentifier.replace(":", VersioningUtils.REPOSITORY_DASH);
            String result = VersioningUtils.getRepositoryName(appName);
            assertEquals(expectedResult, result);
        }
    }

    /**
     * Test of isUntagged method, of class VersioningUtils.
     */
    @Test
    public void testIsUntagged() {
        assertThrows(VersioningException.class, () -> VersioningUtils.isUntagged(APPLICATION_NAME + ":"));
        assertThrows(VersioningException.class, () -> VersioningUtils.isUntagged(":BETA"));
        assertThrows(VersioningException.class, () -> VersioningUtils.isUntagged("::"));
        assertFalse(VersioningUtils.isUntagged(null));
        assertFalse(VersioningUtils.isUntagged(APPLICATION_NAME+":*"));
        assertFalse(VersioningUtils.isUntagged(APPLICATION_NAME+":BETA*"));
        assertFalse(VersioningUtils.isUntagged(APPLICATION_NAME+":BETA"));
        assertFalse(VersioningUtils.isUntagged(APPLICATION_NAME+"::"));
        assertFalse(VersioningUtils.isUntagged(APPLICATION_NAME+":BETA:2"));
    }

     /**
     * Test of isVersionExpression method, of class VersioningUtils.
     */
    @Test
    public void testIsVersionExpression() {
        assertFalse(VersioningUtils.isVersionExpression(null));
        assertFalse(VersioningUtils.isVersionExpression(APPLICATION_NAME));
        assertTrue(VersioningUtils.isVersionExpression(APPLICATION_NAME+":BETA"));
        assertTrue(VersioningUtils.isVersionExpression(APPLICATION_NAME+"::"));
    }

    /**
     * Test of isVersionIdentifier method, of class VersioningUtils.
     */
    @Test
    public void testIsVersionIdentifier() {
        assertFalse(VersioningUtils.isVersionIdentifier(APPLICATION_NAME+":*"));
        assertFalse(VersioningUtils.isVersionIdentifier(APPLICATION_NAME+":BETA*"));
    }

    // this class is used to fake the List<Application>
    // so we can call the VersioningUtils.matchExpression
    // with an home made set of applications.
    private class ApplicationTest implements Application {
        private final String name;

        public ApplicationTest(final String value){
            this.name = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public AppTenants getAppTenants() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setAppTenants(final AppTenants appTenants) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setName(final String value) throws PropertyVetoException{
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setResources(final Resources resources){
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Resources getResources(){
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getContextRoot() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setContextRoot(final String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getLocation() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLocation(final String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getObjectType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setObjectType(final String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getEnabled() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setEnabled(final String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getLibraries() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLibraries(final String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getAvailabilityEnabled() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setAvailabilityEnabled(final String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getAsyncReplication() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setAsyncReplication (final String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDirectoryDeployed() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDirectoryDeployed(final String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDescription(final String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDeploymentOrder() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDeploymentOrder(final String value) throws PropertyVetoException{
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<Module> getModule() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<Engine> getEngine() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Module getModule(final String moduleName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Properties getDeployProperties() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public DeployCommandParameters getDeployParameters(final ApplicationRef appRef) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map<String, Properties> getModulePropertiesMap() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public boolean isStandaloneModule() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isLifecycleModule() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean containsSnifferType(final String snifferType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File application() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File deploymentPlan() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String archiveType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<Property> getProperty() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property getProperty(final String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getPropertyValue(final String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getPropertyValue(final String name, final String defaultValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ConfigBeanProxy getParent() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T extends ConfigBeanProxy> T getParent(final Class<T> type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T extends ConfigBeanProxy> T createChild(final Class<T> type)
               throws TransactionFailure {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ConfigBeanProxy deepCopy(final ConfigBeanProxy parent) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<ApplicationExtension> getExtensions() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T extends ApplicationExtension> T getExtensionByType(final Class<T> type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T extends ApplicationExtension> List<T> getExtensionsByType(final Class<T> type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property addProperty(final Property prprt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property lookupProperty(final String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property removeProperty(final String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property removeProperty(final Property prprt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
