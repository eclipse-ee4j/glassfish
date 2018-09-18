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
import static org.junit.Assert.assertTrue;
/**
 * 
 * @author Jeremy Lv
 *
 */
public class LoggerSettingsTest extends BaseSeleniumTestClass {

    @Test
    public void testLoggerSettings() {
        gotoDasPage();
        final String rotationLimit = Integer.toString(generateRandomNumber());
        final String rotationTimeLimit = Integer.toString(generateRandomNumber());
        final String flushFrequency = Integer.toString(generateRandomNumber());

        clickAndWait("treeForm:tree:configurations:server-config:loggerSetting:loggerSetting_link");
        if (!driver.findElement(By.id("form1:general:sheetSection:writeSystemLogEnabledProp:writeSystemLogEnabled")).isSelected()){
            clickByIdAction("form1:general:sheetSection:writeSystemLogEnabledProp:writeSystemLogEnabled");
        }
        setFieldValue("form1:general:sheetSection:FileRotationLimitProp:FileRotationLimit", rotationLimit);
        setFieldValue("form1:general:sheetSection:FileRotationTimeLimitProp:FileRotationTimeLimit", rotationTimeLimit);
        setFieldValue("form1:general:sheetSection:FlushFrequencyProp:FlushFrequency", flushFrequency);
        clickAndWait("form1:propertyContentPage:topButtons:saveButton");

        clickAndWait("form1:loggingTabs:loggerLevels");

        clickAndWait("treeForm:tree:configurations:server-config:loggerSetting:loggerSetting_link");
        assertTrue(driver.findElement(By.id("form1:general:sheetSection:writeSystemLogEnabledProp:writeSystemLogEnabled")).isSelected());
        assertEquals(rotationLimit, getValue("form1:general:sheetSection:FileRotationLimitProp:FileRotationLimit", "value"));
        assertEquals(rotationTimeLimit, getValue("form1:general:sheetSection:FileRotationTimeLimitProp:FileRotationTimeLimit", "value"));
        assertEquals(flushFrequency, getValue("form1:general:sheetSection:FlushFrequencyProp:FlushFrequency", "value"));
    }

    @Test
    public void testLogLevels() {
        gotoDasPage();
        final String loggerName = "testLogger" + Integer.toString(generateRandomNumber());
        clickAndWait("treeForm:tree:configurations:server-config:loggerSetting:loggerSetting_link");
        clickAndWait("form1:loggingTabs:loggerLevels");
        String newLevel = "WARNING";
        if ("WARNING".equals(getValue("form1:basicTable:rowGroup1:0:col3:level", "value"))) {
            newLevel = "INFO";
        }

        Select select = new Select(driver.findElement(By.id("form1:basicTable:topActionsGroup1:change_list")));
        select.selectByVisibleText(newLevel);
        clickByIdAction("form1:basicTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");

        waitForButtonEnabled("form1:basicTable:topActionsGroup1:button2");

        clickByIdAction("form1:basicTable:topActionsGroup1:button2");
        waitforBtnDisable("form1:basicTable:topActionsGroup1:button2");
        clickAndWait("form1:title:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:loggerSetting:loggerSetting_link");
        clickAndWait("form1:loggingTabs:loggerLevels");
        assertEquals(newLevel, getValue("form1:basicTable:rowGroup1:0:col3:level", "value"));

        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:loggerSetting:loggerSetting_link");
        clickAndWait("form1:loggingTabs:loggerLevels");
        // Add Logger
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton", "Logger Settings");
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", loggerName);
        clickAndWait("form1:title:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
        assertTableRowCount("form1:basicTable", count);
        
        //delete the property used to test
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:loggerSetting:loggerSetting_link");
        clickAndWait("form1:loggingTabs:loggerLevels");
        String prefix = getTableRowByVal("form1:basicTable", loggerName, "col2:col1St");
        String selectId = prefix + "col1:select";
        clickByIdAction(selectId);
        clickByIdAction("form1:basicTable:topActionsGroup1:button1");
        waitforBtnDisable("form1:basicTable:topActionsGroup1:button1");
        clickAndWait("form1:title:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
    } 
}
