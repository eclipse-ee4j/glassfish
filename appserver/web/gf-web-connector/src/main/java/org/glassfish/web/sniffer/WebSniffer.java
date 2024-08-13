/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.deployment.GenericSniffer;
import org.glassfish.web.WarType;
import org.jvnet.hk2.annotations.Service;



/**
 * Implementation of the Sniffer for the web container.
 *
 * @author Jerome Dochez
 */
@Service(name="web")
@Singleton
public class WebSniffer  extends GenericSniffer {

    @Inject WarType warType;
    @Inject ServiceLocator locator;

    public WebSniffer() {
        super("web", "WEB-INF/web.xml", null);
    }

    @Override
    public String[] getURLPatterns() {
        // anything finishing with jsp or jspx
        return new String[] { "*.jsp", "*.jspx" };
    }

    /**
     * Returns true if the passed file or directory is recognized by this
     * sniffer.
     * @param context deployment context
     * @return true if the location is recognized by this sniffer
     */
    @Override
    public boolean handles(DeploymentContext context) {
        ArchiveType archiveType = habitat.getService(ArchiveType.class, context.getArchiveHandler().getArchiveType());
        if (archiveType != null && !supportsArchiveType(archiveType)) {
            return false;
        }
        return DeploymentUtils.isArchiveOfType(context.getSource(), warType, context, locator);
    }

    /**
     * Returns true if the passed file or directory is recognized by this
     * instance.
     *
     * @param location the file or directory to explore
     * @return true if this sniffer handles this application type
     */
    @Override
    public boolean handles(ReadableArchive location) {
        return DeploymentUtils.isArchiveOfType(location, warType, locator);
    }

    private static final String[] containers = { "com.sun.enterprise.web.WebContainer" };

    @Override
    public String[] getContainersNames() {
        return containers;
    }

    @Override
    public boolean isUserVisible() {
        return true;
    }

    @Override
    public boolean isJakartaEE() {
        return true;
    }

    private static final List<String> deploymentConfigurationPaths =
            initDeploymentConfigurationPaths();

    private static List<String> initDeploymentConfigurationPaths() {
        final List<String> result = new ArrayList<>();
        result.add("WEB-INF/web.xml");
        result.add("WEB-INF/sun-web.xml");
        result.add("WEB-INF/glassfish-web.xml");
        result.add("WEB-INF/weblogic.xml");
        return result;
    }

    /**
     * Returns the web-oriented descriptor paths that might exist in a web
     * app.
     *
     * @return list of the deployment descriptor paths
     */
    @Override
    protected List<String> getDeploymentConfigurationPaths() {
        return deploymentConfigurationPaths;
    }

    /**
     * @return the set of the sniffers that should not co-exist for the
     * same module. For example, ejb and appclient sniffers should not
     * be returned in the sniffer list for a certain module.
     * This method will be used to validate and filter the retrieved sniffer
     * lists for a certain module
     *
     */
    @Override
    public String[] getIncompatibleSnifferTypes() {
        return new String[] {"connector"};
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
    @Override
    public boolean supportsArchiveType(ArchiveType archiveType) {
        if (archiveType.equals(warType)) {
            return true;
        }
        return false;
    }

    // TODO(Sahoo): Ideally we should have separate sniffer for JSP, but since WebSniffer is already
    // handling JSPs, we must make sure that all JSP related modules get installed by WebSniffer as well.
    private final String[] containerModuleNames = {"org.glassfish.main.web.glue",
            "org.glassfish.wasp.wasp"
    };

    @Override
    protected String[] getContainerModuleNames() {
        return containerModuleNames;
    }

}
