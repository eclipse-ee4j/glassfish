/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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
 *
 * @author Jeremy Lv
 *
 */
public class JmsResourcesTest extends BaseSeleniumTestClass {

    @Test
    public void testAddingConnectionFactories() throws Exception {
        final String poolName = "JMSConnFactory" + generateRandomString();
        final String description = "Test Pool - " + poolName;

        StandaloneTest standaloneTest = new StandaloneTest();
        ClusterTest clusterTest = new ClusterTest();
        standaloneTest.deleteAllStandaloneInstances();
        clusterTest.deleteAllCluster();

        clickAndWait("treeForm:tree:resources:jmsResources:jmsConnectionFactories:jmsConnectionFactories_link");
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton");

        setFieldValue("form:propertySheet:generalPropertySheet:jndiProp:jndiProp", poolName);
        Select select = new Select(driver.findElement(By.id("form:propertySheet:generalPropertySheet:resTyped:resType")));
        select.selectByVisibleText("jakarta.jms.TopicConnectionFactory");
        setFieldValue("form:propertySheet:generalPropertySheet:descProp:descProp", description);
        Select select1 = new Select(driver.findElement(By.id("form:propertySheet:poolPropertySheet:transprop:trans")));
        select1.selectByVisibleText("LocalTransaction");
        clickAndWait("form:propertyContentPage:topButtons:newButton");

        String prefix = getTableRowByValue("propertyForm:resourcesTable", poolName, "colName");
        assertEquals(poolName, getText(prefix + "colName:link"));

        //test disable button
        String selectId = prefix + "colSelect:select";
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:disableButton");

        //test enable button
        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:enableButton");

        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:deleteConnButton");
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:deleteConnButton");
        closeAlertAndGetItsText();
        waitForAlertProcess("modalBody");
    }

    @Test
    public void testAddingConnectionFactoriesWithTargets() throws Exception {
        final String poolName = "JMSConnFactory" + generateRandomString();
        final String description = "Test Pool - " + poolName;
        final String instanceName = "standalone" + generateRandomString();

        StandaloneTest instanceTest = new StandaloneTest();
        instanceTest.createStandAloneInstance(instanceName);

        clickAndWait("treeForm:tree:resources:jmsResources:jmsConnectionFactories:jmsConnectionFactories_link");
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton");

        setFieldValue("form:propertySheet:generalPropertySheet:jndiProp:jndiProp", poolName);
        Select select = new Select(driver.findElement(By.id("form:propertySheet:generalPropertySheet:resTyped:resType")));
        select.selectByVisibleText("jakarta.jms.TopicConnectionFactory");
        setFieldValue("form:propertySheet:generalPropertySheet:descProp:descProp", description);
        Select select1 = new Select(driver.findElement(By.id("form:propertySheet:poolPropertySheet:transprop:trans")));
        select1.selectByVisibleText("LocalTransaction");


        Select select2 = new Select(driver.findElement(By.id("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available")));
        select2.selectByVisibleText(instanceName);
        select2.selectByVisibleText("server");
        clickByIdAction("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");
        clickAndWait("form:propertyContentPage:topButtons:newButton");

        String prefix = getTableRowByValue("propertyForm:resourcesTable", poolName, "colName");
        assertEquals(poolName, getText(prefix + "colName:link"));

        //test disable button
        String selectId = prefix + "colSelect:select";
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:disableButton");

        //test enable button
        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:enableButton");

        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        String clickId = prefix + "colName:link";
        clickByIdAction(clickId);
        clickAndWait("propertyForm:resEditTabs:targetTab");
        clickAndWait("propertyForm:targetTable:topActionsGroup1:manageTargetButton");
        Select select5 = new Select(driver.findElement(By.id("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_selected")));
        select5.selectByVisibleText(instanceName);
        select5.selectByVisibleText("server");
        clickByIdAction("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_removeButton");
        clickByIdAction("form:propertyContentPage:topButtons:saveButton");


        gotoDasPage();
        clickAndWait("treeForm:tree:resources:jmsResources:jmsConnectionFactories:jmsConnectionFactories_link");
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:deleteConnButton");
        closeAlertAndGetItsText();
        waitForAlertProcess("modalBody");

        //Delete the instance
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link");
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
    }

