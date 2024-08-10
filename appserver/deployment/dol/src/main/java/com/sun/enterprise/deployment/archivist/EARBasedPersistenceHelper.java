/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.archivist.PersistenceArchivist.SubArchivePURootScanner;

import java.util.Map;
import java.util.Set;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.ModuleDescriptor;

/**
 * Common logic supporting persistence archivists that deal with EARs.
 *
 * @author tjquinn
 */
public class EARBasedPersistenceHelper {

    /**
     * @return true if the jarName corresponds to component jar (like a war or ejb.jar) in an .ear false otherwise
     */
    public static boolean isComponentJar(String jarName, Set<ModuleDescriptor<BundleDescriptor>> moduleDescriptors) {
        boolean isComponentJar = false;
        for (ModuleDescriptor md : moduleDescriptors) {
            String archiveUri = md.getArchiveUri();
            if (jarName.equals(archiveUri)) {
                isComponentJar = true;
                break;
            }
        }
        return isComponentJar;
    }

    /**
     * Adds candidate persistence archives from the EAR's library directory
     * and, if selected, from the top-level.
     * @param earArchive ReadableArchive for the EAR
     * @param app application's descriptor
     * @param includeTopLevel whether or not to include top-level JARs for scanning
     * @param probablePersistentArchives map to which new candidates will be added
     */
    protected static void addLibraryAndTopLevelCandidates(final ReadableArchive earArchive,
            final Application app,
            final boolean includeTopLevel,
            final Map<String,ReadableArchive> probablePersistentArchives) {
        //Get probable archives from root of the ear
        if (includeTopLevel) {
            SubArchivePURootScanner earRootScanner = new EARTopLevelJARPURootScanner(app);
            probablePersistentArchives.putAll(
                    PersistenceArchivist.getProbablePersistenceRoots(earArchive, earRootScanner));
        }

        //Geather all jars in lib of ear
        SubArchivePURootScanner libPURootScannerScanner = new EARLibraryPURootScanner(app);
        probablePersistentArchives.putAll(
                PersistenceArchivist.getProbablePersistenceRoots(earArchive, libPURootScannerScanner));

    }


    /**
     * Allows scanning of library JARs in an EAR.
     * <p>
     * This implementation correctly handles the semantics of the
     * <library-directory> element (and its absence) from the descriptor.
     * That is, if the element is missing use the default value "/lib."  If
     * the element is present and non-null, use it.  If the element is
     * present and empty then no library directory exists in the application.
     */
    static class EARLibraryPURootScanner extends SubArchivePURootScanner {

        private final Application app;

        /**
         * Creates a new instance of the scanner, using the specified
         * Application descriptor.
         *
         * @param app descriptor for the application
         */
        protected EARLibraryPURootScanner(
                final Application app) {
            this.app = app;
        }

        @Override
        String getPathOfSubArchiveToScan() {
            /*
             * Take advantage of the fact that the app's getLibraryDirectory
             * method handles all the semantics of the <library-directory>
             * element.
             */
            return app.getLibraryDirectory();
        }

    }

    /**
     * Allows scanning of the top-level JARs of an EAR.
     */
    static class EARTopLevelJARPURootScanner extends SubArchivePURootScanner {

        private final Application app;

        protected EARTopLevelJARPURootScanner(final Application app) {
            this.app = app;
        }

        @Override
        public String getPathOfSubArchiveToScan() {
            // We are scanning root of ear.
            return "";
        }

        @Override
        public boolean isProbablePuRootJar(String jarName) {
            return super.isProbablePuRootJar(jarName) &&
                    // component roots are not scanned while scanning ear. They will be handled
                    // while scanning the component.
                    !isComponentJar(jarName,(app.getModules()));
        }
    }
}
