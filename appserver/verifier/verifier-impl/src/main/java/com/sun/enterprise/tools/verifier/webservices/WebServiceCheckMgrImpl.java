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

package com.sun.enterprise.tools.verifier.webservices;

import java.util.Iterator;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.tools.verifier.CheckMgr;
import com.sun.enterprise.tools.verifier.VerifierFrameworkContext;
import com.sun.enterprise.tools.verifier.JarCheck;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.api.deployment.archive.ArchiveType;

/**
 * WebServices harness
 */
public class WebServiceCheckMgrImpl extends CheckMgr implements JarCheck {

    /**
     * name of the file containing the list of tests for the webservice
     * architecture
     */
    private final String testsListFileName = "TestNamesWebServices.xml"; // NOI18N
    private final String sunONETestsListFileName = getSunPrefix().concat(
            testsListFileName);
    private String moduleName;

    public WebServiceCheckMgrImpl(VerifierFrameworkContext verifierFrameworkContext) {
        this.verifierFrameworkContext = verifierFrameworkContext;
    }

    /**
     * Check Ejb for spec. conformance
     *
     * @param descriptor WebServices descriptor
     */
    public void check(Descriptor descriptor) throws Exception {
        WebServicesDescriptor rootDescriptor = (WebServicesDescriptor) descriptor;
        ArchiveType moduleType = rootDescriptor.getBundleDescriptor()
                .getModuleType();
        if (moduleType != null && moduleType.equals(DOLUtils.ejbType()))
            moduleName = Result.EJB;
        else if (moduleType != null && moduleType.equals(DOLUtils.warType()))
            moduleName = Result.WEB;
        for (Iterator itr = rootDescriptor.getWebServices().iterator();
             itr.hasNext();) {
            WebService wsDescriptor = (WebService) itr.next();  
            // need to pass WebServiceEndpoint's to check
            for (Iterator endPtItr = wsDescriptor.getEndpoints().iterator();
                 endPtItr.hasNext();) {
                super.check((WebServiceEndpoint) endPtItr.next());
            }
        }
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

    protected String getSchemaVersion(Descriptor descriptor) {
        return ((WebServiceEndpoint) descriptor).getWebService().
                getWebServicesDescriptor().getSpecVersion();
    }

    protected void setModuleName(Result r) {
        r.setModuleName(moduleName);
    }
    
    protected BundleDescriptor getBundleDescriptor(Descriptor descriptor) {
        return ((WebServiceEndpoint) descriptor).getBundleDescriptor();
    }

    protected ComponentNameConstructor getComponentNameConstructor(
            Descriptor descriptor) {
        return new ComponentNameConstructor((WebServiceEndpoint)descriptor);
    }

}
