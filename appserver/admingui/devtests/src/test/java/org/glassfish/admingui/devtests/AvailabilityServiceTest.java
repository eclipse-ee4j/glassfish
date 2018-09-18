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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AvailabilityServiceTest extends BaseSeleniumTestClass {
    public static final String ID_AVAILABILITY_SERVICE_TREE_NODE = "treeForm:tree:configurations:default-config:availabilityService:availabilityService_link";
    private static final String ID_DEFAULT_CONFIG_TURNER = "treeForm:tree:configurations:default-config:default-config_turner:default-config_turner_image";
    private static final String TRIGGER_AVAILABILTY_SERVICE_NODE = "i18ncs.tree.availsvc";
    private static final String TRIGGER_AVAILABILTY_SERVICE_PAGE = "i18ncs.availabilty.TitlePageHelp";
    private static final String TRIGGER_WEB_AVAILABILTY = "i18n_web.availability.webContainerAvailabilityInfo";
    private static final String TRIGGER_EJB_AVAILABILTY = "i18n_ejb.availability.ejbContainerAvailabilityInfo";
    private static final String TRIGGER_JMS_AVAILABILTY = "i18njms.availability.jmsAvailabilityInfo";
//    private static final String TRIGGER_SUCCESS_MSG = "New values successfully saved";

    @Test
    public void testAvailabilityService() {
        // Expand node
        if (!isTextPresent(TRIGGER_AVAILABILTY_SERVICE_NODE)) {
            clickAndWait(ID_DEFAULT_CONFIG_TURNER, TRIGGER_AVAILABILTY_SERVICE_NODE);
        }
        clickAndWait(ID_AVAILABILITY_SERVICE_TREE_NODE, TRIGGER_AVAILABILTY_SERVICE_PAGE);

        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", generateRandomString());
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", generateRandomString());
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", generateRandomString());
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        assertTableRowCount("propertyForm:basicTable", count);
        deleteAllTableRows("propertyForm:basicTable", 1);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
    }

    @Test
    public void testWebContainerAvailability() {
        if (!isTextPresent(TRIGGER_AVAILABILTY_SERVICE_NODE)) {
            clickAndWait(ID_DEFAULT_CONFIG_TURNER, TRIGGER_AVAILABILTY_SERVICE_NODE);
        }
        clickAndWait(ID_AVAILABILITY_SERVICE_TREE_NODE, TRIGGER_AVAILABILTY_SERVICE_PAGE);
        clickAndWait("propertyForm:availabilityTabs:webAvailabilityTab", TRIGGER_WEB_AVAILABILTY);

        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", generateRandomString());
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", generateRandomString());
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", generateRandomString());
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        assertTableRowCount("propertyForm:basicTable", count);
        deleteAllTableRows("propertyForm:basicTable", 1);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
    }

    @Test
    public void testEjbContainerAvailability() {
        if (!isTextPresent(TRIGGER_AVAILABILTY_SERVICE_NODE)) {
            clickAndWait(ID_DEFAULT_CONFIG_TURNER, TRIGGER_AVAILABILTY_SERVICE_NODE);
        }
        clickAndWait(ID_AVAILABILITY_SERVICE_TREE_NODE, TRIGGER_AVAILABILTY_SERVICE_PAGE);
        clickAndWait("propertyForm:availabilityTabs:ejbAvailabilityTab", TRIGGER_EJB_AVAILABILTY);

        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", generateRandomString());
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", generateRandomString());
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", generateRandomString());
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        assertTableRowCount("propertyForm:basicTable", count);
        deleteAllTableRows("propertyForm:basicTable", 1);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
    }

    @Test
    public void testJMSAvailability() {
        final String clusterName = "cluster" + generateRandomString();
        final String CLUSTER_TYPE = "enhanced";
        final String DB_VENDOR = "mysql";
        final String DB_USER = generateRandomString();
        final String DB_URL = "jdbc:mysql://hostname:portno/dbname?password=" + generateRandomString();
        final String DB_PASSWORD = generateRandomString();

        ClusterTest ct = new ClusterTest();
        ct.createCluster(clusterName);

        try {
            String TRIGGER_AVAILABILTY_SERVICE_PAGE_LINK = "treeForm:tree:configurations:" + clusterName + "-config:availabilityService:availabilityService_link";
            clickAndWaitForElement("Masthead:homeLink", TRIGGER_AVAILABILTY_SERVICE_PAGE_LINK);
            clickAndWait(TRIGGER_AVAILABILTY_SERVICE_PAGE_LINK, TRIGGER_AVAILABILTY_SERVICE_PAGE);
            clickAndWait("propertyForm:availabilityTabs:jmsAvailabilityTab", TRIGGER_JMS_AVAILABILTY);

            markCheckbox("propertyForm:propertySheet:propertSectionTextField:AvailabilityEnabledProp:avail");
//            selectDropdownOption("propertyForm:propertySheet:propertSectionTextField:ClusterTypeProp:clusterType", CLUSTER_TYPE);
            
            setFieldValue("propertyForm:propertySheet:propertSectionTextField:DbVendorProp:DbVendor", DB_VENDOR);
            setFieldValue("propertyForm:propertySheet:propertSectionTextField:DbUserNameProp:DbUserName", DB_USER);
            setFieldValue("propertyForm:propertySheet:propertSectionTextField:DbUrlProp:DbUrl", DB_URL);
            setFieldValue("propertyForm:propertySheet:propertSectionTextField:DbPasswordProp:DbPassword", DB_PASSWORD);

            clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);

        clickAndWait("propertyForm:availabilityTabs:availabilityTab", TRIGGER_AVAILABILTY_SERVICE_PAGE);
        clickAndWait("propertyForm:availabilityTabs:jmsAvailabilityTab", TRIGGER_JMS_AVAILABILTY);

            assertTrue(selenium.isChecked("propertyForm:propertySheet:propertSectionTextField:AvailabilityEnabledProp:avail"));
            assertEquals(DB_VENDOR, getFieldValue("propertyForm:propertySheet:propertSectionTextField:DbVendorProp:DbVendor"));
            assertEquals(DB_USER, getFieldValue("propertyForm:propertySheet:propertSectionTextField:DbUserNameProp:DbUserName"));
            assertEquals(DB_PASSWORD, getFieldValue("propertyForm:propertySheet:propertSectionTextField:DbPasswordProp:DbPassword"));
            assertEquals(DB_URL, getFieldValue("propertyForm:propertySheet:propertSectionTextField:DbUrlProp:DbUrl"));

            int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
            setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", generateRandomString());
            setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", generateRandomString());
            setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", generateRandomString());
            clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
            assertTableRowCount("propertyForm:basicTable", count);
        } finally {
            ct.deleteAllClusters();
        }
    }
}
