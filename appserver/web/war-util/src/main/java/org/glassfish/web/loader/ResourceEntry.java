/*
 * Copyright (c) 2023, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.glassfish.web.loader;

import com.sun.appserv.server.util.PreprocessorUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;

import static java.lang.System.Logger.Level.WARNING;

/**
 * Resource entry.
 *
 * @author Remy Maucherat 2007
 * @author David Matejcek 2023
 */
public class ResourceEntry {
    private static final Logger LOG = LogFacade.getSysLogger(ResourceEntry.class);


    /**
     * The "last modified" time of the origin file at the time this class
     * was loaded, in milliseconds since the epoch.
     */
    public long lastModified = -1;


    /**
     * Binary content of the resource. May be null.
     */
    public byte[] binaryContent;


    /**
     * Loaded class. May be null if the entry is not a class or the class was not loaded.
     */
    public volatile Class<?> loadedClass;


    /**
     * {@link URL} of the entry source.
     * Can be file, or an url linking inside the JAR file ie. <code>jar:file:/x.jar!/hi.jpg</code>
     * <p>
     * For classes it is null after the class is loaded.
     */
    public URL source;


    /**
     * {@link URL} of the entry codebase, ie: <code>file:/x.jar</code>
     * <p>
     * For classes it is null after the class is loaded.
     */
    public URL codeBase;


    /**
     * Manifest (if the resource was loaded from a JAR).
     * <p>
     * For classes it is null after the class is loaded.
     */
    public Manifest manifest;


    /**
     * Certificates (if the resource was loaded from a JAR).
     * <p>
     * For classes it is null after the class is loaded.
     */
    public Certificate[] certificates;


    /**
     * Use this constructor if codebase and source are the same.
     *
     * @param url resource location
     */
    ResourceEntry(URL url) {
        this(url, url);
    }


    /**
     * @param codeBase
     * @param source {@link URL} of the entry source
     */
    ResourceEntry(URL codeBase, URL source) {
        this.codeBase = codeBase;
        this.source = source;
    }


    /**
     * Reads the resource's binary data from the given input stream and closes the stream.
     */
    void readEntryData(String name, InputStream binaryStream, int contentLength, JarEntry jarEntry) {
        byte[] bytes = new byte[contentLength];
        try {
            int pos = 0;
            while (true) {
                int n = binaryStream.read(bytes, pos, bytes.length - pos);
                if (n <= 0) {
                    break;
                }
                pos += n;
            }
        } catch (Exception e) {
            LOG.log(WARNING, "Unable to read data for class " + name, e);
            return;
        } finally {
            try {
                binaryStream.close();
            } catch(IOException e) {
                LOG.log(WARNING, "Could not close the stream for " + name, e);
            }
        }

        // Preprocess the loaded byte code if bytecode preprocesser is enabled
        if (PreprocessorUtil.isPreprocessorEnabled()) {
            binaryContent = PreprocessorUtil.processClass(name, bytes);
        } else {
            binaryContent = bytes;
        }

        // The certificates are only available after the JarEntry
        // associated input stream has been fully read
        if (jarEntry != null) {
            certificates = jarEntry.getCertificates();
        }
    }


    /**
     * Returns loaded class or the source as string.
     */
    @Override
    public String toString() {
        return loadedClass == null ? source.toExternalForm() : loadedClass.toString();
    }
}

