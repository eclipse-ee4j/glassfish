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

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class WorkSecurityMapTest extends BaseSeleniumTestClass {

    private static final String TRIGGER_WORK_SECURITY_MAPS = "i18njca.workSecurityMaps.pageTitleHelp";
    private static final String TRIGGER_NEW_WORK_SECURITY_MAP = "i18njca.workSecurityMap.newPageTitleHelp";
    public static final String TRIGGER_EDIT_WORK_SECURITY_MAP = "i18njca.workSecurityMap.editPageTitleHelp";

    @Test
    public void testWorkSecurityMaps() throws Exception {
        final String testWorkSecurityMap = generateRandomString();
        final String testGroupMapKey = generateRandomString();
        final String testGroupMapValue = generateRandomString();

        clickAndWait("treeForm:tree:resources:Connectors:workSecurityMaps:workSecurityMaps_link", TRIGGER_WORK_SECURITY_MAPS);

        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_WORK_SECURITY_MAP);

        setFieldValue("propertyForm:propertySheet:propertSectionTextField:mapNameNew:mapName", testWorkSecurityMap);
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:groupProp:eisgrouptext", testGroupMapKey + "=" + testGroupMapValue);
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_WORK_SECURITY_MAPS);

        assertTrue(isTextPresent(testWorkSecurityMap));
        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", testWorkSecurityMap), TRIGGER_EDIT_WORK_SECURITY_MAP);
        Assert.assertEquals(testGroupMapKey + "=" + testGroupMapValue, getFieldValue("propertyForm:propertySheet:propertSectionTextField:groupProp:eisgrouptext"));
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_WORK_SECURITY_MAPS);

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", testWorkSecurityMap);
    }
}
