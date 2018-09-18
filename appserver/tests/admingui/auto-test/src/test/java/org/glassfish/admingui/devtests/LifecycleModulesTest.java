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
 * @author jeremylv
 *
 */
public class LifecycleModulesTest extends BaseSeleniumTestClass {

    public static final String ID_LIFECYCLE_TABLE = "propertyForm:deployTable";
    
    @Test
    public void testLifecycleModules() {
        final String lifecycleName = "TestLifecycle"+generateRandomString();
        final String lifecycleClassname = "org.foo.nonexistent.Lifecyclemodule";

        gotoDasPage();
        clickByIdAction("treeForm:tree:lifecycles:lifecycles_link");
        clickByIdAction("propertyForm:deployTable:topActionsGroup1:newButton");
        setFieldValue("form:propertySheet:propertSectionTextField:IdTextProp:IdText", lifecycleName);
        setFieldValue("form:propertySheet:propertSectionTextField:classNameProp:classname", lifecycleClassname);
        clickByIdAction("form:propertyContentPage:topButtons:newButton");
        String prefix = getTableRowByValue(ID_LIFECYCLE_TABLE, lifecycleName, "col1");
        try {
            assertEquals(lifecycleName, getText(prefix + "col1:link"));
        } catch (Error e) {
            verificationErrors.append(e.toString());
        };
        
        //test Disable button and add some property 
        String clickId = getTableRowByValue(ID_LIFECYCLE_TABLE, lifecycleName, "col1")+"col0:select";
        testDisableButton(clickId, prefix);
        
        //test Enable button and delete some property
        testEnableButton(clickId, prefix);
        
        //delete the lifecycle
        testDeleteButton(clickId);
    }

    private void testDeleteButton(String clickId) {
        gotoDasPage();
        clickByIdAction("treeForm:tree:lifecycles:lifecycles_link");
        clickByIdAction(clickId);
        clickByIdAction("propertyForm:deployTable:topActionsGroup1:button1");
        String msg = closeAlertAndGetItsText();
        waitForAlertProcess("modalBody");
        try {
            assertTrue(msg.indexOf("Selected Lifecycle Module(s) will be deleted.") != -1);
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }
    }

    private void testEnableButton(String clickId, String prefix) {
        gotoDasPage();
        clickByIdAction("treeForm:tree:lifecycles:lifecycles_link");
        clickByIdAction(clickId);
        clickByIdAction("propertyForm:deployTable:topActionsGroup1:button2");
        isCheckboxSelected(clickId);
        clickByIdAction(prefix + "col1:link");
        
        //delete property
        isElementPresent("propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        clickByIdAction("propertyForm:basicTable:rowGroup1:0:col1:select");
        clickByIdAction("propertyForm:basicTable:topActionsGroup1:button1");
        clickByIdAction("propertyForm:propertyContentPage:topButtons:saveButton");
        assertEquals(true, driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:statusEdit:status")).isSelected());
    }
    
    private void testDisableButton(String clickId, String prefix) {
        clickByIdAction(clickId);
        isElementPresent("propertyForm:deployTable:topActionsGroup1:button3");
        clickByIdAction("propertyForm:deployTable:topActionsGroup1:button3");
        isCheckboxSelected(clickId);
        clickByIdAction(prefix + "col1:link");
        
        //add property and verify
        isElementPresent("propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        int lifecyclePropCount = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St","test");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St","value");
        clickByIdAction("propertyForm:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
        assertTableRowCount("propertyForm:basicTable", lifecyclePropCount);
        assertEquals(false, driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:statusEdit:status")).isSelected());
    }
}
