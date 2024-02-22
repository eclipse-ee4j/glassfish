/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.io.IOException;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.jvnet.hk2.annotations.Service;

/**
 * This class is responsible for writing deployment descriptors
 * after a deployment action has occured to a abstract archive instance.
 *
 * @author  Jerome Dochez
 */
@Service
public class DescriptorArchivist {
    @Inject
    private ArchivistFactory archivistFactory;

    @Inject
    private  Provider<ApplicationArchivist> archivistProvider;

    /**
     * Writes an application deployment descriptors
     *
     * @param application object
     * @param in abstract archive
     */
    public void write(Application application, ReadableArchive in, WritableArchive out) throws IOException {
        if (application.isVirtual()) {
            ModuleDescriptor<BundleDescriptor> aModule = application.getModules().iterator().next();
            Archivist<BundleDescriptor> moduleArchivist = archivistFactory.getArchivist(aModule.getModuleType());
            write(aModule.getDescriptor(), moduleArchivist, in, out);
        } else {
            // this is a real application.
            // let's start by writing out all submodules deployment descriptors
            for (ModuleDescriptor<BundleDescriptor> aModule : application.getModules()) {
                Archivist<BundleDescriptor> moduleArchivist = archivistFactory.getArchivist(aModule.getModuleType());
                try (WritableArchive moduleArchive = out.createSubArchive(aModule.getArchiveUri());
                    ReadableArchive moduleArchive2 = in.getSubArchive(aModule.getArchiveUri())) {
                    write(aModule.getDescriptor(), moduleArchivist, moduleArchive2, moduleArchive);
                }
            }

            // now let's write the application descriptor
            ApplicationArchivist archivist = archivistProvider.get();
            archivist.setDescriptor(application);
            archivist.writeDeploymentDescriptors(in, out);
        }
    }

    /**
     * Writes a bundle descriptor
     *
     * @param bundle descriptor
     * @param archivist responsible for writing such bundle type
     * @param in archive to read from
     * @param out archive to write to
     * @throws IOException
     */
    protected void write(BundleDescriptor bundle, Archivist<BundleDescriptor> archivist, ReadableArchive in,
        WritableArchive out) throws IOException {
        archivist.setDescriptor(bundle);
        archivist.writeDeploymentDescriptors(in, out);
    }
}
