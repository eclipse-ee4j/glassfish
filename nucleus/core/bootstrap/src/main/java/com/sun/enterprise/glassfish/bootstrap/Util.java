/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class Util {

    private static Logger logger = LogFacade.BOOTSTRAP_LOGGER;

    private Util(){}

    public static Logger getLogger() {
        return logger;
    }
    
    /**
     * Gets a property value from the supplied properties object, if not defined there from system properties.
     *
     * @param properties Properties to be searched ahead of system properties.
     * @param propertyName name of the property
     * @param defaultValue default property value
     * @return value of the property
     */
    static String getPropertyOrSystemProperty(Properties properties, String propertyName, String defaultValue) {
        String value = properties.getProperty(propertyName);
        return value != null ? value : System.getProperty(propertyName, defaultValue);
    }

    /**
     * @see #getPropertyOrSystemProperty(java.util.Properties, String, String)
     */
    static String getPropertyOrSystemProperty(Properties properties, String propertyName) {
        return getPropertyOrSystemProperty(properties, propertyName, null);
    }

    static boolean deleteRecursive(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else {
                deleteRecursive(f);
            }
        }
        return dir.delete();
    }

    static long getLastModified(File directory, long current) {

        for (File file : directory.listFiles()) {
            long lastModified;
            if (file.isDirectory()) {
                lastModified = getLastModified(file, current);
            } else {
                lastModified = file.lastModified();
            }
            if (lastModified > current) {
                current = lastModified;
            }
        }
        return current;
    }

    /**
     * This method is used to copy a given file to another file
     * using the buffer sixe specified
     *
     * @param fin  the source file
     * @param fout the destination file
     */
    static void copyFile(File fin, File fout) throws IOException {

        InputStream inStream = new BufferedInputStream(new FileInputStream(fin));
        FileOutputStream fos = new FileOutputStream(fout);
        copy(inStream, fos, fin.length());
    }

    static void copyWithoutClose(InputStream in, FileOutputStream out, long size) throws IOException {

        ReadableByteChannel inChannel = Channels.newChannel(in);
        FileChannel outChannel = out.getChannel();
        outChannel.transferFrom(inChannel, 0, size);

    }

    static void copy(InputStream in, FileOutputStream out, long size) throws IOException {

        try {
            copyWithoutClose(in, out, size);
        } finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }

    static URI whichJar(Class clazz) {
        URL url = clazz.getClassLoader().getResource(
                clazz.getName().replace(".", "/") + ".class");
        if (url != null) {
            URLConnection con = null;
            try {
                con = url.openConnection();
                if (con instanceof JarURLConnection) {
                    return JarURLConnection.class.cast(con).getJarFileURL().toURI();
                }
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }
        }
        return null;
    }

    static File getJDKToolsJar() {
        File javaHome = new File(System.getProperty("java.home"));
        File jdktools = null;
        if (javaHome.getParent() != null) {
            jdktools = new File(javaHome.getParent(),
                    "lib" + File.separator + "tools.jar");
        }
        return jdktools;
    }


    public static void substVars(Properties props) {
        // Perform variable substitution for system properties.
        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            props.setProperty(name,
                    FelixUtil.substVars(props.getProperty(name), name, null, props));
        }
    }

    /**
     * Override property values in the given properties object by values set in corresponding property names in
     * System properties object.
     *
     * @param platformConf which will be updated by corresponding values in System properties.
     * @param excluding property names that should not be overridden
     */
    public static void overrideBySystemProps(Properties platformConf, Collection<String> excluding) {
        Properties sysProps = System.getProperties();
        for (Map.Entry<Object, Object> entry: platformConf.entrySet()) {
            if (excluding.contains(entry.getKey())) {
                continue;
            }
            Object systemPropValue = sysProps.get(entry.getKey());
            if (systemPropValue != null && !systemPropValue.equals(entry.getValue())) {
                platformConf.put(entry.getKey(), systemPropValue);
            }
        }
    }
}
