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

package org.glassfish.web.embed.impl;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.apf.ProcessingResult;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.classmodel.reflect.Parser;
import org.glassfish.internal.embedded.*;
import org.glassfish.web.LogFacade;
import org.glassfish.web.deployment.archivist.WebArchivist;
import org.jvnet.hk2.annotations.Service;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.deployment.annotation.impl.ModuleScanner;

/**
 * @author Jerome Dochez
 */
@Service @PerLookup
public class EmbeddedWebArchivist extends WebArchivist {

    private static final Logger _logger = LogFacade.getLogger();

    private final ModuleScanner scanner = new ModuleScanner() {

            final Set<Class> elements = new HashSet<Class>();

            @Override
            public void process(ReadableArchive archiveFile, Object bundleDesc, ClassLoader classLoader, Parser parser)
                    throws IOException {
                // in embedded mode, we don't scan archive, we just process all classes.
                Enumeration<String> entries = archiveFile.entries();
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement();
                    if (entry.endsWith(".class")) {
                        try {
                            elements.add(classLoader.loadClass(toClassName(entry)));
                        } catch (ClassNotFoundException e) {
                            deplLogger.log(Level.FINER,
                                           MessageFormat.format(
                                            _logger.getResourceBundle().getString(LogFacade.CANNOT_LOAD_CLASS), entry),
                                    e);
                        }
                    }
                }

            }

            private String toClassName(String entryName) {
                String name = entryName.substring("WEB-INF/classes/".length(), entryName.length()-".class".length());
                return name.replaceAll("/",".");

            }

            public void process(File archiveFile, Object bundleDesc, ClassLoader classLoader) throws IOException {

            }

            @Override
            public Set getElements() {
                return elements;
            }

    };


    static private URL defaultWebXmlLocation = null;

    protected void setDefaultWebXml(URL defaultWebXml) {
        defaultWebXmlLocation = defaultWebXml;
    }

    @Override
    protected URL getDefaultWebXML() throws IOException {
        if (defaultWebXmlLocation != null) {
            return defaultWebXmlLocation;
        } else {
            URL defaultWebXml = super.getDefaultWebXML();
            return defaultWebXml == null ? getClass().getClassLoader().getResource(
                    "org/glassfish/web/embed/default-web.xml") : defaultWebXml;
        }
    }

    @Override
    protected ProcessingResult processAnnotations(RootDeploymentDescriptor bundleDesc,
                                               ModuleScanner scanner,
                                               ReadableArchive archive)
            throws AnnotationProcessorException, IOException {

        // in embedded mode, I ignore all scanners and parse all possible classes.
        if (archive instanceof ScatteredArchive) {
            return super.processAnnotations(bundleDesc, this.scanner, archive);
        } else {
            return super.processAnnotations(bundleDesc, scanner, archive);
        }
    }
}
