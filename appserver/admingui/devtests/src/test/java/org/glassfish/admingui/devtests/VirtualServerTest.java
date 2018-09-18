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

import static org.junit.Assert.assertTrue;

public class VirtualServerTest extends BaseSeleniumTestClass {
    public static final String TRIGGER_VIRTUAL_SERVERS = "i18n_web.vs.ListPageHelp";
    public static final String TRIGGER_NEW_VIRTUAL_SERVER = "i18n_web.vs.NewPageHelp";
    public static final String TRIGGER_EDIT_VIRTUAL_SERVER = "i18n_web.vs.PageHelp";

    @Test
    public void testAddVirtualServer() {
        final String serverName = "vs" + generateRandomString();

        clickAndWait("treeForm:tree:configurations:server-config:virtualServers:virtualServers_link", TRIGGER_VIRTUAL_SERVERS);
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton", TRIGGER_NEW_VIRTUAL_SERVER);
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:IdTextProp:IdText", serverName);
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:hostsProp:Hosts", "localhost");
        selectDropdownOption("propertyForm:propertySheet:propertSectionTextField:stateProp:state", "i18n_web.vs.StateOn");
        selectDropdownOption("propertyForm:propertySheet:propertSectionTextField:enableLog:state", "i18n_web.vs.alwaysEnable");
        selectDropdownOption("propertyForm:propertySheet:al:enableLog:log", "i18n_web.vs.alwaysEnable");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:logFileProp:LogFile", "logfile.txt");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:docroot:docroot", "/tmp");
        addSelectSelection("propertyForm:propertySheet:propertSectionTextField:nwProps:nw", "http-listener-1");
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", "property");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", "description");

        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_VIRTUAL_SERVERS);

        assertTrue(tableContainsRow("propertyForm:configs", "col1", serverName));

        clickAndWait(getLinkIdByLinkText("propertyForm:configs", serverName), TRIGGER_EDIT_VIRTUAL_SERVER);

        assertTableRowCount("propertyForm:basicTable", count);

        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_VIRTUAL_SERVERS);

        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", serverName);
    }
}
