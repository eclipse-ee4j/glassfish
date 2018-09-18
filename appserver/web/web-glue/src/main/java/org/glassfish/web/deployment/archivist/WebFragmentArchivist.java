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

package org.glassfish.web.deployment.archivist;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.Archive;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.web.deployment.descriptor.WebFragmentDescriptor;
import org.glassfish.web.deployment.io.WebFragmentDeploymentDescriptorFile;
import org.glassfish.hk2.api.ServiceLocator;

import java.io.IOException;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


/**
 * This module is responsible for reading and write web fragment
 * archive files (jar).
 * This is not a @Service, does not have @Scoped(PerLookup.class)
 * and does not implements PrivateArchivist as it will not be
 * looked up through ArchivistFactory.
 *
 * @author  Shing Wai Chan
 * @version 
 */
class WebFragmentArchivist extends Archivist<WebFragmentDescriptor> {

    WebFragmentArchivist (WebArchivist webArchivist, ServiceLocator habitat) {
        this.habitat = habitat;
        initializeContext(webArchivist);
    }

    /**
     * @return the  module type handled by this archivist
     * as defined in the application DTD
     *
     */
    @Override
    public ArchiveType getModuleType() {
        return null;
    }        

    /**
     * Archivist read XML deployment descriptors and keep the
     * parsed result in the DOL descriptor instances. Sets the descriptor
     * for a particular Archivst type
     */
    public void setDescriptor(Application descriptor) {
        this.descriptor = null;
    }  
    
    /**
     * @return the DeploymentDescriptorFile responsible for handling
     * standard deployment descriptor
     */
    @Override
    public DeploymentDescriptorFile<WebFragmentDescriptor> getStandardDDFile() {
        if (standardDD == null) {
            standardDD = new WebFragmentDeploymentDescriptorFile();
        }
        return standardDD;
    }
    
    /**
     * @return the list of the DeploymentDescriptorFile responsible for
     *         handling the configuration deployment descriptors
     */
    public List<ConfigurationDeploymentDescriptorFile> getConfigurationDDFiles() {
        return Collections.emptyList();
    }

    /**
     * @return a default BundleDescriptor for this archivist
     */
    @Override
    public WebFragmentDescriptor getDefaultBundleDescriptor() {
        return new WebFragmentDescriptor();
    }

    /**
     * perform any post deployment descriptor reading action
     *
     * @param descriptor the deployment descriptor for the module
     * @param archive the module archive
     */
    @Override
    protected void postOpen(WebFragmentDescriptor descriptor, ReadableArchive archive)
        throws IOException
    {
        super.postOpen(descriptor, archive);
        postValidate(descriptor, archive);
    }

    /**
     * In the case of web archive, the super handles() method should be able 
     * to make a unique identification.  If not, then the archive is definitely 
     * not a war.
     */
    @Override
    protected boolean postHandles(ReadableArchive abstractArchive)
            throws IOException {
        return false;
    }

    @Override
    protected String getArchiveExtension() {
        return WEB_FRAGMENT_EXTENSION;
    }
    
    /**
     * @return a list of libraries included in the archivist
     */
    @Override
    public Vector getLibraries(Archive archive) {
        return null;
    }
}
