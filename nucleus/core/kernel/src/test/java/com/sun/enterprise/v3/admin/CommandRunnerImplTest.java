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

import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.common.util.admin.CommandModelImpl;
import org.glassfish.hk2.api.MultiException;
import org.junit.jupiter.api.Test;
import org.jvnet.hk2.annotations.Service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * junit test to test {@link CommandRunnerImpl} class
 */
public class CommandRunnerImplTest {

    @Test
    public void getUsageTextTest() {
        String expectedUsageText = "Usage: dummy-admin --foo=foo [--bar=false] --hello=there world ";
        CommandModel model = new CommandModelImpl(DummyAdminCommand.class);
        String actualUsageText = CommandRunnerImpl.getUsageText(model);
        assertEquals(expectedUsageText, actualUsageText);
    }

    @Test
    public void validateParametersTest() {
        ParameterMap params = new ParameterMap();
        params.set("foo", "bar");
        params.set("hello", "world");
        params.set("one", "two");
        MultiException ce = assertThrows(MultiException.class,
            () -> CommandRunnerImpl.validateParameters(new CommandModelImpl(DummyAdminCommand.class), params));
        assertThat(ce.getMessage(), stringContainsInOrder(" Invalid option: one"));
    }

    @Test
    public void skipValidationTest() {
        DummyAdminCommand dac = new DummyAdminCommand();
        assertFalse(CommandRunnerImpl.skipValidation(dac));
        SkipValidationCommand svc = new SkipValidationCommand();
        assertTrue(CommandRunnerImpl.skipValidation(svc));
    }

    /** Mock - does nothing */
    private static class SkipValidationCommand implements AdminCommand {

        /**
         *  This field is used via reflection!
         *  See {@link CommandRunnerImpl#skipValidation(AdminCommand)}
         */
        @SuppressWarnings("unused")
        boolean skipParamValidation = true;

        @Override
        public void execute(AdminCommandContext context) {
            // ignore everything
        }
    }


    @Service(name="dummy-admin")
    public static class DummyAdminCommand implements AdminCommand {
        @Param(optional=false)
        String foo;

        @Param(name="bar", defaultValue="false", optional=true)
        String foobar;

        @Param(optional=false, defaultValue="there")
        String hello;

        @Param(optional=false, primary=true)
        String world;

        @Override
        public void execute(AdminCommandContext context) {
            // ignore everything
        }
    }
}
