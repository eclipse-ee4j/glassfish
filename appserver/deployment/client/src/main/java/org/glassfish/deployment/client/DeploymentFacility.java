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

package org.glassfish.deployment.client;

import java.util.Map;
import java.util.List;
import java.io.File;
import java.io.IOException;

import java.net.URI;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import org.glassfish.api.deployment.archive.ReadableArchive;

import com.sun.enterprise.util.HostAndPort;

/**
 * This interface defines basic deployment related facilities 
 * such as deploying any j2ee modules on a Domain Admin Server
 * or target servers as well as retrieving non portable artifacts
 * for successful runs in a client mode configuration.
 *
 * @author Jerome Dochez
 */
public interface DeploymentFacility {

    final static String STUBS_JARFILENAME = "clientstubs.jar";
    
    /**
     * Connects to a particular instance of the domain adminstration 
     * server using the provided connection information
     * <p>
     * Most other methods require that connect be invoked first.
     * @param targetDAS the {@link ServerConnectionIdentifier} that specifies which DAS to connect to
     * @return whether or not the connection attempt succeeded
     */
    
    public boolean connect(ServerConnectionIdentifier targetDAS);
    
    /** 
     * @return true if we are connected to a domain adminstration 
     * server
     */
    public boolean isConnected();
    
    /** 
     * Disconnects from a domain administration server and releases
     * all associated resouces.
     */
    public boolean disconnect();
        
    /**
     * Initiates a deployment operation on the server, using a source 
     * archive abstraction and an optional deployment plan if the 
     * server specific information is not embedded in the source 
     * archive. The deploymentOptions is a key-value pair map of 
     * deployment options for this operation. 
     * <p>
     * Once the deployment 
     * is successful, the TargetModuleID objects representing where the application
     * now resides are available using the {@link DFProgressObject#getResultTargetModuleIDs} method.
     * 
     * @param targets the Target objects to which to deploy the application - an empty array indicates a request to deploy to the default server
     * @param source URI to the Java EE module abstraction (with or without 
     * the server specific artifacts). 
     * @param deploymentPlan URI to the optional deployment plan if the source 
     * archive is portable.
     * @param deploymentOptions deployment options
     * @return a DFProgressObject to receive deployment events and status
     * @throws IllegalStateException if the client has not invoked connect yet
     */
    public DFProgressObject deploy(Target[] targets, URI source, 
        URI deploymentPlan, Map deploymentOptions);
    
    public DFProgressObject deploy(Target[] targets, ReadableArchive source,
        ReadableArchive deploymentPlan, Map deploymentOptions) throws IOException;

    // FIXME : This will go once admin-cli changes its code
    public DFProgressObject undeploy(Target[] targets, String moduleID);

    /**
     * Initiates an undeployment operation of the specified module affecting the selected targets.
     * <p>
     * After the undeployment operation completes successfully use the {@link DFProgressObject#getResultTargetModuleIDs} method to retrieve
     * the TargetModuleID objects representing the targets from which the module has been undeployed.
     * @param targets the Target objects indicating which targets from which to undeploy the application; an empty targets array is a request to undeploy from the default server
     * @param moduleID identifies the module to undeploy
     * @return a {@link DFProgressObject} to receive undeployment events and completion status
     * @throws IllegalStateException if the deployment facility has not been connected to the back-end
     */
    public DFProgressObject undeploy(Target[] targets, String moduleID, Map options);
    
    /**
     * Enables a deployed component on the provided list of targets.
     */ 
    public DFProgressObject enable(Target[] targets, String moduleID);

    /**
     * Disables a deployed component on the provided list of targets
     */
    public DFProgressObject disable(Target[] targets, String moduleID);
    
    /**
     * Add an application ref on the selected targets
     */ 
    public DFProgressObject createAppRef(Target[] targets, String moduleID, Map options);

    /**
     * remove the application ref for the provided list of targets.
     */
    public DFProgressObject deleteAppRef(Target[] targets, String moduleID, Map options);    

    /**
     * get the host and port information
     */
    public HostAndPort getHostAndPort(String target) throws IOException;

    /**
     * get the host and port information with security enabled attribute
     */
    public HostAndPort getHostAndPort(String target, boolean securityEnabled) throws IOException;

    /**
     * get the host and port information with the specified virtual server and
     * security enabled attribute
     */
    public HostAndPort getVirtualServerHostAndPort(String target, String virtualServer, boolean securityEnabled) throws IOException;

   /**
     * get the host and port information with the specified module id and 
     * security enabled attribute
     */
    public HostAndPort getHostAndPort(String target, String modID, boolean securityEnabled) throws IOException;

   /**
     * list all application refs that are present in the provided list of targets
     */
    public TargetModuleID[] listAppRefs(String[] targets) throws IOException;

    /**
     * list all application refs that are present in the provided list of targets
     */
    public TargetModuleID[] _listAppRefs(String[] targets) throws IOException;

    /**
     * list all application refs that are present in the provided list of targets with the specified state
     */
    public TargetModuleID[] _listAppRefs(String[] targets, String state) throws IOException;

    /**
     * list all application refs that are present in the provided list of targets with the specified state and specified type
     */
    public TargetModuleID[] _listAppRefs(String[] targets, String state, String type) throws IOException;

    /**
     * list all application refs that are present in the provided list of targets with the specified state and specified type
     */
    public TargetModuleID[] _listAppRefs(Target[] targets, String state, String type) throws IOException;

    /**
     * Get sub module info for ear
     */
    public List<String> getSubModuleInfoForJ2EEApplication(String appName) throws IOException;

    /**
     * Get context root for the module
     */
    public String getContextRoot(String moduleName) throws IOException;

    /**
     * Get module type for the module
     */
   public ModuleType getModuleType(String moduleName) throws IOException;

    /**
     * list all targets
     */
    public Target[] listTargets() throws IOException;

    /**
     * list the referenced targets for application
     */
    public Target[] listReferencedTargets(String appName) throws IOException;

    
    /**
     * Downloads a particular file from the server repository. 
     * The filePath is a relative path from the root directory of the 
     * deployed component identified with the moduleID parameter. 
     * The resulting downloaded file should be placed in the 
     * location directory keeping the relative path constraint. 
     * 
     * @param location is the root directory where to place the 
     * downloaded file
     * @param moduleID is the moduleID of the deployed component 
     * to download the file from
     * @param moduleURI is the relative path to the file in the repository 
     * or STUBS_JARFILENAME to download the appclient jar file.
     * @return the downloaded local file absolute path.
     */
    public String downloadFile(File location, String moduleID, 
            String moduleURI) throws IOException;
    
    /**
     * Downloads the client stubs from the server repository.
     *
     * @param location is the root path where to place the 
     * downloaded stubs
     * @param moduleID is the moduleID of the deployed component
     * to download the stubs for
     */
    public void getClientStubs(String location, String moduleID) 
        throws IOException;

    /**
     * Wait for a progress object to be in a completed state 
     * (sucessful or failed) and return the DeploymentStatus for 
     * this progress object.
     * @param the progress object to wait for completion
     * @return the deployment status
     */
    public DFDeploymentStatus waitFor(DFProgressObject po);
    
    /**
     * Creates an array of Target objects corresponding to the list of target names.
     * @param targets the names of the targets of interest
     * @return an array of Target objects for the selected target names
     */
    public Target[] createTargets(String[] targets );
    
}
