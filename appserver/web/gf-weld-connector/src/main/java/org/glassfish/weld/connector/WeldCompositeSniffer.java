/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld.connector;

import java.io.IOException;
import java.util.Enumeration;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.javaee.core.deployment.ApplicationHolder;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Singleton;


/**
 * This sniffer determines if there are any beans.xml at the ear level.
 */
@Service(name = "weldCompositeSniffer")
@Singleton
public class WeldCompositeSniffer extends WeldSniffer {

    public boolean handles(DeploymentContext context) {
        ArchiveType archiveType = habitat.getService(ArchiveType.class, context.getArchiveHandler().getArchiveType());
        if (archiveType != null && !supportsArchiveType(archiveType)) {
            return false;
        }

        boolean isWeldApplication = false;
        ApplicationHolder holder = context.getModuleMetaData(ApplicationHolder.class);
        ReadableArchive appRoot = context.getSource();
        if ((holder != null) && (holder.app != null)) {
            isWeldApplication = scanLibDir(appRoot, holder.app.getLibraryDirectory(), context);
        }

        return isWeldApplication;
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
        if (archiveType.toString().equals("ear")) {
            return true;
        }
        return false;
    }

    // This method returns true if at least one /lib jar is a Weld archive
    // A more thorough scan is done in WeldDeployer to extract all Weld archives
    // under the /lib directory.
    private boolean scanLibDir(ReadableArchive archive, String libLocation, DeploymentContext context) {
        boolean entryPresent = false;
        if (libLocation != null && !libLocation.isEmpty()) {
            Enumeration<String> entries = archive.entries(libLocation);
            while (entries.hasMoreElements() && !entryPresent) {
                String entryName = entries.nextElement();
                // if a jar in lib dir and not WEB-INF/lib/foo/bar.jar
                if (entryName.endsWith(WeldUtils.JAR_SUFFIX) &&
                    entryName.indexOf(WeldUtils.SEPARATOR_CHAR, libLocation.length() + 1 ) == -1 ) {
                    try {
                        ReadableArchive jarInLib = archive.getSubArchive(entryName);
                        entryPresent = isArchiveCDIEnabled(context, jarInLib, WeldUtils.META_INF_BEANS_XML);
                        if (!entryPresent) {
                            entryPresent = WeldUtils.isImplicitBeanArchive(context, jarInLib);
                        }
                        jarInLib.close();
                        if (entryPresent) break;
                    } catch (IOException e) {
                    }
                }
            }
        }
        return entryPresent;
    }

}
