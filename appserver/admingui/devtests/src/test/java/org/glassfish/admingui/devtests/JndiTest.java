/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JndiTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_CUSTOM_RESOURCES = "i18njmail.customResources.pageTitleHelp";
    private static final String TRIGGER_NEW_CUSTOM_RESOURCE = "i18njmail.customResource.newPageTitleHelp";
    public static final String TRIGGER_EDIT_CUSTOM_RESOURCE = "i18njmail.customResource.editPageTitleHelp";
    public static final String TRIGGER_EDIT_EXTERNAL_RESOURCE = "i18njmail.jndiResource.editPageTitleHelp";
    private static final String TRIGGER_EXTERNAL_RESOURCES = "i18njmail.externalResources.pageTitleHelp";
    private static final String TRIGGER_NEW_EXTERNAL_RESOURCE = "i18njmail.jndiResource.newPageTitleHelp";

    private static final String ENABLE_STATUS = "Enabled on 2 of 2 Target(s)";
    private static final String DISABLE_STATUS = "Enabled on 0 of 2 Target(s)";

    @Test
    public void testCustomResources() {
        final String resourceName = "customResource" + generateRandomString();

        StandaloneTest standaloneTest = new StandaloneTest();
        ClusterTest clusterTest = new ClusterTest();
        standaloneTest.deleteAllStandaloneInstances();
        clusterTest.deleteAllClusters();

        clickAndWait("treeForm:tree:resources:jndi:customResources:customResources_link", TRIGGER_CUSTOM_RESOURCES);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_CUSTOM_RESOURCE);

        setFieldValue("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
        selectDropdownOption("form:propertySheet:propertSectionTextField:cp:Classname", "java.lang.Double");
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("form:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        setFieldValue("form:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("form:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_CUSTOM_RESOURCES);

        assertTrue(isTextPresent(resourceName));

        testDisableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "form1:propertySheet:propertSectionTextField:statusProp:enabled",
                "form1:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CUSTOM_RESOURCES,
                TRIGGER_EDIT_CUSTOM_RESOURCE,
                "off");
        testEnableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "form1:propertySheet:propertSectionTextField:statusProp:enabled",
                "form1:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CUSTOM_RESOURCES,
                TRIGGER_EDIT_CUSTOM_RESOURCE,
                "on");

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
    }

    @Test
    public void testCustomResourcesWithTargets() {
        final String resourceName = "customResource" + generateRandomString();
        final String instanceName = "standalone" + generateRandomString();

        StandaloneTest instanceTest = new StandaloneTest();
        instanceTest.createStandAloneInstance(instanceName);

        clickAndWait("treeForm:tree:resources:jndi:customResources:customResources_link", TRIGGER_CUSTOM_RESOURCES);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_CUSTOM_RESOURCE);

        setFieldValue("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
        selectDropdownOption("form:propertySheet:propertSectionTextField:cp:Classname", "java.lang.Double");
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("form:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        setFieldValue("form:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("form:basicTable:rowGroup1:0:col4:col1St", "description");

        addSelectSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available", instanceName);
        addSelectSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available", "server");
        pressButton("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_CUSTOM_RESOURCES);

        assertTrue(isTextPresent(resourceName));
        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", resourceName), TRIGGER_EDIT_CUSTOM_RESOURCE);
        assertTableRowCount("form1:basicTable", count);
        clickAndWait("form1:propertyContentPage:topButtons:cancelButton", TRIGGER_CUSTOM_RESOURCES);

        testDisableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "form1:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "form1:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CUSTOM_RESOURCES,
                TRIGGER_EDIT_CUSTOM_RESOURCE,
                DISABLE_STATUS);
        testEnableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "form1:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "form1:propertyContentPage:topButtons:cancelButton",
                TRIGGER_CUSTOM_RESOURCES,
                TRIGGER_EDIT_CUSTOM_RESOURCE,
                ENABLE_STATUS);
        testManageTargets("treeForm:tree:resources:jndi:customResources:customResources_link",
                          "propertyForm:resourcesTable",
                          "propertyForm:targetTable:topActionsGroup1:button2",
                          "propertyForm:targetTable:topActionsGroup1:button3",
                          "form1:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                          "propertyForm:resEditTabs:general",
                          "form1:resEditTabs:targetTab",
                          TRIGGER_CUSTOM_RESOURCES,
                          TRIGGER_EDIT_CUSTOM_RESOURCE,
                          resourceName,
                          instanceName);
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
        //Delete the instance
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link", instanceTest.TRIGGER_INSTANCES_PAGE);
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
        assertFalse(tableContainsRow("propertyForm:instancesTable", "col0", instanceName));
    }

    @Test
    public void testExternalResources() {
        final String resourceName = "externalResource" + generateRandomString();
        final String description = resourceName + " - description";

        StandaloneTest standaloneTest = new StandaloneTest();
        ClusterTest clusterTest = new ClusterTest();
        standaloneTest.deleteAllStandaloneInstances();
        clusterTest.deleteAllClusters();

        clickAndWait("treeForm:tree:resources:jndi:externalResources:externalResources_link", TRIGGER_EXTERNAL_RESOURCES);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_EXTERNAL_RESOURCE);

        setFieldValue("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
        selectDropdownOption("form:propertySheet:propertSectionTextField:cp:Classname", "java.lang.Double");
        setFieldValue("form:propertySheet:propertSectionTextField:jndiLookupProp:jndiLookup", resourceName);
        setFieldValue("form:propertySheet:propertSectionTextField:descProp:desc", description);
        addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("form:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        setFieldValue("form:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("form:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_EXTERNAL_RESOURCES);

        testDisableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "form:propertySheet:propertSectionTextField:statusProp:enabled",
                "form:propertyContentPage:topButtons:cancelButton",
                TRIGGER_EXTERNAL_RESOURCES,
                TRIGGER_EDIT_EXTERNAL_RESOURCE,
                "off");
        testEnableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "form:propertySheet:propertSectionTextField:statusProp:enabled",
                "form:propertyContentPage:topButtons:cancelButton",
                TRIGGER_EXTERNAL_RESOURCES,
                TRIGGER_EDIT_EXTERNAL_RESOURCE,
                "on");

//        selectTableRowByValue("propertyForm:resourcesTable", resourceName);
//        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:button3");
//        selenium.click("propertyForm:resourcesTable:topActionsGroup1:button3");
//        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:button3");
//
//        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", resourceName), TRIGGER_EDIT_EXTERNAL_RESOURCE);
//
//        assertEquals("off", selenium.getValue("form1:propertySheet:propertSectionTextField:statusProp:enabled"));
//        clickAndWait("form1:propertyContentPage:topButtons:cancelButton", TRIGGER_EXTERNAL_RESOURCES);
//
//        selectTableRowByValue("propertyForm:resourcesTable", resourceName);
//        waitForButtonEnabled("propertyForm:resourcesTable:topActionsGroup1:button2");
//        selenium.click("propertyForm:resourcesTable:topActionsGroup1:button2");
//        waitForButtonDisabled("propertyForm:resourcesTable:topActionsGroup1:button2");
//
//        clickAndWait("propertyForm:resourcesTable:rowGroup1:0:col1:link", TRIGGER_EDIT_EXTERNAL_RESOURCE);
//        assertEquals("on", selenium.getValue("form1:propertySheet:propertSectionTextField:statusProp:enabled"));
//        clickAndWait("form1:propertyContentPage:topButtons:cancelButton", TRIGGER_EXTERNAL_RESOURCES);

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
    }

    @Test
    public void testExternalResourcesWithTargets() {
        final String resourceName = "externalResource" + generateRandomString();
        final String description = resourceName + " - description";
        final String instanceName = "standalone" + generateRandomString();

        StandaloneTest instanceTest = new StandaloneTest();
        instanceTest.createStandAloneInstance(instanceName);

        clickAndWait("treeForm:tree:resources:jndi:externalResources:externalResources_link", TRIGGER_EXTERNAL_RESOURCES);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_EXTERNAL_RESOURCE);

        setFieldValue("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
        selectDropdownOption("form:propertySheet:propertSectionTextField:cp:Classname", "java.lang.Double");
        setFieldValue("form:propertySheet:propertSectionTextField:jndiLookupProp:jndiLookup", resourceName);
        setFieldValue("form:propertySheet:propertSectionTextField:descProp:desc", description);
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("form:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        setFieldValue("form:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("form:basicTable:rowGroup1:0:col4:col1St", "description");

        addSelectSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available", instanceName);
        addSelectSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available", "server");
        pressButton("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_EXTERNAL_RESOURCES);

        assertTrue(isTextPresent(resourceName));
        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", resourceName), TRIGGER_EDIT_EXTERNAL_RESOURCE);
        assertTableRowCount("form:basicTable", count);
        clickAndWait("form:propertyContentPage:topButtons:cancelButton", TRIGGER_EXTERNAL_RESOURCES);

        testDisableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "form:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "form:propertyContentPage:topButtons:cancelButton",
                TRIGGER_EXTERNAL_RESOURCES,
                TRIGGER_EDIT_EXTERNAL_RESOURCE,
                DISABLE_STATUS);
        testEnableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "form:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "form:propertyContentPage:topButtons:cancelButton",
                TRIGGER_EXTERNAL_RESOURCES,
                TRIGGER_EDIT_EXTERNAL_RESOURCE,
                ENABLE_STATUS);

        testManageTargets("treeForm:tree:resources:jndi:externalResources:externalResources_link",
                          "propertyForm:resourcesTable",
                          "propertyForm:targetTable:topActionsGroup1:button2",
                          "propertyForm:targetTable:topActionsGroup1:button3",
                          "form:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                          "propertyForm:resEditTabs:general",
                          "form:resEditTabs:targetTab",
                          TRIGGER_EXTERNAL_RESOURCES,
                          TRIGGER_EDIT_EXTERNAL_RESOURCE,
                          resourceName,
                          instanceName);
        //Delete the External resource
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
        //Delete the instance
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link", instanceTest.TRIGGER_INSTANCES_PAGE);
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
        assertFalse(tableContainsRow("propertyForm:instancesTable", "col0", instanceName));
    }
}
