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
import org.openqa.selenium.NoSuchElementException;

import static org.junit.Assert.*;
/**
 * 
 * @author Jeremy Lv
 *
 */
public class JvmSettingsTest extends BaseSeleniumTestClass {
    
    public static final String ID_JVM_OPTIONS_TABLE = "propertyForm:basicTable";
    
    @Test
    public void testJvmGeneralSettings() {
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:jvmSettings:jvmSettings_link");
        if (!driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:debugEnabledProp:debug")).isSelected())
            clickByIdAction("propertyForm:propertySheet:propertSectionTextField:debugEnabledProp:debug");
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", "a");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "b");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", "c");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
        
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:jvmSettings:jvmSettings_link");
        assertTableRowCount("propertyForm:basicTable", count);
        assertTrue(driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:debugEnabledProp:debug")).isSelected());
        
        //delete the property used to test
        clickByIdAction("propertyForm:basicTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image");
        clickByIdAction("propertyForm:basicTable:topActionsGroup1:button1");
        waitforBtnDisable("propertyForm:basicTable:topActionsGroup1:button1");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
    }

    @Test
    public void testJvmSettings() {
        gotoDasPage();
        String jvmOptionName = "-Dfoo"+generateRandomString();
        clickAndWait("treeForm:tree:configurations:server-config:jvmSettings:jvmSettings_link");
        clickAndWait("propertyForm:javaConfigTab:jvmOptions");

        sleep(5000);
        int count = addTableRow(ID_JVM_OPTIONS_TABLE, "propertyForm:basicTable:topActionsGroup1:addSharedTableButton", "Options");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", jvmOptionName);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
        
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:jvmSettings:jvmSettings_link");
        clickAndWait("propertyForm:javaConfigTab:jvmOptions");
        sleep(5000);
        assertTableRowCount(ID_JVM_OPTIONS_TABLE, count);
        
        //delete the property used to test
        String prefix = getTableRowByVal("propertyForm:basicTable", jvmOptionName, "col3:col1St");
        String selectId = prefix + "col1:select";
        clickByIdAction(selectId);
        clickByIdAction("propertyForm:basicTable:topActionsGroup1:button1");
        waitforBtnDisable("propertyForm:basicTable:topActionsGroup1:button1");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
    }

    @Test
    public void testJvmProfilerForDas() {
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:jvmSettings:jvmSettings_link");
        clickAndWait("propertyForm:javaConfigTab:profiler");
        try {
            if (driver.findElement(By.id(("propertyForm:propertyContentPage:topButtons:deleteButton"))).isEnabled()) {
                clickAndWait("propertyForm:propertyContentPage:topButtons:deleteButton");
                if (driver.findElement(By.className("label_sun4")).isDisplayed()) {
                    assertEquals("Profiler successfully deleted.", driver.findElement(By.className("label_sun4")).getText());;
                }
            }
        } catch(NoSuchElementException e){
            setFieldValue("propertyForm:propertySheet:propertSectionTextField:profilerNameProp:ProfilerName", "profiler" + generateRandomString());
            int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton", "Options");
            sleep(500);
            setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "-Dfoo=" + generateRandomString());
            clickAndWait("propertyForm:propertyContentPage:topButtons:newButton");
            assertTableRowCount("propertyForm:basicTable", count);

            gotoDasPage();
            clickAndWait("treeForm:tree:configurations:server-config:jvmSettings:jvmSettings_link");
            clickAndWait("propertyForm:javaConfigTab:profiler");
            clickByIdAction("propertyForm:propertyContentPage:topButtons:deleteButton");
            assertTrue(closeAlertAndGetItsText().matches("^Profiler will be deleted\\.  Continue[\\s\\S]$"));
        }
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

        isElementPresent("treeForm:tree:configurations:"+ configName +":"+ configName +"_turner:"+ configName +"_turner_image");
        clickByIdAction("treeForm:tree:configurations:"+ configName +":"+ configName +"_turner:"+ configName +"_turner_image");
        isElementPresent("treeForm:tree:configurations:"+ configName +":jvmSettings:jvmSettings_link");
        clickAndWait("treeForm:tree:configurations:"+ configName +":jvmSettings:jvmSettings_link");
        clickAndWait("propertyForm:javaConfigTab:profiler" );

        setFieldValue("propertyForm:propertySheet:propertSectionTextField:profilerNameProp:ProfilerName", "profiler" + generateRandomString());
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton", "Options");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "-Dfoo=" + generateRandomString());
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton");
        assertTableRowCount("propertyForm:basicTable", count);

        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:"+ configName +":jvmSettings:jvmSettings_link");
        clickAndWait("propertyForm:javaConfigTab:profiler");
        clickAndWait("propertyForm:propertyContentPage:topButtons:deleteButton");
        assertTrue(closeAlertAndGetItsText().matches("^Profiler will be deleted\\.  Continue[\\s\\S]$"));
        assertTrue(driver.findElement(By.id("propertyForm:propertyContentPage:topButtons:newButton")).isDisplayed());
        
        if (start){
            st.gotoStandaloneInstancesPage();
            st.stopInstance(instanceName);
        }
        st.deleteStandAloneInstance(instanceName);
    }

}
