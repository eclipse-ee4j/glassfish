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

package org.glassfish.uberjar.bootstrap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.glassfish.embeddable.GlassFish;

/**
 * @author bhavanishankar@dev.java.net
 */

public class Util {

    private static Logger logger = Logger.getLogger("embedded-glassfish");

    public static Logger getLogger() {
        return logger;
    }

    // serverId:GlassFish map
    private final static Map<String, GlassFish> gfMap =
            new HashMap<String, GlassFish>();

    public static synchronized void addServer(String serverId, GlassFish glassfish) {
        gfMap.put(serverId, glassfish);
    }


    public static synchronized void removeServer(String serverId) {
        gfMap.remove(serverId);
    }

    public static GlassFish getServer(String serverId) {
        return gfMap.get(serverId);
    }

    public static URI whichJar(Class clazz) {
        logger.finer("ResourceName = " + clazz.getName().replace(".", "/") + ".class");
        URL url = clazz.getClassLoader().getResource(
                clazz.getName().replace(".", "/") + ".class");
        logger.finer("url = " + url);
        if (url != null) {
            URLConnection con = null;
            try {
                con = url.openConnection();
                logger.finer("con = " + con);
                if (con instanceof JarURLConnection) {
                    return JarURLConnection.class.cast(con).getJarFileURL().toURI();
                }

            } catch (Exception ioe) {
                ioe.printStackTrace();
            }
        }
        return null;
    }

    public static boolean isUber(URI uri) {
        String uriString = uri.toString();
        String jarFileName = uriString.substring(uriString.lastIndexOf("/") + 1);
        return jarFileName.indexOf("glassfish-embedded") != -1 ? true : false;
    }

    private static String MODULES_DIR_PREFIX = "modules";
    private static String MODULES_DIR_SUFFIX = "_jar/";
    private static final String JARFILE_URL_PREFIX = "jar:file:";
    private static final String JARENTRY_PREFIX = "!/";

    public static List<URL> getModuleJarURLs(File modulesJarFile) {
        List<URL> moduleJarURLs = new ArrayList<>();
        try (JarFile modulesJar = new JarFile(modulesJarFile)) {
            Enumeration<JarEntry> entries = modulesJar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    continue;
                }
                if (!entry.getName().startsWith(MODULES_DIR_PREFIX) ||
                        !entry.getName().endsWith(MODULES_DIR_SUFFIX)) {
                    continue;
                }
                moduleJarURLs.add(new URL(JARFILE_URL_PREFIX + modulesJar.getName() +
                        JARENTRY_PREFIX + entry.getName()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return moduleJarURLs;
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

}
