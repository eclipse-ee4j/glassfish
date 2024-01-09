/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.deployment;

import static java.util.logging.Level.SEVERE;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import com.sun.enterprise.deploy.shared.AbstractArchiveHandler;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.loader.ASURLClassLoader;
import com.sun.enterprise.util.LocalStringManagerImpl;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveDetector;
import org.glassfish.api.deployment.archive.EjbArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.ejb.LogFacade;
import org.glassfish.loader.util.ASClassLoaderUtil;
import org.jvnet.hk2.annotations.Service;


/**
 * @author sanjeeb.sahoo@oracle.com
 */
@Service(name = EjbArchiveType.ARCHIVE_TYPE)
public class EjbJarHandler extends AbstractArchiveHandler {

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(EjbJarHandler.class);

    private static final Logger LOG = LogFacade.getLogger();

    @Inject
    @Named(EjbArchiveType.ARCHIVE_TYPE)
    private ArchiveDetector detector;

    @Override
    public String getArchiveType() {
        return EjbArchiveType.ARCHIVE_TYPE;
    }


    @Override
    public boolean handles(ReadableArchive archive) throws IOException {
        return detector.handles(archive);
    }


    @Override
    public String getVersionIdentifier(ReadableArchive archive) {
        String versionIdentifier = null;
        try {
            versionIdentifier = new GFEjbJarXMLParser(archive).extractVersionIdentifierValue(archive);
        } catch (XMLStreamException | IOException e) {
            LOG.log(SEVERE, e.getMessage());
        }

        return versionIdentifier;
    }


    @Override
    public ClassLoader getClassLoader(final ClassLoader parent, DeploymentContext context) {
        ASURLClassLoader cloader = new ASURLClassLoader(parent);

        try {
            String compatProp = context.getAppProps().getProperty(DeploymentProperties.COMPATIBILITY);
            // If user does not specify the compatibility property
            // let's see if it's defined in glassfish-ejb-jar.xml
            if (compatProp == null) {
                GFEjbJarXMLParser gfEjbJarXMLParser = new GFEjbJarXMLParser(context.getSource());
                compatProp = gfEjbJarXMLParser.getCompatibilityValue();
                if (compatProp != null) {
                    context.getAppProps().put(DeploymentProperties.COMPATIBILITY, compatProp);
                }
            }

            // If user does not specify the compatibility property
            // let's see if it's defined in sun-ejb-jar.xml
            if (compatProp == null) {
                SunEjbJarXMLParser sunEjbJarXMLParser = new SunEjbJarXMLParser(context.getSourceDir());
                compatProp = sunEjbJarXMLParser.getCompatibilityValue();
                if (compatProp != null) {
                    context.getAppProps().put(DeploymentProperties.COMPATIBILITY, compatProp);
                }
            }

            // If the compatibility property is set to "v2", we should add
            // all the jars under the ejb module root to maintain backward
            // compatibility of v2 jar visibility
            if (compatProp != null && compatProp.equals("v2")) {
                List<URL> moduleRootLibraries = ASClassLoaderUtil.getURLsAsList(null,
                    new File[] {context.getSourceDir()}, true);
                for (URL url : moduleRootLibraries) {
                    cloader.addURL(url);
                }
            }

            cloader.addURL(context.getSource().getURI().toURL());
            cloader.addURL(context.getScratchDir("ejb").toURI().toURL());
            // Add libraries referenced from manifest
            for (URL url : getManifestLibraries(context)) {
                cloader.addURL(url);
            }
        } catch (Exception e) {
            LOG.log(SEVERE, e.getMessage());
            throw new RuntimeException(e);
        }

        return cloader;
    }

    private static class GFEjbJarXMLParser {

        private XMLStreamReader parser;
        private String compatValue;

        GFEjbJarXMLParser(ReadableArchive archive) throws FileNotFoundException, IOException {
            InputStream input = null;
            File runtimeAltDDFile = archive.getArchiveMetaData(DeploymentProperties.RUNTIME_ALT_DD, File.class);
            if (runtimeAltDDFile != null && runtimeAltDDFile.getPath().indexOf(DescriptorConstants.GF_PREFIX) != -1
                && runtimeAltDDFile.exists() && runtimeAltDDFile.isFile()) {
                DOLUtils.validateRuntimeAltDDPath(runtimeAltDDFile.getPath());
                input = new FileInputStream(runtimeAltDDFile);
            } else {
                input = archive.getEntry("META-INF/glassfish-ejb-jar.xml");
            }

            if (input != null) {
                try {
                    read(input);
                } catch (Throwable t) {
                    String msg = I18N.getLocalString("ejb.deployment.exception_parsing_glassfishejbjarxml",
                        "Error in parsing glassfish-ejb-jar.xml for archive [{0}]: {1}", archive.getURI(),
                        t.getMessage());
                    throw new RuntimeException(msg);
                } finally {
                    if (parser != null) {
                        try {
                            parser.close();
                        } catch (Exception ex) {
                            // ignore
                        }
                    }
                    try {
                        input.close();
                    } catch (Exception ex) {
                        // ignore
                    }
                }
            }
        }


