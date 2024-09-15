/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package com.sun.enterprise.glassfish.bootstrap.commandline;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ondro Mihalyi
 */
public class CommandLineParser {

    private static Logger logger = Logger.getLogger(CommandLineParser.class.getName());

    public Arguments parse(String[] commandLineArgs) {
        Arguments arguments = new Arguments();
        arguments.setDefaults();
        for (int i = 0; i < commandLineArgs.length; i++) {
            String arg = commandLineArgs[i];
            if (arg.startsWith("-")) {
                final int initialCharsToIgnore = arg.startsWith("--") ? 2 : 1;
                String[] keyValue = arg.substring(2).split("=", initialCharsToIgnore);
                try {
                    if (keyValue.length == 2) {
                        arguments.setOption(keyValue[0], keyValue[1]);
                    } else {
                        arguments.setOption(keyValue[0], null); // No value, it's a flag
                    }
                } catch (UnknownPropertyException e) {
                    logger.log(Level.WARNING, e, () -> "Unknown argument " + arg);
                }
            } else if (isdeployable(arg)) {
                arguments.deployables.add(arg);
            } else {
                arguments.commands.add(arg);
            }
        }
        return arguments;
    }

    private boolean isdeployable(String arg) {
        final File file = new File(arg);
        return file.exists();
    }

}
