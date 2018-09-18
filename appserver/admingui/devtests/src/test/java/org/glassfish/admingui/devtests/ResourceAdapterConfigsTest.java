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
import org.omg.CORBA.SetOverrideType;

import static org.junit.Assert.*;

public class ResourceAdapterConfigsTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_RESOURCE_ADAPTER_CONFIGS = "i18njca.resourceAdapterConfig.pageTitleHelp";
    private static final String TRIGGER_NEW_RESOURCE_ADAPTER = "i18njca.resourceAdapterConfig.newPageTitleHelp";
    public static final String TRIGGER_EDIT_RESOURCE_ADAPTER_CONFIG = "i18njca.resourceAdapterConfig.editPageTitleHelp";

    @Test
    public void testResourceAdapterConfigs() throws Exception {
        clickAndWait("treeForm:tree:resources:resourceAdapterConfigs:resourceAdapterConfigs_link", TRIGGER_RESOURCE_ADAPTER_CONFIGS);

        if (tableContainsRow("propertyForm:poolTable", "col1", "jmsra")) {
            deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", "jmsra");

        }

        // Create new RA config
        clickAndWait("propertyForm:poolTable:topActionsGroup1:newButton", TRIGGER_NEW_RESOURCE_ADAPTER);
        selectDropdownOption("propertyForm:propertySheet:propertSectionTextField:threadPoolsIdProp:threadpoolsid", "thread-pool-1");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_RESOURCE_ADAPTER_CONFIGS, TIMEOUT*10);

        // Verify config was saved and update values
        assertTrue(isTextPresent("jmsra"));
        clickAndWait(getLinkIdByLinkText("propertyForm:poolTable", "jmsra"), TRIGGER_EDIT_RESOURCE_ADAPTER_CONFIG);
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_RESOURCE_ADAPTER_CONFIGS);
        
        // Remove config
        deleteRow("propertyForm:poolTable:topActionsGroup1:button1", "propertyForm:poolTable", "jmsra");
    }
}
