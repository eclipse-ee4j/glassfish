/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.admin;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Supposed to have JDBC connection pool and resource tests.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 Prelude
 */
@Test(groups = {"adminconsole"}, description = "Admin Console tests")
public class AdminConsoleTests extends BaseAdminConsoleTest {

    /**
     * Request /commonTask.jsf and verify that the common task page was rendered.
     * @throws java.lang.Exception
     */
    @Test
    public void testCommonTasks() throws Exception {
        Assert.assertTrue(getUrlAndTestForStrings(this.adminUrl + "commonTask.jsf?bare=true",
                "id=\"form:commonTasksSection\""),
                "The Common Task page does not appear to have been rendered.");
    }

    /**
     * Request /applications/applications.jsf and verify that the applications page was rendered.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeployedAppPage() throws Exception {
        Assert.assertTrue(getUrlAndTestForStrings(this.adminUrl + "applications/applications.jsf?bare=true",
                "id=\"propertyForm:deployTable\""),
                "The Deployed Applications table does not appear to have been rendered.");
    }

    /**
     * Request /common/security/realms/realms.jsf to test that pages from plugin module can be rendered.
     * @throws java.lang.Exception
     */
    @Test
    public void testRealmsList() throws Exception {
        Assert.assertTrue(getUrlAndTestForStrings(this.adminUrl + "common/security/realms/realms.jsf?bare=true",
                "id=\"propertyForm:realmsTable\""),
                "The Security realms table does not appear to have been rendered.");
    }

    /*
     * Disabling for now, we have a new help system in place -- the old help system has been removed.
    @Test
    public void testHelpPage() throws Exception {
        Assert.assertTrue(getUrlAndTestForStrings(this.adminUrl + "com_sun_webui_jsf/help/helpwindow.jsf?&windowTitle=Help+Window&helpFile=CONTEXT_HELP.html",
                "id=\"navFrame\"", "id=\"buttonNavFrame\"", "id=\"contentFrame\""));
        Assert.assertTrue(getUrlAndTestForStrings(this.adminUrl + "com_sun_webui_jsf/help/navigator.jsf",
                "id=\"helpNavigatorForm:javaHelpTabSet\""));
        Assert.assertTrue(getUrlAndTestForStrings(this.adminUrl + "com_sun_webui_jsf/help/buttonnav.jsf",
                "input id=\"helpButtonNavForm_hidden\""));
        Assert.assertTrue(getUrlAndTestForStrings(this.adminUrl + "html/en/help/CONTEXT_HELP.html",
                "body class=\"HlpBdy\""));
    }
    */
}
