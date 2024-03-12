/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.server.util;

import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This class provides static methods to make accessible the version as well as
 * the individual parts that make up the version
 */
public class Version {

    private static final String KEY_PRODUCT_NAME = "product.name";
    private static final String KEY_PRODUCT_NAME_ABBREVIATION = "product.name.abbreviation";
    private static final String KEY_PRODUCT_VERSION = "product.version";
    private static final String KEY_GIT_COMMIT = "product.build.git.commit";
    private static final String KEY_BASED_ON = "based.on";

    private static final String KEY_ADMIN_COMMAND_NAME = "admin.command.name";
    private static final String KEY_DOMAIN_TEMPLATE_DEFAULTJARFILENAME = "domain.template.defaultJarFileName";
    private static final String KEY_DOMAIN_DEFAULT_ADMIN_GROUPS = "domain.admin.groups";

    private static final List<Properties> VERSION_PROPERTIES = new ArrayList<>();
    private static final Map<String,Properties> VERSION_PROPERTIES_MAP = new HashMap<>();
    private static final Properties PROPERTIES = loadVersionProp();

    private static final String PRODUCT_NAME;
    private static final String PRODUCT_NAME_ABBREVIATION;
    private static final String VERSION;
    private static final String VERSION_RELEASE;
    private static final int VERSION_MAJOR;
    private static final int VERSION_MINOR;
    private static final int VERSION_PATCH;

    private static final String COMMIT;

    static {
        PRODUCT_NAME = getProperty(KEY_PRODUCT_NAME, "GlassFish");
        PRODUCT_NAME_ABBREVIATION = getProperty(KEY_PRODUCT_NAME_ABBREVIATION, "GF");
        VERSION = getProperty(KEY_PRODUCT_VERSION, "");
        int dotBeforeMinor = VERSION.indexOf('.');
        int dotBeforePatch = VERSION.indexOf('.', dotBeforeMinor + 2);
        int suffixStart = dotBeforePatch > 0 ? dotBeforePatch + 1
            : (dotBeforeMinor > 0 ? dotBeforeMinor + 1 : 0);
        for (; suffixStart < VERSION.length(); suffixStart++) {
            if (!Character.isDigit(VERSION.charAt(suffixStart))) {
                break;
            }
        }
        if (dotBeforeMinor > 0) {
            VERSION_MAJOR = parseInt(VERSION.substring(0, dotBeforeMinor), 0);
            if (dotBeforePatch > dotBeforeMinor) {
                VERSION_MINOR = parseInt(VERSION.substring(dotBeforeMinor + 1, dotBeforePatch), 0);
                if (suffixStart > dotBeforePatch) {
                    VERSION_PATCH = parseInt(VERSION.substring(dotBeforePatch + 1, suffixStart), 0);
                } else {
                    VERSION_PATCH = parseInt(VERSION.substring(dotBeforePatch + 1), 0);
                }
            } else {
                if (suffixStart > dotBeforeMinor) {
                    VERSION_MINOR = parseInt(VERSION.substring(dotBeforeMinor + 1, suffixStart), 0);
                } else {
                    VERSION_MINOR = parseInt(VERSION.substring(dotBeforeMinor + 1), 0);
                }
                VERSION_PATCH = 0;
            }
        } else {
            if (suffixStart > 0) {
                VERSION_MAJOR = parseInt(VERSION.substring(0, suffixStart), 0);
            } else {
                VERSION_MAJOR = parseInt(VERSION, 0);
            }
            VERSION_MINOR = 0;
            VERSION_PATCH = 0;
        }
        VERSION_RELEASE = Integer.toString(VERSION_MAJOR) + '.' + Integer.toString(VERSION_MINOR) + '.'
            + Integer.toString(VERSION_PATCH);
        COMMIT = getProperty(KEY_GIT_COMMIT, null);
    }


    /**
     * @return whole version string
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * @return major.minor.patch
     */
    public static String getVersionNumber() {
        return VERSION_RELEASE;
    }


    /**
     * @return Eclipse GlassFish 7.0.0
     */
    public static String getProductId() {
        return getProductName() + " " + getVersionNumber();
    }


