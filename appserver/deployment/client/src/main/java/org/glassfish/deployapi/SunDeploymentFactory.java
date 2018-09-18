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

package org.glassfish.deployapi;

import org.glassfish.deployment.client.ServerConnectionIdentifier;
import org.glassfish.deployment.common.DeploymentUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;

import org.glassfish.logging.annotation.LogMessageInfo;

/**
 *Concrete implementation of the JSR 88 DeploymentFactory interface.
 * @author  dochez
 * @author  tjquinn
 */
public class SunDeploymentFactory implements DeploymentFactory {
    
    private static LocalStringManagerImpl xlocalStrings =
        new LocalStringManagerImpl(SunDeploymentFactory.class);
    
    private static String DMGR_NOT_CONNECTED = xlocalStrings.getLocalString(
    "enterprise.deployapi.spi.DMgrnotconnected", // NOI18N
    "Deployment Manager is not connected to J2EE Resources"); // NOI18N
    
    // URI String that this factory can handle. May need to be rebuilt.
    
    //The following URISTRING is what we supported in PE Beta.  Keeping it for
    //backward compatibility
    private final static String PE_BETA_URISTRING = "deployer:Sun:S1AS::"; // NOI18N
    
    //The following URISTRINNG is what we use for PE FCS and in the future
    private final static String DEFAULT_URISTRING = "deployer:Sun:AppServer::"; // NOI18N
    private final static String HTTPS = "https";
    private final static String URI_SEPARATOR = ":";// NOI18N
    private final static String LOCAL_HOST = "localhost";// NOI18N
    private final static int HOST_PORT = 4848; // default DAS port
    
    private final static String[] supportedURIs = { PE_BETA_URISTRING,
        DEFAULT_URISTRING };
    
    // All the registered mangers are shared by all instances of the Factory
    private static Hashtable connectedDeploymentManagers;
    private static Hashtable disconnectedDeploymentManagers;

    private final static String HTTPS_PROTOCOL = "s1ashttps";
    private final static String HTTP_PROTOCOL = "s1ashttp";

    public static final Logger deplLogger = org.glassfish.deployment.client.AbstractDeploymentFacility.deplLogger;

    @LogMessageInfo(message = "Deployment manager load failure.  Unable to find {0}",cause="A deployment manager is not available.",action="Correct the reference to the deployment manager.", level="SEVERE")
    private static final String NO_DEPLOYMENT_MANAGER = "AS-DEPLOYMENT-04019";

