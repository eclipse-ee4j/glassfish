/*
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

package com.sun.enterprise.glassfish.bootstrap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Most part is copied from com.sun.appserv.connectors.internal.api.ConnectorsUtil
 * <p/>
 * For JMS to work, rar extraction should be done while creating the GlassFish
 * instance. Along with jmsra, added extraction logic for others as well.
 *
 * @author bhavanishankar@dev.java.net
 */

public class JarUtil {

    /**
     * JAXR  system resource adapter name.
     */
    public static final String JAXR_RA_NAME = "jaxr-ra";

    /**
     * JDBC datasource  system resource adapter name.
     */
    public static final String JDBCDATASOURCE_RA_NAME = "__ds_jdbc_ra";

    /**
     * JDBC connectionpool datasource  system resource adapter name.
     */
    public static final String JDBCCONNECTIONPOOLDATASOURCE_RA_NAME = "__cp_jdbc_ra";

    /**
     * JDBC XA datasource  system resource adapter name.
     */
    public static final String JDBCXA_RA_NAME = "__xa_jdbc_ra";

    /**
     * JDBC Driver Manager system resource adapter name.
     */
    public static final String JDBCDRIVER_RA_NAME = "__dm_jdbc_ra";

    /**
     * JMS datasource  system resource adapter name.
     */
    public static final String DEFAULT_JMS_ADAPTER = "jmsra";

    public static final String RAR_EXTENSION = ".rar";

    public static final List<String> systemRarNames = Collections.unmodifiableList(
            Arrays.asList(
                    JAXR_RA_NAME,
                    JDBCDATASOURCE_RA_NAME,
                    JDBCCONNECTIONPOOLDATASOURCE_RA_NAME,
                    JDBCXA_RA_NAME,
                    JDBCDRIVER_RA_NAME,
                    DEFAULT_JMS_ADAPTER
            ));

    private static final Logger logger = LogFacade.BOOTSTRAP_LOGGER;

    private static String getSystemModuleLocation(String installDir, String rarName) {
        return installDir + File.separator + "lib" +
                File.separator + "install" +
                File.separator + "applications" +
                File.separator + rarName;
    }

    private static boolean systemModuleLocationExists(String installDir, String rarName) {
        return new File(getSystemModuleLocation(installDir, rarName)).exists();
    }

    public static boolean extractRars(String installDir) {
        boolean extracted = true;
        for (String rarName : systemRarNames) {
            extracted = extracted & extractRar(installDir, rarName);
        }
        return extracted;
    }

    public static void setEnv(String installDir) {
        String imqLib =  System.getProperty("com.sun.aas.imqLib",
                getSystemModuleLocation(installDir, DEFAULT_JMS_ADAPTER));
        System.setProperty("com.sun.aas.imqLib", imqLib);
    }

    public static boolean extractRar(String installDir, String rarName) {
        if (systemModuleLocationExists(installDir, rarName)) {
            return false;
        }
        InputStream is = JarUtil.class.getClassLoader().
                getResourceAsStream(rarName + RAR_EXTENSION);
        if (is != null) {
            String fileName = installDir + File.separator + rarName + RAR_EXTENSION;
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(fileName);
                Util.copy(is, os, is.available());
            } catch (IOException e) {
                LogFacade.log(logger,
                        Level.WARNING,
                        LogFacade.BOOTSTRAP_CANT_EXTRACT_ARCHIVE,
                        e,
                        rarName);

                return false;
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException ioe) {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.log(Level.FINEST, "Exception while closing archive [ " +
                                fileName + " ]", ioe);
                    }
                }

                try {
                    is.close();
                } catch (IOException ioe) {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.log(Level.FINEST, "Exception while closing archive [ " +
                                rarName + " ]", ioe);
                    }
                }
            }

            File file = new File(fileName);
            if (file.exists()) {
                try {
                    extractJar(file, installDir);
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                logger.log(Level.INFO, LogFacade.BOOTSTRAP_CANT_FIND_RAR, new Object[] { rarName, fileName });
                return false;
            }
        } else {
            logger.log(Level.FINEST, "could not find RAR [ " + rarName +
                    " ] in the archive, skipping .rar extraction");
            return false;
        }
    }

    public static void extractJar(File jarFile, String destDir) throws IOException {
        java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile);
        java.util.Enumeration enum1 = jar.entries();
        while (enum1.hasMoreElements()) {
            java.util.jar.JarEntry file = (java.util.jar.JarEntry) enum1.nextElement();
            java.io.File f = new java.io.File(destDir + java.io.File.separator + file.getName());
            if (file.isDirectory()) {
                f.mkdir();
                continue;
            } else if (f.exists()) {
                continue;
            }
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = jar.getInputStream(file);
                fos = new FileOutputStream(f);
                int count = 0;
                byte[] buffer = new byte[8192];
                while ((count = is.read(buffer, 0, buffer.length)) != -1) {
                    fos.write(buffer, 0, count);
                }
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception e) {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.log(Level.FINEST, "exception while closing archive [ " +
                                f.getName() + " ]", e);
                    }
                }

                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (Exception e) {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.log(Level.FINEST, "exception while closing archive [ " +
                                file.getName() + " ]", e);
                    }
                }
            }
        }
    }

}
