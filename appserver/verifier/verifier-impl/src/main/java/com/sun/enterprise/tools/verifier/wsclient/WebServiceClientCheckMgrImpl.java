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

package com.sun.enterprise.tools.verifier.wsclient;

import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.api.deployment.archive.ArchiveType;

/**
 * WebServices harness
 */
public class WebServiceClientCheckMgrImpl extends CheckMgr implements JarCheck {

    /**
     * name of the file containing the list of tests for the webservice client
     * architecture
     */
    private static final String testsListFileName = "TestNamesWebServicesClient.xml"; // NOI18N
    private static final String sunONETestsListFileName = getSunPrefix()
            .concat(testsListFileName);
    private String moduleName;

    public WebServiceClientCheckMgrImpl(VerifierFrameworkContext verifierFrameworkContext) {
        this.verifierFrameworkContext = verifierFrameworkContext;
    }

    /**
     * Check Ejb for spec. conformance
     *
     * @param descriptor WebServices descriptor
     */
    public void check(Descriptor descriptor) throws Exception {
        ServiceReferenceDescriptor rootDescriptor = (ServiceReferenceDescriptor) descriptor;
        ArchiveType moduleType = rootDescriptor.getBundleDescriptor()
                .getModuleType();
        if (moduleType != null && moduleType.equals(DOLUtils.ejbType()))
            moduleName = Result.EJB;
        else if (moduleType != null && moduleType.equals(DOLUtils.warType()))
            moduleName = Result.WEB;
        else if (moduleType != null && moduleType.equals(DOLUtils.carType()))
            moduleName = Result.APPCLIENT;
        super.check(rootDescriptor);
    }

    /**
     * return the configuration file name for the list of tests pertinent to the
     * connector architecture
     *
     * @return <code>String</code> filename containing the list of tests
     */
    protected String getTestsListFileName() {
        return testsListFileName;
    }

    /**
     * @return <code>String</code> filename containing sunone tests
     */
    protected String getSunONETestsListFileName() {
        return sunONETestsListFileName;
    }

    /**
     * A webservices client can be an application client or an ejb or a web component
     * For a j2ee1.4 specific webservices client the version of client descriptor
     * is 1.1. For jee 5.0 this version is 1.0
     * @param descriptor
     * @return webservices client descriptor version
     */
    protected String getSchemaVersion(Descriptor descriptor) {
        String wsclientVersion = null;
        String version = ((ServiceReferenceDescriptor) descriptor).getBundleDescriptor()
                .getSpecVersion();
        if(moduleName.equals(Result.EJB)){
            if("2.1".equals(version)) wsclientVersion = "1.1"; // NOI18N
            else if("3.0".equals(version)) wsclientVersion = "1.2"; // NOI18N
        } else if(moduleName.equals(Result.WEB)){
            if("2.4".equals(version)) wsclientVersion = "1.1"; // NOI18N
            else if("2.5".equals(version)) wsclientVersion = "1.2"; // NOI18N
        } else if(moduleName.equals(Result.APPCLIENT)){
            if("1.4".equals(version)) wsclientVersion = "1.1"; // NOI18N
            else if("5".equals(version)) wsclientVersion = "1.2"; // NOI18N
        }
        if(wsclientVersion==null) {
            wsclientVersion = ""; // should we not throw exception?
        }
        return wsclientVersion;
    }

    protected void setModuleName(Result r) {
        r.setModuleName(moduleName);
    }
    
    protected BundleDescriptor getBundleDescriptor(Descriptor descriptor) {
        return ((ServiceReferenceDescriptor)descriptor).getBundleDescriptor();
    }
    protected ComponentNameConstructor getComponentNameConstructor(
            Descriptor descriptor) {
        return new ComponentNameConstructor((ServiceReferenceDescriptor)descriptor);
    }

}
