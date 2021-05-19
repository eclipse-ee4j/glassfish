/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.Assert.*;

import org.glassfish.common.util.admin.CommandModelImpl;
import org.glassfish.hk2.api.MultiException;
import org.junit.Test;
import org.junit.Before;
import org.glassfish.api.Param;
import org.glassfish.api.admin.ParameterMap;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandModel;
import org.jvnet.hk2.annotations.Service;

/**
 * junit test to test CommandRunner class
 */
public class CommandRunnerTest {
    private CommandRunnerImpl cr = null;

    @Test
    public void getUsageTextTest() {
        String expectedUsageText = "Usage: dummy-admin --foo=foo [--bar=false] --hello=there world ";
        DummyAdminCommand dac = new DummyAdminCommand();
        CommandModel model = new CommandModelImpl(DummyAdminCommand.class);
        String actualUsageText = cr.getUsageText(model);
        assertEquals(expectedUsageText, actualUsageText);
    }

    @Test
    public void validateParametersTest() {
        ParameterMap params = new ParameterMap();
        params.set("foo", "bar");
        params.set("hello", "world");
        params.set("one", "two");
        try {
            cr.validateParameters(new CommandModelImpl(DummyAdminCommand.class), params);
        }
        catch (MultiException ce) {
            String expectedMessage = " Invalid option: one";
            assertTrue(ce.getMessage().contains(expectedMessage));
        }
    }

    @Test
    public void skipValidationTest() {
        DummyAdminCommand dac = new DummyAdminCommand();
        assertFalse(cr.skipValidation(dac));
        SkipValidationCommand svc = new SkipValidationCommand();
        assertTrue(cr.skipValidation(svc));
    }

    @Before
    public void setup() {
        cr = new CommandRunnerImpl();
    }

        //mock-up DummyAdminCommand object
    @Service(name="dummy-admin")
    public class DummyAdminCommand implements AdminCommand {
        @Param(optional=false)
        String foo;

        @Param(name="bar", defaultValue="false", optional=true)
        String foobar;

        @Param(optional=false, defaultValue="there")
        String hello;

        @Param(optional=false, primary=true)
        String world;

        @Override
        public void execute(AdminCommandContext context) {}
    }

        //mock-up SkipValidationCommand
    public class SkipValidationCommand implements AdminCommand {
        boolean skipParamValidation=true;
        @Override
        public void execute(AdminCommandContext context) {}
    }


}
