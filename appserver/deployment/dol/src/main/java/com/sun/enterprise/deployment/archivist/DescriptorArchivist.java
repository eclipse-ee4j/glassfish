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

package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * This class is responsible for writing deployment descriptors
 * after a deployment action has occured to a abstract archive instance.
 *
 * @author  Jerome Dochez
 */
@Service
public class DescriptorArchivist {
    @Inject
    protected ArchivistFactory archivistFactory;

    @Inject
    private  Provider<ApplicationArchivist> archivistProvider;

    /**
     * writes an application deployment descriptors
     * @param the application object
     * @param the abstract archive
     */
    public void write(Application application, ReadableArchive in,
        WritableArchive out) throws IOException {
        if (application.isVirtual()) {
            ModuleDescriptor aModule = (ModuleDescriptor) application.getModules().iterator().next();
            Archivist moduleArchivist = archivistFactory.getArchivist(aModule.getModuleType());
            write((BundleDescriptor)aModule.getDescriptor(), moduleArchivist, in, out);
        } else {
            // this is a real application.
            // let's start by writing out all submodules deployment descriptors
            for (ModuleDescriptor aModule : application.getModules()) {
                Archivist moduleArchivist = archivistFactory.getArchivist(aModule.getModuleType());
                WritableArchive moduleArchive = out.createSubArchive(aModule.getArchiveUri());
                ReadableArchive moduleArchive2 = in.getSubArchive(aModule.getArchiveUri());
                write((BundleDescriptor)aModule.getDescriptor(),  moduleArchivist, moduleArchive2, moduleArchive);
            }

            // now let's write the application descriptor
            ApplicationArchivist archivist = archivistProvider.get();
            archivist.setDescriptor(application);
            archivist.writeDeploymentDescriptors(in, out);
        }
    }

    /**
     * writes a bundle descriptor
     * @param the bundle descriptor
     * @param the archivist responsible for writing such bundle type
     * @param the archive to write to
     */
    protected void write(BundleDescriptor bundle, Archivist archivist, ReadableArchive in, WritableArchive out)
        throws IOException
    {
        archivist.setDescriptor(bundle);
        archivist.writeDeploymentDescriptors(in, out);
    }
}
