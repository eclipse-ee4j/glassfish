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

import java.util.List;
import java.util.Map;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * A local export command.
 *
 * @author Bill Shannon
 */
@Service(name = "export")
@PerLookup
public class ExportCommand extends CLICommand {

    private static final LocalStringsImpl strings = new LocalStringsImpl(ExportCommand.class);

    @Param(name = "environment-variable", primary = true, optional = true, multiple = true)
    private List<String> vars;

    @Override
    public int executeCommand() throws CommandException, CommandValidationException {
        int ret = 0; // by default, success

        // if no operands, print out everything
        if (vars == null || vars.size() == 0) {
            for (Map.Entry<String, String> e : env.entrySet())
                logger.info(e.getKey() + " = " + quote(e.getValue()));
        } else {
            // otherwise, process each operand
            for (String arg : vars) {
                // separate into name and value
                String name, value;
                int eq = arg.indexOf('=');
                if (eq < 0) { // no value
                    name = arg;
                    value = null;
                } else {
                    name = arg.substring(0, eq);
                    value = arg.substring(eq + 1);
                }

                // check that name is legitimate
                if (!name.startsWith(Environment.getPrefix())) {
                    logger.info(strings.get("badEnvVarSet", name, Environment.getPrefix()));
                    ret = -1;
                    continue;
                }

                // if no value, print it, otherwise set it
                if (value == null) {
                    String v = env.get(name);
                    if (v != null)
                        logger.info(name + " = " + v);
                } else
                    env.put(name, value);
            }
        }
        return ret;
    }
}
