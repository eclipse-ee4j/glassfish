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

/*
 * RuntimeVersion.java
 *
 * Created on March 14, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore;

import com.sun.jdo.api.persistence.support.JDOException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

public class RuntimeVersion {
    private static Properties _properties = new Properties();
    private final static ResourceBundle vendor_info = I18NHelper.loadBundle(
            RuntimeVersion.class);


    private static String product_version = "product.version.number"; // NOI18N
    private static String build_time = "product.build.time"; // NOI18N
    private static String runtime_version = "runtime.version.number"; // NOI18N
    private static String vendor_name = "VendorName"; // NOI18N
    private static String version_number = "VersionNumber"; // NOI18N
    private static String vendor = I18NHelper.getMessage(vendor_info, "vendor"); // NOI18N

    public static void main(String[] args) {
        if (args == null || args.length == 0 ||
                (args.length == 1 && args[0].equals("-version"))) // NOI18N
        {
            RuntimeVersion rt = new RuntimeVersion();
            rt.loadProperties("/com/sun/jdo/spi/persistence/support/sqlstore/sys.properties"); // NOI18N
            System.out.println(parse_version());
        }
        System.exit(0);
    }

    /**
     * Constructor without parameters
     */
    public RuntimeVersion() {
    }

    /**
     * Constructor without parameters
     */
    public RuntimeVersion(String fileName) {
        loadProperties(fileName);
    }

    /**
     * Load properties file
     */
    public static void loadProperties(String fileName) {
        try {
            InputStream in = RuntimeVersion.class.getResourceAsStream(fileName);
            if (in == null)
                throw new FileNotFoundException(fileName);

            _properties.load(in);
            in.close();
        } catch (java.io.IOException e) {
            throw new JDOException(null, e);
        }
    }

    /**
     * Return Vendor properties for a given file name
     */
    public static Properties getVendorProperties(String fileName) {
        loadProperties(fileName);
        return getVendorProperties();
    }

    /**
     * Return Vendor properties
     */
    public static Properties getVendorProperties() {
        if (_properties == null)
            return null;

        Properties _vendorProperties = new Properties();
        _vendorProperties.setProperty(vendor_name, vendor);
        _vendorProperties.setProperty(version_number, parse_version());

        return _vendorProperties;
    }

    /**
     * Parse the build date and create a localized version
     * return version as String
     */
    private static String parse_version() {
        if (_properties == null)
            return null;

        String majorVersion = _properties.getProperty(product_version);
        String minorVersion = _properties.getProperty(runtime_version);
        String buildTime = _properties.getProperty(build_time);

        // Parse the build date and create a localized version
        String s = null;
        try {
            DateFormat dateFormatter = DateFormat.getDateTimeInstance();
            SimpleDateFormat propertyFormat = new SimpleDateFormat("MM/dd/yy hh:mm:ss"); // NOI18N
            s = dateFormatter.format(propertyFormat.parse(buildTime));
        } catch (Exception e) {
            s = ""; // NOI18N
        }

        return I18NHelper.getMessage(vendor_info, "fullVersion", majorVersion, minorVersion, s); // NOI18N

    }
}
