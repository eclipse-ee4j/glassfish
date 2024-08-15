/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.javaee.full.deployment;

import com.sun.enterprise.deployment.EarType;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.deployment.GenericSniffer;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.deployment.common.DeploymentUtils.isArchiveOfType;

/**
 * Ear sniffers snifs ear files.
 *
 * @author Jerome Dochez
 */
@Service(name = "ear")
public class EarSniffer extends GenericSniffer {

    @Inject
    EarType earType;

    @Inject
    ServiceLocator locator;

    public EarSniffer() {
        super("ear", "META-INF/application.xml", null);
    }

    @Override
    public String[] getContainersNames() {
        return new String[] { "org.glassfish.javaee.full.deployment.EarContainer" };
    }

    /**
     * Returns true if the passed file or directory is recognized by this composite sniffer.
     *
     * @param context deployment context
     * @return true if the location is recognized by this sniffer
     */
    @Override
    public boolean handles(DeploymentContext context) {
        ArchiveType archiveType = habitat.getService(ArchiveType.class, context.getArchiveHandler().getArchiveType());
        if (archiveType != null && !supportsArchiveType(archiveType)) {
            return false;
        }

        return isArchiveOfType(context.getSource(), earType, context, locator);
    }

    /**
     * Returns true if the passed file or directory is recognized by this instance.
     *
     * @param location the file or directory to explore
     * @return true if this sniffer handles this application type
     */
    @Override
    public boolean handles(ReadableArchive location) {
        return DeploymentUtils.isArchiveOfType(location, earType, locator);
    }

    @Override
    public boolean isUserVisible() {
        return true;
    }

    @Override
    public boolean isJakartaEE() {
        return true;
    }

    /**
     * This API is used to help determine if the sniffer should recognize the current archive. If the sniffer does not
     * support the archive type associated with the current deployment, the sniffer should not recognize the archive.
     *
     * @param archiveType the archive type to check
     * @return whether the sniffer supports the archive type
     */
    @Override
    public boolean supportsArchiveType(ArchiveType archiveType) {
        if (archiveType.equals(earType)) {
            return true;
        }
        return false;
    }

    private static final List<String> deploymentConfigurationPaths = initDeploymentConfigurationPaths();

    private static List<String> initDeploymentConfigurationPaths() {
        final List<String> result = new ArrayList<>();
        result.add("META-INF/application.xml");
        result.add("META-INF/sun-application.xml");
        result.add("META-INF/glassfish-application.xml");
        result.add("META-INF/weblogic-application.xml");
        return result;
    }

    /**
     * Returns the descriptor paths that might exist at the root of the ear.
     *
     * @return list of the deployment descriptor paths
     */
    @Override
    protected List<String> getDeploymentConfigurationPaths() {
        return deploymentConfigurationPaths;
    }

}
