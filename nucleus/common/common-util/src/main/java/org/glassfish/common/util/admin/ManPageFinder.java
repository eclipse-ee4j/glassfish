/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.common.util.admin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *  A utility class that gets the plain text man page for the
 *  given command.  It searches (using Class.getResource()) for
 *  the pages, and returns the first one found.
 *
 *  For any given man page multiple instances of that page can exist.
 *  Man pages are come in sections (1 through 9, 1m through 9m),
 *  locales (language, country, variant), and by command version.
 *  These instances are ordered by section number (1 - 9, 1m *  - 9m),
 *  local specificity (most specific to least specific) and then by
 *  version (later versions before earlier versions).
 *
 *  This is probably <em>not</em> what is wanted (I think what is
 *  wanted is versions before sections before language specificity),
 *  but is this way because of the way the Java Class.getResource()
 *  mechanism works.
 *
 *  All methods will throw a NullPointerException if given null object
 *  arguments.
 *
 *  All methods will throw an IllegalArgumentException if their
 *  arguments are non-null but are otherwise meaningless.
 */

public class ManPageFinder {
    private static final String[] sections = {
        "1", "1m", "2", "2m", "3", "3m", "4", "4m", "5", "5m",
        "6", "6m", "7", "7m", "8", "8m", "9", "9m", "5asc" };

    private ManPageFinder() {
        // no instances allowed
    }

    /**
     * Get the man page for the given command for the given locale
     * using the given classloader.
     *
     * @param cmdName the command name
     * @param cmdClass the command class
     * @param locale the locale to be used to find the man page
     * @param classLoader the class loader to be used to find the man page
     */
    public static BufferedReader getCommandManPage(
                        String cmdName, String cmdClass,
                        Locale locale, ClassLoader classLoader, Logger logger) {

        InputStream s = null;

        Iterator it = getPossibleLocations(cmdName, cmdClass, locale, logger);
        while (s == null && it.hasNext()) {
            s = classLoader.getResourceAsStream((String)it.next());
        }

        if (s == null) {
            return null;
        }
        return new BufferedReader(new InputStreamReader(s, UTF_8));
    }

    private static Iterator getPossibleLocations(final String cmdName,
            final String cmdClass, final Locale locale, final Logger logger) {
        return new Iterator() {
            final String[] locales = getLocaleLocations(locale);
            private int i = 0;
            private int j = 0;
            private final String helpdir = getHelpDir(cmdClass);
            private final String commandName = cmdName;

            @Override
            public boolean hasNext() {
                return i < locales.length && j < sections.length;
            }

            @Override
            public Object next() throws NoSuchElementException{
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final String result = helpdir + locales[i] + "/" +
                                        commandName + "." + sections[j++];

                if (j == sections.length) {
                    i++;
                    if (i < locales.length) {
                        j = 0;
                    }
                }
                logger.log(Level.FINER, "Trying to get this manpage: {0}", result);
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static String getHelpDir(String cmdClass) {
        // The man page is assumed to be packaged with the
        // command class.
        String pkgname =
            cmdClass.substring(0, cmdClass.lastIndexOf('.'));
        return pkgname.replace('.', '/');
    }

    private static String[] getLocaleLocations(Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        List<String> l = new ArrayList<>();
        l.add("");

        if (language != null && language.length() > 0) {
            l.add("/" + language);
            if (country != null && country.length() > 0) {
                l.add("/" + language + "_" + country);
                if (variant != null && variant.length() > 0) {
                    l.add("/" + language + "_" + country + "_" + variant);
                }
            }
        }
        Collections.reverse(l);
        return l.toArray(new String[l.size()]);
    }
}
