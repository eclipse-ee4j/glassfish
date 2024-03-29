/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.webservices.deployment;

import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.WebServicesDescriptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.gmbal.ManagedOperation;
import org.glassfish.webservices.WebServiceDeploymentNotifier;


/**
 * MBean that provides deployed Web Service endpoints.
 *
 * Keeps track of 109 deployed applications.
 *
 * @author Jitendra Kotamraju
 */
@AMXMetadata(type="web-service-mon", group="monitoring")
@ManagedObject
@Description("Deployed Web Services")
public class WebServicesDeploymentMBean {
    // appName --> Application
    private final Map<String, Application> applications = new HashMap<>();

    private static class Application {
        final String appName;
        final Map<String, Module> modules;

        Application(String appName) {
            this.appName = appName;
            modules = new HashMap<>();
        }


        // moduleName --> <endpointName --> DeployedEndpointData>
        Map<String, Map<String, DeployedEndpointData>> getDeployedEndpointData() {
            Map<String, Map<String, DeployedEndpointData>> tempEndpoints = new HashMap<>();
            for (Map.Entry<String, Module> e : modules.entrySet()) {
                tempEndpoints.put(e.getKey(), e.getValue().getDeployedEndpointData());

            }
            return tempEndpoints;
        }


        // moduleName --> <endpointName --> DeployedEndpointData>
        Map<String, Map<String, DeployedEndpointData>> getDeployedEndpointData(String moduleName) {
            Module module = modules.get(moduleName);
            if (module == null) {
                return Collections.emptyMap();
            }
            Map<String, Map<String, DeployedEndpointData>> tempEndpoints = new HashMap<>();
            tempEndpoints.put(moduleName, module.getDeployedEndpointData());
            return tempEndpoints;
        }


        // moduleName --> <endpointName --> DeployedEndpointData>
        Map<String, Map<String, DeployedEndpointData>> getDeployedEndpointData(String moduleName, String endpointName) {
            Module module = modules.get(moduleName);
            if (module == null) {
                return Collections.emptyMap();
            }
            Map<String, Map<String, DeployedEndpointData>> tempEndpoints = new HashMap<>();
            tempEndpoints.put(moduleName, module.getDeployedEndpointData(endpointName));
            return tempEndpoints;
        }


        void addEndpoint(String moduleName, String endpointName, DeployedEndpointData endpointData) {
            Module module = modules.get(moduleName);
            if (module == null) {
                module = new Module(appName, moduleName);
                modules.put(moduleName, module);
            }
            module.addEndpoint(endpointName, endpointData);
        }


        void removeEndpoint(String moduleName, String endpointName) {
            Module module = modules.get(moduleName);
            if (module != null) {
                module.removeEndpoint(endpointName);
                if (module.isEmpty()) {
                    modules.remove(moduleName);
                }
            }
        }