    /**
     * @return
     *         <pre>
     * Eclipse GlassFish 7.0.0-SNAPSHOT (commit: 93176e2555176091c8522e43d1d32a0a30652d4a)
     *         </pre>
     */
    public static String getProductIdInfo() {
        return getProductName() + " " + getVersion() + " (commit: " + COMMIT
            + ")";
    }

    /**
     * @return major version
     */
    public static int getMajorVersion() {
        return VERSION_MAJOR;
    }

    /**
     * @return minor version
     */
    public static int getMinorVersion() {
        return VERSION_MINOR;
    }

    /**
     * @return patch version
     */
    public static int getPatchVersion() {
        return VERSION_PATCH;
    }

    /**
     * @return Product Name, ie. Eclipse GlassFish
     */
    public static String getProductName() {
        return PRODUCT_NAME;
    }

    /**
     * @return Abbreviated Product Name, ie. GF
     */
    public static String getProductNameAbbreviation() {
        return PRODUCT_NAME_ABBREVIATION;
    }

    /**
     * @return the admin client command string which represents the name of the
     * command use for performing admin related domain tasks.
     */
    public static String getAdminClientCommandName() {
        return getProperty(KEY_ADMIN_COMMAND_NAME, "nadmin");
    }

    /**
     * @return template name use to create default domain.
     */
    public static String getDomainTemplateDefaultJarFileName() {
        return getProperty(KEY_DOMAIN_TEMPLATE_DEFAULTJARFILENAME, "nucleus-domain.jar");
    }

    public static String getDomainDefaultAdminGroups() {
        return getProperty(KEY_DOMAIN_DEFAULT_ADMIN_GROUPS, "asadmin");
    }

    /**
     * Fetch the value for the property identified by key from the first Properties object
     * in the list.
     * If it doesn't exist look in the based on Properties, recursively.
     * If still not found, return the default, def.
     */
    private static String getProperty(String key, String def) {
        return getProperty(PROPERTIES, key, def);
    }

    private static String getProperty(Properties p, String key, String def) {
        if (p == null) {
            return def;
        }
        String v = p.getProperty(key);
        if (v != null) {
            return v;
        }
        String basedon = p.getProperty(KEY_BASED_ON);
        if (basedon != null) {
            Properties bp = VERSION_PROPERTIES_MAP.get(basedon);
            if (bp != null) {
                return getProperty(bp, key, def);
            }
        }
        return def;
    }


    private static Properties loadVersionProp() {
        String installRoot = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        if (installRoot == null) {
            System.out.println("installRoot is null");
            return null;
        }
        File directory = new File(installRoot).toPath().resolve(Path.of("config", "branding")).toFile();
        if (directory.isDirectory()) {
            for (File file : directory.listFiles(f1 -> f1.getName().endsWith(".properties") && f1.canRead())) {
                try (FileReader fr = new FileReader(file)) {
                    Properties p = new Properties();
                    p.load(fr);
                    VERSION_PROPERTIES.add(p);
                    String shortName = p.getProperty(KEY_PRODUCT_NAME_ABBREVIATION);
                    if (shortName != null) {
                        VERSION_PROPERTIES_MAP.put(shortName, p);
                    }
                } catch (IOException ex) {
                    // ignore files that cannot be read
                }
            }
        }
        // sort the list based on the based-on property.  If a is based on b,
        // then a is earlier then b in the list.
        Comparator<Properties> comparator = (p1, p2) -> {
            String abp1 = p1.getProperty(KEY_PRODUCT_NAME_ABBREVIATION);
            String bo1 = p1.getProperty(KEY_BASED_ON);
            String abp2 = p2.getProperty(KEY_PRODUCT_NAME_ABBREVIATION);
            String bo2 = p2.getProperty(KEY_BASED_ON);
            if (bo1 != null && abp2 != null && bo1.contains(abp2)) {
                return -1;
            }
            if (bo2 != null && abp1 != null && bo2.contains(abp1)) {
                return 1;
            }
            return 0;
        };
        Collections.sort(VERSION_PROPERTIES, comparator);
        return VERSION_PROPERTIES.isEmpty() ? null : VERSION_PROPERTIES.get(0);
    }


    private static int parseInt(String source, int defaultValue) {
        try {
            return Integer.parseInt(source);
        } catch (Exception e) {
            return 0;
        }
    }
}
