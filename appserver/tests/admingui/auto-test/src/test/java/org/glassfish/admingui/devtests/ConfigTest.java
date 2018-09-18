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
 * @author jeremylv
 *
 */
public class ConfigTest extends BaseSeleniumTestClass {

    public static final String ID_CLUSTERS_TABLE = "propertyForm:configs";
    
    @Test
    public void testCreateAndDeleteConfig() {
        final String configName= "test-config-"+generateRandomString();
        gotoDasPage();
        clickByIdAction("treeForm:tree:configurations:configurations_link");
        clickByIdAction("propertyForm:configs:topActionsGroup1:newButton");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:NameProp:Name", configName);
        clickByIdAction("propertyForm:propertyContentPage:topButtons:okButton");
        
        String prefix = getTableRowByValue(ID_CLUSTERS_TABLE, configName, "col1");
        try {
            assertEquals(configName, getText(prefix + "col1:link"));
        } catch (Error e) {
            verificationErrors.append(e.toString());
        };

        String clickId = getTableRowByValue(ID_CLUSTERS_TABLE, configName, "col1")+"col0:select";
        clickByIdAction(clickId);
        clickByIdAction("propertyForm:configs:topActionsGroup1:button1");
        closeAlertAndGetItsText();
        waitForAlertProcess("modalBody");
    }
}
