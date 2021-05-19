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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
/**
 *
 * @author Jeremy Lv
 *
 */
public class HttpServiceTest extends BaseSeleniumTestClass {

    @Test
    public void testHttpService() {
        gotoDasPage();
        final String interval = Integer.toString(generateRandomNumber(2880));
        final String maxFiles = Integer.toString(generateRandomNumber(50));
        final String bufferSize = Integer.toString(generateRandomNumber(65536));
        final String logWriteInterval = Integer.toString(generateRandomNumber(600));

        clickAndWait("treeForm:tree:configurations:server-config:httpService:httpService_link");
        if(!driver.findElement(By.id("form1:propertySheet:http:acLog:ssoEnabled")).isSelected())
            clickByIdAction("form1:propertySheet:http:acLog:ssoEnabled");

        if(!driver.findElement(By.id("form1:propertySheet:accessLog:acLog:accessLoggingEnabled")).isSelected())
            clickByIdAction("form1:propertySheet:accessLog:acLog:accessLoggingEnabled");

        setFieldValue("form1:propertySheet:accessLog:intervalProp:Interval", interval);
        setFieldValue("form1:propertySheet:accessLog:MaxHistoryFiles:MaxHistoryFiles", maxFiles);
        setFieldValue("form1:propertySheet:accessLog:accessLogBufferSize:accessLogBufferSize", bufferSize);
        setFieldValue("form1:propertySheet:accessLog:accessLogWriteInterval:accessLogWriteInterval", logWriteInterval);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        sleep(500);
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:httpService:httpService_link");
        assertEquals(true, driver.findElement(By.id("form1:propertySheet:http:acLog:ssoEnabled")).isSelected());
        assertEquals(interval, getValue("form1:propertySheet:accessLog:intervalProp:Interval", "value"));
        assertEquals(maxFiles, getValue("form1:propertySheet:accessLog:MaxHistoryFiles:MaxHistoryFiles", "value"));
        assertEquals(bufferSize, getValue("form1:propertySheet:accessLog:accessLogBufferSize:accessLogBufferSize", "value"));
        assertEquals(logWriteInterval, getValue("form1:propertySheet:accessLog:accessLogWriteInterval:accessLogWriteInterval", "value"));
        assertTableRowCount("form1:basicTable", count);

        //delete the property used to test
        clickByIdAction("form1:basicTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");
        clickByIdAction("form1:basicTable:topActionsGroup1:button1");
        waitforBtnDisable("form1:basicTable:topActionsGroup1:button1");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
    }
}
