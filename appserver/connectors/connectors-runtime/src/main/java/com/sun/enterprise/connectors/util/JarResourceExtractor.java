/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors.util;

import com.sun.logging.LogDomains;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * JarResourceExtractor: JarResourceExtractor maps all resources included in a Zip or Jar file.
 * Additionaly, it provides a method to extract one as a blob.
 *
 * @author Sivakumar Thyagarajan
 */

public final class JarResourceExtractor {
    static Logger _logger = LogDomains.getLogger(JarResourceExtractor.class, LogDomains.RSR_LOGGER);

    //resourceName as String Vs contents as byte[]
    private Hashtable htJarContents = new Hashtable();

    /**
     * creates a JarResourceExtractor. It extracts all resources from a Jar into an
     * internal hashtable, keyed by resource names.
     *
     * @param jarFileName
     *            a jar or zip file
     */
    public JarResourceExtractor(String jarFileName) {
        init(jarFileName);
    }

    /**
     * Extracts a jar resource as a blob.
     *
     * @param name
     *            a resource name.
     */
    public byte[] getResource(String name) {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.finer("getResource: " + name);
        }
        return (byte[]) htJarContents.get(name);
    }

    /** initializes internal hash tables with Jar file resources. */
    private void init(String jarFileName) {
        ZipInputStream zis = null;
        try {
            //extract resources and put them into the hashtable.
            FileInputStream fis = new FileInputStream(jarFileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            zis = new ZipInputStream(bis);
            extractResources(zis);
        } catch (Exception ex){
            ex.printStackTrace();
        }finally{
            if(zis != null){
                try{
                    zis.close();
                }catch(Exception e){}
            }
        }

    }

    /**
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void extractResources(ZipInputStream zis) throws FileNotFoundException, IOException {
        ZipEntry ze = null;
        while ((ze = zis.getNextEntry()) != null) {
            if(_logger.isLoggable(Level.FINER)) {
                _logger.finer("ExtractResources : " + ze.getName());
            }
            extractZipEntryContents(ze, zis);
        }
    }

    /**
     * @param zis
     * @throws IOException
     */
    private void extractZipEntryContents(ZipEntry ze, ZipInputStream zis) throws IOException {
            if (ze.isDirectory()) {
                return;
            }

            if(_logger.isLoggable(Level.FINER)) {
                _logger.finer("ze.getName()=" + ze.getName() + ","
                        + "getSize()=" + ze.getSize());
            }

            byte[] b = getZipEntryContents(ze,zis);
            //If it is a jar go RECURSIVE !!
            if(ze.getName().trim().endsWith(".jar")){
                if(_logger.isLoggable(Level.FINER)) {
                    _logger.finer("JAR - going into it !!");
                }
                BufferedInputStream bis = new BufferedInputStream( (new ByteArrayInputStream(b)));
                extractResources(new ZipInputStream(bis));
            } else {
                //add to internal resource hashtable
                htJarContents.put(ze.getName(), b );
                if (ze.getName().trim().endsWith("class")){
                    if(_logger.isLoggable(Level.FINER)) {
                        _logger.finer("CLASS added " + ze.getName());
                    }
                }
                if(_logger.isLoggable(Level.FINER)) {
                    _logger.finer(ze.getName() + ",size="
                        + b.length + ",csize=" + ze.getCompressedSize());
                }
            }
    }

    private byte[] getZipEntryContents(ZipEntry ze, ZipInputStream zis) throws IOException{
        int size = (int) ze.getSize();

        byte[] b = null;
        // -1 means unknown size.
        if (size != -1) {
            //got a proper size, read 'size' bytes
            b = new byte[size];

            int rb = 0;
            int chunk = 0;

            while ((size - rb) > 0) {
                chunk = zis.read(b, rb, size - rb);
                if (chunk == -1) {
                    break;
                }
                rb += chunk;
            }
        } else {
            //size of entry unknown .. keep on reading till we hit a -1
            ArrayList al = new ArrayList();
            int c = 0;
            while( (c = zis.read()) != -1) {
                al.add(Byte.valueOf((byte) c));
            }
            Byte[] btArr = (Byte[])al.toArray(new Byte[al.size()]);
            b = new byte[btArr.length];
            if(_logger.isLoggable(Level.FINER)) {
                _logger.finer("ByteArray length" + btArr.length);
            }
            for (int i = 0; i < btArr.length; i++) {
                b[i] = btArr[i].byteValue();
            }
        }

        return b;
    }
}
