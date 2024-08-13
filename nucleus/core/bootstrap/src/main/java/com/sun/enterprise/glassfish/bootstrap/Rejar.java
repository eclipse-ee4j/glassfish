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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Nov 10, 2008
 * Time: 8:53:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class Rejar {

    public Rejar() {
    }

    public void rejar(File out, File modules) throws IOException {

        Map<String, ByteArrayOutputStream> metadata = new HashMap<String, ByteArrayOutputStream>();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(out);
            Set<String> names = new HashSet<String>();
            names.add(Attributes.Name.MAIN_CLASS.toString());
            JarOutputStream jos = null;
            try {
                jos = new JarOutputStream(fos, getManifest());
                processDirectory(jos, modules, names, metadata);
                for (File directory : modules.listFiles(new FileFilter() {
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                })) {
                    processDirectory(jos, directory, names, metadata);
                }

                // copy the inhabitants files.
                for (Map.Entry<String, ByteArrayOutputStream> e : metadata.entrySet()) {
                    copy(e.getValue().toByteArray(), e.getKey(), jos);
                }
                jos.flush();
            } finally {
                if (jos!=null) {
                    try {
                        jos.close();
                    } catch(IOException ioe) {
                        // ignore
                    }
                }
            }
        } finally {
            if (fos!=null) {
                try {
                    fos.close();
                } catch(IOException ioe) {
                    // ignore
                }
            }
        }
    }

    protected Manifest getManifest() throws IOException {
        Manifest m = new Manifest();
        m.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        m.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "com.sun.enterprise.glassfish.bootstrap.ASMain");
        return m;
    }

    protected void processDirectory(JarOutputStream jos, File directory, Set<String> names, Map<String, ByteArrayOutputStream> metadata ) throws IOException {

            for (File module : directory.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    if (pathname.getName().endsWith("jar")) {
                        return true;
                    }
                    return false;
                }
            })) {
                // add module
                JarFile in = new JarFile(module);
                try {
                    Enumeration<JarEntry> entries = in.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry je = entries.nextElement();
                        if (je.getName().endsWith("MANIFEST.MF") || names.contains(je.getName())) {
                            continue;
                        }
                        if (je.isDirectory())
                            continue;

                        if (je.getName().startsWith("META-INF/inhabitants/")
                                || je.getName().startsWith("META-INF/services/")) {
                            ByteArrayOutputStream stream = metadata.get(je.getName());
                            if (stream==null) {
                                metadata.put(je.getName(), stream = new ByteArrayOutputStream());
                            }
                            stream.write(("# from "+ module.getName() + "\n").getBytes());
                            copy(in, je, stream);
                        } else {
                            names.add(je.getName());
                            copy(in, je, jos);
                        }
                    }
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Throwable t) {
                                // Ignore
                        }
                    }
                }

            };
    }

    protected  void copy(JarFile in, JarEntry je, JarOutputStream jos) throws IOException {
        try {
            jos.putNextEntry(new JarEntry(je.getName()));
            copy(in, je, (OutputStream) jos);
        } finally {
            jos.flush();
            jos.closeEntry();
        }
    }

    protected void copy(JarFile in, JarEntry je, OutputStream os) throws IOException {
        copy(in, je, Channels.newChannel(os));
    }

    protected void copy(JarFile in, JarEntry je, WritableByteChannel out) throws IOException {
        InputStream is = in.getInputStream(je);
        try {
            ReadableByteChannel inChannel = Channels.newChannel(is);
            ByteBuffer byteBuffer = ByteBuffer.allocate(Long.valueOf(je.getSize()).intValue());
            inChannel.read(byteBuffer);
            byteBuffer.rewind();
            out.write(byteBuffer);
        } finally {
            is.close();
        }
    }

    protected void copy(byte[] bytes, String name, JarOutputStream jos) throws IOException {
        try {
            jos.putNextEntry(new JarEntry(name));
            jos.write(bytes);
        } finally {
            jos.closeEntry();
        }
    }
}
