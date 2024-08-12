/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.config.modularity.tests;

import com.sun.enterprise.config.modularity.ConfigModularityUtils;
import com.sun.enterprise.config.modularity.customization.ConfigBeanDefaultValue;
import com.sun.enterprise.config.modularity.customization.ConfigCustomizationToken;
import com.sun.enterprise.config.modularity.customization.FileTypeDetails;
import com.sun.enterprise.config.modularity.customization.PortTypeDetails;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.tests.utils.junit.DomainXml;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.types.Property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing Basic Functionality of Config Modularity API
 *
 * @author Masoud Kalali
 */
@ExtendWith(ConfigApiJunit5Extension.class)
@DomainXml(value = "Config-Modularity.xml")
public class BasicModularityTest {

    @Inject
    private ServiceLocator locator;
    @Inject
    private ConfigModularityUtils configModularityUtils;

    @Test
    public void fromClassNameToClassTest() throws Exception {
        // This part passes as the configuration for the class is present in the domain.xml
        Class clz = configModularityUtils.getClassForFullName(ConfigExtensionZero.class.getName());
        assertNotNull(clz, "Cannot get config bean class using the class name");
        assertEquals(ConfigExtensionZero.class.getName(), clz.getName(),
            "The mapped class is not the same as the provided class name");

        // this part fails as the configuration is not present in domain.xml which was is a
        // regression somewhere
        clz = configModularityUtils.getClassForFullName(ConfigExtensionTwo.class.getName());
        assertNotNull(clz, "Cannot get config bean class using the class name");
        assertEquals(ConfigExtensionTwo.class.getName(), clz.getName(),
            "The mapped class is not the same as the provided class name");
    }


    @Test
    public void locationTest() {
        String location = "domain/configs/config[$CURRENT_INSTANCE_CONFIG_NAME]/config-extension-one/property[prop.foo]";
        Class owningClass = configModularityUtils.getOwningClassForLocation(location);
        assertNotNull(owningClass, "Cannot find owning class for: " + location);
        assertEquals(Property.class.getName(), owningClass.getName(),
            "Cannot find the right owning class for location");
    }


    @Test
    public void owningObjectTest() {
        String location = "domain/configs/config[$CURRENT_INSTANCE_CONFIG_NAME]/config-extension-one/property[prop.foo]";
        ConfigBeanProxy obj = configModularityUtils.getOwningObject(location);
        assertNotNull(obj, "Cannot find owning object for: " + location);
        assertEquals("prop.foo.value.custom", ((Property) obj).getValue(),
            "Getting Owning object for location is not right");
    }


    @Test
    public void moduleConfigurationXmlParserTest() {
        List<ConfigBeanDefaultValue> values = configModularityUtils
            .getDefaultConfigurations(SimpleExtensionTypeOne.class, "admin-");
        assertEquals(2, values.size(), "Incorrect number of config bean configuration read ");
        ConfigCustomizationToken token = configModularityUtils
            .getDefaultConfigurations(SimpleExtensionTypeOne.class, "embedded-").get(0).getCustomizationTokens().get(0);
        assertEquals("CUSTOM_TOKEN", token.getName(), "Customization Token reading broken ");
        assertEquals("token-default-value", token.getValue(), "Customization Token reading broken ");
    }


