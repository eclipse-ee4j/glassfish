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

package org.glassfish.appclient.server.connector;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.glassfish.api.deployment.archive.ArchiveDetector;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Detects client archive (car) type archives.
 * It's rank can be set using system property {@link #CAR_DETECTOR_RANK_PROP}.
 * Default rank is {@link #DEFAULT_CAR_DETECTOR_RANK}.
 *
 * @author sanjeeb.sahoo@oracle.com
 */
@Service(name = CarDetector.ARCHIVE_TYPE)
@Singleton
public class CarDetector implements ArchiveDetector {
    public static final String CAR_DETECTOR_RANK_PROP = "glassfish.car.detector.rank";
    public static final int DEFAULT_CAR_DETECTOR_RANK = 500;
    public static final String ARCHIVE_TYPE = CarType.ARCHIVE_TYPE;

    @Inject
    private ServiceLocator serviceLocator;
    @Inject
    private AppClientSniffer sniffer;
    @Inject
    private CarType archiveType;
    private ArchiveHandler archiveHandler;

    private Logger logger = Logger.getLogger(getClass().getPackage().getName());

    private static final String APPLICATION_CLIENT_XML = "META-INF/application-client.xml";
    private static final String SUN_APPLICATION_CLIENT_XML = "META-INF/sun-application-client.xml";
    private static final String GF_APPLICATION_CLIENT_XML = "META-INF/glassfish-application-client.xml";

    @Override
    public int rank() {
        return Integer.getInteger(CAR_DETECTOR_RANK_PROP, DEFAULT_CAR_DETECTOR_RANK);
    }

    @Override
    public boolean handles(ReadableArchive archive) throws IOException {
        try {
            if (archive.exists(APPLICATION_CLIENT_XML) ||
                archive.exists(SUN_APPLICATION_CLIENT_XML) ||
                archive.exists(GF_APPLICATION_CLIENT_XML)) {
                return true;
            }

            Manifest manifest = archive.getManifest();
            if (manifest != null &&
                manifest.getMainAttributes().containsKey(
                Attributes.Name.MAIN_CLASS)) {
                return true;
            }
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
                    throw new RuntimeException(e);
                }
                archiveHandler = serviceLocator.getService(ArchiveHandler.class, ARCHIVE_TYPE);
            }
            return archiveHandler;
        }
    }

    @Override
    public ArchiveType getArchiveType() {
        return archiveType;
    }
}
