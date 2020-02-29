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

package com.sun.enterprise.security.ee;

import com.sun.enterprise.security.SecurityLifecycle;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.deployment.common.DeploymentUtils;
import com.sun.enterprise.deployment.util.DOLUtils;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.deployment.GenericSniffer;
import org.jvnet.hk2.annotations.Service;

import javax.enterprise.deploy.shared.ModuleType;

import java.util.logging.Logger;
import java.io.IOException;

import com.sun.enterprise.module.HK2Module;
import java.lang.annotation.Annotation;

import javax.inject.Inject;

/**
 * SecuritySniffer for security related activities
 */
@Service(name="Security")
public class SecuritySniffer extends GenericSniffer {

    final String[] containers = { "com.sun.enterprise.security.ee.SecurityContainer" };

    @Inject
    private ServiceLocator habitat;

    private ServiceHandle<SecurityLifecycle> lifecycle;
    
    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation>[] ejbAnnotations = new Class[]{
        javax.ejb.Stateless.class, javax.ejb.Stateful.class,
        javax.ejb.MessageDriven.class, javax.ejb.Singleton.class
    };

    public SecuritySniffer() {
        super("security", "WEB-INF/web.xml", null);
    }

    /**
     * Returns true if the passed file or directory is recognized by this
     * sniffer.
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
        if (archiveType != null && (archiveType.equals(DOLUtils.warType()) || archiveType.equals(DOLUtils.earType()) || archiveType.equals(DOLUtils.ejbType())))
            return true;
        return handles(context.getSource());
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
        return (DeploymentUtils.isArchiveOfType(location, DOLUtils.warType(), habitat) || DeploymentUtils.isArchiveOfType(location, DOLUtils.earType(), habitat) || isJar(location));
    }

    /**
     * Sets up the container libraries so that any imported bundle from the
     * connector jar file will now be known to the module subsystem
     * <p/>
     * This method returns a {@link com.sun.enterprise.module.ModuleDefinition} for the module containing
     * the core implementation of the container. That means that this module
     * will be locked as long as there is at least one module loaded in the
     * associated container.
     *
     * @param containerHome is where the container implementation resides
     * @param logger        the logger to use
     * @return the module definition of the core container implementation.
     * @throws java.io.IOException exception if something goes sour
     */
    @Override
     public HK2Module[] setup(String containerHome, Logger logger) throws IOException {
        lifecycle = habitat.getServiceHandle(SecurityLifecycle.class);
        lifecycle.getService();
        return null;
    }

    /**
     * Tears down a container, remove all imported libraries from the module
     * subsystem.
     */
    @Override
     public void tearDown() {
        if (lifecycle!=null) {
            lifecycle.destroy();
        }
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
        return containers;
    }

    @Override
    public Class<? extends Annotation>[] getAnnotationTypes() {
        return ejbAnnotations;
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
        if (archiveType.toString().equals(ModuleType.WAR.toString()) ||
            archiveType.toString().equals(ModuleType.EJB.toString())) {
            return true;
        }
        return false;
    }

    private boolean isJar(ReadableArchive location) {
        // check for ejb-jar.xml
        boolean result = false;
        try {
                result = location.exists("META-INF/ejb-jar.xml");
            } catch (IOException ioEx) {
                //TODO
            }
        return result;
    }
     
}
