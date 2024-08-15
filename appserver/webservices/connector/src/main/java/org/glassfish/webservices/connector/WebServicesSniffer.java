/*
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

package org.glassfish.webservices.connector;

import jakarta.inject.Singleton;

import java.io.IOException;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.internal.deployment.GenericSniffer;
import org.jvnet.hk2.annotations.Service;

/**
 * This is the Sniffer for Webservices
 * @author Bhakti Mehta
 */
@Service(name="webservices")
@Singleton
public class WebServicesSniffer extends GenericSniffer {

    private static final Class[]  handledAnnotations = new Class[] {jakarta.jws.WebService.class,
            jakarta.xml.ws.WebServiceProvider.class, jakarta.xml.ws.WebServiceRef.class};

    final String[] containers = {
        "org.glassfish.webservices.WebServicesContainer",
        "org.glassfish.webservices.metroglue.MetroContainer"
    };

    public WebServicesSniffer() {
        super("webservices", null, null);
    }

    /**
     * .ear (the resource can be present in lib dir of the ear)
     * Returns true if the archive contains webservices.xml either in WEB-INF or META-INF directories
     */
    @Override
    public boolean handles(ReadableArchive location) {
        return isEntryPresent(location, "WEB-INF/webservices.xml") ||
                isEntryPresent(location, "META-INF/webservices.xml");
    }

    private boolean isEntryPresent(ReadableArchive location, String entry) {
        boolean entryPresent = false;
        try {
            entryPresent = location.exists(entry);
        } catch (IOException e) {
            // ignore
        }
        return entryPresent;
    }

    @Override
    public String[] getContainersNames() {
        return containers;
    }

    @Override
    public Class<? extends java.lang.annotation.Annotation>[] getAnnotationTypes() {
        return handledAnnotations;
    }

    @Override
    public boolean isUserVisible() {
        return true;
    }

    @Override
    public String[] getURLPatterns() {
        return null;
    }

    /**
     *
     * This API is used to help determine if the sniffer should recognize
     * the current archive.
     * If the sniffer does not support the archive type associated with
     * the current deployment, the sniffer should not recognize the archive.
     *
     * @param archiveType the archive type to check
     * @return whether the sniffer supports the archive type
     *
     */
    public boolean supportsArchiveType(ArchiveType archiveType) {
        if (archiveType.toString().equals("war") ||
            archiveType.toString().equals("ejb")) {
            return true;
        }
        return false;
    }
}
