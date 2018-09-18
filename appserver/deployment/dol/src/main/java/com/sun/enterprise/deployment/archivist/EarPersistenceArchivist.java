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
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.util.DOLUtils;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.Archive;
import org.xml.sax.SAXException;
import org.jvnet.hk2.annotations.Service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;

@Service
@ExtensionsArchivistFor("jpa")
public class EarPersistenceArchivist extends PersistenceArchivist {

    @Override
    public boolean supportsModuleType(ArchiveType moduleType) {
        return moduleType!=null && moduleType.equals(DOLUtils.earType());
    }


    /**
     * Reads persistence.xml from spec defined pu roots of an ear.
     * Spec defined pu roots are - (1)Non component jars in root of ear (2)jars in lib of ear
     */
    @Override
    public Object open(Archivist main, ReadableArchive earArchive, final RootDeploymentDescriptor descriptor) throws IOException, SAXException {

        if(deplLogger.isLoggable(Level.FINE)) {
            deplLogger.logp(Level.FINE, "EarArchivist",
                    "readPersistenceDeploymentDescriptors", "archive = {0}",
                    earArchive.getURI());
        }


        Map<String, ReadableArchive> probablePersitenceArchives = new HashMap<String,  ReadableArchive>();
        try {
            if (! (descriptor instanceof Application)) {
                return null;
            }
            final Application app = Application.class.cast(descriptor);

            // TODO: need to compute includeRoot, not hard-code it, in the next invocation. The flag should be set to true if operating in v2 compatibility mode false otherwise.
            // Check with Hong how to get hold of the flag here?
            EARBasedPersistenceHelper.addLibraryAndTopLevelCandidates(earArchive, app, true /* includeRoot */,
                    probablePersitenceArchives);

            for(Map.Entry<String, ReadableArchive> pathToArchiveEntry : probablePersitenceArchives.entrySet()) {
                readPersistenceDeploymentDescriptor(main, pathToArchiveEntry.getValue(), pathToArchiveEntry.getKey(), descriptor);
            }
        } finally {
            for(Archive subArchive : probablePersitenceArchives.values()) {
                subArchive.close();
            }
        }
        return null;
    }

}
