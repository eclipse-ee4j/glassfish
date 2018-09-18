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

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 23, 2010
 * Time: 4:31:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class LifecycleModulesTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_LIFECYCLE_MODULES = "i18nc.lifecycleModules.titleHelp";
    private static final String TRIGGER_EDIT_LIFECYCLE_MODULE = "i18nc.lifecycleModule.editPageTitle";
    private static final String TRIGGER_NEW_LIFECYCLE_MODULE = "i18nc.lifecycleModule.newPageTitle";

    @Test
    public void testLifecycleModules() {
        final String lifecycleName = "TestLifecycle"+generateRandomString();
        final String lifecycleClassname = "org.foo.nonexistent.Lifecyclemodule";

        StandaloneTest standaloneTest = new StandaloneTest();
        ClusterTest clusterTest = new ClusterTest();
        standaloneTest.deleteAllStandaloneInstances();
        clusterTest.deleteAllClusters();

        clickAndWait("treeForm:tree:lifecycles:lifecycles_link", TRIGGER_LIFECYCLE_MODULES);
        clickAndWait("propertyForm:deployTable:topActionsGroup1:newButton", TRIGGER_NEW_LIFECYCLE_MODULE);
        setFieldValue("form:propertySheet:propertSectionTextField:IdTextProp:IdText", lifecycleName);
        setFieldValue("form:propertySheet:propertSectionTextField:classNameProp:classname", lifecycleClassname);

        /*
        final String property = "property";
        final String value = "value";
        final String description = "description";
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", property);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", value);
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", description);
        */
        clickAndWaitForElement("form:propertyContentPage:topButtons:newButton", "propertyForm:deployTable");
        assertTrue(isTextPresent(lifecycleName));

        testDisableButton(lifecycleName, "propertyForm:deployTable", "propertyForm:deployTable:topActionsGroup1:button3",
                "propertyForm:propertySheet:propertSectionTextField:statusEdit:status",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_LIFECYCLE_MODULES,
                TRIGGER_EDIT_LIFECYCLE_MODULE,
                "off");

        testEnableButton(lifecycleName, "propertyForm:deployTable", "propertyForm:deployTable:topActionsGroup1:button2",
                "propertyForm:propertySheet:propertSectionTextField:statusEdit:status",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_LIFECYCLE_MODULES,
                TRIGGER_EDIT_LIFECYCLE_MODULE,
                "on");

        deleteRow("propertyForm:deployTable:topActionsGroup1:button1", "propertyForm:deployTable", lifecycleName);
    }
}
