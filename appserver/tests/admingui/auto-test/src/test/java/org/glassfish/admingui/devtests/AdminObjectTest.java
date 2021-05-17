/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

/**
 * It is used test the Connector-->admin object resource page
 * @author Jeremy Lv
 *
 */
public class AdminObjectTest extends BaseSeleniumTestClass {

    @Test
    public void testAdminObjectResources() throws Exception {
        final String resName = "adminObject" + generateRandomString();
        final String description = "Admin Object Resource - " + resName;

        StandaloneTest standaloneTest = new StandaloneTest();
        ClusterTest clusterTest = new ClusterTest();
        standaloneTest.deleteAllStandaloneInstances();
        clusterTest.deleteAllCluster();

        //Go to Admin Object Resources Page.
        clickAndWait("treeForm:tree:resources:Connectors:adminObjectResources:adminObjectResources_link");

        //New Admin Object Resources
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton");

        setFieldValue("form:propertySheet:propertSectionTextField:nameNew:name", resName);
        setFieldValue("form:propertySheet:propertSectionTextField:descriptionProp:descAdaptor", description);

        int emptyCount = getTableRowCountByValue("form:basicTable", "", "col3:col1St", false);
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addButton");

        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col3:col1St", "value");
        count = count - emptyCount;
        clickAndWait("form:propertyContentPage:topButtons:newButton");

        String prefix = getTableRowByValue("propertyForm:resourcesTable", resName, "col1");
        assertEquals(resName, getText(prefix + "col1:link"));
        assertEquals(description, getText(prefix + "col4:typeDesc"));
        String clickId = prefix + "col1:link";
        clickByIdAction(clickId);
        assertTableRowCount("propertyForm:basicTable", count);

        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton");

        //test disable button
        isElementPresent("propertyForm:resourcesTable:topActionsGroup1:newButton");
        String selectId = prefix + "col0:select";
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:button3");

        //test enable button
        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:button2");
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:button2");

        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:button1");
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resName);
    }

    @Test
    public void testAdminObjectResourcesWithTargets() {
        final String resName = "adminObject" + generateRandomString();
        final String description = "Admin Object Resource - " + resName;
        final String instanceName = "standalone" + generateRandomString();

        StandaloneTest instanceTest = new StandaloneTest();
        instanceTest.createStandAloneInstance(instanceName);

        //Go to Admin Object Resources Page.
        clickAndWait("treeForm:tree:resources:Connectors:adminObjectResources:adminObjectResources_link");

        //New Admin Object Resources
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton");

        setFieldValue("form:propertySheet:propertSectionTextField:nameNew:name", resName);
        setFieldValue("form:propertySheet:propertSectionTextField:descriptionProp:descAdaptor", description);
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addButton");

        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col3:col1St", "value");

        int emptyCount = getTableRowCountByValue("form:basicTable", "", "col3:col1St", false);
        count = count - emptyCount;

        Select select = new Select(driver.findElement(By.id("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available")));
        select.selectByVisibleText(instanceName);
        select.selectByVisibleText("server");
        clickByIdAction("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");

        clickAndWait("form:propertyContentPage:topButtons:newButton");

        String prefix = getTableRowByValue("propertyForm:resourcesTable", resName, "col1");
        assertEquals(resName, getText(prefix + "col1:link"));
        assertEquals(description, getText(prefix + "col4:typeDesc"));
        String clickId = prefix + "col1:link";
        clickByIdAction(clickId);
        assertTableRowCount("propertyForm:basicTable", count);

        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton");

        //test disable button
        isElementPresent("propertyForm:resourcesTable:topActionsGroup1:newButton");
        String selectId = prefix + "col0:select";
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:button3");

        //test enable button
        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:button2");
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:button2");

        //test manage target
        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:button2");
        clickByIdAction(clickId);
        clickAndWait("propertyForm:resEditTabs:targetTab");
        clickAndWait("propertyForm:targetTable:topActionsGroup1:manageTargetButton");
        Select select1 = new Select(driver.findElement(By.id("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_selected")));
        select1.selectByVisibleText(instanceName);
        select1.selectByVisibleText("server");
        clickByIdAction("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_removeButton");
        clickByIdAction("form:propertyContentPage:topButtons:saveButton");

        // Delete admin object resource
        clickAndWait("treeForm:tree:resources:Connectors:adminObjectResources:adminObjectResources_link");
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resName);

        //Delete the instance
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link");
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
    }
}
