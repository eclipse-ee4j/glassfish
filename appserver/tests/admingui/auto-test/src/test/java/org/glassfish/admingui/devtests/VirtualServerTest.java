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
/**
 *
 * @author Jeremy Lv
 *
 */
public class VirtualServerTest extends BaseSeleniumTestClass {

    @Test
    public void testAddVirtualServer() {
        gotoDasPage();
        final String serverName = "vs" + generateRandomString();

        clickAndWait("treeForm:tree:configurations:server-config:virtualServers:virtualServers_link");
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:IdTextProp:IdText", serverName);
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:hostsProp:Hosts", "localhost");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:logFileProp:LogFile", "logfile.txt");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:docroot:docroot", "/tmp");
        Select select = new Select(driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:nwProps:nw")));
        select.selectByVisibleText("http-listener-1");
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", "property");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", "description");

        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton");

        String prefix = getTableRowByValue("propertyForm:configs", serverName, "col1");
        assertEquals(serverName, getText(prefix + "col1:link"));
        String clickId = prefix + "col1:link";
        clickByIdAction(clickId);

        assertTableRowCount("propertyForm:basicTable", count);

        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton");

        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", serverName);
    }
}
