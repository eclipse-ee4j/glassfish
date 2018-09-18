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
import static org.junit.Assert.assertTrue;

/**
 * Author: jeremy_lv
 * To change this template use File | Settings | File Templates.
 */
public class ConnectorServiceTest extends BaseSeleniumTestClass {

    @Test
    public void testConnectorService() {
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:connectorService:connectorService_link");

        String policy = "derived";
        if (getValue("propertyForm:propertySheet:propertSectionTextField:ClassLoadingPolicy:ClassLoadingPolicy", "value").equals(policy)) {
            policy = "global";
        }
        final String timeout = Integer.toString(generateRandomNumber(120));

        setFieldValue("propertyForm:propertySheet:propertSectionTextField:timeout:tiimeout", timeout);
        Select select = new Select(driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:ClassLoadingPolicy:ClassLoadingPolicy")));
        select.selectByVisibleText(policy);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
        
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:connectorService:connectorService_link");
        assertEquals(timeout, getValue("propertyForm:propertySheet:propertSectionTextField:timeout:tiimeout", "value"));
        assertEquals(policy, getValue("propertyForm:propertySheet:propertSectionTextField:ClassLoadingPolicy:ClassLoadingPolicy", "value"));
    }
}
