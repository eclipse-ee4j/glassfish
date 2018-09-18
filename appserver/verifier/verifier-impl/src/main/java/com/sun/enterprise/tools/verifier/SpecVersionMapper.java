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

package com.sun.enterprise.tools.verifier;

import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * This class is responsible for mapping Java EE platform version to
 * various component spec versions.
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class SpecVersionMapper {

    private static LocalStringManagerImpl smh = 
                                StringManagerHelper.getLocalStringsManager();

    public static final String JavaEEVersion_1_2 = "1.2"; // NOI18N

    public static final String JavaEEVersion_1_3 = "1.3"; // NOI18N

    public static final String JavaEEVersion_1_4 = "1.4"; // NOI18N

    public static final String JavaEEVersion_5 = "5"; // NOI18N

    private static String[][] PlatformVersionToEJBVersionMap = {
        {JavaEEVersion_1_2, "1.1"}, // NOI18N
        {JavaEEVersion_1_3, "2.0"}, // NOI18N
        {JavaEEVersion_1_4, "2.1"}, // NOI18N
        {JavaEEVersion_5, "3.0"} // NOI18N
    };

    private static String[][] PlatformVersionToAppClientVersionMap = {
        {JavaEEVersion_1_2, "1.2"}, // NOI18N
        {JavaEEVersion_1_3, "1.3"}, // NOI18N
        {JavaEEVersion_1_4, "1.4"}, // NOI18N
        {JavaEEVersion_5, "5"} // NOI18N
    };

    private static String[][] PlatformVersionToJCAVersionMap = {
        {JavaEEVersion_1_3, "1.0"}, // NOI18N
        {JavaEEVersion_1_4, "1.5"}, // NOI18N
        {JavaEEVersion_5, "1.5"} // NOI18N
    };

    private static String[][] PlatformVersionToWebAppVersionMap = {
        {JavaEEVersion_1_2, "2.2"}, // NOI18N
        {JavaEEVersion_1_3, "2.3"}, // NOI18N
        {JavaEEVersion_1_4, "2.4"}, // NOI18N
        {JavaEEVersion_5, "2.5"} // NOI18N
    };

    private static String[][] PlatformVersionToWebServiceVersionMap = {
        {JavaEEVersion_1_4, "1.1"}, // NOI18N
        {JavaEEVersion_5, "1.2"} // NOI18N
    };

    private static String[][] PlatformVersionToWebServiceClientVersionMap = {
        {JavaEEVersion_1_4, "1.1"}, // NOI18N
        {JavaEEVersion_5, "1.2"} // NOI18N
    };
    
    private static String throwException (String platformVersion) 
            throws IllegalArgumentException {
        throw new IllegalArgumentException(
                smh.getLocalString("com.sun.enterprise.tools.verifier.SpecVersionMapper.exception", // NOI18N
                        "Not able to map platform version [ {0} ] component version.", // NOI18N
                        new Object[] {platformVersion}));
    }

    public static String getEJBVersion(String platformVersion)
            throws IllegalArgumentException {
        for (String[] row : PlatformVersionToEJBVersionMap) {
            if (row[0].equals(platformVersion)) {
                return row[1];
            }
        }
        return throwException(platformVersion);
    }

    public static String getJCAVersion(String platformVersion)
            throws IllegalArgumentException {
        for (String[] row : PlatformVersionToJCAVersionMap) {
            if (row[0].equals(platformVersion)) {
                return row[1];
            }
        }
        return throwException(platformVersion);
    }

    public static String getWebAppVersion(String platformVersion)
            throws IllegalArgumentException {
        for (String[] row : PlatformVersionToWebAppVersionMap) {
            if (row[0].equals(platformVersion)) {
                return row[1];
            }
        }
        return throwException(platformVersion);
    }

    public static String getAppClientVersion(String platformVersion)
            throws IllegalArgumentException {
        for (String[] row : PlatformVersionToAppClientVersionMap) {
            if (row[0].equals(platformVersion)) {
                return row[1];
            }
        }
        return throwException(platformVersion);
    }

    public static String getWebServiceVersion(String platformVersion)
            throws IllegalArgumentException {
        for (String[] row : PlatformVersionToWebServiceVersionMap) {
            if (row[0].equals(platformVersion)) {
                return row[1];
            }
        }
        return throwException(platformVersion);
    }

    public static String getWebServiceClientVersion(String platformVersion) {
        for (String[] row : PlatformVersionToWebServiceClientVersionMap) {
            if (row[0].equals(platformVersion)) {
                return row[1];
            }
        }
        return throwException(platformVersion);
    }
}
