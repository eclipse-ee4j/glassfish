/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.connector.module;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.deployment.GenericSniffer;
import com.sun.enterprise.module.HK2Module;
import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.deployment.common.DeploymentUtils;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.lang.annotation.Annotation;

import jakarta.inject.Inject;

/**
 * Sniffer for detecting resource-adapter modules
 *
 * @author Jagadish Ramu
 */
@Service(name = ConnectorConstants.CONNECTOR_MODULE)
@Singleton
public class ConnectorSniffer extends GenericSniffer {

    @Inject
    private Logger logger;

    @Inject RarType rarType;
    @Inject ServiceLocator locator;

    private static final Class[]  connectorAnnotations = new Class[] {
            jakarta.resource.spi.Connector.class };

    public ConnectorSniffer() {
        super(ConnectorConstants.CONNECTOR_MODULE, "META-INF/ra.xml", null);
    }

    final String[] containerNames = {"com.sun.enterprise.connectors.module.ConnectorContainer"};

    /**
     * Sets up the container libraries so that any imported bundle from the
     * connector jar file will now be known to the module subsystem
     *
     * @param containerHome is where the container implementation resides
     * @param logger        the logger to use
     * @throws java.io.IOException exception if something goes sour
     */
    @Override
    public HK2Module[] setup(String containerHome, Logger logger) throws IOException {
        // do nothing, we are embedded in GFv3 for now
        return null;
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
     * Returns the HK2Module type
     *
     * @return the container name
     */
    public String getModuleType() {
        return ConnectorConstants.CONNECTOR_MODULE;
    }

    /**
     * Returns the list of annotations types that this sniffer is interested in.
     * If an application bundle contains at least one class annotated with
     * one of the returned annotations, the deployment process will not
     * call the handles method but will invoke the containers deployers as if
     * the handles method had been called and returned true.
     *
     * @return list of annotations this sniffer is interested in or an empty array
     */
    @Override
    public Class<? extends Annotation>[] getAnnotationTypes() {
        return connectorAnnotations;
    }

    /**
     * @return whether this sniffer should be visible to user
     *
     */
    public boolean isUserVisible() {
        return true;
    }

    /**
     * @return whether this sniffer represents a Java EE container type
     *
     */
    public boolean isJavaEE() {
        return true;
    }

    /**
     * @return the set of the sniffers that should not co-exist for the
     * same module. For example, ejb and appclient sniffers should not
     * be returned in the sniffer list for a certain module.
     * This method will be used to validate and filter the retrieved sniffer
     * lists for a certain module
     *
     */
    public String[] getIncompatibleSnifferTypes() {
        return new String[] {"ejb", "web"};
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
        return DeploymentUtils.isArchiveOfType(context.getSource(), rarType, context, locator);
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
        if (archiveType.equals(rarType)) {
            return true;
        }
        return false;
    }

    private static final List<String> deploymentConfigurationPaths =
            initDeploymentConfigurationPaths();

    private static List<String> initDeploymentConfigurationPaths() {
        final List<String> result = new ArrayList<String>();
        result.add("META-INF/ra.xml");
        result.add("META-INF/sun-ra.xml");
        result.add("META-INF/weblogic-ra.xml");
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
