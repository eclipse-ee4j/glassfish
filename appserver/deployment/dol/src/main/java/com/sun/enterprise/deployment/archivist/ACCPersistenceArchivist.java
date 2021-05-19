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

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.deploy.shared.MultiReadableArchive;
import org.glassfish.api.deployment.archive.ArchiveType;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;
import org.xml.sax.SAXException;

/**
 * PersistenceArchivist for app clients that knows how to scan for PUs in
 * the app client itself as well as in library JARs (or top-level JARs from
 * the containing EAR) that might accompany the app client.
 *
 */
@Service
@ExtensionsArchivistFor("jpa")
public class ACCPersistenceArchivist extends PersistenceArchivist {



    @Inject
    private ProcessEnvironment env;

    @Inject
    private ArchiveFactory archiveFactory;

    @Override
    public boolean supportsModuleType(ArchiveType moduleType) {
        return (moduleType != null && moduleType.equals(DOLUtils.carType())) && (env.getProcessType() == ProcessType.ACC) ;
    }

    @Override
    public Object open(Archivist main, ReadableArchive archive, RootDeploymentDescriptor descriptor) throws IOException, SAXException {
        if(deplLogger.isLoggable(Level.FINE)) {
            deplLogger.logp(Level.FINE, "ACCPersistencerArchivist",
                    "readPersistenceDeploymentDescriptors", "archive = {0}",
                    archive.getURI());
        }

        final Map<String,ReadableArchive> candidatePersistenceArchives =
                new HashMap<String,ReadableArchive>();

        /*
         * The descriptor had better be an ApplicationClientDescriptor!
         */
        if ( ! (descriptor instanceof ApplicationClientDescriptor)) {
            return null;
        }

        final ApplicationClientDescriptor acDescr = ApplicationClientDescriptor.class.cast(descriptor);

        try {
            final Manifest mf = archive.getManifest();
            final Attributes mainAttrs = mf.getMainAttributes();
            /*
             * We must scan the app client archive itself.
             */
            URI clientURI = clientURI(archive, acDescr);
            candidatePersistenceArchives.put(clientURI.toASCIIString(), archive);

            /*
             * If this app client
             * was deployed as part of an EAR then scan any library JARs and, if the
             * client was also deployed or launched in v2-compatibility mode, any
             * top-level JARs in the EAR.
             *
             * Exactly how we do this depends on whether this is a deployed client
             * (which will reside in a client download directory) or a non-deployed
             * one (which will reside either as a stand-alone client or within an
             * EAR).
             */
            if (isDeployed(mainAttrs)) {
                if ( ! isDeployedClientAlsoStandAlone(mainAttrs)) {
                    addOtherDeployedScanTargets(archive, mainAttrs, candidatePersistenceArchives);
                }
            } else if ( ! isStandAlone(acDescr)) {
                addOtherNondeployedScanTargets(archive, acDescr, candidatePersistenceArchives);
            }

            for (Map.Entry<String, ReadableArchive> pathToArchiveEntry : candidatePersistenceArchives.entrySet()) {
                readPersistenceDeploymentDescriptor(main,
                        pathToArchiveEntry.getValue(),
                        pathToArchiveEntry.getKey(),
                        descriptor);
            }
        } finally {
            for (Map.Entry<String, ReadableArchive> pathToArchiveEntry : candidatePersistenceArchives.entrySet()) {
                //pathToArchiveEntry.getValue().close();
            }
        }
        return null;
    }


    private boolean isDeployedClientAlsoStandAlone(final Attributes mainAttrs) {
        final String relativePathToGroupFacade = mainAttrs.getValue(AppClientArchivist.GLASSFISH_GROUP_FACADE);
        return relativePathToGroupFacade == null;
    }

