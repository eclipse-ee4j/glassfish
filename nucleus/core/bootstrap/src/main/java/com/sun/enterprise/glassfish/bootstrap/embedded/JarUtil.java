/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.glassfish.bootstrap.embedded;

import com.sun.enterprise.glassfish.bootstrap.log.LogFacade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Most part is copied from com.sun.appserv.connectors.internal.api.ConnectorsUtil
 * <p/>
 * For JMS to work, rar extraction should be done while creating the GlassFish
 * instance. Along with jmsra, added extraction logic for others as well.
 *
 * @author bhavanishankar@dev.java.net
 * @author David Matejcek
 */
final class JarUtil {

    /**
     * JAXR  system resource adapter name.
     */
    private static final String JAXR_RA_NAME = "jaxr-ra";

    /**
     * JDBC datasource  system resource adapter name.
     */
    private static final String JDBCDATASOURCE_RA_NAME = "__ds_jdbc_ra";

    /**
     * JDBC connectionpool datasource  system resource adapter name.
     */
    private static final String JDBCCONNECTIONPOOLDATASOURCE_RA_NAME = "__cp_jdbc_ra";

    /**
     * JDBC XA datasource  system resource adapter name.
     */
    private static final String JDBCXA_RA_NAME = "__xa_jdbc_ra";

    /**
     * JDBC Driver Manager system resource adapter name.
     */
    private static final String JDBCDRIVER_RA_NAME = "__dm_jdbc_ra";

    /**
     * JMS datasource  system resource adapter name.
     */
    private static final String DEFAULT_JMS_ADAPTER = "jmsra";

    private static final String RAR_EXTENSION = ".rar";

    private static final List<String> SYSTEM_RAR_NAMES = Collections.unmodifiableList(
            Arrays.asList(
                    JAXR_RA_NAME,
                    JDBCDATASOURCE_RA_NAME,
                    JDBCCONNECTIONPOOLDATASOURCE_RA_NAME,
                    JDBCXA_RA_NAME,
                    JDBCDRIVER_RA_NAME,
                    DEFAULT_JMS_ADAPTER
            ));

    private static final Logger LOG = LogFacade.BOOTSTRAP_LOGGER;

    private JarUtil() {
        // utility class
    }

    public static boolean extractRars(String installDir) {
        boolean extracted = true;
        for (String rarName : SYSTEM_RAR_NAMES) {
            extracted = extracted & extractRar(installDir, rarName);
        }
        return extracted;
    }

    public static void setEnv(String installDir) {
        String location = getSystemModuleLocation(installDir, DEFAULT_JMS_ADAPTER);
        String imqLib = System.getProperty("com.sun.aas.imqLib", location);
        System.setProperty("com.sun.aas.imqLib", imqLib);
    }

    private static boolean extractRar(String installDir, String rarName) {
        if (systemModuleLocationExists(installDir, rarName)) {
            return false;
        }
        String rarFileName = rarName + RAR_EXTENSION;
        try (InputStream rarInJar = JarUtil.class.getClassLoader().getResourceAsStream(rarFileName)) {
            if (rarInJar == null) {
                LOG.log(Level.CONFIG, "The RAR file wasn't found: [" + rarFileName + "]");
                return false;
            }
            try (JarInputStream jarInputStream = new JarInputStream(rarInJar)) {
                extractJar(jarInputStream, installDir);
                return true;
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Exception while extracting resource [" + rarFileName + "]", e);
                return false;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected exception when opening resource [" + rarFileName + "]", e);
        }
    }

    private static boolean systemModuleLocationExists(String installDir, String rarName) {
        return new File(getSystemModuleLocation(installDir, rarName)).exists();
    }

    private static String getSystemModuleLocation(String installDir, String rarName) {
        return installDir + File.separator + "lib" +
                File.separator + "install" +
                File.separator + "applications" +
                File.separator + rarName;
    }

    private static void extractJar(JarInputStream jar, String destDir) throws IOException {
        while (true) {
            JarEntry entry = jar.getNextJarEntry();
            if (entry == null) {
                return;
            }
            try {
                File outputFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                    continue;
                } else if (outputFile.exists()) {
                    continue;
                }
                try (FileOutputStream out = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[8192];
                    int readCount = 0;

                    while ((readCount = jar.read(buffer)) >= 0) {
                        out.write(buffer, 0, readCount);
                    }
                }
            } finally {
                jar.closeEntry();
            }
        }
    }
}
