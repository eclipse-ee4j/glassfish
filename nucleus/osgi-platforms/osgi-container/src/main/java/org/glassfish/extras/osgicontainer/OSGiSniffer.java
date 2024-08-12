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

package org.glassfish.extras.osgicontainer;

import jakarta.inject.Inject;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.internal.deployment.GenericSniffer;
import org.jvnet.hk2.annotations.Service;

/**
 * Sniffer for OSGi bundles
 *
 * @author Jerome Dochez, Sanjeeb Sahoo
 */

@Service(name = "osgi")
public class OSGiSniffer extends GenericSniffer  {

    /*
    It should extends from GenericCompositeSniffer, but I think there is some issues in deployment backend if we
    model it as a composite sniffer, so for now we treat osgi slightly differently.
     */

    @Inject
    private OSGiArchiveType osgiArchiveType;
    public static final String CONTAINER_NAME = "osgi";

    public OSGiSniffer() {
        super(CONTAINER_NAME, null, null);
    }

    @Override
    public boolean handles(ReadableArchive location) {
        // I always return false, unless --type is used to specifically request OSGi
        // bundle installation.
        return false;
    }

    public String[] getContainersNames() {
        return new String[]{CONTAINER_NAME};
    }

    @Override
    /**
     * @return whether this sniffer should be visible to user
     *
     */
    public boolean isUserVisible() {
        return true;
    }

    @Override
    public boolean handles(DeploymentContext context) {
        ArchiveType archiveType = habitat.getService(ArchiveType.class, context.getArchiveHandler().getArchiveType());
        return supportsArchiveType(archiveType);
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
        if (archiveType.toString().equals(osgiArchiveType.toString())) {
            return true;
        }
        return false;
    }
}
