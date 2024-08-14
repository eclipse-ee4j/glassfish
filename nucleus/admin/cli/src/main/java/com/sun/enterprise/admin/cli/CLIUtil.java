/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.admin.cli.remote.RemoteCLICommand;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.api.admin.InvalidCommandException;

/**
 * CLI Utility class
 */
public class CLIUtil {

    private static final int MAX_COMMANDS_TO_DISPLAY = 75;

    private static final LocalStringsImpl strings = new LocalStringsImpl(CLIUtil.class);

    /**
     * Read passwords from the password file and save them in a java.util.Map.
     *
     * @param passwordFileName password file name
     * @param withPrefix decides whether prefix should be taken into account
     * @return Map of the password name and value
     */
    public static Map<String, String> readPasswordFileOptions(final String passwordFileName, boolean withPrefix) throws CommandException {

        Map<String, String> passwordOptions = new HashMap<String, String>();
        boolean readStdin = passwordFileName.equals("-");
        InputStream is = null;
        try {
            is = new BufferedInputStream(readStdin ? System.in : new FileInputStream(passwordFileName));
            final Properties prop = new Properties();
            prop.load(is);
            for (Object key : prop.keySet()) {
                final String entry = (String) key;
                if (entry.startsWith(Environment.getPrefix())) {
                    final String optionName = withPrefix ? entry
                            : entry.substring(Environment.getPrefix().length()).toLowerCase(Locale.ENGLISH);
                    final String optionValue = prop.getProperty(entry);
                    passwordOptions.put(optionName, optionValue);
                }
            }
        } catch (final Exception e) {
            throw new CommandException(e);
        } finally {
            try {
                if (!readStdin && is != null)
                    is.close();
            } catch (final Exception ignore) {
            }
        }
        return passwordOptions;
    }

    /**
     * Display the commands from the list that are the closest match to the specified command.
     */
    public static void displayClosestMatch(final String commandName, String[] commands, final String msg, final Logger logger)
            throws InvalidCommandException {
        try {
            // remove leading "*" and ending "*" chars
            int beginIndex = 0;
            int endIndex = commandName.length();
            if (commandName.startsWith("*"))
                beginIndex = 1;
            if (commandName.endsWith("*"))
                endIndex = commandName.length() - 1;
            final String trimmedCommandName = commandName.substring(beginIndex, endIndex);

            // if pattern doesn't start with "_", remove hidden commands
            if (!trimmedCommandName.startsWith("_")) {
                List<String> ncl = new ArrayList<String>();
                for (String cmd : Arrays.asList(commands))
                    if (!cmd.startsWith("_"))
                        ncl.add(cmd);
                commands = ncl.toArray(new String[ncl.size()]);
            }

            // sort commands in alphabetical order
            Arrays.sort(commands);

            // add all matches to the search String since we want
            // to search all the commands that match the string
            final String[] matchedCommands = getMatchedCommands(trimmedCommandName, commands);
            //".*"+trimmedCommandName+".*", commands);
            // don't want to display more than 50 commands
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            if (matchedCommands.length > 0 && matchedCommands.length < MAX_COMMANDS_TO_DISPLAY) {
                pw.println(msg != null ? msg : strings.get("ClosestMatchedCommands"));
                for (String eachCommand : matchedCommands)
                    pw.println("    " + eachCommand);
            } else {
                // find the closest distance
                final String nearestString = StringEditDistance.findNearest(trimmedCommandName, commands);
                // don't display the string if the edit distance is too large
                if (StringEditDistance.editDistance(trimmedCommandName, nearestString) < 5) {
                    pw.println(msg != null ? msg : strings.get("ClosestMatchedCommands"));
                    pw.println("    " + nearestString);
                } else
                    throw new InvalidCommandException(commandName);
            }
            pw.flush();
            logger.severe(sw.toString());
        } catch (Exception e) {
            throw new InvalidCommandException(commandName);
        }
    }

