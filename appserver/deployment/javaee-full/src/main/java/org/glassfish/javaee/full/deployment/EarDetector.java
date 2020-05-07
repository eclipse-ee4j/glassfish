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

package org.glassfish.javaee.full.deployment;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.glassfish.api.deployment.archive.ArchiveDetector;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.deployment.EarType;
import com.sun.enterprise.deployment.deploy.shared.Util;

/**
 * Detects ear type archives.
 * It's rank can be set using system property {@link #EAR_DETECTOR_RANK_PROP}.
 * Default rank is {@link #DEFAULT_EAR_DETECTOR_RANK}.
 *
 * @author sanjeeb.sahoo@oracle.com
 */
@Service(name = EarDetector.ARCHIVE_TYPE)
@Singleton
public class EarDetector implements ArchiveDetector {

    public static final String EAR_DETECTOR_RANK_PROP = "glassfish.ear.detector.rank";
    public static final int DEFAULT_EAR_DETECTOR_RANK = 100;
    public static final String ARCHIVE_TYPE = EarType.ARCHIVE_TYPE;

    @Inject private ServiceLocator serviceLocator;
    @Inject private EarSniffer sniffer;
    @Inject private EarType archiveType;
    private ArchiveHandler archiveHandler;

    private static final String APPLICATION_XML = "META-INF/application.xml";
    private static final String SUN_APPLICATION_XML = "META-INF/sun-application.xml";
    private static final String GF_APPLICATION_XML = "META-INF/glassfish-application.xml";
    private static final String EAR_EXTENSION = ".ear";
    private static final String EXPANDED_WAR_SUFFIX = "_war";
    private static final String EXPANDED_RAR_SUFFIX = "_rar";
    private static final String EXPANDED_JAR_SUFFIX = "_jar";


    private Logger logger = Logger.getLogger(getClass().getPackage().getName());

    @Override
    public int rank() {
        return Integer.getInteger(EAR_DETECTOR_RANK_PROP, DEFAULT_EAR_DETECTOR_RANK);
    }

    @Override
    public boolean handles(ReadableArchive archive) throws IOException {
        boolean isEar = false;
        try{
            if (Util.getURIName(archive.getURI()).endsWith(EAR_EXTENSION)) {
                return true;
            }

            isEar = archive.exists(APPLICATION_XML) ||
                    archive.exists(SUN_APPLICATION_XML) ||
                    archive.exists(GF_APPLICATION_XML);

            if (!isEar) {
                isEar = isEARFromIntrospecting(archive);
            }
        }catch(IOException ioe){
            //ignore
        }
        return isEar;
    }

    @Override
    public ArchiveHandler getArchiveHandler() {
        synchronized (this) {
            if(archiveHandler == null) {
                try {
                    sniffer.setup(null, logger);
                } catch (IOException e) {
                    throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                }
                archiveHandler = serviceLocator.getService(ArchiveHandler.class,ARCHIVE_TYPE);
            }
            return archiveHandler;
        }
    }

    @Override
    public ArchiveType getArchiveType() {
        return archiveType;
    }

    // introspecting the sub archives to see if any of them
    // ended with expected suffix
    private static boolean isEARFromIntrospecting(ReadableArchive archive)
        throws IOException {
        for (String entryName : archive.getDirectories()) {
            // we don't have other choices but to look if any of
            // the subdirectories is ended with expected suffix
            if ( entryName.endsWith(EXPANDED_WAR_SUFFIX) ||
                 entryName.endsWith(EXPANDED_RAR_SUFFIX) ||
                 entryName.endsWith(EXPANDED_JAR_SUFFIX) ) {
                return true;
            }
        }
        return false;
    }
}

