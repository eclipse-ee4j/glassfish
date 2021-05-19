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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Jeremy Lv
 *
 */
public class WebContainerTest extends BaseSeleniumTestClass {

    @Test
    public void testGeneralTab() {
        gotoDasPage();
        final String property = "property" + generateRandomString();
        final String value = generateRandomString();
        final String description = "Description for " + property;

        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link");

        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", property);
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", value);
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", description);
        clickAndWait("form1:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link");
        assertTableRowCount("form1:basicTable", count);


        //Delete all of the property after the tests finished
        if (count != 0){
            gotoDasPage();
            clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link");
            clickByIdAction("form1:basicTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");
            clickByIdAction("form1:basicTable:topActionsGroup1:button1");
            waitforBtnDisable("form1:basicTable:topActionsGroup1:button1");
            clickAndWait("form1:propertyContentPage:topButtons:saveButton");
            assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
        }
    }

    @Test
    public void testSessionProperties() {
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link");
        clickAndWait("form1:webContainerTabs:sessionTab");
        waitForElementPresent("TtlTxt_sun4", "Session Properties");

        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", "description");

        setFieldValue("form1:sessionPropSheet:sessionPropSheetSection:SessionTimeoutProp:SessionTimeout", "300");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link");
        clickAndWait("form1:webContainerTabs:sessionTab");
        waitForElementPresent("TtlTxt_sun4", "Session Properties");

        assertTableRowCount("form1:basicTable", count);
        assertEquals("300", getValue("form1:sessionPropSheet:sessionPropSheetSection:SessionTimeoutProp:SessionTimeout", "value"));
        setFieldValue("form1:sessionPropSheet:sessionPropSheetSection:SessionTimeoutProp:SessionTimeout", "1800");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        //Delete all of the property after the tests finished
        if (count != 0){
            gotoDasPage();
            clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link");
            clickAndWait("form1:webContainerTabs:sessionTab");
            waitForElementPresent("TtlTxt_sun4", "Session Properties");
            clickByIdAction("form1:basicTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");
            clickByIdAction("form1:basicTable:topActionsGroup1:button1");
            waitforBtnDisable("form1:basicTable:topActionsGroup1:button1");
            clickAndWait("form1:propertyContentPage:topButtons:saveButton");
            assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
        }
    }

    @Test
    public void testManagerProperties() {
        final String reapInterval = Integer.toString(generateRandomNumber(100));
        final String maxSessions = Integer.toString(generateRandomNumber(1000));
        final String sessFileName = generateRandomString();

        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link");
        clickAndWait("form1:webContainerTabs:managerTab");
        waitForElementPresent("TtlTxt_sun4", "Manager Properties");

        setFieldValue("form1:managerPropSheet:managerPropSheetSection:ReapIntervalProp:ReapInterval", reapInterval);
        setFieldValue("form1:managerPropSheet:managerPropSheetSection:MaxSessionsProp:MaxSessions", maxSessions);
        setFieldValue("form1:managerPropSheet:managerPropSheetSection:SessFileNameProp:SessFileName", sessFileName);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link");
        clickAndWait("form1:webContainerTabs:managerTab");
        waitForElementPresent("TtlTxt_sun4", "Manager Properties");

        assertEquals(reapInterval, getValue("form1:managerPropSheet:managerPropSheetSection:ReapIntervalProp:ReapInterval", "value"));
        assertEquals(maxSessions, getValue("form1:managerPropSheet:managerPropSheetSection:MaxSessionsProp:MaxSessions", "value"));
        assertEquals(sessFileName, getValue("form1:managerPropSheet:managerPropSheetSection:SessFileNameProp:SessFileName", "value"));
        assertTableRowCount("form1:basicTable", count);

        //Delete all of the property after the tests finished
        if (count != 0){
            gotoDasPage();
            clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link");
            clickAndWait("form1:webContainerTabs:managerTab");
            waitForElementPresent("TtlTxt_sun4", "Manager Properties");
            clickByIdAction("form1:basicTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");
            clickByIdAction("form1:basicTable:topActionsGroup1:button1");
            waitforBtnDisable("form1:basicTable:topActionsGroup1:button1");
            clickAndWait("form1:propertyContentPage:topButtons:saveButton");
            assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
        }
    }

    @Test
    public void testStoreProperties() {
        final String directory = generateRandomString();

        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link");
        clickAndWait("form1:webContainerTabs:storeTab");
//        waitForElementPresent("TtlTxt_sun4", "Store Properties");

        isElementPresent("form1:storePropSheet:storePropSheetSection:DirectoryProp:Directory");
        setFieldValue("form1:storePropSheet:storePropSheetSection:DirectoryProp:Directory", directory);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link");
        clickAndWait("form1:webContainerTabs:storeTab");
        waitForElementPresent("TtlTxt_sun4", "Store Properties");
        assertEquals(directory, getValue("form1:storePropSheet:storePropSheetSection:DirectoryProp:Directory", "value"));
        assertTableRowCount("form1:basicTable", count);

        setFieldValue("form1:storePropSheet:storePropSheetSection:DirectoryProp:Directory", "");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        //Delete all of the property after the tests finished
        if (count != 0){
            gotoDasPage();
            clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link");
            clickAndWait("form1:webContainerTabs:storeTab");
            waitForElementPresent("TtlTxt_sun4", "Store Properties");
            clickByIdAction("form1:basicTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");
            clickByIdAction("form1:basicTable:topActionsGroup1:button1");
            waitforBtnDisable("form1:basicTable:topActionsGroup1:button1");
            clickAndWait("form1:propertyContentPage:topButtons:saveButton");
            assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
        }
    }

    /*  To test the property table.
     *  If the property value/name is empty,  that property will not be created.
     *  If the property value is (), property will be persisted with the empty value.
     */
    @Test
    public void testWebContainerPropertyTable() {

        final String property1 = "property1" + generateRandomString();
        final String value1 = "value"+generateRandomString();
        final String description1 = "Description for " + property1;
        final String property2 = "property2" + generateRandomString();
        final String value2 = "";
        final String description2 = "Description for " + property2;

        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link");

        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", property1);
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", value1);
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", description1);
        clickAndWait("form1:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link");
        count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", property2);
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", value2);
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", description2);
        clickAndWait("form1:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        assertTableRowCount("form1:basicTable", count-1);

        //Delete all of the property after the tests finished
        if (count != 0){
            gotoDasPage();
            clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link");
            clickByIdAction("form1:basicTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");
            clickByIdAction("form1:basicTable:topActionsGroup1:button1");
            waitforBtnDisable("form1:basicTable:topActionsGroup1:button1");
            clickAndWait("form1:propertyContentPage:topButtons:saveButton");
            assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
        }
    }
}
