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

package com.sun.enterprise.connectors.deployment.util;

import com.sun.enterprise.connectors.connector.module.RarType;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.annotation.introspection.ResourceAdapterAnnotationScanner;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFor;
import com.sun.enterprise.deployment.deploy.shared.InputJarArchive;
import com.sun.enterprise.deployment.util.*;
import com.sun.enterprise.deployment.io.ConnectorDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.runtime.ConnectorRuntimeDDFile;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.ArchiveType;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import jakarta.inject.Inject;

/**
 * This class is responsible for handling J2EE Connector archive files.
 *
 * @author Sheetal Vartak
 * @version
 */
@Service
@PerLookup
@ArchivistFor(RarType.ARCHIVE_TYPE)
public class ConnectorArchivist extends Archivist<ConnectorDescriptor> {

    @Inject
    private ConnectorVisitor connectorValidator;

    @Inject
    private RarType rarType;

    /**
     * @return the  module type handled by this archivist
     * as defined in the application DTD
     *
     */
    @Override
    public ArchiveType getModuleType() {
        return rarType;
    }



    /**
     * @return the DeploymentDescriptorFile responsible for handling
     * standard deployment descriptor
     */
    @Override
    public DeploymentDescriptorFile getStandardDDFile() {
        if (standardDD == null) {
            standardDD = new ConnectorDeploymentDescriptorFile();
        }
        return standardDD;
    }

    /**
     * @return the list of the DeploymentDescriptorFile responsible for
     *         handling the configuration deployment descriptors
     */
    public List<ConfigurationDeploymentDescriptorFile> getConfigurationDDFiles() {
        if (confDDFiles == null) {
            confDDFiles = new ArrayList<ConfigurationDeploymentDescriptorFile>();
            confDDFiles.add(new ConnectorRuntimeDDFile());
        }
        return confDDFiles;
    }

    /**
     * @return a default BundleDescriptor for this archivist
     */
    @Override
    public ConnectorDescriptor getDefaultBundleDescriptor() {
        return new ConnectorDescriptor();
    }

    /**
     * @return true if the archivist is handling the provided archive
     */
    @Override
    protected boolean postHandles(ReadableArchive abstractArchive)
            throws IOException {
        /*
         * Connectors can be defined via annotations only within RARs. The
         * Archivist's handles method will invoke this postHandles only if
         * the archive is a directory; if it were truly a JAR file then it
         * would have a file type of .rar, the ArchivistFactory.getArchivist
         * method would already have chosen to provide a ConnectorArchivist.
         * So the fact that control reaches here means either that this is
         * a directory or it's a non-.rar archive file.  So we need to run the anno
         * detector only if the archive is a directory.
         *
         * (This is particularly helpful in the case of an app client being
         * run in the ACC, because the archive is a JAR file, not an
         * expanded directory.  We know that no connectors will be defined
         * in a file with type .jar so there's no need to scan the archive's
         * classes looking for the relevant annos.)
         */
        if (abstractArchive instanceof InputJarArchive) {
            return false;
        }
        ConnectorAnnotationDetector detector =
                    new ConnectorAnnotationDetector(new ResourceAdapterAnnotationScanner());
        return detector.hasAnnotationInArchive(abstractArchive);

    }

    @Override
    protected String getArchiveExtension() {
        return CONNECTOR_EXTENSION;
    }

    /**
     * perform any post deployment descriptor reading action
     *
     * @param descriptor the deployment descriptor for the module
     * @param archive the module archive
     */
    @Override
    protected void postOpen(ConnectorDescriptor descriptor, ReadableArchive archive)
        throws IOException {
        super.postOpen(descriptor, archive);
        descriptor.visit(connectorValidator);
    }


    /**
     * validates the DOL Objects associated with this archivist, usually
     * it requires that a class loader being set on this archivist or passed
     * as a parameter
     */
    @Override
    public void validate(ClassLoader aClassLoader) {
        ClassLoader cl = aClassLoader;
        if (cl==null) {
            cl = classLoader;
        }
        if (cl==null) {
            return;
        }
        descriptor.setClassLoader(cl);
        descriptor.visit(connectorValidator);
    }

}
