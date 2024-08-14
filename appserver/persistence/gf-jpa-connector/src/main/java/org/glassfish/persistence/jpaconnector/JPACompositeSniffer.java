/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.persistence.jpaconnector;


import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.archivist.EARBasedPersistenceHelper;
import com.sun.enterprise.deployment.archivist.PersistenceArchivist;

import jakarta.inject.Singleton;

import java.util.Enumeration;
import java.util.Set;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.javaee.core.deployment.ApplicationHolder;
import org.jvnet.hk2.annotations.Service;

/**
 * Sniffer handling ears
 *
 * @author Mitesh Meswani
 */
@Service(name = "jpaCompositeSniffer")
@Singleton
public class JPACompositeSniffer extends JPASniffer {

    /**
     * Decides whether we have any pu roots at ear level
     */
    public boolean handles(DeploymentContext context) {
        ArchiveType archiveType = habitat.getService(ArchiveType.class, context.getArchiveHandler().getArchiveType());
        if (archiveType != null && !supportsArchiveType(archiveType)) {
            return false;
        }

        // Scans for pu roots in the "lib" dir of an application.
        // We do not scan for PU roots in root of .ear. JPA 2.0 spec will clarify that it is  not a portable use case.
        // It is not portable use case because JavaEE spec implies that jars in root of ears are not visible by default
        // to components (Unless an explicit Class-Path manifest entry is present) and can potentially be loaded by
        // different class loaders (corresponding to each component that refers to it) thus residing in different name
        // space. It does not make sense to make them visible at ear level (and thus in a single name space)
        boolean isJPAApplication = false;
        ApplicationHolder holder = context.getModuleMetaData(ApplicationHolder.class);
        ReadableArchive appRoot = context.getSource();
        if (holder != null && holder.app != null) {
            isJPAApplication = scanForPURootsInLibDir(appRoot, holder.app.getLibraryDirectory());

            if(!isJPAApplication) {
                if(DeploymentUtils.useV2Compatibility(context) ) {
                    //Scan for pu roots in root of ear
                    isJPAApplication = scanForPURRootsInEarRoot(context, holder.app.getModules());
                }
            }
        }
        return isJPAApplication;
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


    private boolean scanForPURRootsInEarRoot(DeploymentContext ctx, Set<ModuleDescriptor<BundleDescriptor>> modules) {
        boolean puPresentInEarRoot = false;
        Enumeration<String> entriesInEar = ctx.getSource().entries();
        while(entriesInEar.hasMoreElements() && !puPresentInEarRoot) {
            String entry = entriesInEar.nextElement();
            puPresentInEarRoot = PersistenceArchivist.isProbablePuRootJar(entry) && !EARBasedPersistenceHelper.isComponentJar(entry, modules);
        }
        return puPresentInEarRoot;
    }
}
