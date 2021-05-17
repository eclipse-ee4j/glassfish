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
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
/**
 *
 * @author Jeremy Lv
 *
 */
public class AvailabilityServiceTest extends BaseSeleniumTestClass {
    public static final String ID_AVAILABILITY_SERVICE_TREE_NODE = "treeForm:tree:configurations:default-config:availabilityService:availabilityService_link";
    private static final String ID_DEFAULT_CONFIG_TURNER = "treeForm:tree:configurations:default-config:default-config_turner:default-config_turner_image";

    @Test
    public void testAvailabilityService() {
        // Expand node
        gotoDasPage();
        clickAndWait(ID_DEFAULT_CONFIG_TURNER);
        clickAndWait(ID_AVAILABILITY_SERVICE_TREE_NODE);

        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", generateRandomString());
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", generateRandomString());
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", generateRandomString());
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
        assertTableRowCount("propertyForm:basicTable", count);

        //Delete the property used to test after the test finished
        gotoDasPage();
        clickAndWait(ID_AVAILABILITY_SERVICE_TREE_NODE);
        clickByIdAction("propertyForm:basicTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");
        clickByIdAction("propertyForm:basicTable:topActionsGroup1:button1");
        waitforBtnDisable("propertyForm:basicTable:topActionsGroup1:button1");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
    }

//    //TODO:the test need to be finished after the issue of GLASSFISH-20810 had to be resolved!
//    @Test
//    public void testWebContainerAvailability() {
//        gotoDasPage();
//        clickAndWait(ID_DEFAULT_CONFIG_TURNER);
//        clickAndWait(ID_AVAILABILITY_SERVICE_TREE_NODE);
//        clickAndWait("propertyForm:availabilityTabs:webAvailabilityTab");
//
//        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
//        sleep(500);
//        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", generateRandomString());
//        sleep(500);
//        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", generateRandomString());
//        sleep(500);
//        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", generateRandomString());
//        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
//        assertTableRowCount("propertyForm:basicTable", count);
//    }
//
//  //TODO:the test need to be finished after the issue of GLASSFISH-20810 had to be resolved!
//    @Test
//    public void testEjbContainerAvailability() {
//        gotoDasPage();
//        clickAndWait(ID_DEFAULT_CONFIG_TURNER);
//        clickAndWait(ID_AVAILABILITY_SERVICE_TREE_NODE);
//        clickAndWait("propertyForm:availabilityTabs:ejbAvailabilityTab");
//
//        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
//        sleep(500);
//        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", generateRandomString());
//        sleep(500);
//        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", generateRandomString());
//        sleep(500);
//        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", generateRandomString());
//        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
//        assertTableRowCount("propertyForm:basicTable", count);
//
//    }

    @Test
    public void testJMSAvailability() {
        final String clusterName = "cluster" + generateRandomString();
        final String DB_VENDOR = "mysql";
        final String DB_USER = generateRandomString();
        final String DB_URL = "jdbc:mysql://hostname:portno/dbname?password=" + generateRandomString();
        final String DB_PASSWORD = generateRandomString();

        ClusterTest ct = new ClusterTest();
        ct.createCluster(clusterName);

        clickAndWait("treeForm:tree:configurations:" + clusterName + "-config:availabilityService:availabilityService_link");
        clickAndWait("propertyForm:availabilityTabs:jmsAvailabilityTab");

        if (!driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:AvailabilityEnabledProp:avail")).isSelected()) {
            clickAndWait("propertyForm:propertySheet:propertSectionTextField:AvailabilityEnabledProp:avail:avail_label");
        }

        isElementPresent("propertyForm:propertySheet:propertSectionTextField:ConfigStoreTypeProp:ConfigStoreType");
        Select select = new Select(driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:ConfigStoreTypeProp:ConfigStoreType")));
        select.selectByVisibleText("masterbroker");
        isElementPresent("propertyForm:propertySheet:propertSectionTextField:MessageStoreTypeProp:MessageStoreType");
        Select select1 = new Select(driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:MessageStoreTypeProp:MessageStoreType")));
        select1.selectByVisibleText("file");

        setFieldValue("propertyForm:propertySheet:propertSectionTextField:DbVendorProp:DbVendor", DB_VENDOR);
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:DbUserNameProp:DbUserName", DB_USER);
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:DbPasswordProp:DbPassword", DB_PASSWORD);
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:DbUrlProp:DbUrl", DB_URL);
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", generateRandomString());
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", generateRandomString());
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", generateRandomString());

        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:" + clusterName + "-config:availabilityService:availabilityService_link");
        clickAndWait("propertyForm:availabilityTabs:jmsAvailabilityTab");

//        //The availability service in JMS Availability tap can't be enabled, seems this is a bug need to be resolved first!
//        assertTrue(driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:AvailabilityEnabledProp:avail")).isSelected());

        assertEquals(DB_VENDOR, getValue("propertyForm:propertySheet:propertSectionTextField:DbVendorProp:DbVendor", "value"));
        assertEquals(DB_USER, getValue("propertyForm:propertySheet:propertSectionTextField:DbUserNameProp:DbUserName", "value"));
        assertEquals(DB_PASSWORD, getValue("propertyForm:propertySheet:propertSectionTextField:DbPasswordProp:DbPassword", "value"));
        assertEquals(DB_URL, getValue("propertyForm:propertySheet:propertSectionTextField:DbUrlProp:DbUrl", "value"));
        assertTableRowCount("propertyForm:basicTable", count);

        //Delete the property used to test after the test finished
        clickByIdAction("propertyForm:basicTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");
        clickByIdAction("propertyForm:basicTable:topActionsGroup1:button1");
        waitforBtnDisable("propertyForm:basicTable:topActionsGroup1:button1");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        ct.deleteAllCluster();
    }
}
