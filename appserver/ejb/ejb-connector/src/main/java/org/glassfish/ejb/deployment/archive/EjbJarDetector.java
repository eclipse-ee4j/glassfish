/*
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

import org.glassfish.api.deployment.archive.ArchiveDetector;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.deployment.common.GenericAnnotationDetector;
import org.glassfish.hk2.api.ServiceLocator;

import jakarta.inject.Inject;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Detects EJB jar type archive.
 * It's rank can be set using system property {@link #EJB_JAR_DETECTOR_RANK_PROP}.
 * Default rank is {@link #DEFAULT_EJB_JAR_DETECTOR_RANK}.
 *
 * @author sanjeeb.sahoo@oracle.com
 */
@Service(name = EjbJarDetector.ARCHIVE_TYPE)
@Singleton
public class EjbJarDetector implements ArchiveDetector {
    public static final String EJB_JAR_DETECTOR_RANK_PROP = "glassfish.ejb.jar.detector.rank";
    public static final int DEFAULT_EJB_JAR_DETECTOR_RANK = 400;
    public static final String ARCHIVE_TYPE = EjbType.ARCHIVE_TYPE;

    @Inject
    private EjbSniffer sniffer;
    @Inject
    private EjbType archiveType;
    @Inject
    private ServiceLocator baseServiceLocator;

    private ArchiveHandler archiveHandler; // lazy initialisation
    private Logger logger = Logger.getLogger(getClass().getPackage().getName());

    private static final String EJB_JAR_XML = "META-INF/ejb-jar.xml";
    private static final String SUN_EJB_JAR_XML = "META-INF/sun-ejb-jar.xml";
    private static final String GF_EJB_JAR_XML = "META-INF/glassfish-ejb-jar.xml";
    @Override
    public int rank() {
        return Integer.getInteger(EJB_JAR_DETECTOR_RANK_PROP, DEFAULT_EJB_JAR_DETECTOR_RANK);
    }

    @Override
    public boolean handles(ReadableArchive archive) {
        try {
            if (archive.exists(EJB_JAR_XML) ||
                archive.exists(SUN_EJB_JAR_XML) ||
                archive.exists(GF_EJB_JAR_XML)) {
                return true;
            }

            GenericAnnotationDetector detector =
                new GenericAnnotationDetector(sniffer.getAnnotationTypes());

            return detector.hasAnnotationInArchive(archive);
        }catch(IOException ioe){
            //ignore
        }
        return false;
    }

    @Override
    public ArchiveHandler getArchiveHandler() {
        synchronized (this) {
            if (archiveHandler == null) {
                try {
                    sniffer.setup(null, logger);
                } catch (IOException e) {
                    throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                }
                archiveHandler = baseServiceLocator.getService(ArchiveHandler.class, ARCHIVE_TYPE);
            }
            return archiveHandler;
        }
    }

    @Override
    public ArchiveType getArchiveType() {
        return archiveType;
    }
}
