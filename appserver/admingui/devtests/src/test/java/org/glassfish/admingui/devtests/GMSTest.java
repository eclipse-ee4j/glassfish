/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

public class GMSTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_GMS = "i18ncs.gms.TitlePageHelp";
    private static final String TRIGGER_CONFIGURATION = "i18ncs.common.ConfigurationCol";

    @Test
    public void testConfig() {
        final String protocolMaxTrial = Integer.toString(generateRandomNumber(100));
        clickAndWait("treeForm:tree:configurations:default-config:default-config_turner:default-config_turner_image", TRIGGER_CONFIGURATION);
        clickAndWait("treeForm:tree:configurations:default-config:gms:gms_link", TRIGGER_GMS);
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:fdMax:fdMax", protocolMaxTrial);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        assertEquals(protocolMaxTrial, getFieldValue("propertyForm:propertySheet:propertSectionTextField:fdMax:fdMax"));
        
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", "a");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "b");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", "c");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        assertTableRowCount("propertyForm:basicTable", count);
        deleteAllTableRows("propertyForm:basicTable", 1);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
    }
}
