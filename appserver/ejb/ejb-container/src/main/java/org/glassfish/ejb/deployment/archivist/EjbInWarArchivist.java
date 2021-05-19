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

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.List;
import java.util.Collection;
import java.io.IOException;

import com.sun.ejb.containers.EjbContainerUtil;
import com.sun.enterprise.deployment.annotation.impl.ModuleScanner;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ExtensionsArchivist;
import com.sun.enterprise.deployment.archivist.ExtensionsArchivistFor;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.BundleDescriptor;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.ejb.deployment.annotation.impl.EjbInWarScanner;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.io.EjbDeploymentDescriptorFile;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;

import org.jvnet.hk2.annotations.Service;

/**
 * @author Mahesh Kannan
 */

@Service
@PerLookup
@ExtensionsArchivistFor("ejb")
public class EjbInWarArchivist extends ExtensionsArchivist {

    @Inject
    ServiceLocator serviceLocator;

    @Inject
    Provider<EjbInWarScanner> scanner;

    /**
     * @return the DeploymentDescriptorFile responsible for handling
     *         standard deployment descriptor
     */
    @Override
    public DeploymentDescriptorFile getStandardDDFile(RootDeploymentDescriptor descriptor) {
        if (standardDD == null) {
            standardDD = new EjbDeploymentDescriptorFile();
        }
        return standardDD;
    }

    /**
     * @return the list of the DeploymentDescriptorFile responsible for
     *         handling the configuration deployment descriptors
     */
    public List<ConfigurationDeploymentDescriptorFile> getConfigurationDDFiles(RootDeploymentDescriptor descriptor) {
        if (confDDFiles == null) {
            confDDFiles = DOLUtils.getConfigurationDeploymentDescriptorFiles(serviceLocator,
                    EjbContainerUtil.EJB_CONTAINER_NAME);
        }
        return confDDFiles;
    }

    /**
     * Returns the scanner for this archivist, usually it is the scanner registered
     * with the same module type as this archivist, but subclasses can return a
     * different version
     *
     */
    @Override
    public ModuleScanner getScanner() {
        return scanner.get();
    }

    @Override
    public boolean supportsModuleType(ArchiveType moduleType) {
        return moduleType != null && moduleType.equals(DOLUtils.warType());
    }

    @Override
    public RootDeploymentDescriptor getDefaultDescriptor() {
        return new EjbBundleDescriptorImpl();
    }

    /**
     * writes the deployment descriptors (standard and runtime)
     * to a JarFile using the right deployment descriptor path
     *
     * @param in the input archive
     * @param out the abstract archive file to write to
     */
    @Override
    public void writeDeploymentDescriptors(Archivist main, BundleDescriptor descriptor, ReadableArchive in, WritableArchive out) throws IOException {
        Collection<EjbBundleDescriptorImpl> ejbExtensions =
            descriptor.getExtensionsDescriptors(EjbBundleDescriptorImpl.class);

        for (EjbBundleDescriptorImpl ejbBundle : ejbExtensions) {
            super.writeDeploymentDescriptors(main, ejbBundle, in, out);
        }
    }
}

