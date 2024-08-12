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

package org.glassfish.appclient.server.core;

import com.sun.enterprise.deploy.shared.AbstractArchiveHandler;
import com.sun.enterprise.loader.ASURLClassLoader;
import com.sun.enterprise.security.ee.perms.PermsArchiveDelegate;
import com.sun.enterprise.security.ee.perms.SMGlobalPolicyUtil;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveDetector;
import org.glassfish.api.deployment.archive.CarArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.appclient.server.core.jws.JavaWebStartInfo;
import org.jvnet.hk2.annotations.Service;

import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * @author sanjeeb.sahoo@oracle.com
 */
@Service(name = CarArchiveType.ARCHIVE_TYPE)
public class CarHandler extends AbstractArchiveHandler {

    @Inject
    @Named(CarArchiveType.ARCHIVE_TYPE)
    private ArchiveDetector detector;

    private static final Logger LOG = Logger.getLogger(JavaWebStartInfo.APPCLIENT_SERVER_MAIN_LOGGER,
                JavaWebStartInfo.APPCLIENT_SERVER_LOGMESSAGE_RESOURCE);

    @Override
    public String getArchiveType() {
        return detector.getArchiveType().toString();
    }

    @Override
    public String getVersionIdentifier(ReadableArchive archive) {
        String versionIdentifier = null;
        try {
            GFCarXMLParser gfXMLParser = new GFCarXMLParser();
            versionIdentifier = gfXMLParser.extractVersionIdentifierValue(archive);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage());
        } catch (XMLStreamException e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }
        return versionIdentifier;

    }

    @Override
    public boolean handles(ReadableArchive archive) throws IOException {
        return detector.handles(archive);
    }

    @Override
    public ClassLoader getClassLoader(final ClassLoader parent, DeploymentContext context) {
        PrivilegedAction<ASURLClassLoader> action = () -> new ASURLClassLoader(parent);
        ASURLClassLoader cloader = AccessController.doPrivileged(action);
        try {
            cloader.addURL(context.getSource().getURI().toURL());
            // add libraries referenced from manifest
            for (URL url : getManifestLibraries(context)) {
                cloader.addURL(url);
            }

            try {
                final DeploymentContext dc = context;
                final ClassLoader cl = cloader;

                AccessController.doPrivileged(
                        new PermsArchiveDelegate.SetPermissionsAction(
                                SMGlobalPolicyUtil.CommponentType.car, dc, cl));
            } catch (PrivilegedActionException e) {
                throw new SecurityException(e.getException());
            }


        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return cloader;
    }

    private static class GFCarXMLParser {
        private XMLStreamReader parser;

        protected String extractVersionIdentifierValue(ReadableArchive archive) throws XMLStreamException, IOException {

            InputStream input = null;
            String versionIdentifierValue = null;
            String rootElement = null;

            try {
                rootElement = "glassfish-application-client";
                input = archive.getEntry("META-INF/glassfish-application-client.xml");
                if (input != null) {
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
                    parser.close();
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
    }
}
