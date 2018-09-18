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

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AdminObjectTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_ADMIN_OBJECT_RESOURCES = "i18njca.adminObjectResources.pageTitleHelp";

    private static final String TRIGGER_NEW_ADMIN_OBJECT_RESOURCE = "i18njca.adminObject.NewPageTitleHelp";

    public static final String TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE = "i18njca.adminObjectResource.editPageHelp";

    @Test
    public void testAdminObjectResources() throws Exception {
        final String resName = "adminObject" + generateRandomString();
        final String description = "Admin Object Resource - " + resName;

        StandaloneTest standaloneTest = new StandaloneTest();
        ClusterTest clusterTest = new ClusterTest();
        standaloneTest.deleteAllStandaloneInstances();
        clusterTest.deleteAllClusters();

        //Go to Admin Object Resources Page.
        clickAndWait("treeForm:tree:resources:Connectors:adminObjectResources:adminObjectResources_link", TRIGGER_ADMIN_OBJECT_RESOURCES);

        //New Admin Object Resources
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_ADMIN_OBJECT_RESOURCE);

        setFieldValue("form:propertySheet:propertSectionTextField:nameNew:name", resName);
        setFieldValue("form:propertySheet:propertSectionTextField:descriptionProp:descAdaptor", description);
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addButton");

        setFieldValue("form:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        setFieldValue("form:basicTable:rowGroup1:0:col3:col1St", "value");
        //setFieldValue("form:basicTable:rowGroup1:0:col4:col1St", "description");
        int emptyCount = getTableRowCountByValue("form:basicTable", "", "col3:col1St", false);
        count = count - emptyCount;
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_ADMIN_OBJECT_RESOURCES);

        assertTrue(isTextPresent(resName));
        assertTrue(isTextPresent(description));
        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", resName), TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE);
        assertTableRowCount("propertyForm:basicTable", count);

        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_ADMIN_OBJECT_RESOURCES);

        testDisableButton(resName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:enabled",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_ADMIN_OBJECT_RESOURCES,
                TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE,
                "off");
        testEnableButton(resName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:enabled",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_ADMIN_OBJECT_RESOURCES,
                TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE,
                "on");

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resName);
    }

    @Test
    public void testAdminObjectResourcesWithTargets() {
        final String resName = "adminObject" + generateRandomString();
        final String description = "Admin Object Resource - " + resName;
        final String instanceName = "standalone" + generateRandomString();
        final String enableStatus = "Enabled on 2 of 2 Target(s)";
        final String disableStatus = "Enabled on 0 of 2 Target(s)";

        StandaloneTest instanceTest = new StandaloneTest();
        ClusterTest clusterTest = new ClusterTest();

        instanceTest.deleteAllStandaloneInstances();
        clusterTest.deleteAllClusters();

        instanceTest.createStandAloneInstance(instanceName);

        //Go to Admin Object Resources Page.
        clickAndWait("treeForm:tree:resources:Connectors:adminObjectResources:adminObjectResources_link", TRIGGER_ADMIN_OBJECT_RESOURCES);

        //New Admin Object Resources
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_ADMIN_OBJECT_RESOURCE);

        setFieldValue("form:propertySheet:propertSectionTextField:nameNew:name", resName);
        setFieldValue("form:propertySheet:propertSectionTextField:descriptionProp:descAdaptor", description);
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addButton");

        setFieldValue("form:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        setFieldValue("form:basicTable:rowGroup1:0:col3:col1St", "value");
        //setFieldValue("form:basicTable:rowGroup1:0:col4:col1St", "description");

        int emptyCount = getTableRowCountByValue("form:basicTable", "", "col3:col1St", false);
        count = count - emptyCount;

        addSelectSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available", instanceName);
        addSelectSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available", "server");
        pressButton("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");

        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_ADMIN_OBJECT_RESOURCES);

        assertTrue(isTextPresent(resName));
        assertTrue(isTextPresent(description));
        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", resName), TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE);
        assertTableRowCount("propertyForm:basicTable", count);

        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_ADMIN_OBJECT_RESOURCES);

        testDisableButton(resName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_ADMIN_OBJECT_RESOURCES,
                TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE,
                disableStatus);
        testEnableButton(resName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_ADMIN_OBJECT_RESOURCES,
                TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE,
                enableStatus);

        testManageTargets("treeForm:tree:resources:Connectors:adminObjectResources:adminObjectResources_link",
                "propertyForm:resourcesTable",
                "propertyForm:targetTable:topActionsGroup1:button2",
                "propertyForm:targetTable:topActionsGroup1:button3",
                "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "propertyForm:resEditTabs:general",
                "propertyForm:resEditTabs:targetTab",
                TRIGGER_ADMIN_OBJECT_RESOURCES,
                TRIGGER_EDIT_ADMIN_OBJECT_RESOURCE,
                resName,
                instanceName);

        // Delete admin object resource
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resName);

        //Delete the instance
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link", instanceTest.TRIGGER_INSTANCES_PAGE);
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
    }
}
