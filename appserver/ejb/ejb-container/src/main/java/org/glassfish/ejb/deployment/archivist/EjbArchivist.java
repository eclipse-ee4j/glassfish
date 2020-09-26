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

package org.glassfish.ejb.deployment.archivist;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import jakarta.inject.Inject;

import com.sun.ejb.containers.EjbContainerUtil;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.annotation.introspection.EjbComponentAnnotationScanner;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFor;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.util.AnnotationDetector;
import com.sun.enterprise.deployment.util.DOLUtils;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.ejb.deployment.archive.EjbType;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.io.EjbDeploymentDescriptorFile;
import org.glassfish.ejb.deployment.util.EjbBundleValidator;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

/**
 * This class is responsible for handling J2EE EJB Bundlearchive files.
 *
 * @author  Jerome Dochez
 */
@Service
@PerLookup
@ArchivistFor(EjbType.ARCHIVE_TYPE)
public class EjbArchivist extends Archivist<EjbBundleDescriptorImpl> {

    @Inject
    private EjbType ejbType;

    /**
     * @return the  module type handled by this archivist
     * as defined in the application DTD
     *
     */
    @Override
    public ArchiveType getModuleType() {
        return ejbType;
    }

    /**
     * Set the DOL descriptor  for this Archivist, used by super classes
     */
    public void setDescriptor(Application descriptor) {
        // this is acceptable if the application actually represents
        // a standalone module
        Set<EjbBundleDescriptorImpl> ejbBundles = descriptor.getBundleDescriptors(EjbBundleDescriptorImpl.class);
        if (ejbBundles.size()>0) {
            this.descriptor = ejbBundles.iterator().next();
            if (this.descriptor.getModuleDescriptor().isStandalone())
                return;
            else
                this.descriptor=null;
        }
    }

    /**
     * @return the DeploymentDescriptorFile responsible for handling
     * standard deployment descriptor
     */
    @Override
    public DeploymentDescriptorFile getStandardDDFile() {
        if (standardDD == null) {
            standardDD = new EjbDeploymentDescriptorFile();
        }
        return standardDD;
    }

    /**
     * @return the list of the DeploymentDescriptorFile responsible for
     *         handling the configuration deployment descriptors
     */
    public List<ConfigurationDeploymentDescriptorFile> getConfigurationDDFiles() {
        if (confDDFiles == null) {
            confDDFiles = DOLUtils.getConfigurationDeploymentDescriptorFiles(habitat, EjbContainerUtil.EJB_CONTAINER_NAME);
        }
        return confDDFiles;
    }

    /**
     * @return a default BundleDescriptor for this archivist
     */
    @Override
    public EjbBundleDescriptorImpl getDefaultBundleDescriptor() {
        return new EjbBundleDescriptorImpl();
    }

    /**
     * perform any post deployment descriptor reading action
     *
     * @param descriptor deployment descriptor for the module
     * @param archive the module archive
     */
    @Override
    protected void postOpen(EjbBundleDescriptorImpl descriptor, ReadableArchive archive)
        throws IOException
    {
        super.postOpen(descriptor, archive);
        postValidate(descriptor, archive);
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
        descriptor.visit(new EjbBundleValidator());
    }

    @Override
    protected String getArchiveExtension() {
        return EJB_EXTENSION;
    }

    @Override
    protected boolean postHandles(ReadableArchive abstractArchive)
            throws IOException {
        AnnotationDetector detector =
                    new AnnotationDetector(new EjbComponentAnnotationScanner());
        return (!DeploymentUtils.isArchiveOfType(abstractArchive, DOLUtils.warType(), locator)) &&
                detector.hasAnnotationInArchiveWithNoScanning(abstractArchive);
    }
}
