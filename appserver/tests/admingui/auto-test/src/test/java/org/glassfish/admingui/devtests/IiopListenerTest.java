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
public class IiopListenerTest extends BaseSeleniumTestClass {

    @Test
    public void testAddIiopListener() {
        gotoDasPage();
        final String iiopName = "testIiopListener" + generateRandomString();
        final String networkAddress = "0.0.0.0";
        final String listenerPort = Integer.toString(generateRandomNumber(32768));;
        final String certName = "s1as";

        clickAndWait("treeForm:tree:configurations:server-config:orb:iiopListeners:iiopListeners_link");
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton");
        setFieldValue("propertyForm:propertySheet:generalSettingsSetion:IiopNameTextProp:IiopNameText", iiopName);
        setFieldValue("propertyForm:propertySheet:generalSettingsSetion:NetwkAddrProp:NetwkAddr", networkAddress);
        setFieldValue("propertyForm:propertySheet:generalSettingsSetion:ListenerPortProp:ListenerPort", listenerPort);

        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", "a");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "b");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", "c");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton");

        String prefix = getTableRowByValue("propertyForm:configs", iiopName, "col1");
        assertEquals(iiopName, getText(prefix + "col1:link"));

        String clickId = prefix + "col1:link";
        clickByIdAction(clickId);
        assertEquals(networkAddress, getValue("propertyForm:propertySheet:generalSettingsSetion:NetwkAddrProp:NetwkAddr", "value"));
        assertEquals(listenerPort, getValue("propertyForm:propertySheet:generalSettingsSetion:ListenerPortProp:ListenerPort", "value"));

        assertTableRowCount("propertyForm:basicTable", count);

        // access the SSL Page
        clickAndWait("propertyForm:iiopTab:sslEdit");
        setFieldValue("propertyForm:propertySheet:propertySheetSection:CertNicknameProp:CertNickname", certName);
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        assertEquals(certName, getValue("propertyForm:propertySheet:propertySheetSection:CertNicknameProp:CertNickname", "value"));

        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton");

        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", iiopName);
    }

    @Test
    public void testORB() {
        gotoDasPage();
        final String totalConn = "1048";
        final String maxMsgSize = "2048";
        clickAndWait("treeForm:tree:configurations:server-config:orb:orb_link");
        setFieldValue("form1:propertySheet:propertySectionTextField:TotalConnsProp:TotalConns", totalConn);
        Select select = new Select(driver.findElement(By.id("form1:propertySheet:propertySectionTextField:MaxMsgSizeProp:MaxMsgSize")));
        select.selectByVisibleText(maxMsgSize);

        clickAndWait("form1:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        assertEquals(totalConn, getValue("form1:propertySheet:propertySectionTextField:TotalConnsProp:TotalConns", "value"));
        assertEquals(maxMsgSize, getValue("form1:propertySheet:propertySectionTextField:MaxMsgSizeProp:MaxMsgSize", "value"));
    }
}
