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

public class ThreadPoolsTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_THREAD_POOLS = "i18n_web.configuration.threadPoolPageTitleHelp";
    private static final String TRIGGER_EDIT_THREAD_POOL = "i18n_web.configuration.threadPoolEditPageTitle";
    private static final String TRIGGER_NEW_THREAD_POOL = "i18n_web.configuration.threadPoolNewPageTitle";

    @Test
    public void testAddThreadPool() {
        final String threadPoolName = "testThreadPool"+generateRandomString();

        clickAndWait("treeForm:tree:configurations:server-config:threadPools:threadPools_link", TRIGGER_THREAD_POOLS);
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton", TRIGGER_NEW_THREAD_POOL);
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:nameProp:nameText", threadPoolName);
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:max:max", "8192");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:maxThread:maxThread", "10");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:minThread:minThread", "4");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:timeout:timeout", "1800");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_THREAD_POOLS);
        
        assertTrue(isTextPresent(threadPoolName));
        clickAndWait(getLinkIdByLinkText("propertyForm:configs", threadPoolName), TRIGGER_EDIT_THREAD_POOL);
        assertEquals("8192", getFieldValue("propertyForm:propertySheet:propertSectionTextField:max:max"));
        assertEquals("10", getFieldValue("propertyForm:propertySheet:propertSectionTextField:maxThread:maxThread"));
        assertEquals("4", getFieldValue("propertyForm:propertySheet:propertSectionTextField:minThread:minThread"));
        assertEquals("1800", getFieldValue("propertyForm:propertySheet:propertSectionTextField:timeout:timeout"));
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_THREAD_POOLS);

        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", threadPoolName);
    }
}
