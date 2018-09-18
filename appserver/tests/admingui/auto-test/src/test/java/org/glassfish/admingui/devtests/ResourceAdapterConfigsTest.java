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

import static org.junit.Assert.*;
/**
 * 
 * @author Jeremy Lv
 *
 */
public class ResourceAdapterConfigsTest extends BaseSeleniumTestClass {
    @Test
    public void testResourceAdapterConfigs() throws Exception {
            gotoDasPage();
            clickAndWait("treeForm:tree:resources:resourceAdapterConfigs:resourceAdapterConfigs_link");
            int emptyCount = getTableRowCountByValue("propertyForm:poolTable", "jmsra", "col1:link", true);
            if (emptyCount != 0){
                gotoDasPage();
                clickAndWait("treeForm:tree:resources:resourceAdapterConfigs:resourceAdapterConfigs_link");
                deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", "jmsra");
            }

            // Create new RA config
            clickAndWait("propertyForm:poolTable:topActionsGroup1:newButton");
            Select select = new Select(driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:threadPoolsIdProp:threadpoolsid")));
            select.selectByVisibleText("thread-pool-1");
            clickAndWait("propertyForm:propertyContentPage:topButtons:newButton");

            // Verify config was saved and update values
            String prefix = getTableRowByValue("propertyForm:poolTable", "jmsra", "col1");
            assertEquals("jmsra", getText(prefix + "col1:link"));
            String clickId = prefix + "col1:link";
            clickByIdAction(clickId);
            Select select1 = new Select(driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:threadPoolsIdProp:threadpoolsid")));
            assertTrue(select1.getFirstSelectedOption().getAttribute("value").equals("thread-pool-1"));
            clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton");
            
            // Remove config
            gotoDasPage();
            clickAndWait("treeForm:tree:resources:resourceAdapterConfigs:resourceAdapterConfigs_link");
            deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", "jmsra");
    }
}
