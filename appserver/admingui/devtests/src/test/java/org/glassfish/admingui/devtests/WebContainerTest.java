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
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 10, 2010
 * Time: 3:48:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class WebContainerTest extends BaseSeleniumTestClass {
    private static final String TAB_GENERAL_PROPERTIES = "i18n_web.headings.webGeneral";

    private static final String TAB_SESSION_PROPERTIES = "i18n_web.webSession.sessionTimeoutHelp";

    private static final String TAB_MANAGER_PROPERTIES = "i18n_web.webManager.reapIntervalHelp";

    private static final String TAB_STORE_PROPERTIES = "i18n_web.webStore.directoryHelp";

    @Test
    public void testGeneralTab() {
        final String property = "property" + generateRandomString();
        final String value = generateRandomString();
        final String description = "Description for " + property;

        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link", TAB_GENERAL_PROPERTIES);

        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", property);
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", value);
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", description);
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);

        clickAndWait("form1:webContainerTabs:sessionTab", TAB_SESSION_PROPERTIES);
        clickAndWait("form1:webContainerTabs:general", TAB_GENERAL_PROPERTIES);

        assertTableRowCount("form1:basicTable", count);
    }

    @Test
    public void testSessionProperties() {
        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link", TAB_GENERAL_PROPERTIES);
        clickAndWait("form1:webContainerTabs:sessionTab", TAB_SESSION_PROPERTIES);

        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", "description");

        setFieldValue("form1:sessionPropSheet:sessionPropSheetSection:SessionTimeoutProp:SessionTimeout", "300");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);

        clickAndWait("form1:webContainerTabs:general", TAB_GENERAL_PROPERTIES);

        clickAndWait("form1:webContainerTabs:sessionTab", TAB_SESSION_PROPERTIES);

        assertTableRowCount("form1:basicTable", count);
        assertEquals("300", getFieldValue("form1:sessionPropSheet:sessionPropSheetSection:SessionTimeoutProp:SessionTimeout"));

        setFieldValue("form1:sessionPropSheet:sessionPropSheetSection:SessionTimeoutProp:SessionTimeout", "300");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
    }

    @Test
    public void testManagerProperties() {
        final String reapInterval = Integer.toString(generateRandomNumber(100));
        final String maxSessions = Integer.toString(generateRandomNumber(1000));
        final String sessFileName = generateRandomString();

        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link", TAB_GENERAL_PROPERTIES);

        clickAndWait("form1:webContainerTabs:managerTab", TAB_MANAGER_PROPERTIES);

        setFieldValue("form1:managerPropSheet:managerPropSheetSection:ReapIntervalProp:ReapInterval", reapInterval);
        setFieldValue("form1:managerPropSheet:managerPropSheetSection:MaxSessionsProp:MaxSessions", maxSessions);
        setFieldValue("form1:managerPropSheet:managerPropSheetSection:SessFileNameProp:SessFileName", sessFileName);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);

        clickAndWait("form1:webContainerTabs:sessionTab", TAB_SESSION_PROPERTIES);
        clickAndWait("form1:webContainerTabs:managerTab", TAB_MANAGER_PROPERTIES);

        assertEquals(reapInterval, getFieldValue("form1:managerPropSheet:managerPropSheetSection:ReapIntervalProp:ReapInterval"));
        assertEquals(maxSessions, getFieldValue("form1:managerPropSheet:managerPropSheetSection:MaxSessionsProp:MaxSessions"));
        assertEquals(sessFileName, getFieldValue("form1:managerPropSheet:managerPropSheetSection:SessFileNameProp:SessFileName"));
        assertTableRowCount("form1:basicTable", count);
    }

    @Test
    public void testStoreProperties() {
        final String directory = generateRandomString();

        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link", TAB_GENERAL_PROPERTIES);

        clickAndWait("form1:webContainerTabs:storeTab", TAB_STORE_PROPERTIES);

        setFieldValue("form1:storePropSheet:storePropSheetSection:DirectoryProp:Directory", directory);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);

        clickAndWait("form1:webContainerTabs:managerTab", TAB_MANAGER_PROPERTIES);
        clickAndWait("form1:webContainerTabs:storeTab", TAB_STORE_PROPERTIES);
        assertEquals(directory, getFieldValue("form1:storePropSheet:storePropSheetSection:DirectoryProp:Directory"));
        assertTableRowCount("form1:basicTable", count);

        setFieldValue("form1:storePropSheet:storePropSheetSection:DirectoryProp:Directory", "");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
    }

    /*  To test the property table.
     *  If the property value/name is empty,  that property will not be created.
     *  If the property value is (), property will be persisted with the empty value. Need to check this. Not creating as of now. Hence, modified the test.
     */
    @Test
    public void testWebContainerPropertyTable() {

        final String property1 = "property1" + generateRandomString();
        final String value1 = "()";
        final String description1 = "Description for " + property1;
        final String property2 = "property2" + generateRandomString();
        final String value2 = "";
        final String description2 = "Description for " + property2;

        clickAndWait("treeForm:tree:configurations:server-config:webContainer:webContainer_link", TAB_GENERAL_PROPERTIES);

        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", property1);
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", value1);
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", description1);

        count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", property2);
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", value2);
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", description2);
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);

        assertTableRowCount("form1:basicTable", count-2);
    }
}
