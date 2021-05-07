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
import static org.junit.Assert.assertTrue;

public class IiopListenerTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_IIOP_LISTENERS = "i18n_corba.iiopListeners.ListPageHelp";
    private static final String TRIGGER_NEW_IIOP_LISTENER = "i18n_corba.iiopListener.newPageTitle";
    private static final String TRIGGER_EDIT_IIOP_LISTENER = "i18n_corba.iiopListener.editPageTitle";
    private static final String TRIGGER_ORB = "i18n_corba.orb.OrbInfo";
    private static final String TRIGGER_EDIT_IIOP_SSL = "i18n_corba.sslPageTitleHelp";

    //@Test
    public void testAddIiopListener() {
        final String iiopName = "testIiopListener" + generateRandomString();
        final String networkAddress = "0.0.0.0";
        final String listenerPort = Integer.toString(generateRandomNumber(32768));;
        final String certName = "s1as";

        SecurityTest securityTest = new SecurityTest();
        securityTest.enableSecureAdministration(true);
        try{
        clickAndWait("treeForm:tree:configurations:server-config:orb:iiopListeners:iiopListeners_link", TRIGGER_IIOP_LISTENERS);
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton", TRIGGER_NEW_IIOP_LISTENER);
        setFieldValue("propertyForm:propertySheet:generalSettingsSetion:IiopNameTextProp:IiopNameText", iiopName);
        setFieldValue("propertyForm:propertySheet:generalSettingsSetion:NetwkAddrProp:NetwkAddr", networkAddress);
        setFieldValue("propertyForm:propertySheet:generalSettingsSetion:ListenerPortProp:ListenerPort", listenerPort);

        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", "a");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "b");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", "c");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_IIOP_LISTENERS);
        assertTrue(isTextPresent(iiopName));

        clickAndWait(getLinkIdByLinkText("propertyForm:configs", iiopName), TRIGGER_EDIT_IIOP_LISTENER);
        assertEquals(networkAddress, getFieldValue("propertyForm:propertySheet:generalSettingsSetion:NetwkAddrProp:NetwkAddr"));
        assertEquals(listenerPort, getFieldValue("propertyForm:propertySheet:generalSettingsSetion:ListenerPortProp:ListenerPort"));

        assertTableRowCount("propertyForm:basicTable", count);

        // access the SSL Page
        clickAndWait("propertyForm:iiopTab:sslEdit", TRIGGER_EDIT_IIOP_SSL);
        setFieldValue("propertyForm:propertySheet:propertySheetSection:CertNicknameProp:CertNickname", certName);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        assertEquals(certName, getFieldValue("propertyForm:propertySheet:propertySheetSection:CertNicknameProp:CertNickname"));

        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_IIOP_LISTENERS);

        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", iiopName);
        } finally{ securityTest.enableSecureAdministration(false); }
    }

    @Test
    public void testORB() {
        final String totalConn = "1048";
        final String maxMsgSize = "2048";
        clickAndWait("treeForm:tree:configurations:server-config:orb:orb_link", TRIGGER_ORB);
        setFieldValue("form1:propertySheet:propertySectionTextField:TotalConnsProp:TotalConns", totalConn);
        selectDropdownOption("form1:propertySheet:propertySectionTextField:MaxMsgSizeProp:MaxMsgSize", maxMsgSize);
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        assertEquals(totalConn, getFieldValue("form1:propertySheet:propertySectionTextField:TotalConnsProp:TotalConns"));
        assertEquals(maxMsgSize, getFieldValue("form1:propertySheet:propertySectionTextField:MaxMsgSizeProp:MaxMsgSize"));

        // Load default button functionality is broken in all pages, once fixed need to uncomment
        //clickAndWaitForButtonEnabled("form1:propertyContentPage:loadDefaultsButton");
        //assertEquals("1024", getFieldValue("form1:propertySheet:propertySectionTextField:TotalConnsProp:TotalConns"));
        //assertEquals("1024", getFieldValue("form1:propertySheet:propertySectionTextField:MaxMsgSizeProp:MaxMsgSize"));
    }

    @Test
    public void testSSLIiopListener() {
        final String iiopName = "testIiopListener" + generateRandomString();
        final String networkAddress = "0.0.0.0";
        final String listenerPort = Integer.toString(generateRandomNumber(32768));;
        final String certName = "s1as";

        clickAndWait("treeForm:tree:configurations:server-config:orb:iiopListeners:iiopListeners_link", TRIGGER_IIOP_LISTENERS);
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton", TRIGGER_NEW_IIOP_LISTENER);
        setFieldValue("propertyForm:propertySheet:generalSettingsSetion:IiopNameTextProp:IiopNameText", iiopName);
        setFieldValue("propertyForm:propertySheet:generalSettingsSetion:NetwkAddrProp:NetwkAddr", networkAddress);
        setFieldValue("propertyForm:propertySheet:generalSettingsSetion:ListenerPortProp:ListenerPort", listenerPort);

        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", "a");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "b");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", "c");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_IIOP_LISTENERS);
        assertTrue(isTextPresent(iiopName));

        clickAndWait(getLinkIdByLinkText("propertyForm:configs", iiopName), TRIGGER_EDIT_IIOP_LISTENER);
        assertEquals(networkAddress, getFieldValue("propertyForm:propertySheet:generalSettingsSetion:NetwkAddrProp:NetwkAddr"));
        assertEquals(listenerPort, getFieldValue("propertyForm:propertySheet:generalSettingsSetion:ListenerPortProp:ListenerPort"));

        assertTableRowCount("propertyForm:basicTable", count);

        // access the SSL Page
        clickAndWait("propertyForm:iiopTab:sslEdit", TRIGGER_EDIT_IIOP_SSL);
        setFieldValue("propertyForm:propertySheet:propertySheetSection:CertNicknameProp:CertNickname", certName);
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_NEW_VALUES_SAVED);
        assertEquals(certName, getFieldValue("propertyForm:propertySheet:propertySheetSection:CertNicknameProp:CertNickname"));

        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_IIOP_LISTENERS);

        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", iiopName);
    }
}