    /** Creates a new instance of SunDeploymentFactory */
    public SunDeploymentFactory() {
    }
    
    
    /** Return a <tt>connected</tt> DeploymentManager instance.
     *
     * @param uri The URI that specifies the connection parameters
     * @param username An optional username (may be <tt>null</tt> if
     *        no authentication is required for this platform).
     * @param password An optional password (may be <tt>null</yy> if
     *        no authentication is required for this platform).
     * @return A ready DeploymentManager instance.
     * @throws DeploymentManagerCreationException  occurs when a
     *        DeploymentManager could not be returned (server down,
     *        unable to authenticate, etc).
     */
    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        
        if (handlesURI(uri)) {
            ServerConnectionIdentifier hostInfo = null;
            try {
                hostInfo = parseURIForHostInfo(uri);
            } catch(Exception ex) {
                DeploymentManagerCreationException e = new DeploymentManagerCreationException(
                xlocalStrings.getLocalString(
                "enterprise.deployapi.spi.wronghostidentifier",
                "Wrong host identifier in uri {0} ", new Object[] { uri }));
                e.initCause(ex);
                throw e;
            }
            try {
                hostInfo.setUserName(username);
                hostInfo.setPassword(password);
                DeploymentManager answer = null;

                answer = new SunDeploymentManager(hostInfo);
                return answer;
            } catch(Throwable t) {
                DeploymentManagerCreationException e = new DeploymentManagerCreationException(xlocalStrings.getLocalString(
                "enterprise.deployapi.spi.exceptionwhileconnecting", //NOI18N
                "Exception while connecting to {0} : {1}", new Object[] { uri, t.getMessage() })); //NOI18N
                e.initCause(t);
                throw e;
            }
        } else {
            return null;
        }
    }
    
    /** Return a <tt>disconnected</tt> DeploymentManager instance.
     *
     * @param uri the uri of the DeploymentManager to return.
     * @return A DeploymentManager <tt>disconnected</tt> instance.
     * @throws DeploymentManagerCreationException occurs if the
     *         DeploymentManager could not be created.
     */
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if (handlesURI(uri)) {
            return new SunDeploymentManager();
        } else {
            return null;
        }
    }
    
    /** Provide a string with the name of this vendor's DeploymentManager.
     * @return the name of the vendor's DeploymentManager.
     */
    public String getDisplayName() {
        return xlocalStrings.getLocalString(
                "enterprise.deployapi.spi.DisplayName",
                "Sun Java System Application Server");
    }
    
    /** Provide a string identifying version of this vendor's
     * DeploymentManager.
     * @return the name of the vendor's DeploymentManager.
     */
    public String getProductVersion() {
        return xlocalStrings.getLocalString(
                "enterprise.deployapi.spi.ProductVersion", "9.0");
    }
    
    /** Tests whether this factory can create a DeploymentManager
     * object based on the specificed URI.  This does not indicate
     * whether such an attempt will be successful, only whether the
     * factory can handle the uri.
     * @param uri The uri to check
     * @return <tt>true</tt> if the factory can handle the uri.
     */
    public boolean handlesURI(String uri) {
        if (deplLogger.isLoggable(Level.FINE)) {
            deplLogger.fine("handlesURI: URI ["+uri+"]");// NOI18N
        }
        
        if (uri != null) {
            try {
                parseURIForHostInfo(uri);
                return true;
            } catch (Exception ex) {
              deplLogger.log(Level.SEVERE,
                             NO_DEPLOYMENT_MANAGER,
                             uri);
            }
        }
        return false;
    }
    
    
    /**
     * @return the host name/port from the URI passed see JSR88 paragraph 9.2.3
     */
    
    public ServerConnectionIdentifier parseURIForHostInfo(String uri) throws Exception {
        
        String targetURI = null;
        for (int i=0;i<supportedURIs.length;i++) {
            if (uri.indexOf(supportedURIs[i])==0) {
                targetURI = supportedURIs[i];
            }
        }
        
        //if the URI does not contain DEFAULT_URISTRINNG or PE_BETA_URISTRING,
        //then the URI is not valid.
        if (targetURI == null) {
            throw new Exception(xlocalStrings.getLocalString(
            "enterprise.deployapi.spi.invaliduri", // NOI18N
            "Invalid URI"));    // NOI18N
        }
        
        ServerConnectionIdentifier sci = new ServerConnectionIdentifier();
        
        if(uri.length() == targetURI.length()) {
            sci.setHostName(LOCAL_HOST);
            sci.setHostPort(HOST_PORT);
        } else {
            String reminder = uri.substring(targetURI.length());
            String[] splitted = reminder.split(URI_SEPARATOR);
            if (splitted.length<2) {
                throw new Exception(xlocalStrings.getLocalString(
                    "enterprise.deployapi.spi.invaliduri", // NOI18N
                    "Invalid URI"));    // NOI18N                
            }
            if ("".equals(splitted[0])) {
                sci.setHostName(LOCAL_HOST);
            } else {
                sci.setHostName(splitted[0]);
            }
            if ("".equals(splitted[1])) {
                sci.setHostPort(HOST_PORT);
            } else {
                sci.setHostPort(Integer.parseInt(splitted[1]));
            }
            
            if (splitted.length>2) {
                if (HTTPS.equals(splitted[2])) {
                    sci.setSecure(true);
                }
            }
        }
        return sci;
    }
}