    /**
     * Return all the commands that include pattern (just a literal string, not really a pattern) as a substring.
     */
    private static String[] getMatchedCommands(final String pattern, final String[] commands) {
        List<String> matchedCommands = new ArrayList<String>();
        for (int i = 0; i < commands.length; i++) {
            if (commands[i].indexOf(pattern) >= 0)
                matchedCommands.add(commands[i]);
        }
        return matchedCommands.toArray(new String[matchedCommands.size()]);
    }

    /**
     * Return all commands, local and remote.
     *
     * @return the commands as a String array, sorted
     */
    public static String[] getAllCommands(CLIContainer container, ProgramOptions po, Environment env) {
        try {
            String[] remoteCommands = getRemoteCommands(container, po, env);
            String[] localCommands = getLocalCommands(container);
            String[] allCommands = new String[localCommands.length + remoteCommands.length];
            System.arraycopy(localCommands, 0, allCommands, 0, localCommands.length);
            System.arraycopy(remoteCommands, 0, allCommands, localCommands.length, remoteCommands.length);
            Arrays.sort(allCommands);
            return allCommands;
        } catch (CommandValidationException cve) {
            return null;
        } catch (CommandException ce) {
            return null;
        }
    }

    /**
     * Get all the known local commands.
     *
     * @return the commands as a String array, sorted
     */
    public static String[] getLocalCommands(CLIContainer container) {
        Set<String> names = container.getLocalCommandsNames();
        String[] localCommands = names.toArray(new String[names.size()]);
        Arrays.sort(localCommands);
        return localCommands;
    }

    /**
     * Get the list of commands from the remote server.
     *
     * @return the commands as a String array, sorted
     */
    public static String[] getRemoteCommands(CLIContainer container, ProgramOptions po, Environment env)
            throws CommandException, CommandValidationException {
        /*
         * In order to eliminate all local command names from the list
         * of remote commands, we collect the local command names into
         * a Set that we check later when collecting remote command
         * names.
         */
        Set<String> localnames = container.getLocalCommandsNames();

        /*
         * Now get the list of remote commands.
         */
        po.removeDetach();
        RemoteCLICommand cmd = new RemoteCLICommand("list-commands", po, env);
        ActionReport report = cmd.executeAndReturnActionReport("list-commands");
        List<MessagePart> children = report.getTopMessagePart().getChildren();
        List<String> rcmds = new ArrayList<String>(children.size());
        for (ActionReport.MessagePart msg : children) {
            if (!localnames.contains(msg.getMessage())) {
                rcmds.add(msg.getMessage());
            }
        }
        Collections.sort(rcmds);
        String[] remoteCommands = rcmds.toArray(new String[rcmds.size()]);
        Arrays.sort(remoteCommands);
        return remoteCommands;
    }

    /**
     * Log the command, for debugging.
     */
    public static void writeCommandToDebugLog(String cname, Environment env, String[] args, int exit) {
        File log = env.getDebugLogfile();

        if (log == null)
            return;

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(log, true));
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date date = new Date();
            out.write(dateFormat.format(date));
            out.write(" EXIT: " + exit);

            out.write(" " + cname + " ");

            if (args != null) {
                final int maxPath = 22;
                for (int i = 0; i < args.length; ++i) {
                    // bnevins June 20, 2012
                    // Gigantic password file paths make it VERY hard to read the log
                    // file so let's truncate them.
                    String arg = args[i];

                    if (i > 0 && arg.length() > maxPath && "--passwordfile".equals(args[i - 1]))
                        arg = truncate(arg, maxPath);

                    out.write(arg + " ");
                }
            }
        } catch (IOException e) {
            // It is just a debug file.
        } finally {
            if (out != null) {
                try {
                    out.write("\n");
                } catch (Exception e) {
                    // ignore
                }
                try {
                    out.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    private static String truncate(String arg, int max) {
        int len = arg.length();

        if (len < 20)
            return arg;

        return "....." + arg.substring(len - max);
    }
}
