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

package org.glassfish.appclient.server.connector;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.internal.deployment.GenericSniffer;
import org.jvnet.hk2.annotations.Service;

@Service(name = "AppClient")
@Singleton
public class AppClientSniffer extends GenericSniffer {
    private static final String[] stigmas = {
        "META-INF/application-client.xml", "META-INF/sun-application-client.xml", "META-INF/glassfish-application-client.xml"
    };

    private static final String[] containers = {"appclient"};

    @Inject CarType carType;

    public AppClientSniffer() {
        this(containers[0], stigmas[0], null);
    }

    public AppClientSniffer(String containerName, String appStigma, String urlPattern) {
        super(containerName, appStigma, urlPattern);
    }

    @Override
    public String[] getContainersNames() {
        return containers;
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
        for (String s : stigmas) {
            try {
                if (location.exists(s)) {
                    return true;
                }
            } catch (IOException ignore) {
            }
        }

        try {
            Manifest manifest = location.getManifest();
            if (manifest != null &&
                manifest.getMainAttributes().containsKey(Attributes.Name.MAIN_CLASS)) {
                return true;
            }
        } catch (IOException ignore) {
        }
        return false;
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
     * @return empty array
     */
    @Override
    public String[] getIncompatibleSnifferTypes() {
        return new String[] {};
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
        if (archiveType.equals(carType)) {
            return true;
        }
        return false;
    }

    private static final List<String> deploymentConfigurationPaths =
            initDeploymentConfigurationPaths();

    private static List<String> initDeploymentConfigurationPaths() {
        final List<String> result = new ArrayList<>();
        result.add("META-INF/application-client.xml");
        result.add("META-INF/sun-application-client.xml");
        result.add("META-INF/glassfish-application-client.xml");
        result.add("META-INF/weblogic-application-client.xml");
        return result;
    }

    /**
     * Returns the descriptor paths that might exist in an appclient app.
     *
     * @return list of the deployment descriptor paths
     */
    @Override
    protected List<String> getDeploymentConfigurationPaths() {
        return deploymentConfigurationPaths;
    }
}
