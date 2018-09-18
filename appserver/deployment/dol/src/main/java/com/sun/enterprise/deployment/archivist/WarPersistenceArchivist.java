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

package com.sun.enterprise.deployment.archivist;

import org.glassfish.deployment.common.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import com.sun.enterprise.deployment.util.DOLUtils;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.Archive;
import org.xml.sax.SAXException;
import org.jvnet.hk2.annotations.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;

@Service
@ExtensionsArchivistFor("jpa")
public class WarPersistenceArchivist extends PersistenceArchivist {

    @Override
    public boolean supportsModuleType(ArchiveType moduleType) {
        return moduleType != null && moduleType.equals(DOLUtils.warType());
    }

    @Override
    public Object open(Archivist main, ReadableArchive warArchive, RootDeploymentDescriptor descriptor) throws IOException, SAXException {
        final String CLASSES_DIR = "WEB-INF/classes/";

        if(deplLogger.isLoggable(Level.FINE)) {
            deplLogger.logp(Level.FINE, "WarPersistenceArchivist",
                    "readPersistenceDeploymentDescriptors", "archive = {0}",
                    warArchive.getURI());
        }
        Map<String, ReadableArchive> probablePersitenceArchives =  new HashMap<String, ReadableArchive>();
        try {
            SubArchivePURootScanner warLibScanner = new SubArchivePURootScanner() {
                String getPathOfSubArchiveToScan() {
                    return "WEB-INF/lib";
                }
            };
            probablePersitenceArchives = getProbablePersistenceRoots(warArchive, warLibScanner);

            final String pathOfPersistenceXMLInsideClassesDir = CLASSES_DIR+ DescriptorConstants.PERSISTENCE_DD_ENTRY;
            InputStream is = warArchive.getEntry(pathOfPersistenceXMLInsideClassesDir);
            if (is!=null) {
                is.close();
                probablePersitenceArchives.put(CLASSES_DIR, warArchive.getSubArchive(CLASSES_DIR));
            }

            for(Map.Entry<String, ReadableArchive> pathToArchiveEntry : probablePersitenceArchives.entrySet()) {
                readPersistenceDeploymentDescriptor(main, pathToArchiveEntry.getValue(), pathToArchiveEntry.getKey(), descriptor);
            }
        } finally {
            for(Archive probablePersitenceArchive : probablePersitenceArchives.values()) {
                probablePersitenceArchive.close();
            }
        }
        return null;
    }
}
