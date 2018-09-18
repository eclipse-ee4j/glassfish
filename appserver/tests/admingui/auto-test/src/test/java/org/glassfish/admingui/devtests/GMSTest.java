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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
/**
 * 
 * @author Jeremy Lv
 *
 */
public class GMSTest extends BaseSeleniumTestClass {

    @Test
    public void testConfig() {
        gotoDasPage();
        final String protocolMaxTrial = Integer.toString(generateRandomNumber(100));
        clickAndWait("treeForm:tree:configurations:default-config:default-config_turner:default-config_turner_image");
        clickAndWait("treeForm:tree:configurations:default-config:gms:gms_link");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:fdMax:fdMax", protocolMaxTrial);
        
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", "a");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "b");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", "c");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
        
        assertEquals(protocolMaxTrial, getValue("propertyForm:propertySheet:propertSectionTextField:fdMax:fdMax", "value"));
        assertTableRowCount("propertyForm:basicTable", count);
        
        //delete the property used to test
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:default-config:gms:gms_link");
        clickByIdAction("propertyForm:basicTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");
        clickByIdAction("propertyForm:basicTable:topActionsGroup1:button1");
        waitforBtnDisable("propertyForm:basicTable:topActionsGroup1:button1");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
    }
}
