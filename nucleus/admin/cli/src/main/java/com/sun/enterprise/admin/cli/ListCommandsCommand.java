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

import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.admin.cli.CLIConstants.EOL;

/**
 * A local list-commands command.
 *
 * @author bnevins
 * @author Bill Shannon
 */
@Service(name = "list-commands")
@PerLookup
public class ListCommandsCommand extends CLICommand {
    @Inject
    private CLIContainer container;

    private String[] remoteCommands;
    private String[] localCommands;
    private List<Pattern> patterns = new ArrayList<Pattern>();

    @Param(name = "localonly", optional = true)
    private boolean localOnly;

    @Param(name = "remoteonly", optional = true)
    private boolean remoteOnly;

    @Param(name = "command-pattern", primary = true, optional = true, multiple = true)
    private List<String> cmds;

    private static final String SPACES = "                                                            ";

    private static final LocalStringsImpl strings = new LocalStringsImpl(ListCommandsCommand.class);

    @Override
    protected void validate() throws CommandException, CommandValidationException {
        if (localOnly && remoteOnly) {
            throw new CommandException(strings.get("listCommands.notBoth"));
        }
    }

    @Override
    public int executeCommand() throws CommandException, CommandValidationException {

        // convert the patterns to regular expressions
        if (cmds != null)
            for (String pat : cmds)
                patterns.add(Pattern.compile(globToRegex(pat)));

        /*
         * If we need the remote commands, get them first so that
         * we prompt for any passwords before printing anything.
         */
        if (!localOnly) {
            try {
                remoteCommands = matchCommands(CLIUtil.getRemoteCommands(container, programOpts, env));
            } catch (CommandException ce) {
                /*
                 * Hide the real cause of the remote failure (almost certainly
                 * a ConnectException) so that asadmin doesn't try to find the
                 * closest matching local command (it's "list-commands", duh!).
                 */
                throw new CommandException(ce.getMessage());
            }
        }

        if (!remoteOnly) {
            localCommands = matchCommands(CLIUtil.getLocalCommands(container));
            printLocalCommands();
        }
        if (!localOnly && !remoteOnly)
            logger.info(""); // a blank line between them
        if (!localOnly)
            printRemoteCommands();
        logger.info("");
        return 0;
    }

    /**
     * Filter the command list to only those matching the patterns.
     */
    private String[] matchCommands(String[] commands) {
        // filter the commands
        List<String> matched = new ArrayList<String>();
        for (String cmd : commands) {
            if (patterns.size() == 0) {
                if (!cmd.startsWith("_"))
                    matched.add(cmd);
            } else {
                for (Pattern re : patterns)
                    if (re.matcher(cmd).find())
                        if (!cmd.startsWith("_") || re.pattern().startsWith("_"))
                            matched.add(cmd);
            }
        }

        return matched.toArray(new String[matched.size()]);
    }

    /**
     * Convert a shell style glob regular expression to a Java regular expression. Code from:
     * http://stackoverflow.com/questions/1247772
     */
    private String globToRegex(String line) {
        line = line.trim();
        int strLen = line.length();
        StringBuilder sb = new StringBuilder(strLen);
        // Remove beginning and ending * globs because they're useless
        if (line.startsWith("*")) {
            line = line.substring(1);
            strLen--;
        }
        if (line.endsWith("*")) {
            line = line.substring(0, strLen - 1);
            //strLen--;
        }
        boolean escaping = false;
        int inCurlies = 0;
        for (char currentChar : line.toCharArray()) {
            switch (currentChar) {
            case '*':
                if (escaping)
                    sb.append("\\*");
                else
                    sb.append(".*");
                escaping = false;
                break;
            case '?':
                if (escaping)
                    sb.append("\\?");
                else
                    sb.append('.');
                escaping = false;
                break;
            case '.':
            case '(':
            case ')':
            case '+':
            case '|':
            case '^':
            case '$':
            case '@':
            case '%':
                sb.append('\\');
                sb.append(currentChar);
                escaping = false;
                break;
            case '\\':
                if (escaping) {
                    sb.append("\\\\");
                    escaping = false;
                } else
                    escaping = true;
                break;
            case '{':
                if (escaping) {
                    sb.append("\\{");
                } else {
                    sb.append('(');
                    inCurlies++;
                }
                escaping = false;
                break;
            case '}':
                if (inCurlies > 0 && !escaping) {
                    sb.append(')');
                    inCurlies--;
                } else if (escaping)
                    sb.append("\\}");
                else
                    sb.append("}");
                escaping = false;
                break;
            case ',':
                if (inCurlies > 0 && !escaping)
                    sb.append('|');
                else if (escaping)
                    sb.append("\\,");
                else
                    sb.append(",");
                break;
            default:
                escaping = false;
                sb.append(currentChar);
            }
        }
        return sb.toString();
    }

    void printLocalCommands() {
        if (localCommands.length == 0) {
            logger.info(strings.get("listCommands.localCommandNoMatch"));
            return;
        }
        logger.info(strings.get("listCommands.localCommandHeader"));

        for (String s : localCommands) {
            logger.info(s);
        }
    }

    void printRemoteCommands() {
        if (remoteCommands.length == 0) {
            logger.info(strings.get("listCommands.remoteCommandNoMatch"));
            return;
        }

        logger.info(strings.get("listCommands.remoteCommandHeader"));

        // there are a LOT of remote commands -- make 2 columns
        int num = remoteCommands.length;
        int offset = (num / 2) + (num % 2);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < offset; i++) {
            sb.append(remoteCommands[i]);
            sb.append(justify(remoteCommands[i], 40));
            if (i + offset < num) {
                sb.append(remoteCommands[i + offset]);
            }
            if (i < offset - 1)
                sb.append(EOL);
        }
        logger.info(sb.toString());
    }

    private String justify(String s, int width) {
        int numSpaces = width - s.length();

        if (numSpaces > 0)
            return SPACES.substring(0, numSpaces);
        else
            // the command-name is HUGE.  The formatting will be funky now but
            // truncating it is a bad idea.  Just add a one-space separator.
            return " ";
    }
}