        protected String extractVersionIdentifierValue(ReadableArchive archive) throws XMLStreamException, IOException {
            InputStream input = null;
            String versionIdentifierValue = null;
            String rootElement = null;

            try {
                File runtimeAltDDFile = archive.getArchiveMetaData(DeploymentProperties.RUNTIME_ALT_DD, File.class);
                if (runtimeAltDDFile != null && runtimeAltDDFile.getPath().indexOf(DescriptorConstants.GF_PREFIX) != -1
                    && runtimeAltDDFile.exists() && runtimeAltDDFile.isFile()) {
                    DOLUtils.validateRuntimeAltDDPath(runtimeAltDDFile.getPath());
                    input = new FileInputStream(runtimeAltDDFile);
                } else {
                    input = archive.getEntry("META-INF/glassfish-ejb-jar.xml");
                }

                rootElement = "glassfish-ejb-jar";
                if (input != null) {

                    // parse elements only from glassfish-ejb
                    parser = getXMLInputFactory().createXMLStreamReader(input);

                    int event = 0;
                    skipRoot(rootElement);

                    while (parser.hasNext() && (event = parser.next()) != END_DOCUMENT) {
                        if (event == START_ELEMENT) {
                            String name = parser.getLocalName();
                            if ("version-identifier".equals(name)) {
                                versionIdentifierValue = parser.getElementText();
                            } else {
                                skipSubTree(name);
                            }
                        }
                    }
                }
            } finally {
                if (parser != null) {
                    try {
                        parser.close();
                    } catch (Exception ex) {
                        // ignore
                    }
                }
                if (input != null) {
                    try {
                        input.close();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }

            return versionIdentifierValue;
        }


        private void read(InputStream input) throws XMLStreamException {
            parser = getXMLInputFactory().createXMLStreamReader(input);

            int event = 0;
            boolean done = false;
            skipRoot("glassfish-ejb-jar");

            while (!done && (event = parser.next()) != END_DOCUMENT) {
                if (event == START_ELEMENT) {
                    String name = parser.getLocalName();
                    if (DeploymentProperties.COMPATIBILITY.equals(name)) {
                        compatValue = parser.getElementText();
                        done = true;
                    } else {
                        skipSubTree(name);
                    }
                }
            }
        }


        private void skipRoot(String name) throws XMLStreamException {
            while (true) {
                int event = parser.next();
                if (event == START_ELEMENT) {
                    if (!name.equals(parser.getLocalName())) {
                        throw new XMLStreamException();
                    }
                    return;
                }
            }
        }


        private void skipSubTree(String name) throws XMLStreamException {
            while (true) {
                int event = parser.next();
                if (event == END_DOCUMENT) {
                    throw new XMLStreamException();
                } else if (event == END_ELEMENT && name.equals(parser.getLocalName())) {
                    return;
                }
            }
        }


        String getCompatibilityValue() {
            return compatValue;
        }
    }

    private static class SunEjbJarXMLParser {

        private XMLStreamReader parser;
        private String compatValue;

        SunEjbJarXMLParser(File baseDir) throws FileNotFoundException {
            InputStream input = null;
            File f = new File(baseDir, "META-INF/sun-ejb-jar.xml");
            if (f.exists()) {
                input = new FileInputStream(f);
                try {
                    read(input);
                } catch (Throwable t) {
                    String msg = I18N.getLocalString("ejb.deployment.exception_parsing_sunejbjarxml",
                        "Error in parsing sun-ejb-jar.xml for archive [{0}]: {1}", baseDir, t.getMessage());
                    throw new RuntimeException(msg);
                } finally {
                    if (parser != null) {
                        try {
                            parser.close();
                        } catch (Exception ex) {
                            // ignore
                        }
                    }
                    if (input != null) {
                        try {
                            input.close();
                        } catch (Exception ex) {
                            // ignore
                        }
                    }
                }
            }
        }


        private void read(InputStream input) throws XMLStreamException {
            parser = getXMLInputFactory().createXMLStreamReader(input);

            int event = 0;
            boolean done = false;
            skipRoot("sun-ejb-jar");

            while (!done && (event = parser.next()) != END_DOCUMENT) {

                if (event == START_ELEMENT) {
                    String name = parser.getLocalName();
                    if (DeploymentProperties.COMPATIBILITY.equals(name)) {
                        compatValue = parser.getElementText();
                        done = true;
                    } else {
                        skipSubTree(name);
                    }
                }
            }
        }


        private void skipRoot(String name) throws XMLStreamException {
            while (true) {
                int event = parser.next();
                if (event == START_ELEMENT) {
                    if (!name.equals(parser.getLocalName())) {
                        throw new XMLStreamException();
                    }
                    return;
                }
            }
        }


        private void skipSubTree(String name) throws XMLStreamException {
            while (true) {
                int event = parser.next();
                if (event == END_DOCUMENT) {
                    throw new XMLStreamException();
                } else if (event == END_ELEMENT && name.equals(parser.getLocalName())) {
                    return;
                }
            }
        }


        String getCompatibilityValue() {
            return compatValue;
        }
    }
}
