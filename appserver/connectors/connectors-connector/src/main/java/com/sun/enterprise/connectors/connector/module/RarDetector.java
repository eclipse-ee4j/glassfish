/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.connector.module;

import com.sun.enterprise.deploy.shared.FileArchive;
import org.glassfish.api.deployment.archive.ArchiveDetector;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import com.sun.enterprise.deployment.deploy.shared.Util;
import org.glassfish.deployment.common.GenericAnnotationDetector;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.ServiceLocator;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.inject.Inject;

/**
 * Detects rar type archives.
 * It's rank can be set using system property {@link #RAR_DETECTOR_RANK_PROP}.
 * Default rank is {@link #DEFAULT_RAR_DETECTOR_RANK}.
 *
 * @author sanjeeb.sahoo@oracle.com
 */
@Service(name = RarDetector.ARCHIVE_TYPE)
@Singleton
public class RarDetector implements ArchiveDetector {
    private static final Class[] connectorAnnotations = new Class[]{
            jakarta.resource.spi.Connector.class};

    public static final String RAR_DETECTOR_RANK_PROP = "glassfish.rar.detector.rank";
    public static final int DEFAULT_RAR_DETECTOR_RANK = 300;
    public static final String ARCHIVE_TYPE = RarType.ARCHIVE_TYPE;
    @Inject
    private RarType archiveType;
    @Inject
    private ConnectorSniffer sniffer;
    @Inject
    private ServiceLocator services;

    private ArchiveHandler archiveHandler; // lazy initialisation
    private Logger logger = Logger.getLogger(getClass().getPackage().getName());

    private static final String RA_XML = "META-INF/ra.xml";
    private static final String RAR_EXTENSION = ".rar";
    @Override
    public int rank() {
        return Integer.getInteger(RAR_DETECTOR_RANK_PROP, DEFAULT_RAR_DETECTOR_RANK);
    }

    @Override
    public ArchiveHandler getArchiveHandler() {
        synchronized (this) {
            if (archiveHandler == null) {
                try {
                    sniffer.setup(null, logger);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                archiveHandler = services.getService(ArchiveHandler.class, ARCHIVE_TYPE);
            }
            return archiveHandler;
        }
    }

    @Override
    public ArchiveType getArchiveType() {
        return archiveType;
    }

    /**
     * {@inheritDoc}
     */
    public boolean handles(ReadableArchive archive) throws IOException {
        boolean handles = false;
        try{
            if (Util.getURIName(archive.getURI()).endsWith(RAR_EXTENSION)) {
                return true;
            }

            handles = archive.exists(RA_XML);
        }catch(IOException ioe){
            //ignore
        }
        if (!handles && (archive instanceof FileArchive)) {
            GenericAnnotationDetector detector =
                    new GenericAnnotationDetector(connectorAnnotations);
            handles = detector.hasAnnotationInArchive(archive);
        }
        return handles;
    }
}
