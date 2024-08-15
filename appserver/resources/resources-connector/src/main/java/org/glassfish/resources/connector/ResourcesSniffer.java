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

package org.glassfish.resources.connector;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.deployment.GenericSniffer;
import org.glassfish.resourcebase.resources.api.ResourceConstants;
import org.glassfish.resources.util.ResourceUtil;
import org.jvnet.hk2.annotations.Service;

/**
 * Sniffer to detect glassfish-resources.xml in standalone archives
 * @author Jagadish Ramu
 */
@Service(name = ResourceConstants.GF_RESOURCES_MODULE)
public class ResourcesSniffer extends GenericSniffer {
//TODO ASR package name change ?

    final String[] containerNames = {"org.glassfish.resources.module.ResourcesContainer"};
    @Inject ServiceLocator locator;

    public ResourcesSniffer() {
        super(ResourceConstants.GF_RESOURCES_MODULE, ResourceConstants.GF_RESOURCES_LOCATION, null);
    }

    /**
     * Returns true if the passed file or directory is recognized by this
     * instance.
     *
     * @param archive the file or directory to explore
     * @return true if this sniffer handles this application type
     */
    public boolean handles(ReadableArchive archive) {
        return ResourceUtil.hasResourcesXML(archive, locator)
                && archive.getParentArchive() == null;
    }

    /**
     * Returns the list of Containers that this Sniffer enables.
     * <p/>
     * The runtime will look up each container implementing
     * using the names provided in the habitat.
     *
     * @return list of container names known to the habitat for this sniffer
     */
    public String[] getContainersNames() {
        return containerNames;
    }

    /**
     * Returns the Module type
     *
     * @return the container name
     */
    public String getModuleType() {
        return ResourceConstants.GF_RESOURCES_MODULE;
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
        if (archiveType.toString().equals("war") ||
            archiveType.toString().equals("ejb") ||
            archiveType.toString().equals("rar") ||
            archiveType.toString().equals("car")) {
            return true;
        }
        return false;
    }

    private static final List<String> deploymentConfigurationPaths =
            initDeploymentConfigurationPaths();

    private static List<String> initDeploymentConfigurationPaths() {
        final List<String> result = new ArrayList<String>();
        result.add(ResourceConstants.GF_RESOURCES_LOCATION);
        return result;
    }

    /**
     * Returns the descriptor paths that might exist in a connector app.
     *
     * @return list of the deployment descriptor paths
     */
    @Override
    protected List<String> getDeploymentConfigurationPaths() {
        return deploymentConfigurationPaths;
    }
}
