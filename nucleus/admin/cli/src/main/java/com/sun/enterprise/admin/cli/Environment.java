/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Formatter;

/**
 * The environment variables for CLI commands. An instance of this class is passed to each command to give it access to
 * environment variables. Command implementations should access environment information from this class rather than
 * using System.getenv. In multimode, the export command may change environment variables in the instance of this class
 * that is shared by all commands.
 *
 * @author Bill Shannon
 */
public final class Environment {
    // commands that extend AsadminMain may set this as desired
    private static String PREFIX = "AS_ADMIN_";
    private static String SHORT_PREFIX = "AS_";

    private final Map<String, String> env = new HashMap<>();
    private boolean debug;
    private boolean trace;
    private File logfile;
    private Formatter formatter;

    /**
     * Set the prefix for environment variables referenced from the system environment by Environment objects.
     *
     * @param p the new prefix
     */
    public static void setPrefix(String p) {
        PREFIX = p;
    }

    /**
     * Get the prefix for environment variables referenced from the system environment by Environment objects.
     */
    public static String getPrefix() {
        return PREFIX;
    }

    /**
     * Set the short prefix for environment variables referenced from the system enviornment by Environment objects. This
     * effects methods such as debug(), trace(), etc.
     */
    public static void setShortPrefix(String p) {
        SHORT_PREFIX = p;
    }

    /**
     * Get the name of the environment variable used to set debugging on
     */
    public static String getDebugVar() {
        return SHORT_PREFIX + "DEBUG";
    }

    /**
     * Initialize the enviroment with all relevant system environment entries.
     */
    public Environment() {
        this(false);
    }

    /**
     * Constructor that ignores the system environment, mostly used to enable repeatable tests.
     */
    public Environment(boolean ignoreEnvironment) {
        if (ignoreEnvironment) {
            return;
        }
        // initialize it with all relevant system environment entries
        for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            if (e.getKey().startsWith(PREFIX)) {
                env.put(e.getKey().toUpperCase(Locale.ENGLISH), e.getValue());
            }
        }
        String debugFlag = "Debug";
        String debugProp = getDebugVar();
        debug = System.getProperty(debugFlag) != null || Boolean.parseBoolean(System.getenv(debugProp)) || Boolean.getBoolean(debugProp);

        String traceProp = SHORT_PREFIX + "TRACE";
        trace = System.getProperty(traceProp) != null || Boolean.parseBoolean(System.getenv(traceProp)) || Boolean.getBoolean(traceProp);

        // System Prop trumps environmental variable
        String logProp = SHORT_PREFIX + "LOGFILE";
        String fname = System.getProperty(logProp);
        if (fname == null) {
            fname = System.getenv(logProp);
        }
        if (fname != null) {
           logfile = prepareFile(new File(fname));
        }
        final String formatterClass = env.get(PREFIX + "LOG_FORMATTER");
        try {
            formatter = formatterClass == null ? null
                : (Formatter) Class.forName(formatterClass).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("This is not a valid formatter: " + formatterClass, e);
        }
    }

    /**
     * Return the value of the environment entry corresponding to the named option.
     *
     * @param name the option name
     * @return the value of the corresponding environment entry
     */
    public boolean getBooleanOption(String name) {
        return Boolean.parseBoolean(env.get(optionToEnv(name)));
    }

    /**
     * Return the value of the environment entry corresponding to the named option.
     *
     * @param name the option name
     * @return the value of the corresponding environment entry
     */
    public String getStringOption(String name) {
        return env.get(optionToEnv(name));
    }

    /**
     * Is there an environment entry corresponding to the named option?
     *
     * @param name the option name
     * @return true if there's a corresponding environment entry
     */
    public boolean hasOption(String name) {
        return env.containsKey(optionToEnv(name));
    }

    /**
     * Get the named environment entry.
     *
     * @param name the name of the environment entry
     * @return the value of the entry, or null if no such entry
     */
    public String get(String name) {
        return env.get(name);
    }

    /**
     * Set the named environment entry to the specified value.
     *
     * @param name the environment entry name
     * @param value the value
     * @return the previous value of the entry
     */
    public String put(String name, String value) {
        return env.put(name, value);
    }

    /**
     * Remove the name environment entry.
     *
     * @param name the environment entry name
     */
    public void remove(String name) {
        env.remove(name);
    }

    /**
     * Set the environment entry corresponding to the named option to the specified value.
     *
     * @param name the option name
     * @param value the value
     * @return the previous value of the entry
     */
    public String putOption(String name, String value) {
        return env.put(optionToEnv(name), value);
    }

    /**
     * Return a set of all the entries, just like a Map does.
     */
    public Set<Map.Entry<String, String>> entrySet() {
        return env.entrySet();
    }

    /**
     * Convert an option name (e.g., "host") to an environment variable name (e.g., AS_ADMIN_HOST).
     *
     * @param name the option name
     * @return the environment variable name
     */
    private String optionToEnv(String name) {
        return PREFIX + name.replace('-', '_').toUpperCase(Locale.ENGLISH);
    }

    public boolean debug() {
        return debug;
    }

    public boolean trace() {
        return trace;
    }

    public File getDebugLogfile() {
        return logfile;
    }

    public Formatter getLogFormatter() {
        return formatter;
    }


    private static File prepareFile(File file) {
        try {
            if (file.isFile()) {
                if (file.canWrite()) {
                    return file;
                }
                throw new IllegalStateException("The file already exists, but we cannot write to it: " + file);
            }
            final File dir = file.getCanonicalFile().getParentFile();
            if (!dir.isDirectory() && !dir.mkdirs()) {
                throw new IllegalStateException("The directory " + dir
                    + " doesn't exist and isn't even possible to create it. Output to file '" + file
                    + "' is not possible.");
            }
            if (file.createNewFile()) {
                return file;
            }
            // createNewFile returns false just if the file already existed.
            throw new IllegalStateException("Seems somebody else created the file right now, was it intentional? " + file);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create a log file: " + file, e);
        }
    }
}