    @Test
    public void testAddingDestinationResources() throws Exception {
        final String resourceName = "JMSDestination" + generateRandomString();
        final String description = "Test Destination - " + resourceName;

        StandaloneTest standaloneTest = new StandaloneTest();
        ClusterTest clusterTest = new ClusterTest();
        standaloneTest.deleteAllStandaloneInstances();
        clusterTest.deleteAllCluster();

        clickAndWait("treeForm:tree:resources:jmsResources:jmsDestinationResources:jmsDestinationResources_link");
        sleep(1000);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton");
        setFieldValue("form:propertyContentPage:propertySheet:propertSectionTextField:jndiProp:jndi", resourceName);
        setFieldValue("form:propertyContentPage:propertySheet:propertSectionTextField:nameProp:name", "somePhysicalDestination");
        setFieldValue("form:propertyContentPage:propertySheet:propertSectionTextField:descProp:desc", description);
        clickAndWait("form:propertyContentPage:topButtons:newButton");

        String prefix = getTableRowByValue("propertyForm:resourcesTable", resourceName, "colName");
        assertEquals(resourceName, getText(prefix + "colName:link"));


        //test disable button
        String selectId = prefix + "colSelect:select";
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:disableButton");

        //test enable button
        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:enableButton");

        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:deleteDestButton");
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:deleteDestButton");
        closeAlertAndGetItsText();
        waitForAlertProcess("modalBody");
    }

    @Test
    public void testAddingDestinationResourcesWithTargets() throws Exception {
        final String resourceName = "JMSDestination" + generateRandomString();
        final String instanceName = "standalone" + generateRandomString();
        final String description = "Test Destination - " + resourceName;

        StandaloneTest instanceTest = new StandaloneTest();
        instanceTest.createStandAloneInstance(instanceName);

        clickAndWait("treeForm:tree:resources:jmsResources:jmsDestinationResources:jmsDestinationResources_link");
        sleep(1000);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton");
        setFieldValue("form:propertyContentPage:propertySheet:propertSectionTextField:jndiProp:jndi", resourceName);
        setFieldValue("form:propertyContentPage:propertySheet:propertSectionTextField:nameProp:name", "somePhysicalDestination");
        setFieldValue("form:propertyContentPage:propertySheet:propertSectionTextField:descProp:desc", description);

        Select select = new Select(driver.findElement(By.id("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available")));
        select.selectByVisibleText(instanceName);
        select.selectByVisibleText("server");
        clickByIdAction("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");

        clickAndWait("form:propertyContentPage:topButtons:newButton");

        String prefix = getTableRowByValue("propertyForm:resourcesTable", resourceName, "colName");
        assertEquals(resourceName, getText(prefix + "colName:link"));

        //test disable button
        String selectId = prefix + "colSelect:select";
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:disableButton");

        //test enable button
        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:enableButton");

        waitforBtnDisable("propertyForm:resourcesTable:topActionsGroup1:enableButton");
        String clickId = prefix + "colName:link";
        clickByIdAction(clickId);
        clickAndWait("propertyForm:resEditTabs:targetTab");
        clickAndWait("propertyForm:targetTable:topActionsGroup1:manageTargetButton");
        Select select5 = new Select(driver.findElement(By.id("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_selected")));
        select5.selectByVisibleText(instanceName);
        select5.selectByVisibleText("server");
        clickByIdAction("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_removeButton");
        clickByIdAction("form:propertyContentPage:topButtons:saveButton");

        gotoDasPage();
        clickAndWait("treeForm:tree:resources:jmsResources:jmsDestinationResources:jmsDestinationResources_link");
        clickByIdAction(selectId);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:deleteDestButton");
        closeAlertAndGetItsText();
        waitForAlertProcess("modalBody");

        //Delete the instance
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link");
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
    }

/*
    @Test
    public void testAddingTransport() {

    }
*/
}
