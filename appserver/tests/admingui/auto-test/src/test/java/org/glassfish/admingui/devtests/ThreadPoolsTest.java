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
public class ThreadPoolsTest extends BaseSeleniumTestClass {

    @Test
    public void testAddThreadPool() {
        gotoDasPage();
        final String threadPoolName = "testThreadPool"+generateRandomString();

        clickAndWait("treeForm:tree:configurations:server-config:threadPools:threadPools_link");
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:nameProp:nameText", threadPoolName);
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:max:max", "8192");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:maxThread:maxThread", "10");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:minThread:minThread", "4");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:timeout:timeout", "1800");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton");
        
        String prefix = getTableRowByValue("propertyForm:configs", threadPoolName, "col1");
        assertEquals(threadPoolName, getText(prefix + "col1:link"));
        
        String clickId = prefix + "col1:link";
        clickAndWait(clickId);
        
        assertEquals("8192", getValue("propertyForm:propertySheet:propertSectionTextField:max:max", "value"));
        assertEquals("10", getValue("propertyForm:propertySheet:propertSectionTextField:maxThread:maxThread", "value"));
        assertEquals("4", getValue("propertyForm:propertySheet:propertSectionTextField:minThread:minThread", "value"));
        assertEquals("1800", getValue("propertyForm:propertySheet:propertSectionTextField:timeout:timeout", "value"));
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton");

        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", threadPoolName);
    }
}
