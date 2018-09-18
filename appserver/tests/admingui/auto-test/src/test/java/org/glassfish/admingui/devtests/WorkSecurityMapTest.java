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
/**
 * 
 * @author Jeremy Lv
 *
 */
public class WorkSecurityMapTest extends BaseSeleniumTestClass {


    @Test
    public void testWorkSecurityMaps() throws Exception {
        gotoDasPage();
        final String testWorkSecurityMap = generateRandomString();
        final String testGroupMapKey = generateRandomString();
        final String testGroupMapValue = generateRandomString();

        clickAndWait("treeForm:tree:resources:Connectors:workSecurityMaps:workSecurityMaps_link");

        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton");

        setFieldValue("propertyForm:propertySheet:propertSectionTextField:mapNameNew:mapName", testWorkSecurityMap);
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:groupProp:eisgrouptext", testGroupMapKey + "=" + testGroupMapValue);
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton");

        String prefix = getTableRowByValue("propertyForm:resourcesTable", testWorkSecurityMap, "col1");
        assertEquals(testWorkSecurityMap, getText(prefix + "col1:link"));

        String clickId = prefix + "col1:link";
        clickByIdAction(clickId);
        
        assertEquals(testGroupMapKey + "=" + testGroupMapValue, getValue("propertyForm:propertySheet:propertSectionTextField:groupProp:eisgrouptext", "value"));
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton");

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", testWorkSecurityMap);
    }
}
