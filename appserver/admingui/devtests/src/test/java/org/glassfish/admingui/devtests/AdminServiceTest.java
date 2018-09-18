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

public class AdminServiceTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_EDIT_JMX_CONNECTOR = "Edit JMX Connector";
    private static final String TRIGGER_SSL = "Requires the client to authenticate itself to the server";

    @Test
    public void testEditJmxConntector() {
        String address = generateRandomNumber(255)+"."+generateRandomNumber(255)+"."+generateRandomNumber(255)+"."+generateRandomNumber(255);
        clickAndWait("treeForm:tree:configurations:server-config:adminService:adminService_link", TRIGGER_EDIT_JMX_CONNECTOR);
        markCheckbox("form1:propertySheet:propertySheetSection:SecurityProp:Security");
        setFieldValue("form1:propertySheet:propertySheetSection:AddressProp:Address", address);
        int count = addTableRow("form1:basicTable","form1:basicTable:topActionsGroup1:addSharedTableButton");
        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        clickAndWait("form1:jmxConnectorTab:jmxSSLEdit", TRIGGER_SSL);
        clickAndWait("treeForm:tree:configurations:server-config:adminService:adminService_link", TRIGGER_EDIT_JMX_CONNECTOR);
        assertEquals(address, getFieldValue("form1:propertySheet:propertySheetSection:AddressProp:Address"));
        assertTableRowCount("form1:basicTable", count);
    }
    
    @Test
    public void testSsl() {
        final String nickname = "nickname"+generateRandomString();
        final String keystore = "keystore"+generateRandomString()+".jks";
        final String maxCertLength = Integer.toString(generateRandomNumber(10));

        SecurityTest securityTest = new SecurityTest();
        securityTest.enableSecureAdministration(true);
        try {
            clickAndWait("treeForm:tree:configurations:server-config:adminService:adminService_link", TRIGGER_EDIT_JMX_CONNECTOR);
            clickAndWait("form1:jmxConnectorTab:jmxSSLEdit", TRIGGER_SSL);

            clearCheckbox("propertyForm:propertySheet:propertySheetSection:SSL3Prop:SSL3");
            clearCheckbox("propertyForm:propertySheet:propertySheetSection:TLSProp:TLS");
            markCheckbox("propertyForm:propertySheet:propertySheetSection:ClientAuthProp:ClientAuth");
            setFieldValue("propertyForm:propertySheet:propertySheetSection:CertNicknameProp:CertNickname", nickname);
            setFieldValue("propertyForm:propertySheet:propertySheetSection:keystore:keystore", keystore);
            setFieldValue("propertyForm:propertySheet:propertySheetSection:maxCertLength:maxCertLength", maxCertLength);
//        selenium.click("propertyForm:propertySheet:sun_propertySheetSection433:CommonCiphersProp:commonAddRemove:commonAddRemove_addAllButton");
//        selenium.click("propertyForm:propertySheet:sun_propertySheetSection433:EphemeralCiphersProp:ephemeralAddRemove:ephemeralAddRemove_addAllButton");
//        selenium.click("propertyForm:propertySheet:sun_propertySheetSection433:OtherCiphersProp:otherAddRemove:otherAddRemove_addAllButton");
            if (isElementPresent("propertyForm:propertyContentPage:topButtons:newButton")) {
                clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_NEW_VALUES_SAVED);
            } else {
                clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
            }
            clickAndWait("treeForm:tree:configurations:server-config:adminService:adminService_link", TRIGGER_EDIT_JMX_CONNECTOR);
            clickAndWait("form1:jmxConnectorTab:jmxSSLEdit", TRIGGER_SSL);

            assertEquals(false, isChecked("propertyForm:propertySheet:propertySheetSection:SSL3Prop:SSL3"));
            assertEquals(false, isChecked("propertyForm:propertySheet:propertySheetSection:TLSProp:TLS"));
            assertEquals(true, isChecked("propertyForm:propertySheet:propertySheetSection:ClientAuthProp:ClientAuth"));
            assertEquals(nickname, getFieldValue("propertyForm:propertySheet:propertySheetSection:CertNicknameProp:CertNickname"));
            assertEquals(keystore, getFieldValue("propertyForm:propertySheet:propertySheetSection:keystore:keystore"));
            assertEquals(maxCertLength, getFieldValue("propertyForm:propertySheet:propertySheetSection:maxCertLength:maxCertLength"));
//        assertTrue(selenium.isTextPresent("SSL_RSA_WITH_RC4_128_MD5 SSL_RSA_WITH_RC4_128_SHA TLS_RSA_WITH_AES_128_CBC_SHA TLS_RSA_WITH_AES_256_CBC_SHA SSL_RSA_WITH_3DES_EDE_CBC_SHA __________________________________"));
//        assertTrue(selenium.isTextPresent("TLS_DHE_RSA_WITH_AES_128_CBC_SHA TLS_DHE_RSA_WITH_AES_256_CBC_SHA SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA TLS_DHE_DSS_WITH_AES_128_CBC_SHA TLS_DHE_DSS_WITH_AES_256_CBC_SHA SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA ______________________________________"));
//        assertTrue(selenium.isTextPresent("SSL_RSA_WITH_DES_CBC_SHA SSL_DHE_RSA_WITH_DES_CBC_SHA SSL_DHE_DSS_WITH_DES_CBC_SHA SSL_RSA_EXPORT_WITH_RC4_40_MD5 SSL_RSA_EXPORT_WITH_DES40_CBC_SHA SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA __________________________________________"));
        
         } finally {
             securityTest.enableSecureAdministration(false);
         }
    }
}
