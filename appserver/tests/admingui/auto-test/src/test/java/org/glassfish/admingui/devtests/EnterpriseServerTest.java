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

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
/**
 * 
 * @author Jeremy Lv
 *
 */
public class EnterpriseServerTest extends BaseSeleniumTestClass {
    public static final String TRIGGER_ADVANCED_APPLICATIONS_CONFIGURATION = "i18nc.domain.AppsConfigPageHelp";
    public static final String TRIGGER_GENERAL_INFORMATION = "i18n.instance.GeneralTitle";
    public static final String TRIGGER_ADVANCED_DOMAIN_ATTRIBUTES = "i18nc.domain.DomainAttrsPageTitleHelp";
    public static final String TRIGGER_SYSTEM_PROPERTIES = "i18n.common.AdditionalProperties"; // There is no page help on sysprops pages anymore, it seems
    public static final String TRIGGER_RESOURCES = "i18nc.resourcesTarget.pageTitleHelp";


    @Test
    public void testAdvancedDomainAttributes() {
        gotoDasPage();
        clickByIdAction("treeForm:tree:nodes:nodes_link");
        clearByIdAction("propertyForm:propertySheet:propertSectionTextField:localeProp:Locale");
        sendKeysByIdAction("propertyForm:propertySheet:propertSectionTextField:localeProp:Locale", "en");
        clickByIdAction("propertyForm:propertyContentPage:topButtons:saveButton");
        assertEquals("New values successfully saved.", driver.findElement(By.cssSelector("span.label_sun4")).getText());
        clickByIdAction("treeForm:tree:nodes:nodes_link");
        try {
            assertEquals("en", getValue("propertyForm:propertySheet:propertSectionTextField:localeProp:Locale", "value"));
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:localeProp:Locale", "");
        clickByIdAction("propertyForm:propertyContentPage:topButtons:saveButton");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
    }

    @Test
    public void testSystemProperties() {
        final String property = generateRandomString();
        final String value = property + "value";

        gotoDasPage();
        clickAndWait("propertyForm:serverInstTabs:serverInstProps");

        int count = addTableRow("propertyForm:sysPropsTable", "propertyForm:sysPropsTable:topActionsGroup1:addSharedTableButton");
        sleep(500);
        setFieldValue("propertyForm:sysPropsTable:rowGroup1:0:col2:col1St", property);
        sleep(500);
        setFieldValue("propertyForm:sysPropsTable:rowGroup1:0:overrideValCol:overrideVal", value);
        clickAndWait("propertyForm:SysPropsPage:topButtons:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));

        gotoDasPage();
        clickAndWait("propertyForm:serverInstTabs:serverInstProps");
        assertTableRowCount("propertyForm:sysPropsTable", count);
        
        //delete the property used to test
        gotoDasPage();
        clickAndWait("propertyForm:serverInstTabs:serverInstProps");
        String prefix = getTableRowByVal("propertyForm:sysPropsTable", property, "col2:col1St");
        String selectId = prefix + "col1:select";
        clickByIdAction(selectId);
        clickByIdAction("propertyForm:sysPropsTable:topActionsGroup1:button1");
        waitforBtnDisable("propertyForm:sysPropsTable:topActionsGroup1:button1");
        clickAndWait("propertyForm:SysPropsPage:topButtons:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
    }

    @Test
    public void testServerResourcesPage() {
        final String jndiName = "jdbcResource"+generateRandomString();
        final String description = "devtest test for server->resources page- " + jndiName;
        final String tableID = "propertyForm:resourcesTable";

        gotoDasPage();
        JdbcTest jdbcTest = new JdbcTest();
        jdbcTest.createJDBCResource(jndiName, description, "server", "server");
        
        gotoDasPage();
        clickAndWait("propertyForm:serverInstTabs:resources");
        String prefix = getTableRowByValue(tableID, jndiName, "col1");
        assertTrue(isTextPresent(prefix, jndiName, tableID));

        int jdbcCount = getTableRowCountByValue(tableID, "JDBC Resources", "col3:type");
        int customCount = getTableRowCountByValue(tableID, "Custom Resources", "col3:type");

        Select select = new Select(driver.findElement(By.id("propertyForm:resourcesTable:topActionsGroup1:filter_list")));
        select.selectByVisibleText("Custom Resources");
        waitForTableRowCount(tableID, customCount);

        select = new Select(driver.findElement(By.id("propertyForm:resourcesTable:topActionsGroup1:filter_list")));
        select.selectByVisibleText("JDBC Resources");
        waitForTableRowCount(tableID, jdbcCount);

        String clickId = getTableRowByValue(tableID, jndiName, "col1") + "col1:link";
        clickByIdAction(clickId);
        waitForButtonEnabled("propertyForm:propertyContentPage:topButtons:saveButton");
        clickByIdAction("propertyForm:propertyContentPage:topButtons:saveButton");

        jdbcTest.deleteJDBCResource(jndiName, "server", "server");
    }

    public void waitForTableRowCount(String tableID, int count) {
        for (int i = 0;; i++) {
            if (i >= 1000) {
                Assert.fail("timeout");
            }
            try {
                int tableCount = getTableRowCount(tableID);
                if (tableCount == count) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            sleep(500);
        }
    }

    public void gotoDasPage() {
        driver.get(baseUrl + "/common/index.jsf");
        clickByIdAction("treeForm:tree:applicationServer:applicationServer_link");
    }
}
