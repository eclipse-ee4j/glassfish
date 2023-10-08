/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deploy.shared;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.internal.deployment.GenericHandler;
import org.glassfish.logging.annotation.LogMessageInfo;

/**
 * Common methods for ArchiveHandler implementations
 *
 * @author Jerome Dochez
 */
public abstract class AbstractArchiveHandler extends GenericHandler {

    public static final Logger deplLogger = org.glassfish.deployment.common.DeploymentContextImpl.deplLogger;

    @LogMessageInfo(message = "Exception while getting manifest classpath: ", level="WARNING")
    private static final String MANIFEST_CLASSPATH_ERROR = "NCLS-DEPLOYMENT-00024";
    private static XMLInputFactory xmlInputFactory;

    static {
        xmlInputFactory = XMLInputFactory.newFactory();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        // set an zero-byte XMLResolver as IBM JDK does not take SUPPORT_DTD=false
        // unless there is a jvm option com.ibm.xml.xlxp.support.dtd.compat.mode=false
        xmlInputFactory.setXMLResolver(new XMLResolver() {
                @Override
                public Object resolveEntity(String publicID,
                        String systemID, String baseURI, String namespace)
                        throws XMLStreamException {

                    return new ByteArrayInputStream(new byte[0]);
                }
            });
    }

    public List<URL> getManifestLibraries(DeploymentContext context) {
        try {
            Manifest manifest = getManifest(context.getSource());
            return DeploymentUtils.getManifestLibraries(context, manifest);
        } catch (IOException ioe) {
            deplLogger.log(Level.WARNING, MANIFEST_CLASSPATH_ERROR, ioe);
            return new ArrayList<>();
        }
    }


    protected static XMLInputFactory getXMLInputFactory() {
        return xmlInputFactory;
    }
}
