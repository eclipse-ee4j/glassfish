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
package org.glassfish.runnablejar.commandline;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ondro Mihalyi
 */
public class CommandLineParser {

    private static final Logger logger = Logger.getLogger(CommandLineParser.class.getName());

    public Arguments parse(String[] commandLineArgs) {
        Arguments arguments = new Arguments();
        arguments.setDefaults();
        for (int i = 0; i < commandLineArgs.length; i++) {
            String arg = commandLineArgs[i];
            if (arg.startsWith("-")) {
                int initialCharsToIgnore = arg.startsWith("--") ? 2 : 1;
                String optionPart = arg.substring(initialCharsToIgnore);

                String key;
                String value;

                // Use split with a limit of 2 to handle '=' in the value
                String[] keyValue = optionPart.split("=", 2);
                key = keyValue[0];

                try {
                    boolean nextArgIsValue = false;
                    if (keyValue.length == 2) {
                        // Case 1: Handles --key=value or -k=value
                        value = keyValue[1];
                    } else {
                        // Case 2: Handles --key value (space) OR --key (flag)

                        // Check if a "value" argument exists next
                        boolean hasNextArg = i + 1 < commandLineArgs.length;
                        // Check if the next arg is NOT an option itself
                        nextArgIsValue = hasNextArg && !commandLineArgs[i + 1].startsWith("-");

                        if (nextArgIsValue) {
                            // This is the --key value case
                            value = commandLineArgs[i + 1];
                            i++;
                        } else {
                            // This is a flag like --verbose
                            value = "true";
                        }
                    }

                    Option option = arguments.setOption(key, value);
                    if (nextArgIsValue && option != null && !option.handlesValue(value)) {
                        i--;
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
