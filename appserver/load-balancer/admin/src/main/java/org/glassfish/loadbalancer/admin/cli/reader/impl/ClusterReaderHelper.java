/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.loadbalancer.admin.cli.reader.impl;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.loadbalancer.admin.cli.LbLogUtil;
import org.glassfish.loadbalancer.admin.cli.reader.api.LbReaderException;
import org.glassfish.loadbalancer.admin.cli.reader.api.WebModuleReader;

/**
 * Impl class for ClusterReader. This provides loadbalancer
 * data for a cluster.
 *
 * @author Kshitiz Saxena
 */
public class ClusterReaderHelper {

    /**
     * Returns the web module readers for a set of application refs.
     *
     * @param   refs            Application ref(s) from cluster or stand alone
     *                          instance
     * @param   target          Name of the cluster or stand alone instance
     *
     * @return  WebModuleReader[]   Array of the corresponding web module
     *                              reader(s).
     *
     * @throws  LbReaderException   In case of any error(s).
     */
    public static WebModuleReader[] getWebModules(Domain domain, ApplicationRegistry appRegistry,
            List<ApplicationRef> refs, String target) {

        List<WebModuleReader> list = new ArrayList<WebModuleReader>();
        Set<String> contextRoots = new HashSet<String>();

        Iterator<ApplicationRef> refAppsIter = refs.iterator();
        HashMap<String, ApplicationRef> refferedApps =
                new HashMap<String, ApplicationRef>();
        while (refAppsIter.hasNext()) {
            ApplicationRef appRef = refAppsIter.next();
            refferedApps.put(appRef.getRef(), appRef);
        }

        Applications applications = domain.getApplications();
        Set<Application> apps = new HashSet<Application>();
        apps.addAll(applications.getApplicationsWithSnifferType("web"));
        apps.addAll(applications.getApplicationsWithSnifferType("webservices"));

        Iterator<Application> appsIter = apps.iterator();
        while (appsIter.hasNext()) {
            Application app = appsIter.next();
            String appName = app.getName();
            if (!refferedApps.containsKey(appName)) {
                continue;
            }
            ApplicationInfo appInfo = appRegistry.get(appName);
            if (appInfo == null) {
                String msg = LbLogUtil.getStringManager().getString("UnableToGetAppInfo", appName);
                LbLogUtil.getLogger().log(Level.WARNING, msg);
                continue;
            }
            com.sun.enterprise.deployment.Application depApp = appInfo.getMetaData(com.sun.enterprise.deployment.Application.class);
            Iterator<BundleDescriptor> bundleDescriptorIter = depApp.getBundleDescriptors().iterator();

            while (bundleDescriptorIter.hasNext()) {
                BundleDescriptor bundleDescriptor = bundleDescriptorIter.next();
                try{
                if (bundleDescriptor instanceof WebBundleDescriptor) {
                    WebModuleReader wmr = new WebModuleReaderImpl(appName, refferedApps.get(appName),
                            app, (WebBundleDescriptor) bundleDescriptor);
                    if(!contextRoots.contains(wmr.getContextRoot())){
                        contextRoots.add(wmr.getContextRoot());
                        list.add(wmr);
                    }
                } else if (bundleDescriptor instanceof EjbBundleDescriptor) {
                    EjbBundleDescriptor ejbBundleDescriptor = (EjbBundleDescriptor) bundleDescriptor;
                    if (!ejbBundleDescriptor.hasWebServices()) {
                        continue;
                    }
                    Iterator<WebServiceEndpoint> wsIter = ejbBundleDescriptor.getWebServices().getEndpoints().iterator();
                    while (wsIter.hasNext()) {
                        WebServiceEndpointReaderImpl wsr = new WebServiceEndpointReaderImpl(appName, refferedApps.get(appName), app, wsIter.next());
                        if(!contextRoots.contains(wsr.getContextRoot())){
                            contextRoots.add(wsr.getContextRoot());
                            list.add(wsr);
                        }
                    }
                }
                }catch(LbReaderException ex){
                    String msg = LbLogUtil.getStringManager().getString("UnableToGetContextRoot", appName, ex.getMessage());
                    LbLogUtil.getLogger().log(Level.WARNING, msg);
                    if (LbLogUtil.getLogger().isLoggable(Level.FINE)) {
                        LbLogUtil.getLogger().log(Level.FINE, "Exception when getting context root for application", ex);
                    }
                }
            }
        }
        contextRoots.clear();
        // returns the web module reader as array
        WebModuleReader[] webModules = new WebModuleReader[list.size()];
        return (WebModuleReader[]) list.toArray(webModules);
    }
}
