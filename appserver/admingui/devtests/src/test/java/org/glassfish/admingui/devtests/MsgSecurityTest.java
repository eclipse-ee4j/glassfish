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

import junit.framework.Assert;
import java.util.ArrayList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MsgSecurityTest extends BaseSeleniumTestClass {
    public static final String TRIGGER_MESSAGE_SECURITY_CONFIGURATIONS = "i18nc.msgSecurity.ListPageTitle";
    public static final String TRIGGER_NEW_MESSAGE_SECURITY_CONFIGURATION = "i18nc.msgSecurity.NewMsgSecurityInfo";
    public static final String TRIGGER_EDIT_MESSAGE_SECURITY_CONFIGURATION = "i18nc.msgSecurity.EditMsgSecurity";
    public static final String TRIGGER_EDIT_PROVIDER_CONFIGURATION = "i18nc.msgProvider.EditPageTitle";
    public static final String TRIGGER_PROVIDER_CONFIGURATION = "i18nc.msgSecProvider.TableTitle";
    public static final String TRIGGER_NEW_PROVIDER_CONFIGURATION = "i18nc.msgSecProvider.NewPageTitleHelp";
    public static final String ADMIN_PWD_DOMAIN_ATTRIBUTES = "i18nc.domain.DomainAttrsPageTitle";
    public static final String ADMIN_PWD_NEW_ADMINPWD = "i18nc.domain.AdminPasswordTitle";
    private static final String TRIGGER_CONFIGURATION = "i18nc.configurations.PageTitleHelp";
    private static final String TRIGGER_NEW_CONFIGURATION = "i18nc.configurations.NewPageTitle";
    private static final String ID_MSG_SECURITY_TABLE_NEW_BUTTON = "propertyForm:configs:topActionsGroup1:newButton";

    @Test
    public void testCreateMsgSecurityConfigWithNoDefaultProvider() {
        final String providerName = "provider" + generateRandomString();
        final String className = "com.example.Foo";
        final String configName= "Config-" + generateRandomString();
        final String propertyName= "property-" + generateRandomString();
        final String layer = "HttpServlet";

        copyConfig("default-config", configName);
        createMsgSecurityConfig(configName, layer, providerName, "client", false ,propertyName);

        String prefix = getTableRowByValue("propertyForm:configs", layer, "col1");
        assertEquals(layer, getText(prefix + "col1:authlayer"));
        //since we didn't mark this as default provider, ensure it is not listed in the table.
        assertEquals("", getText(prefix + "col2"));
        assertEquals("", getText(prefix + "col3"));
        clickAndWait(prefix + "col1:authlayer",TRIGGER_EDIT_MESSAGE_SECURITY_CONFIGURATION );

        //clean up by removing the config.
        clickAndWait("treeForm:tree:configurations:configurations_link", TRIGGER_CONFIGURATION);
        String cp = getTableRowByValue("propertyForm:configs", configName, "col1");
        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", configName);
    }

    @Test
    public void testCreateMsgSecurityConfigWithDefaultProvider() {
        final String providerName = "provider" + generateRandomString();
        final String className = "com.example.Foo";
        final String configName= "Config-" + generateRandomString();
        final String propertyName= "property-" + generateRandomString();
        final String layer = "HttpServlet";

        copyConfig("default-config", configName);
        createMsgSecurityConfig(configName, layer, providerName, "server", true, propertyName);

        String prefix = getTableRowByValue("propertyForm:configs", layer, "col1");
        assertEquals(layer, getText(prefix + "col1:authlayer"));
        //since we didn't mark this as default provider, ensure it is not listed in the table.
        assertEquals(providerName, getText(prefix + "col2:defaultprov"));
        assertEquals("", getText(prefix + "col3"));
        clickAndWait(prefix + "col1:authlayer",TRIGGER_EDIT_MESSAGE_SECURITY_CONFIGURATION );
        assertEquals(configName, getText("propertyForm:propertySheet:configNameSheet:configName:configName"));

        //clean up by removing the config.
        clickAndWait("treeForm:tree:configurations:configurations_link", TRIGGER_CONFIGURATION);
        String cp = getTableRowByValue("propertyForm:configs", configName, "col1");
        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", configName);
    }

    @Test
    public void testCreateMsgSecurityConfigWithDefaultClientProvider() {
        final String providerName = "provider" + generateRandomString();
        final String className = "com.example.Foo";
        final String configName= "Config-" + generateRandomString();
        final String propertyName= "property-" + generateRandomString();
        final String layer = "HttpServlet";

        copyConfig("default-config", configName);
        createMsgSecurityConfig(configName, layer, providerName, "client", true, propertyName);

        String prefix = getTableRowByValue("propertyForm:configs", layer, "col1");
        assertEquals(layer, getText(prefix + "col1:authlayer"));
        //since we didn't mark this as default provider, ensure it is not listed in the table.
        assertEquals("", getText(prefix + "col2"));
        assertEquals(providerName, getText(prefix + "col3:defaultclientprov"));
        clickAndWait(prefix + "col1:authlayer",TRIGGER_EDIT_MESSAGE_SECURITY_CONFIGURATION );
        assertEquals(configName, getText("propertyForm:propertySheet:configNameSheet:configName:configName"));

        //clean up by removing the config.
        clickAndWait("treeForm:tree:configurations:configurations_link", TRIGGER_CONFIGURATION);
        String cp = getTableRowByValue("propertyForm:configs", configName, "col1");
        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", configName);
    }

    @Test
    public void testCreateMsgSecurityConfigWithDefaultClientServerProvider() {
        final String providerName = "provider" + generateRandomString();
        final String className = "com.example.Foo";
        final String configName= "Config-" + generateRandomString();
        final String propertyName= "property-" + generateRandomString();
        final String layer = "HttpServlet";

        copyConfig("default-config", configName);
        createMsgSecurityConfig(configName, layer, providerName, "client-server", true, propertyName);

        String prefix = getTableRowByValue("propertyForm:configs", layer, "col1");
        assertEquals(layer, getText(prefix + "col1:authlayer"));
        //since we didn't mark this as default provider, ensure it is not listed in the table.
        assertEquals(providerName, getText(prefix + "col2:defaultprov"));
        assertEquals(providerName, getText(prefix + "col3:defaultclientprov"));
        clickAndWait(prefix + "col1:authlayer",TRIGGER_EDIT_MESSAGE_SECURITY_CONFIGURATION );
        assertEquals(configName, getText("propertyForm:propertySheet:configNameSheet:configName:configName"));
        //clean up by removing the config.
        clickAndWait("treeForm:tree:configurations:configurations_link", TRIGGER_CONFIGURATION);
        String cp = getTableRowByValue("propertyForm:configs", configName, "col1");
        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", configName);
    }
    

    @Test
    public void testCreateAdditionalProviders() {
        final String providerName = "provider" + generateRandomString();
        final String providerName2 = "provider" + generateRandomString();
        final String className = "com.example.Foo";
        final String configName= "Config-" + generateRandomString();
        final String propertyName= "property-" + generateRandomString();
        final String layer = "HttpServlet";

        copyConfig("default-config", configName);
        createMsgSecurityConfig(configName, layer, providerName, "client", true, propertyName);


        String prefix = getTableRowByValue("propertyForm:configs", layer, "col1");
        assertEquals(layer, getText(prefix + "col1:authlayer"));
        //since we didn't mark this as default provider, ensure it is not listed in the table.
        assertEquals("", getText(prefix + "col2"));
        assertEquals(providerName, getText(prefix + "col3:defaultclientprov"));
        clickAndWait("propertyForm:configs:rowGroup1:0:col1:authlayer",TRIGGER_EDIT_MESSAGE_SECURITY_CONFIGURATION );
        assertEquals(configName, getText("propertyForm:propertySheet:configNameSheet:configName:configName"));

        clickAndWait("propertyForm:msgSecurityTabs:providers",TRIGGER_PROVIDER_CONFIGURATION );
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton", TRIGGER_NEW_PROVIDER_CONFIGURATION);

        setFieldValue("propertyForm:propertySheet:providerConfSection:ProviderIdTextProp:ProviderIdText", providerName2);
        markCheckbox("propertyForm:propertySheet:providerConfSection:DefaultProviderProp:def");
	selectDropdownOption("propertyForm:propertySheet:providerConfSection:ProviderTypeProp:ProviderType", "server");
        setFieldValue("propertyForm:propertySheet:providerConfSection:ClassNameProp:ClassName", "CLASSNAME");
        selectDropdownOption("propertyForm:propertySheet:requestPolicySection:AuthSourceProp:AuthSource", "sender");
	selectDropdownOption("propertyForm:propertySheet:requestPolicySection:AuthRecipientProp:AuthRecipient", "before-content");
	selectDropdownOption("propertyForm:propertySheet:responsePolicySection:AuthSourceProp:AuthSource", "content");
        addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", propertyName);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", "DESC");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_PROVIDER_CONFIGURATION);

        String proPrefix = getTableRowByValue("propertyForm:configs", providerName2, "col1");
        assertEquals("server", getText(proPrefix + "col2:provType"));
        assertEquals("true", getText(proPrefix + "col3:default"));
        assertEquals("CLASSNAME", getText(proPrefix + "col4:defaultclientprov"));

        clickAndWait(proPrefix+"col1:authlayer",TRIGGER_EDIT_PROVIDER_CONFIGURATION );
        assertEquals(configName, getText("propertyForm:propertySheet:configNameSheet:configName:configName"));

        assertEquals(getSelectedValue("propertyForm:propertySheet:requestPolicySection:AuthSourceProp:AuthSource"), "sender");
        assertEquals(getSelectedValue("propertyForm:propertySheet:requestPolicySection:AuthRecipientProp:AuthRecipient"), "before-content");
	assertEquals(getSelectedValue("propertyForm:propertySheet:responsePolicySection:AuthSourceProp:AuthSource"), "content");
        assertEquals(getSelectedValue("propertyForm:propertySheet:responsePolicySection:AuthRecipientProp:AuthRecipient"), "");
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton",TRIGGER_PROVIDER_CONFIGURATION);

        rowActionWithConfirm("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", providerName2);
        Assert.assertFalse(isTextPresent(providerName2));
        clickAndWait("treeForm:tree:configurations:configurations_link", TRIGGER_CONFIGURATION);
        String cp = getTableRowByValue("propertyForm:configs", configName, "col1");
        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", configName);
    }





    public void createMsgSecurityConfig(String configName, String layer, String providerName, String type, boolean isDefault, String propertyName){

        String TRIGGER_MESSAGE_SECURITY_CONFIGURATIONS_LINK = "treeForm:tree:configurations:" + configName + ":security:messageSecurity:messageSecurity_link";
        clickAndWaitForElement("Masthead:homeLink", TRIGGER_MESSAGE_SECURITY_CONFIGURATIONS_LINK);
        clickAndWait(TRIGGER_MESSAGE_SECURITY_CONFIGURATIONS_LINK, TRIGGER_MESSAGE_SECURITY_CONFIGURATIONS);
        clickAndWait(ID_MSG_SECURITY_TABLE_NEW_BUTTON,  TRIGGER_NEW_MESSAGE_SECURITY_CONFIGURATION);
        selectDropdownOption("propertyForm:propertySheet:propertySheetSection:AuthLayerProp:AuthLayer", layer);
        setFieldValue("propertyForm:propertySheet:providerConfSection:ProviderIdTextProp:ProviderIdText", providerName);
        if (isDefault){
            markCheckbox("propertyForm:propertySheet:providerConfSection:DefaultProviderProp:def");
        }
	selectDropdownOption("propertyForm:propertySheet:providerConfSection:ProviderTypeProp:ProviderType", type);
	setFieldValue("propertyForm:propertySheet:providerConfSection:ClassNameProp:ClassName", "CLASSNAME");
        addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", propertyName);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
	setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", "DESC");
	clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_MESSAGE_SECURITY_CONFIGURATIONS);
    }
    


    public void copyConfig(String srcName, String newConfigName) {
        clickAndWait("treeForm:tree:configurations:configurations_link", TRIGGER_CONFIGURATION);
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton", TRIGGER_NEW_CONFIGURATION);
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:NameProp:Name", newConfigName);
        selectDropdownOption("propertyForm:propertySheet:propertSectionTextField:ConfigProp:Config", srcName);
        clickAndWait("propertyForm:propertyContentPage:topButtons:okButton", TRIGGER_CONFIGURATION);
        assertTrue(tableContainsRow("propertyForm:configs", "col1", newConfigName));
    }
}
