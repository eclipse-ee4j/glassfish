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

import static org.junit.Assert.assertEquals;

public class LoggerSettingsTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_LOGGER_SETTINGS = "i18nc.log.LoggingSettingsHelp";
    private static final String TRIGGER_LOG_LEVELS = "i18nc.log.LogLevelsPageTitleHelp";

    @Test
    public void testLoggerSettings() {
        final String rotationLimit = Integer.toString(generateRandomNumber(500*1000, Integer.MAX_VALUE - 1));
        final String rotationTimeLimit = Integer.toString(generateRandomNumber(0, Integer.MAX_VALUE - 1));
        final String flushFrequency = Integer.toString(generateRandomNumber());

        clickAndWait("treeForm:tree:configurations:server-config:loggerSetting:loggerSetting_link", TRIGGER_LOGGER_SETTINGS);
        markCheckbox("form1:general:sheetSection:writeSystemLogEnabledProp:writeSystemLogEnabled");
        boolean enabled = isChecked("form1:general:sheetSection:writeSystemLogEnabledProp:writeSystemLogEnabled");
        setFieldValue("form1:general:sheetSection:FileRotationLimitProp:FileRotationLimit", rotationLimit);
        setFieldValue("form1:general:sheetSection:FileRotationTimeLimitProp:FileRotationTimeLimit", rotationTimeLimit);
        setFieldValue("form1:general:sheetSection:FlushFrequencyProp:FlushFrequency", flushFrequency);
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);

        clickAndWait("form1:loggingTabs:loggerLevels", TRIGGER_LOG_LEVELS);

        clickAndWait("treeForm:tree:configurations:server-config:loggerSetting:loggerSetting_link", TRIGGER_LOGGER_SETTINGS);
        assertEquals(enabled, isChecked("form1:general:sheetSection:writeSystemLogEnabledProp:writeSystemLogEnabled"));
        assertEquals(rotationLimit, getFieldValue("form1:general:sheetSection:FileRotationLimitProp:FileRotationLimit"));
        assertEquals(rotationTimeLimit, getFieldValue("form1:general:sheetSection:FileRotationTimeLimitProp:FileRotationTimeLimit"));
        assertEquals(flushFrequency, getFieldValue("form1:general:sheetSection:FlushFrequencyProp:FlushFrequency"));
    }

    @Test
    public void testLogLevels() {
        final String loggerName = "testLogger" + Integer.toString(generateRandomNumber());
        clickAndWait("treeForm:tree:configurations:server-config:loggerSetting:loggerSetting_link", TRIGGER_LOGGER_SETTINGS);
        clickAndWait("form1:loggingTabs:loggerLevels", TRIGGER_LOG_LEVELS);
        String newLevel = "WARNING";
        if ("WARNING".equals(getFieldValue("form1:basicTable:rowGroup1:0:col3:level"))) {
            newLevel = "INFO";
        }

        selectDropdownOption("form1:basicTable:topActionsGroup1:change_list", newLevel);
        pressButton("form1:basicTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");

        waitForButtonEnabled("form1:basicTable:topActionsGroup1:button2");

        pressButton("form1:basicTable:topActionsGroup1:button2");
        waitForButtonDisabled("form1:basicTable:topActionsGroup1:button2");

        clickAndWait("form1:title:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);

        clickAndWait("form1:loggingTabs:loggerGeneral", TRIGGER_LOGGER_SETTINGS);
        clickAndWait("form1:loggingTabs:loggerLevels", TRIGGER_LOG_LEVELS);
        assertEquals(newLevel, getFieldValue("form1:basicTable:rowGroup1:0:col3:level"));

        clickAndWait("treeForm:tree:configurations:server-config:loggerSetting:loggerSetting_link", TRIGGER_LOGGER_SETTINGS);
        clickAndWait("form1:loggingTabs:loggerLevels", TRIGGER_LOG_LEVELS);

        // Add Logger
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton", "Logger Settings");
        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", loggerName);
        clickAndWait("form1:title:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        assertTableRowCount("form1:basicTable", count);

        // delete functionality through dev test framework does not select the rows and it hangs
        // need to be fixed
        //deleteRow("form1:basicTable:topActionsGroup1:button1", "form1:basicTable", loggerName);
        //clickAndWait("form1:title:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
    }
}
