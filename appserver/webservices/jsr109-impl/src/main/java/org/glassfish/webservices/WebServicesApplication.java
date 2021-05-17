/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.webservices;

import java.net.URL;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;


import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.container.EndpointRegistrationException;

import org.glassfish.web.deployment.util.WebServerInfo;

import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.deployment.*;
import org.glassfish.grizzly.servlet.ServletHandler;

/**
 * This class implements the ApplicationContainer and will be used
 * to register endpoints to the grizzly ServletAdapter
 * Thus when a request is received it is directed to our EjbWebServiceServlet
 * so that it can process the request
 *
 * @author Bhakti Mehta
 */

public class WebServicesApplication implements ApplicationContainer {

    private ArrayList<EjbEndpoint> ejbendpoints;

    private ServletHandler httpHandler;

    private final RequestDispatcher dispatcher;

    private DeploymentContext deploymentCtx;

    private static final Logger logger = LogUtils.getLogger();

    private ClassLoader cl;
    private Application app;
    private Set<String> publishedFiles;

    public WebServicesApplication(DeploymentContext context,  RequestDispatcher dispatcherString, Set<String> publishedFiles){
        this.deploymentCtx = context;
        this.dispatcher = dispatcherString;
        this.ejbendpoints = getEjbEndpoints();
        this.httpHandler = new EjbWSAdapter();
        this.publishedFiles = publishedFiles;
    }

    public Object getDescriptor() {
        return null;
    }

    public boolean start(ApplicationContext startupContext) throws Exception {

        cl = startupContext.getClassLoader();

        try {
           app = deploymentCtx.getModuleMetaData(Application.class);

            DeployCommandParameters commandParams = ((DeploymentContext)startupContext).getCommandParameters(DeployCommandParameters.class);
            String virtualServers = commandParams.virtualservers;
            Iterator<EjbEndpoint> iter = ejbendpoints.iterator();
            EjbEndpoint ejbendpoint = null;
            while(iter.hasNext()) {
                ejbendpoint = iter.next();
                String contextRoot = ejbendpoint.contextRoot;
                WebServerInfo wsi = new WsUtil().getWebServerInfoForDAS();
                URL rootURL = wsi.getWebServerRootURL(ejbendpoint.isSecure);
                dispatcher.registerEndpoint(contextRoot, httpHandler, this, virtualServers);
                //Fix for issue 13107490 and 17648
                if (wsi.getHttpVS() != null && wsi.getHttpVS().getPort()!=0) {
                    logger.log(Level.INFO, LogUtils.EJB_ENDPOINT_REGISTRATION,
                            new Object[] {app.getAppName(), rootURL + contextRoot});
                }
            }

        } catch (EndpointRegistrationException e) {
            logger.log(Level.SEVERE,  LogUtils.ENDPOINT_REGISTRATION_ERROR, e.toString());
        }
        return true;
    }


    private ArrayList<EjbEndpoint> getEjbEndpoints() {
        ejbendpoints = new ArrayList<EjbEndpoint>();

        Application app = deploymentCtx.getModuleMetaData(Application.class);

        Set<BundleDescriptor> bundles = app.getBundleDescriptors();
        for(BundleDescriptor bundle : bundles) {
            collectEjbEndpoints(bundle);
        }

        return ejbendpoints;
    }

    private void collectEjbEndpoints(BundleDescriptor bundleDesc) {
        WebServicesDescriptor wsDesc = bundleDesc.getWebServices();
        for (WebService ws : wsDesc.getWebServices()) {
            for (WebServiceEndpoint endpoint : ws.getEndpoints()) {
                //Only add for ejb based endpoints
                if (endpoint.implementedByEjbComponent()) {
                    ejbendpoints.add(new EjbEndpoint(endpoint.getEndpointAddressUri(), endpoint.isSecure()));
                }
            }
        }
    }

    public boolean stop(ApplicationContext stopContext) {
        try {
            Iterator<EjbEndpoint> iter = ejbendpoints.iterator();
            String contextRoot;
            EjbEndpoint endpoint;
            while(iter.hasNext()) {
                endpoint = iter.next();
                contextRoot = endpoint.contextRoot;
                dispatcher.unregisterEndpoint(contextRoot);
            }
        } catch (EndpointRegistrationException e) {
            logger.log(Level.SEVERE,  LogUtils.ENDPOINT_UNREGISTRATION_ERROR ,e.toString());
            return false;
        }
        return true;
    }

    public boolean suspend() {
        return false;
    }

    public boolean resume() throws Exception {
        return false;
    }

    public ClassLoader getClassLoader() {
        return cl;
    }

    Application getApplication() {
        return app;
    }

    static class EjbEndpoint {
        private final String contextRoot;

        private boolean isSecure;

        EjbEndpoint(String contextRoot,boolean secure){
            this.contextRoot = contextRoot;
            this.isSecure = secure;
        }
    }

    Set<String> getPublishedFiles() {
        return publishedFiles;
    }
}
