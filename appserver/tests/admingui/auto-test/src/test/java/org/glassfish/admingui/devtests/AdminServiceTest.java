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
public class AdminServiceTest extends BaseSeleniumTestClass {

    @Test
    public void testEditJmxConntector() {
        gotoDasPage();
        String address = generateRandomNumber(255)+"."+generateRandomNumber(255)+"."+generateRandomNumber(255)+"."+generateRandomNumber(255);
        clickAndWait("treeForm:tree:configurations:server-config:adminService:adminService_link");
        if (!driver.findElement(By.id("form1:propertySheet:propertySheetSection:SecurityProp:Security")).isSelected())
            clickByIdAction("form1:propertySheet:propertySheetSection:SecurityProp:Security");
        setFieldValue("form1:propertySheet:propertySheetSection:AddressProp:Address", address);
        int count = addTableRow("form1:basicTable","form1:basicTable:topActionsGroup1:addSharedTableButton");
        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:adminService:adminService_link");
        assertTrue(driver.findElement(By.id("form1:propertySheet:propertySheetSection:SecurityProp:Security")).isSelected());
        assertEquals(address, getValue("form1:propertySheet:propertySheetSection:AddressProp:Address", "value"));
        assertTableRowCount("form1:basicTable", count);

        //delete the property used to test
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:adminService:adminService_link");
        clickByIdAction("form1:basicTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");
        clickByIdAction("form1:basicTable:topActionsGroup1:button1");
        waitforBtnDisable("form1:basicTable:topActionsGroup1:button1");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
    }


    @Test
    public void testSsl() {
        gotoDasPage();
        final String nickname = "nickname"+generateRandomString();
        final String keystore = "keystore"+generateRandomString()+".p12";
        final String maxCertLength = Integer.toString(generateRandomNumber(10));

        clickAndWait("treeForm:tree:configurations:server-config:adminService:adminService_link");
        clickAndWait("form1:jmxConnectorTab:jmxSSLEdit");

        waitForElementPresent("TtlTxt_sun4", "SSL");
        if(driver.findElement(By.id("propertyForm:propertySheet:propertySheetSection:SSL3Prop:SSL3")).isSelected()){
            clickByIdAction("propertyForm:propertySheet:propertySheetSection:SSL3Prop:SSL3");
        }
        if(driver.findElement(By.id("propertyForm:propertySheet:propertySheetSection:TLSProp:TLS")).isSelected()){
            clickByIdAction("propertyForm:propertySheet:propertySheetSection:TLSProp:TLS");
        }
        if(!driver.findElement(By.id("propertyForm:propertySheet:propertySheetSection:ClientAuthProp:ClientAuth")).isSelected()){
            clickByIdAction("propertyForm:propertySheet:propertySheetSection:ClientAuthProp:ClientAuth");
        }
        setFieldValue("propertyForm:propertySheet:propertySheetSection:CertNicknameProp:CertNickname", nickname);
        setFieldValue("propertyForm:propertySheet:propertySheetSection:keystore:keystore", keystore);
        setFieldValue("propertyForm:propertySheet:propertySheetSection:maxCertLength:maxCertLength", maxCertLength);
//        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:adminService:adminService_link");
        clickAndWait("form1:jmxConnectorTab:jmxSSLEdit");

        assertEquals(false, driver.findElement(By.id("propertyForm:propertySheet:propertySheetSection:SSL3Prop:SSL3")).isSelected());
        assertEquals(false, driver.findElement(By.id("propertyForm:propertySheet:propertySheetSection:TLSProp:TLS")).isSelected());
        assertEquals(true, driver.findElement(By.id("propertyForm:propertySheet:propertySheetSection:ClientAuthProp:ClientAuth")).isSelected());
        assertEquals(nickname, getValue("propertyForm:propertySheet:propertySheetSection:CertNicknameProp:CertNickname", "value"));
        assertEquals(keystore, getValue("propertyForm:propertySheet:propertySheetSection:keystore:keystore", "value"));
        assertEquals(maxCertLength, getValue("propertyForm:propertySheet:propertySheetSection:maxCertLength:maxCertLength", "value"));
    }

}
