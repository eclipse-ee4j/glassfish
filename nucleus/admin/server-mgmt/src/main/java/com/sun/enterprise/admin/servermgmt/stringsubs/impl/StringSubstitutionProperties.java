/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl;

import com.sun.enterprise.admin.servermgmt.SLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Load and retrieves the string substitution properties.
 */
public class StringSubstitutionProperties {

    private static final Logger _logger = SLogger.getLogger();

    private static final String STRINGSUBS_PROPERTIES = "/com/sun/enterprise/admin/servermgmt/stringsubs/stringsubs.properties";
    private static Properties _properties = null;

    /**
     * Loads the string substitution properties i.e {@link StringSubstitutionProperties#STRINGSUBS_PROPERTIES} file
     */
    private static void load() {
        InputStream in = null;
        try {
            in = StringSubstitutionProperties.class.getResourceAsStream(STRINGSUBS_PROPERTIES);
            _properties = new Properties();
            _properties.load(in);
        } catch (IOException e) {
            _logger.log(Level.INFO, SLogger.INVALID_FILE_LOCATION, STRINGSUBS_PROPERTIES);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception io) {
                    /** ignore */
                }
            }
        }
    }

    /**
     * Searches for the property with the specified key in this property list. The method returns <code>null</code> if the
     * property is not found.
     *
     * @param key the property key.
     * @return the value in this property list with the specified key value.
     */
    public static String getProperty(String key) {
        if (_properties == null) {
            load();
        }
        return _properties.getProperty(key);
    }
}
