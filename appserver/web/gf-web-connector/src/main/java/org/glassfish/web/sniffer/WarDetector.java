/*
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

import org.glassfish.api.deployment.archive.ArchiveDetector;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import com.sun.enterprise.deployment.deploy.shared.Util;
import org.glassfish.web.WarType;
import jakarta.inject.Inject;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.ServiceLocator;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.Enumeration;

/**
 * Detects war type archives.
 * It's rank can be set using system property {@link #WAR_DETECTOR_RANK_PROP}.
 * Default rank is {@link #DEFAULT_WAR_DETECTOR_RANK}.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service(name = WarDetector.ARCHIVE_TYPE)
@Singleton
public class WarDetector implements ArchiveDetector {
    public static final String WAR_DETECTOR_RANK_PROP = "glassfish.war.detector.rank";
    public static final int DEFAULT_WAR_DETECTOR_RANK = 200;
    public static final String ARCHIVE_TYPE = WarType.ARCHIVE_TYPE;

    @Inject WebSniffer sniffer;
    @Inject ServiceLocator services;
    @Inject WarType archiveType;
    private ArchiveHandler archiveHandler;
    private Logger logger = Logger.getLogger(getClass().getPackage().getName());

    private static final String WEB_INF = "WEB-INF";
    private static final String JSP_SUFFIX = ".jsp";
    private static final String WAR_EXTENSION = ".war";

    //for avatar
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
                    archiveHandler = services.getService(ArchiveHandler.class, ARCHIVE_TYPE);
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
