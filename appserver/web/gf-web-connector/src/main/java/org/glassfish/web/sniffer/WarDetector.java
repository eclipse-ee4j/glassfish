/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.sniffer;

import com.sun.enterprise.deployment.deploy.shared.Util;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.glassfish.api.deployment.archive.ArchiveDetector;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.web.WarType;
import org.jvnet.hk2.annotations.Service;

/**
 * Detects war type archives.
 * <p>
 * It's rank can be set using system property {@link #WAR_DETECTOR_RANK_PROP}.
 * Default rank is {@link #DEFAULT_WAR_DETECTOR_RANK}.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service(name = WarType.ARCHIVE_TYPE)
@Singleton
public class WarDetector implements ArchiveDetector {
    private static final Logger logger = Logger.getLogger(WarDetector.class.getName());

    private static final String WAR_DETECTOR_RANK_PROP = "glassfish.war.detector.rank";
    private static final int DEFAULT_WAR_DETECTOR_RANK = 200;

    private static final String WEB_INF = "WEB-INF";
    private static final String JSP_SUFFIX = ".jsp";
    private static final String WAR_EXTENSION = ".war";

    @Inject
    private WebSniffer sniffer;
    @Inject
    private ServiceLocator services;
    @Inject
    private WarType archiveType;
    private ArchiveHandler archiveHandler;

    // for avatar
    private static final String AVATAR = "avatar";

    @Override
    public int rank() {
        return Integer.getInteger(WAR_DETECTOR_RANK_PROP, DEFAULT_WAR_DETECTOR_RANK);
    }

    @Override
    public boolean handles(ReadableArchive archive) {
        try {
            if (Util.getURIName(archive.getURI()).endsWith(WAR_EXTENSION)) {
                return true;
            }

            if (archive.exists(WEB_INF)) {
                return true;
            }

            if (archive.exists(AVATAR)) {
                return true;
            }

            Enumeration<String> entries = archive.entries();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement();
                if (entryName.endsWith(JSP_SUFFIX)) {
                    return true;
                }
            }
            return false;
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
                    sniffer.setup(null, logger);
                    archiveHandler = services.getService(ArchiveHandler.class, WarType.ARCHIVE_TYPE);
                } catch (IOException e) {
                    throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                }
            }
            return archiveHandler;
        }
    }

    @Override
    public ArchiveType getArchiveType() {
        return archiveType;
    }
}
