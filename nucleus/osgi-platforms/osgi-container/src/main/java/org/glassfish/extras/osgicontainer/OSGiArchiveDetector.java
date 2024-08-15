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

package org.glassfish.extras.osgicontainer;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.logging.Logger;

import org.glassfish.api.deployment.archive.ArchiveDetector;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Detects OSGi type archives.
 * This never participates in detection process. it is explicitly specified in user input (in --type argument).
 * So, it always returns false in {@link #handles(ReadableArchive)}.
 *
 * It's rank can be set using system property {@link #OSGI_ARCHIVE_DETECTOR_RANK_PROP}.
 * Default rank is {@link #DEFAULT_OSGI_ARCHIVE_DETECTOR_RANK}.
 *
 * @author sanjeeb.sahoo@oracle.com
 */
@Service(name = OSGiArchiveDetector.OSGI_ARCHIVE_TYPE)
@Singleton
public class OSGiArchiveDetector implements ArchiveDetector {
    public static final String OSGI_ARCHIVE_DETECTOR_RANK_PROP = "glassfish.ear.detector.rank";
    public static final int DEFAULT_OSGI_ARCHIVE_DETECTOR_RANK = Integer.MAX_VALUE; // the last one to be tried.
    public static final String OSGI_ARCHIVE_TYPE = OSGiArchiveType.ARCHIVE_TYPE; // this is what is accepted in deploy --type command

    @Inject
    private ServiceLocator services;
    @Inject
    private OSGiSniffer sniffer;
    @Inject
    private OSGiArchiveType archiveType;
    private ArchiveHandler archiveHandler;

    private Logger logger = Logger.getLogger(getClass().getPackage().getName());

    @Override
    public int rank() {
        return Integer.getInteger(OSGI_ARCHIVE_DETECTOR_RANK_PROP, DEFAULT_OSGI_ARCHIVE_DETECTOR_RANK);
    }

    @Override
    public boolean handles(ReadableArchive archive) throws IOException {
        // this never participates in detection process. it is explicitly specified in user input (in --type argument)
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
                archiveHandler = services.getService(ArchiveHandler.class, OSGI_ARCHIVE_TYPE);
            }
            return archiveHandler;
        }
    }

    @Override
    public ArchiveType getArchiveType() {
        return archiveType;
    }
}
