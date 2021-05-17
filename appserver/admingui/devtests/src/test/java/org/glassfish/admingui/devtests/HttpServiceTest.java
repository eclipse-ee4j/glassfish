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

public class HttpServiceTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_HTTP_SERVICE = "i18n_web.httpService.pageTitleHelp";

    @Test
    public void testHttpService() {
        final String interval = Integer.toString(generateRandomNumber(2880));
        final String maxFiles = Integer.toString(generateRandomNumber(50));
        final String bufferSize = Integer.toString(generateRandomNumber(65536));
        final String logWriteInterval = Integer.toString(generateRandomNumber(600));

        clickAndWait("treeForm:tree:configurations:server-config:httpService:httpService_link", TRIGGER_HTTP_SERVICE);
        markCheckbox("form1:propertySheet:http:acLog:ssoEnabled");
        markCheckbox("form1:propertySheet:accessLog:acLog:accessLoggingEnabled");
        setFieldValue("form1:propertySheet:accessLog:intervalProp:Interval", interval);
        setFieldValue("form1:propertySheet:accessLog:MaxHistoryFiles:MaxHistoryFiles", maxFiles);
        setFieldValue("form1:propertySheet:accessLog:accessLogBufferSize:accessLogBufferSize", bufferSize);
        setFieldValue("form1:propertySheet:accessLog:accessLogWriteInterval:accessLogWriteInterval", logWriteInterval);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");
        setFieldValue("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        setFieldValue("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        reset();
        clickAndWait("treeForm:tree:configurations:server-config:httpService:httpService_link", TRIGGER_HTTP_SERVICE);
        assertEquals(true, isChecked("form1:propertySheet:http:acLog:ssoEnabled"));
        assertEquals(interval, getFieldValue("form1:propertySheet:accessLog:intervalProp:Interval"));
        assertEquals(maxFiles, getFieldValue("form1:propertySheet:accessLog:MaxHistoryFiles:MaxHistoryFiles"));
        assertEquals(bufferSize, getFieldValue("form1:propertySheet:accessLog:accessLogBufferSize:accessLogBufferSize"));
        assertEquals(logWriteInterval, getFieldValue("form1:propertySheet:accessLog:accessLogWriteInterval:accessLogWriteInterval"));
        assertTableRowCount("form1:basicTable", count);
    }
}
