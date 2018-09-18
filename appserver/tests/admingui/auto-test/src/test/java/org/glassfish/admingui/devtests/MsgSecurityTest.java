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
/**
 * 
 * @author Jeremy Lv
 *
 */
public class MsgSecurityTest extends BaseSeleniumTestClass {

    @Test
    public void testCreateMsgSecurityConfigWithNoDefaultProvider() {
        gotoDasPage();
        final String providerName = "provider" + generateRandomString();
        final String configName= "Config-" + generateRandomString();
        final String propertyName= "property-" + generateRandomString();
        final String layer = "HttpServlet";

        copyConfig("default-config", configName);
        gotoDasPage();
        createMsgSecurityConfig(configName, layer, providerName, "client", false ,propertyName);

        String prefix = getTableRowByValue("propertyForm:configs", layer, "col1");
        assertEquals(layer, getText(prefix + "col1:authlayer"));
        //since we didn't mark this as default provider, ensure it is not listed in the table.
        assertEquals("", getText(prefix + "col2"));
        assertEquals(providerName, getText(prefix + "col3"));
        clickAndWait(prefix + "col1:authlayer" );

        //clean up by removing the config.
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:configurations_link");
        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", configName);
    }

    @Test
    public void testCreateMsgSecurityConfigWithDefaultProvider() {
        gotoDasPage();
        final String providerName = "provider" + generateRandomString();
        final String configName= "Config-" + generateRandomString();
        final String propertyName= "property-" + generateRandomString();
        final String layer = "HttpServlet";

        copyConfig("default-config", configName);
        gotoDasPage();
        createMsgSecurityConfig(configName, layer, providerName, "server", true, propertyName);

        String prefix = getTableRowByValue("propertyForm:configs", layer, "col1");
        assertEquals(layer, getText(prefix + "col1:authlayer"));
        //since we didn't mark this as default provider, ensure it is not listed in the table.
        assertEquals(providerName, getText(prefix + "col2:defaultprov"));
        assertEquals("", getText(prefix + "col3"));
        clickAndWait(prefix + "col1:authlayer" );
        assertEquals(configName, getText("propertyForm:propertySheet:configNameSheet:configName:configName"));

        //clean up by removing the config.
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:configurations_link");
        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", configName);
    }

    @Test
    public void testCreateMsgSecurityConfigWithDefaultClientProvider() {
        gotoDasPage();
        final String providerName = "provider" + generateRandomString();
        final String configName= "Config-" + generateRandomString();
        final String propertyName= "property-" + generateRandomString();
        final String layer = "HttpServlet";

        copyConfig("default-config", configName);
        gotoDasPage();
        createMsgSecurityConfig(configName, layer, providerName, "client", true, propertyName);

        String prefix = getTableRowByValue("propertyForm:configs", layer, "col1");
        assertEquals(layer, getText(prefix + "col1:authlayer"));
        //since we didn't mark this as default provider, ensure it is not listed in the table.
        assertEquals("", getText(prefix + "col2"));
        assertEquals(providerName, getText(prefix + "col3:defaultclientprov"));
        clickAndWait(prefix + "col1:authlayer" );
        assertEquals(configName, getText("propertyForm:propertySheet:configNameSheet:configName:configName"));

        //clean up by removing the config.
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:configurations_link");
        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", configName);
    }

    @Test
    public void testCreateMsgSecurityConfigWithDefaultClientServerProvider() {
        gotoDasPage();
        final String providerName = "provider" + generateRandomString();
        final String configName= "Config-" + generateRandomString();
        final String propertyName= "property-" + generateRandomString();
        final String layer = "HttpServlet";

        copyConfig("default-config", configName);
        gotoDasPage();
        createMsgSecurityConfig(configName, layer, providerName, "client-server", true, propertyName);

        String prefix = getTableRowByValue("propertyForm:configs", layer, "col1");
        assertEquals(layer, getText(prefix + "col1:authlayer"));
        //since we didn't mark this as default provider, ensure it is not listed in the table.
        assertEquals(providerName, getText(prefix + "col2:defaultprov"));
        assertEquals(providerName, getText(prefix + "col3:defaultclientprov"));
        clickAndWait(prefix + "col1:authlayer" );
        assertEquals(configName, getText("propertyForm:propertySheet:configNameSheet:configName:configName"));
        //clean up by removing the config.
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:configurations_link");
        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", configName);
    }
    

