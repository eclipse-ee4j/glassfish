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

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import static org.junit.Assert.assertEquals;
/**
 *
 * @author Jeremy Lv
 *
 */
public class JndiTest extends BaseSeleniumTestClass {

    @Test
    public void testCustomResources() {
        final String resourceName = "customResource" + generateRandomString();

        StandaloneTest standaloneTest = new StandaloneTest();
        ClusterTest clusterTest = new ClusterTest();
        standaloneTest.deleteAllStandaloneInstances();
        clusterTest.deleteAllCluster();

        clickAndWait("treeForm:tree:resources:jndi:customResources:customResources_link");
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton");

        isElementPresent("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext");
        setFieldValue("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
        Select select = new Select(driver.findElement(By.id("form:propertySheet:propertSectionTextField:cp:Classname")));
        select.selectByVisibleText("java.lang.Double");
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col3:col1St", "value");
        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form:propertyContentPage:topButtons:newButton");

        String prefix = getTableRowByValue("propertyForm:resourcesTable", resourceName, "col1");
        assertEquals(resourceName, getText(prefix + "col1:link"));

        String clickId = prefix + "col1:link";
        clickByIdAction(clickId);
        assertTableRowCount("form1:basicTable", count);
        clickAndWait("form1:propertyContentPage:topButtons:cancelButton");

        //test disable button
        String selectId = prefix + "col0:select";
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:button3");

        //test enable button
        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:button2");
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:button2");

        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:button1");
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
    }

    @Test
    public void testCustomResourcesWithTargets() {
        final String resourceName = "customResource" + generateRandomString();
        final String instanceName = "standalone" + generateRandomString();

        StandaloneTest instanceTest = new StandaloneTest();
        instanceTest.createStandAloneInstance(instanceName);

        clickAndWait("treeForm:tree:resources:jndi:customResources:customResources_link");
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton");

        isElementPresent("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext");
        setFieldValue("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
        Select select = new Select(driver.findElement(By.id("form:propertySheet:propertSectionTextField:cp:Classname")));
        select.selectByVisibleText("java.lang.Double");
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col3:col1St", "value");
        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col4:col1St", "description");

        Select select2 = new Select(driver.findElement(By.id("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available")));
        select2.selectByVisibleText(instanceName);
        select2.selectByVisibleText("server");
        clickByIdAction("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");
        clickAndWait("form:propertyContentPage:topButtons:newButton");

        String prefix = getTableRowByValue("propertyForm:resourcesTable", resourceName, "col1");
        assertEquals(resourceName, getText(prefix + "col1:link"));

        String clickId = prefix + "col1:link";
        clickByIdAction(clickId);
        assertTableRowCount("form1:basicTable", count);
        clickAndWait("form1:propertyContentPage:topButtons:cancelButton");


        //test disable button
        String selectId = prefix + "col0:select";
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:button3");

        //test enable button
        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:button2");
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:button2");

        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:button2");
        clickByIdAction(clickId);
        clickAndWait("form1:resEditTabs:targetTab");
        clickAndWait("propertyForm:targetTable:topActionsGroup1:manageTargetButton");
        Select select5 = new Select(driver.findElement(By.id("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_selected")));
        select5.selectByVisibleText(instanceName);
        select5.selectByVisibleText("server");
        clickByIdAction("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_removeButton");
        clickByIdAction("form:propertyContentPage:topButtons:saveButton");

        gotoDasPage();
        clickAndWait("treeForm:tree:resources:jndi:customResources:customResources_link");
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
        //Delete the instance
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link");
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
    }

    @Test
    public void testExternalResources() {
        final String resourceName = "externalResource" + generateRandomString();
        final String description = resourceName + " - description";

        StandaloneTest standaloneTest = new StandaloneTest();
        ClusterTest clusterTest = new ClusterTest();
        standaloneTest.deleteAllStandaloneInstances();
        clusterTest.deleteAllCluster();

        clickAndWait("treeForm:tree:resources:jndi:externalResources:externalResources_link");
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton");

        isElementPresent("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext");
        setFieldValue("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
        Select select = new Select(driver.findElement(By.id("form:propertySheet:propertSectionTextField:cp:Classname")));
        select.selectByVisibleText("java.lang.Double");
        setFieldValue("form:propertySheet:propertSectionTextField:jndiLookupProp:jndiLookup", resourceName);
        setFieldValue("form:propertySheet:propertSectionTextField:descProp:desc", description);
        addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col3:col1St", "value");
        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form:propertyContentPage:topButtons:newButton");

        String prefix = getTableRowByValue("propertyForm:resourcesTable", resourceName, "col1");
        assertEquals(resourceName, getText(prefix + "col1:link"));

        //test disable button
        String selectId = prefix + "col0:select";
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:button3");

        //test enable button
        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:button2");
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:button2");

        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:button1");
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
    }

    @Test
    public void testExternalResourcesWithTargets() {
        final String resourceName = "externalResource" + generateRandomString();
        final String description = resourceName + " - description";
        final String instanceName = "standalone" + generateRandomString();

        StandaloneTest instanceTest = new StandaloneTest();
        instanceTest.createStandAloneInstance(instanceName);

        clickAndWait("treeForm:tree:resources:jndi:externalResources:externalResources_link");
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton");

        isElementPresent("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext");
        setFieldValue("form:propertySheet:propertSectionTextField:jndiTextProp:jnditext", resourceName);
        Select select = new Select(driver.findElement(By.id("form:propertySheet:propertSectionTextField:cp:Classname")));
        select.selectByVisibleText("java.lang.Double");
        setFieldValue("form:propertySheet:propertSectionTextField:jndiLookupProp:jndiLookup", resourceName);
        setFieldValue("form:propertySheet:propertSectionTextField:descProp:desc", description);
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col3:col1St", "value");
        sleep(500);
        setFieldValue("form:basicTable:rowGroup1:0:col4:col1St", "description");

        Select select2 = new Select(driver.findElement(By.id("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available")));
        select2.selectByVisibleText(instanceName);
        select2.selectByVisibleText("server");
        clickByIdAction("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");
        clickAndWait("form:propertyContentPage:topButtons:newButton");

        String prefix = getTableRowByValue("propertyForm:resourcesTable", resourceName, "col1");
        assertEquals(resourceName, getText(prefix + "col1:link"));

        String clickId = prefix + "col1:link";
        clickByIdAction(clickId);
        assertTableRowCount("form:basicTable", count);
        clickAndWait("form:propertyContentPage:topButtons:cancelButton");

        //test disable button
        String selectId = prefix + "col0:select";
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:button3");

        //test enable button
        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:button2");
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:button2");

        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:button2");
        clickByIdAction(clickId);
        clickAndWait("form:resEditTabs:targetTab");
        clickAndWait("propertyForm:targetTable:topActionsGroup1:manageTargetButton");
        Select select5 = new Select(driver.findElement(By.id("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_selected")));
        select5.selectByVisibleText(instanceName);
        select5.selectByVisibleText("server");
        clickByIdAction("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_removeButton");
        clickByIdAction("form:propertyContentPage:topButtons:saveButton");

        //Delete the External resource
        gotoDasPage();
        clickAndWait("treeForm:tree:resources:jndi:externalResources:externalResources_link");
        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
        //Delete the instance
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link");
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
    }
}
