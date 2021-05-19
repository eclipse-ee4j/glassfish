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
import static org.junit.Assert.*;

public class JvmSettingsTest extends BaseSeleniumTestClass {
    public static final String TRIGGER_JVM_GENERAL_SETTINGS = "i18nc.jvm.GeneralTitle";
    public static final String TRIGGER_JVM_PATH_SETTINGS = "i18nc.jvm.PathSettingsTitle";
    public static final String TRIGGER_JVM_OPTIONS = "Manage JVM options for the server.";
    public static final String TRIGGER_JVM_PROFILER_SETTINGS = "i18nc.jvm.ProfilerPageName";
    public static final String TRIGGER_JVM_PROFILER_CREATED = "i18nc.jvm.ProfilerCreated";
    public static final String TRIGGER_JVM_PROFILER_DELETED = "i18nc.jvm.ProfilerDeleted";
    public static final String TRIGGER_CONFIG_PAGE = "i18nc.configurations.PageTitleHelp";
    public static final String JVM_LINK_TEXT = "JVM Settings";

    public static final String ID_JVM_OPTIONS_TABLE = "propertyForm:basicTable";
    @Test
    public void testJvmGeneralSettings() {
        clickAndWait("treeForm:tree:configurations:server-config:jvmSettings:jvmSettings_link", TRIGGER_JVM_GENERAL_SETTINGS);
        markCheckbox("propertyForm:propertySheet:propertSectionTextField:debugEnabledProp:debug");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        waitForPageLoad("Restart Required", 1000);
        reset();
        clickAndWait("treeForm:tree:configurations:server-config:jvmSettings:jvmSettings_link", TRIGGER_JVM_GENERAL_SETTINGS);
        markCheckbox("propertyForm:propertySheet:propertSectionTextField:debugEnabledProp:debug");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
    }

    @Test
    public void testJvmSettings() {
        String jvmOptionName = "-Dfoo"+generateRandomString();
        clickAndWait("treeForm:tree:configurations:server-config:jvmSettings:jvmSettings_link", TRIGGER_JVM_GENERAL_SETTINGS);
        clickAndWait("propertyForm:javaConfigTab:jvmOptions", TRIGGER_JVM_OPTIONS);

        int count = addTableRow(ID_JVM_OPTIONS_TABLE, "propertyForm:basicTable:topActionsGroup1:addSharedTableButton", "Options");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", jvmOptionName);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        clickAndWait("propertyForm:javaConfigTab:pathSettings", TRIGGER_JVM_PATH_SETTINGS);
        clickAndWait("propertyForm:javaConfigTab:jvmOptions", TRIGGER_JVM_OPTIONS);
        assertTableRowCount(ID_JVM_OPTIONS_TABLE, count);


        /* I want to clean up, as well as to test the delete button. But i can't get the row to be selected.
         * commented it out for now, since it will always fails.
         *
        selectTableRowByValue(ID_JVM_OPTIONS_TABLE, jvmOptionName, "col1", "col3");
        pressButton("propertyForm:basicTable:topActionsGroup1:button1");
        waitForButtonDisabled("propertyForm:basicTable:topActionsGroup1:button1");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        assertTableRowCount(ID_JVM_OPTIONS_TABLE, count-1);
         *
         */
    }



    @Test
    public void testJvmProfilerForDas() {
        clickAndWait("treeForm:tree:configurations:server-config:jvmSettings:jvmSettings_link", TRIGGER_JVM_GENERAL_SETTINGS);
        clickAndWait("propertyForm:javaConfigTab:profiler", TRIGGER_JVM_PROFILER_SETTINGS);

        if (isElementPresent("propertyForm:propertyContentPage:topButtons:deleteButton")) {
            this.clickAndWait("propertyForm:propertyContentPage:topButtons:deleteButton", TRIGGER_JVM_PROFILER_DELETED);
            if (isConfirmationPresent()) {
                getConfirmation();
            }
        }

        setFieldValue("propertyForm:propertySheet:propertSectionTextField:profilerNameProp:ProfilerName", "profiler" + generateRandomString());
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton", "Options");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "-Dfoo=" + generateRandomString());
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_JVM_PROFILER_CREATED);
        assertTableRowCount("propertyForm:basicTable", count);

        clickAndWait("propertyForm:javaConfigTab:pathSettings", TRIGGER_JVM_PATH_SETTINGS);
        clickAndWait("propertyForm:javaConfigTab:profiler", TRIGGER_JVM_PROFILER_SETTINGS);
        pressButton("propertyForm:propertyContentPage:topButtons:deleteButton");
        assertTrue(getConfirmation().matches("^Profiler will be deleted\\.  Continue[\\s\\S]$"));
    }



    @Test
    public void testJvmProfilerForRunningInstance() {
        testProfilerForInstance (true);
    }

    @Test
    public void testJvmProfilerForStoppedInstance() {
        testProfilerForInstance (false);
    }


    private void testProfilerForInstance(boolean start){
        String instanceName = generateRandomString();
        String configName = instanceName+"-config";
        StandaloneTest st = new StandaloneTest();
        st.createStandAloneInstance(instanceName);
        if (start){
            st.startInstance(instanceName);
        }

        clickAndWait(getLinkIdByLinkText(st.ID_INSTANCE_TABLE, configName), TRIGGER_CONFIG_PAGE );
        clickAndWait(getLinkIdByLinkText("", JVM_LINK_TEXT), TRIGGER_JVM_GENERAL_SETTINGS );
        clickAndWait("propertyForm:javaConfigTab:profiler", TRIGGER_JVM_PROFILER_SETTINGS );

        setFieldValue("propertyForm:propertySheet:propertSectionTextField:profilerNameProp:ProfilerName", "profiler" + generateRandomString());
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton", "Options");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "-Dfoo=" + generateRandomString());
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_JVM_PROFILER_CREATED);
        assertTableRowCount("propertyForm:basicTable", count);

        clickAndWait("propertyForm:javaConfigTab:pathSettings", TRIGGER_JVM_PATH_SETTINGS);
        clickAndWait("propertyForm:javaConfigTab:profiler", TRIGGER_JVM_PROFILER_SETTINGS);
        this.clickAndWait("propertyForm:propertyContentPage:topButtons:deleteButton", TRIGGER_JVM_PROFILER_DELETED);
        if (isConfirmationPresent()) {
            getConfirmation();
        }
        assertFalse(isElementPresent("propertyForm:propertyContentPage:topButtons:deleteButton"));
        assertTrue(isElementPresent("propertyForm:propertyContentPage:topButtons:newButton"));

        st.deleteStandAloneInstance(instanceName);
    }

    public void createStandAloneInstance(String instanceName){
        reset();
        clickAndWait("treeForm:tree:standaloneTreeNode:standaloneTreeNode_link", StandaloneTest.TRIGGER_INSTANCES_PAGE);
        clickAndWait(StandaloneTest.ID_INSTANCE_TABLE_NEW_BUTTON, StandaloneTest.TRIGGER_NEW_PAGE );
        setFieldValue(StandaloneTest.ID_INSTANCE_NAME_TEXT, instanceName);
        selectDropdownOption(StandaloneTest.ID_INSTANCE_NODE_TEXT, StandaloneTest.NODE_NAME);
        selectDropdownOption(StandaloneTest.ID_INSTANCE_CONFIG_SELECT, "default-config");
        markCheckbox(StandaloneTest.ID_INSTANCE_CONFIG_OPTION);
        clickAndWait(StandaloneTest.ID_INSTANCE_NEW_PAGE_BUTTON, StandaloneTest.TRIGGER_INSTANCES_PAGE);
    }

}
