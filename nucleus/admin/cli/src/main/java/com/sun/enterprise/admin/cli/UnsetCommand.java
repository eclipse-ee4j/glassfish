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

package com.sun.enterprise.admin.cli;

import java.util.*;
import org.jvnet.hk2.annotations.*;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.hk2.api.PerLookup;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 * A local unset command to unset environment variables.
 *
 * @author Bill Shannon
 */
@Service(name = "unset")
@PerLookup
public class UnsetCommand extends CLICommand {

    @Param(name = "environment-variable", primary = true, multiple = true)
    private List<String> vars;

    private static final LocalStringsImpl strings = new LocalStringsImpl(UnsetCommand.class);

    @Override
    public int executeCommand() throws CommandException, CommandValidationException {
        int ret = 0; // by default, success

        // process each operand
        for (String name : vars) {
            // check that name is legitimate
            if (!name.startsWith(Environment.getPrefix())) {
                logger.info(strings.get("badEnvVarUnset", name, Environment.getPrefix()));
                ret = -1;
                continue;
            }

            if (env.get(name) == null) {
                logger.info(strings.get("cantRemoveEnvVar", name));
                ret = -1;
            } else
                env.remove(name);
        }
        return ret;
    }
}
