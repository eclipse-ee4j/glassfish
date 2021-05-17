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
public class JVMReportTest extends BaseSeleniumTestClass {


    @Test
    public void testJVMLink() {
        gotoDasPage();
        clickAndWait("treeForm:tree:applicationServer:applicationServer_link");
        assertEquals("JVM Report",getText("propertyForm:propertySheet:serverGeneralPropSheet:jvmProp:jvmlink"));
        String winHandleBefore = driver.getWindowHandle();

        clickAndWait("propertyForm:propertySheet:serverGeneralPropSheet:jvmProp:jvmlink");
        for(String winHandle : driver.getWindowHandles()){
            driver.switchTo().window(winHandle);
        }
        isElementPresent("propertyForm:propertyContentPage:propertySheet:viewPropertySection:ViewProp:View");
        assertTrue(getText("propertyForm:propertyContentPage:propertySheet:reportPropertySection:ReportProp:Report").contains("Operating System Information"));
        driver.close();

        driver.switchTo().window(winHandleBefore);
    }
}

