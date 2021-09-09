/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.v3.admin;

import java.util.Properties;

import org.glassfish.api.admin.ParameterMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * junit test to test AdminAdapter class
 */
public class AdminAdapterTest {
    private AdminAdapter adapter;


    @BeforeEach
    public void setup() {
        adapter = new PublicAdminAdapter();
    }


    @Test
    public void extractParametersTest() {
        ParameterMap props = adapter.extractParameters(
            "uniquetablenames=false&createtables=true&target=server&libraries=foo.jar&dbvendorname=test&deploymentplan=test");
        Properties correctProps = new Properties();
        correctProps.put("uniquetablenames", "false");
        correctProps.put("createtables", "true");
        correctProps.put("target", "server");
        correctProps.put("libraries", "foo.jar");
        correctProps.put("dbvendorname", "test");
        correctProps.put("deploymentplan", "test");
        for (String prop : correctProps.stringPropertyNames()) {
            assertEquals(correctProps.getProperty(prop), props.getOne(prop), "compare Properties");
        }
    }
}
