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
public class TransactionServiceTest extends BaseSeleniumTestClass {

    @Test
    public void testTransactionService() {
        gotoDasPage();
        final String timeout = Integer.toString(generateRandomNumber(60));
        final String retry = Integer.toString(generateRandomNumber(600));
        final String keypoint = Integer.toString(generateRandomNumber(65535));

        clickAndWait("treeForm:tree:configurations:server-config:transactionService:transactionService_link");
        if (!driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:onRestartProp:enabled")).isSelected()){
            clickByIdAction("propertyForm:propertySheet:propertSectionTextField:onRestartProp:enabled");
        }
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:timeoutProp:Timeout", timeout);
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:retryProp:Retry", retry);
        Select select = new Select(driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:heuristicProp:HeuristicDecision")));
        select.selectByVisibleText("Commit");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:keyPointProp:Keypoint", keypoint);
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");

        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:transactionService:transactionService_link");
        assertTrue(driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:onRestartProp:enabled")).isSelected());
        assertEquals(timeout, getValue("propertyForm:propertySheet:propertSectionTextField:timeoutProp:Timeout", "value"));
        assertEquals(retry, getValue("propertyForm:propertySheet:propertSectionTextField:retryProp:Retry", "value"));
        assertEquals("commit", getValue("propertyForm:propertySheet:propertSectionTextField:heuristicProp:HeuristicDecision", "value"));
        assertEquals(keypoint, getValue("propertyForm:propertySheet:propertSectionTextField:keyPointProp:Keypoint", "value"));
        assertTableRowCount("propertyForm:basicTable", count);

        //delete the property used to test
        clickByIdAction("propertyForm:basicTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");
        clickByIdAction("propertyForm:basicTable:topActionsGroup1:button1");
        waitforBtnDisable("propertyForm:basicTable:topActionsGroup1:button1");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
    }
}
