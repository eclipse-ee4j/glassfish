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

package org.glassfish.persistence.common.database;

import com.sun.logging.LogDomains;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.persistence.common.I18NHelper;

/**
 * @author Mitesh Meswani This class provides helper to load reources into property object.
 *
 */
public class PropertyHelper {

    /** The logger */
    private final static Logger logger = LogDomains.getLogger(PropertyHelper.class, LogDomains.JDO_LOGGER);

    /** I18N message handler */
    private final static ResourceBundle messages = I18NHelper.loadBundle("org.glassfish.persistence.common.LogStrings", // NOI18N
            PropertyHelper.class.getClassLoader());

    /**
     * Loads properties list from the specified resource into specified Properties object.
     *
     * @param properties Properties object to load
     * @param resourceName Name of resource.
     * @param classLoader The class loader that should be used to load the resource. If null,primordial class loader is
     * used.
     */
    public static void loadFromResource(Properties properties, String resourceName, ClassLoader classLoader) throws IOException {
        load(properties, resourceName, false, classLoader);
    }

    /**
     * Loads properties list from the specified file into specified Properties object.
     *
     * @param properties Properties object to load
     * @param fileName Fully qualified path name to the file.
     */
    public static void loadFromFile(Properties properties, String fileName) throws IOException {
        load(properties, fileName, true, null);
    }

    /**
     * Loads properties list from the specified resource into specified Properties object.
     *
     * @param resourceName Name of resource. If loadFromFile is true, this is fully qualified path name to a file. param
     * classLoader is ignored. If loadFromFile is false,this is resource name.
     * @param classLoader The class loader that should be used to load the resource. If null,primordial class loader is
     * used.
     * @param properties Properties object to load
     * @param loadFromFile true if resourcename is to be treated as file name.
     */
    private static void load(Properties properties, final String resourceName, final boolean loadFromFile, final ClassLoader classLoader)
            throws IOException {

        InputStream bin = null;
        InputStream in = null;
        boolean debug = logger.isLoggable(Level.FINE);

        if (debug) {
            Object[] items = new Object[] { resourceName, Boolean.valueOf(loadFromFile) };
            logger.log(Level.FINE, I18NHelper.getMessage(messages, "database.PropertyHelper.load", items)); // NOI18N
        }

        in = loadFromFile ? openFileInputStream(resourceName) : openResourceInputStream(resourceName, classLoader);
        if (in == null) {
            throw new IOException(I18NHelper.getMessage(messages, "database.PropertyHelper.failedToLoadResource", resourceName));// NOI18N
        }
        bin = new BufferedInputStream(in);
        try {
            properties.load(bin);
        } finally {
            try {
                bin.close();
            } catch (Exception e) {
                // no action
            }
        }
    }

    /**
     * Open fileName as input stream inside doPriviledged block
     */
    private static InputStream openFileInputStream(final String fileName) throws FileNotFoundException {
        return new FileInputStream(fileName);
    }

    /**
     * Open resourcenName as input stream inside doPriviledged block
     */
    private static InputStream openResourceInputStream(final String resourceName, final ClassLoader classLoader) throws FileNotFoundException {
        if (classLoader != null) {
            return classLoader.getResourceAsStream(resourceName);
        }

        return ClassLoader.getSystemResourceAsStream(resourceName);
    }

}
