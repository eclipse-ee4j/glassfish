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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class JavaMailTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_JAVA_MAIL = "i18njmail.javaMailSessions.pageTitleHelp";
    private static final String TRIGGER_NEW_JAVAMAIL_SESSION = "i18njmail.javaMail.newPageTitleHelp";
    public static final String TRIGGER_EDIT_JAVAMAIL_SESSION = "i18njmail.javaMail.editPageTitleHelp";

    @Test
    public void createMailResource() {
        final String resourceName = "javaMail" + generateRandomString();
        final String description = resourceName + " description";

        StandaloneTest standaloneTest = new StandaloneTest();
        ClusterTest clusterTest = new ClusterTest();
        standaloneTest.deleteAllStandaloneInstances();
        clusterTest.deleteAllClusters();
        
        clickAndWait("treeForm:tree:resources:mailResources:mailResources_link", TRIGGER_JAVA_MAIL);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_JAVAMAIL_SESSION);

        setFieldValue("form:propertySheet:propertSectionTextField:nameNew:name", resourceName);
        setFieldValue("form:propertySheet:propertSectionTextField:hostProp:host", "localhost");
        setFieldValue("form:propertySheet:propertSectionTextField:userProp:user", "user");
        setFieldValue("form:propertySheet:propertSectionTextField:fromProp:from", "return@test.com");
        setFieldValue("form:propertySheet:propertSectionTextField:descProp:desc", description);
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("form:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        setFieldValue("form:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("form:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_JAVA_MAIL);

        assertTrue(isTextPresent(resourceName));

        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", resourceName), TRIGGER_EDIT_JAVAMAIL_SESSION);
        assertTableRowCount("propertyForm:basicTable", count);
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_JAVA_MAIL);

        testDisableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:enabled",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_JAVA_MAIL,
                TRIGGER_EDIT_JAVAMAIL_SESSION,
                "off");
        testEnableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:enabled",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_JAVA_MAIL,
                TRIGGER_EDIT_JAVAMAIL_SESSION,
                "on");

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
    }

    @Test
    public void createMailResourceWithTargets() {
        final String resourceName = "javaMail" + generateRandomString();
        final String description = resourceName + " description";
        final String instanceName = "standalone" + generateRandomString();
        final String enableStatus = "Enabled on 2 of 2 Target(s)";
        final String disableStatus = "Enabled on 0 of 2 Target(s)";

        StandaloneTest instanceTest = new StandaloneTest();
        instanceTest.createStandAloneInstance(instanceName);

        clickAndWait("treeForm:tree:resources:mailResources:mailResources_link", TRIGGER_JAVA_MAIL);
        clickAndWait("propertyForm:resourcesTable:topActionsGroup1:newButton", TRIGGER_NEW_JAVAMAIL_SESSION);

        setFieldValue("form:propertySheet:propertSectionTextField:nameNew:name", resourceName);
        setFieldValue("form:propertySheet:propertSectionTextField:hostProp:host", "localhost");
        setFieldValue("form:propertySheet:propertSectionTextField:userProp:user", "user");
        setFieldValue("form:propertySheet:propertSectionTextField:fromProp:from", "return@test.com");
        setFieldValue("form:propertySheet:propertSectionTextField:descProp:desc", description);
        int count = addTableRow("form:basicTable", "form:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("form:basicTable:rowGroup1:0:col2:col1St", "property" + generateRandomString());
        setFieldValue("form:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("form:basicTable:rowGroup1:0:col4:col1St", "description");

        addSelectSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available", instanceName);
        addSelectSelection("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove_available", "server");
        pressButton("form:targetSection:targetSectionId:addRemoveProp:commonAddRemove:commonAddRemove_addButton");

        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_JAVA_MAIL);

        assertTrue(isTextPresent(resourceName));

        clickAndWait(getLinkIdByLinkText("propertyForm:resourcesTable", resourceName), TRIGGER_EDIT_JAVAMAIL_SESSION);
        assertTableRowCount("propertyForm:basicTable", count);
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_JAVA_MAIL);

        testDisableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button3",
                "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_JAVA_MAIL,
                TRIGGER_EDIT_JAVAMAIL_SESSION,
                disableStatus);
        testEnableButton(resourceName,
                "propertyForm:resourcesTable",
                "propertyForm:resourcesTable:topActionsGroup1:button2",
                "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_JAVA_MAIL,
                TRIGGER_EDIT_JAVAMAIL_SESSION,
                enableStatus);
        testManageTargets("treeForm:tree:resources:mailResources:mailResources_link",
                          "propertyForm:resourcesTable",
                          "propertyForm:targetTable:topActionsGroup1:button2",
                          "propertyForm:targetTable:topActionsGroup1:button3",
                          "propertyForm:propertySheet:propertSectionTextField:statusProp2:enabledStr",
                          "propertyForm:resEditTabs:general",
                          "propertyForm:resEditTabs:targetTab",
                          TRIGGER_JAVA_MAIL,
                          TRIGGER_EDIT_JAVAMAIL_SESSION,
                          resourceName,
                          instanceName);

        deleteRow("propertyForm:resourcesTable:topActionsGroup1:button1", "propertyForm:resourcesTable", resourceName);
        //Delete the instance
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link", instanceTest.TRIGGER_INSTANCES_PAGE);
        deleteRow("propertyForm:instancesTable:topActionsGroup1:button1", "propertyForm:instancesTable", instanceName);
        assertFalse(tableContainsRow("propertyForm:instancesTable", "col0", instanceName));
    }
}
