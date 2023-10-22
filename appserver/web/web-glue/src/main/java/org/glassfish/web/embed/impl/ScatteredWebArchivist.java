/*
 * Copyright (c) 2022, 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.web.embed.impl;

import com.sun.enterprise.deployment.annotation.impl.ModuleScanner;
import com.sun.enterprise.deployment.archivist.ArchivistFor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.ScatteredWarArchiveType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.classmodel.reflect.Parser;
import org.glassfish.internal.api.Globals;
import org.glassfish.web.LogFacade;
import org.glassfish.web.deployment.archivist.WebArchivist;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;
import org.jvnet.hk2.annotations.Service;


/**
 * @author Jerome Dochez
 * @author David Matejcek
 */
@Service
@PerLookup
@ArchivistFor(ScatteredWarArchiveType.ARCHIVE_TYPE)
public class ScatteredWebArchivist extends WebArchivist {

    private static final Logger LOG = LogFacade.getLogger();
    private static URL defaultWebXmlLocation;

    private final EmbeddedWebScanner embeddedScanner = new EmbeddedWebScanner();

    static void setDefaultWebXml(URL defaultWebXml) {
        defaultWebXmlLocation = defaultWebXml;
    }


    @Override
    public URL getDefaultWebXML() throws IOException {
        if (defaultWebXmlLocation != null) {
            return defaultWebXmlLocation;
        }
        URL defaultWebXml = super.getDefaultWebXML();
        return defaultWebXml == null
            ? getClass().getClassLoader().getResource("org/glassfish/web/embed/default-web.xml")
            : defaultWebXml;
    }


    @Override
    public ArchiveType getModuleType() {
        return Globals.getDefaultHabitat().getService(ArchiveType.class, ScatteredWarArchiveType.ARCHIVE_TYPE);
    }


    /**
     * @return the scanner for this archivist, usually it is the scanner regitered
     *         with the same module type as this archivist, but subclasses can return
     *         a different version
     */
    @Override
    public EmbeddedWebScanner getScanner() {
        return this.embeddedScanner;
    }

    private static class EmbeddedWebScanner extends ModuleScanner<WebBundleDescriptorImpl> {

        private final Set<Class<?>> elements = new HashSet<>();
        private ClassLoader classLoader;

        @Override
        public void process(ReadableArchive archiveFile, WebBundleDescriptorImpl descriptor, ClassLoader classLoader,
            Parser parser) throws IOException {
            this.classLoader = classLoader;
            this.elements.clear();
            // in embedded mode, we don't scan archive, we just process all classes.
            Enumeration<String> fileEntries = descriptor.getArchiveFileEntries(archiveFile);
            while (fileEntries.hasMoreElements()) {
                String entry = fileEntries.nextElement();
                if (entry.endsWith(".class")) {
                    try {
                        elements.add(classLoader.loadClass(toClassName(entry)));
                    } catch (ClassNotFoundException e) {
                        LOG.log(Level.WARNING, "Cannot load class " + entry, e);
                    }
                }
            }
        }


        @Override
        protected void process(File archiveFile, WebBundleDescriptorImpl descriptor, ClassLoader classLoader)
            throws IOException {
        }


        private String toClassName(String entryName) {
            String name = entryName.substring("WEB-INF/classes/".length(), entryName.length() - ".class".length());
            return name.replaceAll("/", ".");
        }


        @Override
        public Set<Class<?>> getElements() {
            return elements;
        }


        @Override
        public ClassLoader getClassLoader() {
            return classLoader;
        }
    }
}