        boolean isEmpty() {
            return modules.isEmpty();
        }


        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Application) {
                Application other = (Application) obj;
                if (appName.equals(other.appName)) {
                    return true;
                }
            }
            return false;
        }


        @Override
        public int hashCode() {
            return appName.hashCode();
        }

        @Override
        public String toString() {
            return appName;
        }
    }

    private static class Module {
        final String moduleName;
        final Map<String, Endpoint> endpoints;
        final String appName;

        Module(String appName, String moduleName) {
            this.appName = appName;
            this.moduleName = moduleName;
            endpoints = new HashMap<>();
        }

        // endpointName --> DeployedEndpointData
        Map<String, DeployedEndpointData> getDeployedEndpointData() {
            Map<String, DeployedEndpointData> tempEndpoints = new HashMap<>();
            for(Map.Entry<String, Endpoint> e : endpoints.entrySet()) {
                tempEndpoints.put(e.getKey(), e.getValue().getDeployedEndpointData());

            }
            return tempEndpoints;
        }

        // endpointName --> DeployedEndpointData
        Map<String, DeployedEndpointData> getDeployedEndpointData(String endpointName) {
            Endpoint endpoint = endpoints.get(endpointName);
            if (endpoint == null) {
                return Collections.emptyMap();
            }
            Map<String, DeployedEndpointData> tempEndpoints = new HashMap<>();
            tempEndpoints.put(endpointName, endpoint.getDeployedEndpointData());
            return tempEndpoints;
        }

        void addEndpoint(String endpointName, DeployedEndpointData endpointData) {
            Endpoint endpoint = endpoints.get(endpointName);
            if (endpoint == null) {
                endpoint = new Endpoint(appName, moduleName, endpointName, endpointData);
                endpoints.put(endpointName, endpoint);
            }
        }

        void removeEndpoint(String endpointName) {
            endpoints.remove(endpointName);
        }

        boolean isEmpty() {
            return endpoints.isEmpty();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Module) {
                Module other = (Module)obj;
                if (appName.equals(other.appName) && moduleName.equals(other.moduleName)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(appName, moduleName);
        }

        @Override
        public String toString() {
            return appName + "#" + moduleName;
        }
    }

    private static class Endpoint {
        final String appName;
        final String moduleName;
        final String endpointName;
        final DeployedEndpointData endpointData;

        Endpoint(String appName, String moduleName, String endpointName, DeployedEndpointData endpointData) {
            this.appName = appName;
            this.moduleName = moduleName;
            this.endpointName = endpointName;
            this.endpointData = endpointData;
        }

        DeployedEndpointData getDeployedEndpointData() {
            return endpointData;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Endpoint) {
                Endpoint other = (Endpoint)obj;
                if (appName.equals(other.appName) && moduleName.equals(other.moduleName) && endpointName.equals(other.endpointName)) {
                    return true;
                }
            }
            return false;
        }


        @Override
        public int hashCode() {
            return Objects.hash(appName, moduleName, endpointName);
        }


        @Override
        public String toString() {
            return appName + "#" + moduleName + "#" + endpointName;
        }
    }

    public synchronized void deploy(@ProbeParam("endpoint")WebServiceEndpoint endpoint) {
        // add to [appName+moduleName+endpointName --> deployed data]
        com.sun.enterprise.deployment.Application app = endpoint.getBundleDescriptor().getApplication();
        String appName = app.getAppName();
        String moduleName = endpoint.getBundleDescriptor().getModuleName();
        String endpointName = endpoint.getEndpointName();

        // path (context path+url-pattern) --> deployed data
        //String id = new Endpoint(appName, moduleName, endpointName).toString();
        String path = endpoint.getEndpointAddressPath();
        DeployedEndpointData data = new DeployedEndpointData(path, app, endpoint);

        Application application = applications.get(appName);
        if (application == null) {
            application = new Application(appName);
            applications.put(appName, application);
        }
        application.addEndpoint(moduleName, endpointName, data);
    }

    public synchronized void undeploy(@ProbeParam("endpoint")WebServiceEndpoint endpoint) {
        // remove from [appName+moduleName+endpointName --> deployed data]
        com.sun.enterprise.deployment.Application app = endpoint.getBundleDescriptor().getApplication();
        String appName = app.getAppName();
        String moduleName = endpoint.getBundleDescriptor().getModuleName();
        String endpointName = endpoint.getEndpointName();

        Application application = applications.get(appName);
        if (application != null) {
            application.removeEndpoint(moduleName, endpointName);
            if (application.isEmpty()) {
                applications.remove(appName);
            }
        }
    }


    public synchronized void deploy(WebServicesDescriptor wsDesc, WebServiceDeploymentNotifier notifier) {
        for (WebService svc : wsDesc.getWebServices()) {
            for (WebServiceEndpoint endpoint : svc.getEndpoints()) {
                deploy(endpoint);
                notifier.notifyDeployed(endpoint);
            }
        }
    }


    public synchronized void undeploy(String appName) {
        applications.remove(appName);
    }

    // Give a snapshot of all the endpoints
    public synchronized Map<String, Map<String, Map<String, DeployedEndpointData>>> getEndpoints() {
        if (applications.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, Map<String, DeployedEndpointData>>> endpoints = new HashMap<>();
        for (Entry<String, Application> e : applications.entrySet()) {
            endpoints.put(e.getKey(), e.getValue().getDeployedEndpointData());
        }
        return endpoints;
    }

    // Returns a snapshot of all the endpoints in an application
    // returns non-null collection of endpoints. If there are no endpoints for
    // this application, it returns an empty collection
    public synchronized Map<String, Map<String, Map<String, DeployedEndpointData>>> getEndpoints(String appName) {
        Application app = applications.get(appName);
        if (app == null) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, Map<String, DeployedEndpointData>>> endpoints = new HashMap<>();
        endpoints.put(appName, app.getDeployedEndpointData());
        return endpoints;
    }

    // Returns a snapshot of all the endpoints in an application's module
    public synchronized Map<String, Map<String, Map<String, DeployedEndpointData>>> getEndpoints(String appName,
        String moduleName) {
        Application app = applications.get(appName);
        if (app == null) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, Map<String, DeployedEndpointData>>> endpoints = new HashMap<>();
        endpoints.put(appName, app.getDeployedEndpointData(moduleName));
        return endpoints;
    }


    public synchronized Map<String, Map<String, Map<String, DeployedEndpointData>>> getEndpoint(String appName,
        String moduleName, String endpointName) {
        Application app = applications.get(appName);
        if (app == null) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, Map<String, DeployedEndpointData>>> endpoints = new HashMap<>();
        endpoints.put(appName, app.getDeployedEndpointData(moduleName, endpointName));
        return endpoints;
    }

    // Returns the 109 servlet endpoint for appName+moduleName+servletLink
    // TODO remove
    @ManagedOperation
    public synchronized Map<String, String> getServlet109Endpoint(String appName, String moduleName, String servletLink) {
        return Collections.emptyMap();
    }

    // Returns all the 109 EJB endpoint for appName+moduleName+ejbLink
    // TODO remove
    @ManagedOperation
    public synchronized Map<String, String> getEjb109Endpoint(String appName, String moduleName, String ejbLink) {
        return Collections.emptyMap();
    }

}
