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

import static org.junit.Assert.*;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * 
 * @author Jeremy Lv
 *
 */
public class LogViewerTest extends BaseSeleniumTestClass {

    // basic sanity test for log viewer
    @Test
    public void testLogViewer() {
        gotoDasPage();
        clickAndWait("treeForm:tree:applicationServer:applicationServer_link");
        String winHandleBefore = driver.getWindowHandle();
        clickByIdAction("propertyForm:propertyContentPage:logViewer");
        for(String winHandle : driver.getWindowHandles()){
            driver.switchTo().window(winHandle);
        }
        
        assertTrue(driver.findElement(By.className("TtlTxt_sun4")).getText().equals("Log Viewer"));
        driver.close();
        
        driver.switchTo().window(winHandleBefore);
    }

    // basic sanity test for raw log viewer
    @Test
    public void testRawLogViewer() {
        gotoDasPage();
        clickAndWait("treeForm:tree:applicationServer:applicationServer_link");
        String winHandleBefore = driver.getWindowHandle();
        clickByIdAction("propertyForm:propertyContentPage:logViewerRaw");
        for(String winHandle : driver.getWindowHandles()){
            driver.switchTo().window(winHandle);
        }
        
        assertTrue(driver.findElement(By.className("TtlTxt_sun4")).getText().equals("Raw Log Viewer"));
        driver.close();
        
        driver.switchTo().window(winHandleBefore);

    }
}

