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

package org.glassfish.ejb.deployment.archive;

import org.glassfish.internal.deployment.GenericSniffer;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.ArchiveType;


import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.lang.annotation.Annotation;

/**
 * Implementation of the Sniffer for the Ejb container.
 *
 * @author Mahesh Kannan
 */
@Service(name="Ejb")
@Singleton
public class EjbSniffer  extends GenericSniffer {

    @Inject EjbType ejbType;

    private static final Class[]  ejbAnnotations = new Class[] {
            jakarta.ejb.Stateless.class, jakarta.ejb.Stateful.class,
            jakarta.ejb.MessageDriven.class, jakarta.ejb.Singleton.class };

    public EjbSniffer() {
        this("ejb", "META-INF/ejb-jar.xml", null);
    }

    public EjbSniffer(String containerName, String appStigma, String urlPattern) {
        super(containerName, appStigma, urlPattern);
    }

    final String[] containers = {
            "org.glassfish.ejb.startup.EjbContainerStarter",
    };

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
        boolean result = super.handles(location);    //Check ejb-jar.xml

        if (result == false) {
            try {
                result = location.exists("META-INF/sun-ejb-jar.xml") ||
                    location.exists("META-INF/glassfish-ejb-jar.xml");
            } catch (IOException ioe) {
                // Ignore
            }
        }

        if (result == false) {
            try {
                result = location.exists("WEB-INF/ejb-jar.xml");
            } catch (IOException ioEx) {
                //TODO
            }
        }

        return result;
    }

    @Override
    public Class<? extends Annotation>[] getAnnotationTypes() {
        return ejbAnnotations;
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
     * same module. For example, ejb and connector sniffers should not
     * be returned in the sniffer list for a certain module.
     * This method will be used to validate and filter the retrieved sniffer
     * lists for a certain module
     *
     */
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
    public boolean supportsArchiveType(ArchiveType archiveType) {
        if (archiveType.equals(ejbType) ||
            archiveType.toString().equals("war")) {
            return true;
        }
        return false;
    }

    private static final List<String> deploymentConfigurationPaths =
            initDeploymentConfigurationPaths();

    private static List<String> initDeploymentConfigurationPaths() {
        final List<String> result = new ArrayList<String>();
        result.add("META-INF/ejb-jar.xml");
        result.add("META-INF/sun-ejb-jar.xml");
        result.add("META-INF/glassfish-ejb-jar.xml");
        result.add("META-INF/weblogic-ejb-jar.xml");
        return result;
    }

    /**
     * Returns the descriptor paths that might exist at an ejb app.
     *
     * @return list of the deployment descriptor paths
     */
    @Override
    protected List<String> getDeploymentConfigurationPaths() {
        return deploymentConfigurationPaths;
    }

}
