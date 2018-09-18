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

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 12, 2010
 * Time: 1:36:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionServiceTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_TRANSACTION_SERVICE = "i18n_jts.ts.PageHelp";

    @Test
    public void testTransactionService() {
        final String timeout = Integer.toString(generateRandomNumber(60));
        final String retry = Integer.toString(generateRandomNumber(600));
        final String keypoint = Integer.toString(generateRandomNumber(65535));

        clickAndWait("treeForm:tree:configurations:server-config:transactionService:transactionService_link", TRIGGER_TRANSACTION_SERVICE);
        markCheckbox("propertyForm:propertySheet:propertSectionTextField:onRestartProp:enabled");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:timeoutProp:Timeout", timeout);
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:retryProp:Retry", retry);
        selectDropdownOption("propertyForm:propertySheet:propertSectionTextField:heuristicProp:HeuristicDecision", "Commit");
        setFieldValue("propertyForm:propertySheet:propertSectionTextField:keyPointProp:Keypoint", keypoint);
        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");

        setFieldValue("propertyForm:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col3:col1St", "value");
        setFieldValue("propertyForm:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        reset();

        clickAndWait("treeForm:tree:configurations:server-config:transactionService:transactionService_link", TRIGGER_TRANSACTION_SERVICE);
        assertEquals(true, isChecked("propertyForm:propertySheet:propertSectionTextField:onRestartProp:enabled"));
        assertEquals(timeout, getFieldValue("propertyForm:propertySheet:propertSectionTextField:timeoutProp:Timeout"));
        assertEquals(retry, getFieldValue("propertyForm:propertySheet:propertSectionTextField:retryProp:Retry"));
        assertEquals("commit", getFieldValue("propertyForm:propertySheet:propertSectionTextField:heuristicProp:HeuristicDecision"));
        assertEquals(keypoint, getFieldValue("propertyForm:propertySheet:propertSectionTextField:keyPointProp:Keypoint"));
        assertTableRowCount("propertyForm:basicTable", count);
    }
}