    @Test
    public void testCreateAdditionalProviders() {
        gotoDasPage();
        final String providerName = "provider" + generateRandomString();
        final String providerName2 = "provider" + generateRandomString();
        final String configName= "Config-" + generateRandomString();
        final String propertyName= "property-" + generateRandomString();
        final String layer = "HttpServlet";

        copyConfig("default-config", configName);
        gotoDasPage();
        createMsgSecurityConfig(configName, layer, providerName, "client", true, propertyName);


        String prefix = getTableRowByValue("propertyForm:configs", layer, "col1");
        assertEquals(layer, getText(prefix + "col1:authlayer"));
        //since we didn't mark this as default provider, ensure it is not listed in the table.
        assertEquals("", getText(prefix + "col2"));
        assertEquals(providerName, getText(prefix + "col3:defaultclientprov"));
        clickAndWait("propertyForm:configs:rowGroup1:0:col1:authlayer" );
        assertEquals(configName, getText("propertyForm:propertySheet:configNameSheet:configName:configName"));

        clickAndWait("propertyForm:msgSecurityTabs:providers" );
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton");

        setFieldValue("propertyForm:propertySheet:providerConfSection:ProviderIdTextProp:ProviderIdText", providerName2);
        if (!driver.findElement(By.id("propertyForm:propertySheet:providerConfSection:DefaultProviderProp:def")).isSelected()){
            clickByIdAction("propertyForm:propertySheet:providerConfSection:DefaultProviderProp:def");
        }
        Select select = new Select(driver.findElement(By.id("propertyForm:propertySheet:providerConfSection:ProviderTypeProp:ProviderType")));
        select.selectByVisibleText("server");
        setFieldValue("propertyForm:propertySheet:providerConfSection:ClassNameProp:ClassName", "CLASSNAME");
        Select select1 = new Select(driver.findElement(By.id("propertyForm:propertySheet:requestPolicySection:AuthSourceProp:AuthSource")));
        select1.selectByVisibleText("sender");
        Select select2 = new Select(driver.findElement(By.id("propertyForm:propertySheet:requestPolicySection:AuthRecipientProp:AuthRecipient")));
        select2.selectByVisibleText("before-content");
        Select select3 = new Select(driver.findElement(By.id("propertyForm:propertySheet:responsePolicySection:AuthSourceProp:AuthSource")));
        select3.selectByVisibleText("content");
        addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", propertyName);
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
        sleep(500);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", "DESC");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton");

        String proPrefix = getTableRowByValue("propertyForm:configs", providerName2, "col1");
        assertEquals("server", getText(proPrefix + "col2:provType"));
        assertEquals("true", getText(proPrefix + "col3:default"));
        assertEquals("CLASSNAME", getText(proPrefix + "col4:defaultclientprov"));

        clickAndWait(proPrefix+"col1:authlayer" );
        assertEquals(configName, getText("propertyForm:propertySheet:configNameSheet:configName:configName"));

        Select select4 = new Select(driver.findElement(By.id("propertyForm:propertySheet:requestPolicySection:AuthSourceProp:AuthSource")));
        Select select5 = new Select(driver.findElement(By.id("propertyForm:propertySheet:requestPolicySection:AuthRecipientProp:AuthRecipient")));
        Select select6 = new Select(driver.findElement(By.id("propertyForm:propertySheet:responsePolicySection:AuthSourceProp:AuthSource")));
        Select select7 = new Select(driver.findElement(By.id("propertyForm:propertySheet:responsePolicySection:AuthRecipientProp:AuthRecipient")));
        assertEquals(select4.getFirstSelectedOption().getAttribute("value"), "sender");
        assertEquals(select5.getFirstSelectedOption().getAttribute("value"), "before-content");
        assertEquals(select6.getFirstSelectedOption().getAttribute("value"), "content");
        assertEquals(select7.getFirstSelectedOption().getAttribute("value"), "");
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton");

        //clean up by removing the config.
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:configurations_link");
        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", configName);
    }


    public void createMsgSecurityConfig(String configName, String layer, String providerName, String type, boolean isDefault, String propertyName){

        clickAndWait("treeForm:tree:configurations:" + configName + ":security:messageSecurity:messageSecurity_link");
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton");
        isElementPresent("propertyForm:propertySheet:propertySheetSection:AuthLayerProp:AuthLayer");
        Select select = new Select(driver.findElement(By.id("propertyForm:propertySheet:propertySheetSection:AuthLayerProp:AuthLayer")));
        select.selectByVisibleText(layer);
        setFieldValue("propertyForm:propertySheet:providerConfSection:ProviderIdTextProp:ProviderIdText", providerName);
        if (!driver.findElement(By.id("propertyForm:propertySheet:providerConfSection:DefaultProviderProp:def")).isSelected()){
            clickByIdAction("propertyForm:propertySheet:providerConfSection:DefaultProviderProp:def");
        }
        Select select1 = new Select(driver.findElement(By.id("propertyForm:propertySheet:providerConfSection:ProviderTypeProp:ProviderType")));
        select1.selectByVisibleText(type);
        setFieldValue("propertyForm:propertySheet:providerConfSection:ClassNameProp:ClassName", "CLASSNAME");
        addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", propertyName);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", "DESC");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton");
    }

    public void copyConfig(String srcName, String newConfigName) {
        clickAndWait("treeForm:tree:configurations:configurations_link");
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:NameProp:Name", newConfigName);
        Select select = new Select(driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:ConfigProp:Config")));
        select.selectByVisibleText(srcName);
        clickAndWait("propertyForm:propertyContentPage:topButtons:okButton");
        
        String prefix = getTableRowByValue("propertyForm:configs", newConfigName, "col1");
        assertEquals(newConfigName, getText(prefix + "col1:link"));
    }
}
