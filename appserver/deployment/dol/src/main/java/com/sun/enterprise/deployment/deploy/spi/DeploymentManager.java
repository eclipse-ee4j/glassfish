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

/*
 * DeploymentManager.java
 *
 * Created on April 21, 2004, 9:44 AM
 */

package com.sun.enterprise.deployment.deploy.spi;

import org.glassfish.api.deployment.archive.Archive;
import org.glassfish.api.deployment.archive.WritableArchive;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import java.io.IOException;

/**
 *
 * @author  dochez
 */
public interface DeploymentManager 
    extends javax.enterprise.deploy.spi.DeploymentManager {
    
   /**
    * The distribute method performs three tasks; it validates the
    * deployment configuration data, generates all container specific 
    * classes and interfaces, and moves the fully baked archive to 
    * the designated deployment targets.
    *
    * @param targetList   A list of server targets the user is specifying
    *                     this application be deployed to. 
    * @param moduleArchive The abstraction for the application 
    *                      archive to be disrtibuted.
    * @param deploymentPlan The archive containing the deployment
    *                       configuration information associated with
    *                       this application archive.
    * @param deploymentOptions is a JavaBeans compliant component 
    *                   containing all deployment options for this deployable
    *                   unit. This object must be created using the 
    *                   BeanInfo instance returned by 
    *                   DeploymentConfiguration.getDeploymentOptions
    * @throws IllegalStateException is thrown when the method is
    *                    called when running in disconnected mode.
    * @return ProgressObject an object that tracks and reports the 
    *                       status of the distribution process.
    *
    */

    public ProgressObject distribute(Target[] targetList,
           Archive moduleArchive, Archive deploymentPlan,
           Object deploymentOptions)
           throws IllegalStateException;
    
    /**
     * Creates a new instance of WritableArchive which can be used to 
     * store application elements in a layout that can be directly used by 
     * the application server. Implementation of this method should carefully
     * return the appropriate implementation of the interface that suits 
     * the server needs and provide the fastest deployment time.
     * An archive may already exist at the location and elements may be 
     * read but not changed or added depending on the underlying medium.
     * @param path the directory in which to create this archive if local 
     * storage is a possibility. 
     * @param name is the desired name for the archive
     * @return the writable archive instance
     */    
    public WritableArchive getArchive(java.net.URI path, String name)
        throws IOException;

}
