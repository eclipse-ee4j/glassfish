/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.deployment.archive;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.logging.Logger;

import org.glassfish.api.deployment.archive.ArchiveDetector;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.GenericAnnotationDetector;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Detects EJB jar type archive.
 * It's rank can be set using system property {@link #EJB_JAR_DETECTOR_RANK_PROP}.
 * Default rank is {@link #DEFAULT_EJB_JAR_DETECTOR_RANK}.
 *
 * @author sanjeeb.sahoo@oracle.com
 */
@Service(name = EjbType.ARCHIVE_TYPE)
@Singleton
public class EjbJarDetector implements ArchiveDetector {

    private static final String EJB_JAR_DETECTOR_RANK_PROP = "glassfish.ejb.jar.detector.rank";
    private static final int DEFAULT_EJB_JAR_DETECTOR_RANK = 400;

    private static final Logger LOG = Logger.getLogger(EjbJarDetector.class.getName());

    private static final String EJB_JAR_XML = "META-INF/ejb-jar.xml";
    private static final String SUN_EJB_JAR_XML = "META-INF/sun-ejb-jar.xml";
    private static final String GF_EJB_JAR_XML = "META-INF/glassfish-ejb-jar.xml";

    @Inject
    private EjbSniffer sniffer;
    @Inject
    private EjbType archiveType;
    @Inject
    private ServiceLocator baseServiceLocator;

    // lazy initialisation
    private ArchiveHandler archiveHandler;

    @Override
    public int rank() {
        return Integer.getInteger(EJB_JAR_DETECTOR_RANK_PROP, DEFAULT_EJB_JAR_DETECTOR_RANK);
    }


    @Override
    public boolean handles(ReadableArchive archive) {
        try {
            if (archive.exists(EJB_JAR_XML) || archive.exists(SUN_EJB_JAR_XML) || archive.exists(GF_EJB_JAR_XML)) {
                return true;
            }
            GenericAnnotationDetector detector = new GenericAnnotationDetector(sniffer.getAnnotationTypes());
            return detector.hasAnnotationInArchive(archive);
        } catch (IOException ioe) {
            // ignore
        }
        return false;
    }


    @Override
    public ArchiveHandler getArchiveHandler() {
        synchronized (this) {
            if (archiveHandler == null) {
                try {
                    sniffer.setup(null, LOG);
                } catch (IOException e) {
                    // TODO(Sahoo): Proper Exception Handling
                    throw new RuntimeException(e);
                }
                archiveHandler = baseServiceLocator.getService(ArchiveHandler.class, EjbType.ARCHIVE_TYPE);
            }
            return archiveHandler;
        }
    }


    @Override
    public ArchiveType getArchiveType() {
        return archiveType;
    }
}