    @Test
    public void serializeConfigBean() {
        Config config = locator.<Config> getService(Config.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
        ConfigBeanProxy prox = config.getExtensionByType(ConfigExtensionZero.class);
        String content = configModularityUtils.serializeConfigBean(prox);
        assertEquals("<config-extension-zero dummy=\"dummy-value\"></config-extension-zero>", content,
            "Cannot serialize config beans properly");

    }


    @Test
    public void serializeConfigBeanByType() {
        String content = configModularityUtils.serializeConfigBeanByType(ConfigExtensionOne.class);
        assertEquals("<config-extension-one custom-token=\"${CUSTOM_TOKEN}\">\n"
            + "  <property name=\"prop.foo\" value=\"prop.foo.value.custom\"></property>\n" + "</config-extension-one>",
            content, "Cannot serialize config beans from type");
    }


    @Test
    public void testConfigExtensionPatternImpl() {
        Config config = locator.<Config> getService(Config.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
        SimpleConfigExtension simpleConfigExtension = config.getExtensionByType(SimpleConfigExtension.class);
        SimpleExtensionTypeTwo typeTwo = simpleConfigExtension.getExtensionByType(SimpleExtensionTypeTwo.class);
        assertNotNull(typeTwo, "cannot get extension using extensionmethod");
        assertEquals("attribute.two", typeTwo.getAttributeTwo(), "Retrieved extension is not from the right type... ");
    }


    @Test
    public void testLoadingAndApplyingModuleConfigurationFile() {
        Config config = locator.<Config> getService(Config.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
        ConfigExtensionTwo ext = config.getExtensionByType(ConfigExtensionTwo.class);
        assertNotNull(ext);
        assertEquals("user.customized.value", config.getSystemProperty("CUSTOM_TOKEN").getValue(),
            "The system property is overridden while it should have not");
        assertTrue(config.checkIfExtensionExists(ConfigExtensionTwo.class),
            "Failed to add the extension from module configuration file: ");
    }


    @Test
    public void testHasNoCustomization() {
        Config config = locator.<Config> getService(Config.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
        assertNull(config.getExtensionByType(ConfigExtensionThree.class),
            "The @HasNocustomization annotation is broken");
    }


    @Test
    public void getCurrentConfigurationForConfigBean() throws Exception {
        ConfigBeanDefaultValue def = configModularityUtils
            .getDefaultConfigurations(SimpleExtensionTypeOne.class, "embedded-").get(0);
        SimpleExtensionTypeOne simple = configModularityUtils.getCurrentConfigBeanForDefaultValue(def);
        assertNotNull(simple,
            "Cannot get config bean of a module based on the default module configuration information");
    }


    @Test
    public void testLoadingAdminFile() throws Exception {
        List<ConfigBeanDefaultValue> values = configModularityUtils.getDefaultConfigurations(ConfigExtensionTwo.class,
            "admin");
        assertEquals(ConfigCustomizationToken.CustomizationType.FILE,
            values.get(0).getCustomizationTokens().get(0).getCustomizationType(),
            "Incorrect customization type loaded ");
        assertEquals(FileTypeDetails.FileExistCondition.MUST_EXIST,
            ((FileTypeDetails) values.get(0).getCustomizationTokens().get(0).getTokenTypeDetails()).getExistCondition(),
            "Incorrect customization details value ");
    }


    @Test
    public void testLoadingEmbeddedFile() throws Exception {
        List<ConfigBeanDefaultValue> values = configModularityUtils.getDefaultConfigurations(ConfigExtensionTwo.class,
            "embedded");
        assertEquals(ConfigCustomizationToken.CustomizationType.PORT,
            values.get(0).getCustomizationTokens().get(0).getCustomizationType(),
            "Incorrect customization type loaded ");
        assertEquals("1000",
            ((PortTypeDetails) values.get(0).getCustomizationTokens().get(0).getTokenTypeDetails()).getBaseOffset(),
            "Incorrect customization details value ");
        assertEquals("[a-zA-Z0-9]+", values.get(0).getCustomizationTokens().get(0).getValidationExpression(),
            "validation expression is returned incorrectly ");
    }


    @Test
    public void testLoadingDefaultFile() throws Exception {
        List<ConfigBeanDefaultValue> values = configModularityUtils.getDefaultConfigurations(ConfigExtensionTwo.class,
            "non-existing-runtime-type");
        assertEquals(".*[0-9]{10}.*", values.get(0).getCustomizationTokens().get(0).getValidationExpression(),
            "validation expression is returned incorrectly ");
    }


    @Test
    public void tesOnTheFlyConfigurationGenerationMethod() {
        List<ConfigBeanDefaultValue> values = configModularityUtils.getDefaultConfigurations(SimpleExtensionThree.class,
            "non-existing-runtime-type");
        assertEquals("<xml-doc></xml-doc>", values.get(0).getXmlConfiguration(),
            "On the fly config generation/reading is broken");
    }


    @Test
    @Timeout(unit = TimeUnit.SECONDS, value = 120)
    public void testRanking() {
        Domain domain = locator.<Domain> getService(Domain.class);

        RankedConfigBean rankedConfigBean = domain.getExtensionByType(RankedConfigBean.class);
        assertEquals("simple-value-zero", rankedConfigBean.getSimpleAttribute(), "invalid current value");

        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.proceedTo(4);

        rankedConfigBean = domain.getExtensionByType(RankedConfigBean.class);
        assertEquals("simple-value-one", rankedConfigBean.getSimpleAttribute(), "invalid current value");
    }
}