    private URI clientURI(final ReadableArchive archive,
            final ApplicationClientDescriptor acDesc) throws IOException {
        if (archive instanceof MultiReadableArchive) {
            /*
             * Getting the manifest from a MultiReadableArchive returns the
             * manifest from the facade.
             */
            final Manifest facadeMF = archive.getManifest();
            final Attributes facadeMainAttrs = facadeMF.getMainAttributes();
            final URI clientRelativeURI = URI.create(
                facadeMainAttrs.getValue(AppClientArchivist.GLASSFISH_APPCLIENT));
            if (isDeployedClientAlsoStandAlone(facadeMainAttrs)) {
                return clientRelativeURI;
            }
            /*
             * We need the relative URI to the developer's client JAR within
             * the download directory.
             */
            final URI absURIToClient = ((MultiReadableArchive) archive).getURI(1);
            final String relativeURIPathToAnchorDir =
                    facadeMainAttrs.getValue(AppClientArchivist.GLASSFISH_ANCHOR_DIR);
            final URI absURIToAnchorDir = archive.getURI().resolve(relativeURIPathToAnchorDir);
            return absURIToAnchorDir.relativize(absURIToClient);
        }

        return archive.getURI();
    }

    private boolean isStandAlone(final ApplicationClientDescriptor ac) {
        /*
         * For a non-deployed app (this case), the descriptor for a stand-alone
         * app client has a null application value.
         */
        return (ac.getApplication() == null || ac.isStandalone());
    }

    private boolean isDeployed(final Attributes mainAttrs) throws IOException {
        final String gfClient = mainAttrs.getValue(AppClientArchivist.GLASSFISH_APPCLIENT);
        return gfClient != null;
    }

    private void addOtherDeployedScanTargets(
            final ReadableArchive archive,
            final Attributes mainAttrs,
            Map<String,ReadableArchive> candidates) throws IOException {

        final String otherPUScanTargets = mainAttrs.getValue(
                AppClientArchivist.GLASSFISH_CLIENT_PU_SCAN_TARGETS_NAME);

        /*
         * Include library JARs - listed in the facade's Class-Path - and
         * any additional (typically top-level) JARs to be scanned.
         */

        addScanTargetsFromURIList(archive, otherPUScanTargets, candidates);
    }

    private void addOtherNondeployedScanTargets(final ReadableArchive clientArchive,
            final ApplicationClientDescriptor acDescr,
            final Map<String,ReadableArchive> candidates) {

        /*
         * The archive is a non-deployed one.  We know from an earlier check
         * that this is not a stand-alone app client, so we can use the
         * app client archive's parent archive to get to the containing EAR for
         * use in a subarchive scanner.
         */
        final ReadableArchive earArchive = clientArchive.getParentArchive();

        EARBasedPersistenceHelper.addLibraryAndTopLevelCandidates(earArchive,
                acDescr.getApplication(),
                true,
                candidates);



    }

    private void addScanTargetsFromURIList(final ReadableArchive archive,
            final String relativeURIList,
            final Map<String,ReadableArchive> candidates) throws IOException {
        if (relativeURIList == null || relativeURIList.isEmpty()) {
            return;
        }
        final String[] relativeURIs = relativeURIList.split(" ");
        for (String uriText : relativeURIs) {
            final URI scanTargetURI = archive.getURI().resolve(uriText);
            candidates.put(uriText, archiveFactory.openArchive(scanTargetURI));
        }
    }

    private class AppClientPURootScanner extends SubArchivePURootScanner {

        private final ReadableArchive clientArchive;

        private AppClientPURootScanner(final ReadableArchive clientArchive) {
            this.clientArchive = clientArchive;
        }

        @Override
        ReadableArchive getSubArchiveToScan(ReadableArchive parentArchive) {
            return clientArchive;
        }

        /**
         * The superclass requires this implementation, but it is never used
         * because we also override getSubArchiveToScan.
         *
         * @return
         */
        @Override
        String getPathOfSubArchiveToScan() {
            return "";
        }
    }
}
