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

package org.glassfish.deployapi.actions;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.deploy.spi.exceptions.ClientExecuteException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.TargetModuleID;

import org.glassfish.deployapi.TargetImpl;
import org.glassfish.deployapi.TargetModuleIDImpl;
import com.sun.enterprise.util.LocalStringManagerImpl;


/**
 * This implementation of the ClientConfiguration interface allow
 * for limited support of Application Client
 *
 * @author Jerome Dochez
 */
public class ClientConfigurationImpl implements ClientConfiguration {
    
    TargetModuleIDImpl targetModuleID; // TODO neither transient or Serializable we need to choose one or the other
    String originalArchivePath;
    
    private static LocalStringManagerImpl localStrings =
	  new LocalStringManagerImpl(ClientConfigurationImpl.class);    
    
    /** Creates a new instance of ClientConfigurationImpl */
    public ClientConfigurationImpl(TargetModuleIDImpl targetModuleID) {
        this.targetModuleID = targetModuleID;
    }
                
    /** This method performs an exec and starts the
     * application client running in another process.
     *
     * @throws ClientExecuteException when the configuration
     *         is incomplete.
     */
    public void execute() throws ClientExecuteException {
        if (targetModuleID==null) {
            throw new ClientExecuteException(localStrings.getLocalString(
                "enterprise.deployapi.actions.clientconfigurationimpl.nomoduleid", 
                "No moduleID for deployed application found"));
        }
        TargetImpl target = (TargetImpl) targetModuleID.getTarget();
        String moduleID;
        if (targetModuleID.getParentTargetModuleID()!=null) {            
            moduleID = targetModuleID.getParentTargetModuleID().getModuleID();
        } else {
            moduleID = targetModuleID.getModuleID();
        }
        
        
        try {
            // retrieve the stubs from the server
            String location = target.exportClientStubs(moduleID, System.getProperty("java.io.tmpdir"));
       
            // invoke now the appclient...
            String j2eeHome = System.getProperty("com.sun.aas.installRoot");
            String appClientBinary = j2eeHome + File.separatorChar + "bin" + File.separatorChar + "appclient";
            String command = appClientBinary + " -client " + location;
            
            Runtime.getRuntime().exec(command);
            
        } catch(Exception e) {
            Logger.getAnonymousLogger().log(Level.WARNING, "Error occurred", e); 
            throw new ClientExecuteException(localStrings.getLocalString(
                "enterprise.deployapi.actions.clientconfigurationimpl.exception", 
                "Exception while invoking application client : \n {0}", new Object[] { e.getMessage() }));
        }
    }
}
