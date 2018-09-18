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

package com.sun.enterprise.tools.verifier.tests;
import com.sun.enterprise.deployment.*;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;

/**
 * This class constructs the name of the app client/bean/connector as
 * appName.jarName.componentName
 *
 * @author Sheetal Vartak
 */

public class ComponentNameConstructor {

    private String appName = "";
    private String jarName = "";
    private String componentName = "";
    
    public ComponentNameConstructor(EjbDescriptor ejbDsc) {
	    EjbBundleDescriptor ejbBundle = ejbDsc.getEjbBundleDescriptor();
        ModuleDescriptor moduleDesc = ejbBundle.getModuleDescriptor();
        if(!moduleDesc.isStandalone()){ // print app name only for embedded ones
            this.appName = ejbBundle.getApplication().getRegistrationName();
        }
	    this.jarName = moduleDesc.getArchiveUri();
	    this.componentName = ejbDsc.getName();
    }

    // this takes care of all bundle descriptors.
    public ComponentNameConstructor(BundleDescriptor bundleDesc) {
        ModuleDescriptor moduleDesc = bundleDesc.getModuleDescriptor();
        if(!moduleDesc.isStandalone()){ // print app name only for embedded ones
            this.appName = bundleDesc.getApplication().getRegistrationName();
        }
	    this.jarName = moduleDesc.getArchiveUri();
        // there is no point in printing comp name since it is bundle desc.
    }

    public ComponentNameConstructor(String appName, String jarName, String componentName) {
        this.appName = appName;
        this.jarName = jarName;
        this.componentName = componentName;
    }

    public ComponentNameConstructor(WebServiceEndpoint wse) {
        BundleDescriptor bundleDesc = wse.getBundleDescriptor();
        ModuleDescriptor moduleDesc = bundleDesc.getModuleDescriptor();
        if(!moduleDesc.isStandalone()){ // print app name only for embedded ones
            this.appName = bundleDesc.getApplication().getRegistrationName();
        }
        this.jarName = moduleDesc.getArchiveUri();
        // WebServiceEndpoint path is WebServices->WebService->WebServiceEndpoint
        this.componentName = wse.getWebService().getName()+"#"+wse.getEndpointName(); // NOI18N
    }

    public ComponentNameConstructor(ServiceReferenceDescriptor srd) {
        BundleDescriptor bundleDesc = srd.getBundleDescriptor();
        ModuleDescriptor moduleDesc = bundleDesc.getModuleDescriptor();
        if(!moduleDesc.isStandalone()){ // print app name only for embedded ones
            this.appName = bundleDesc.getApplication().getRegistrationName();
        }
        this.jarName = moduleDesc.getArchiveUri();
        this.componentName = srd.getName();
    }

    public ComponentNameConstructor(WebService wsDsc) {
        BundleDescriptor bundleDesc = wsDsc.getBundleDescriptor();
        ModuleDescriptor moduleDesc = bundleDesc.getModuleDescriptor();
        if(!moduleDesc.isStandalone()){ // print app name only for embedded ones
            this.appName = bundleDesc.getApplication().getRegistrationName();
        }
        this.jarName = moduleDesc.getArchiveUri();
        this.componentName = wsDsc.getName();
    }

    public ComponentNameConstructor(Application application) {
        this.appName = application.getRegistrationName();
    }

    public ComponentNameConstructor(PersistenceUnitDescriptor
            descriptor) {
        PersistenceUnitsDescriptor persistenceUnitsDescriptor =
                descriptor.getParent();
        RootDeploymentDescriptor container = persistenceUnitsDescriptor.getParent();
        if(container.isApplication()) {
            this.appName = Application.class.cast(container).getRegistrationName();
            this.componentName = persistenceUnitsDescriptor.getPuRoot() +
                    "#"+descriptor.getName(); // NOI18N
        } else { // this PU is bundled inside a module
            BundleDescriptor bundleDesc = BundleDescriptor.class.cast(container);
            ModuleDescriptor moduleDesc = bundleDesc.getModuleDescriptor();
            if(!moduleDesc.isStandalone()){ // print app name only for embedded ones
                this.appName = bundleDesc.getApplication().getRegistrationName();
            }
            this.jarName = moduleDesc.getArchiveUri();
            String puRoot = persistenceUnitsDescriptor.getPuRoot();
            // for EJB module, PURoot is empty, so to avoid ## in report, this check is needed.
            this.componentName = ("".equals(puRoot) ? "" : puRoot + "#") + descriptor.getName(); // NOI18N
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(!isNullOrEmpty(appName)){
            sb.append(appName);
        }
        if(!isNullOrEmpty(jarName)){
            if(!isNullOrEmpty(appName)) sb.append("#"); // NOI18N
            sb.append(jarName);
        }
        if(!isNullOrEmpty(componentName)){
            if(!isNullOrEmpty(jarName) || !isNullOrEmpty(appName)) sb.append("#"); // NOI18N
            sb.append(componentName);
        }
        return sb.toString();
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }

}
