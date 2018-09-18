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

package com.sun.enterprise.tools.verifier.appclient;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Set;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.io.AppClientDeploymentDescriptorFile;
import com.sun.enterprise.tools.verifier.CheckMgr;
import com.sun.enterprise.tools.verifier.VerifierFrameworkContext;
import com.sun.enterprise.tools.verifier.JarCheck;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.dd.ParseDD;
import com.sun.enterprise.tools.verifier.wsclient.WebServiceClientCheckMgrImpl;
import org.glassfish.deployment.common.RootDeploymentDescriptor;

/**
 * Application Client harness
 */
public class AppClientCheckMgrImpl extends CheckMgr implements JarCheck {

    /**
     * name of the file containing the list of tests for the application client
     * architecture
     */
    private static final String testsListFileName = "TestNamesAppClient.xml"; // NOI18N
    private static final String sunONETestsListFileName = getSunPrefix()
            .concat(testsListFileName);

    public AppClientCheckMgrImpl(VerifierFrameworkContext verifierFrameworkContext) {
        this.verifierFrameworkContext = verifierFrameworkContext;
    }

    /**
     * Check method introduced for WebServices integration
     *
     * @param descriptor appclient descriptor
     */
    public void check(Descriptor descriptor) throws Exception {
        // run persistence tests first.
        checkPersistenceUnits(ApplicationClientDescriptor.class.cast(descriptor));
        //An ApplicationClient can have WebService References
        checkWebServicesClient(descriptor);

        if (verifierFrameworkContext.isPartition() &&
                !verifierFrameworkContext.isAppClient())
            return;
        // run the ParseDD test
        if (getSchemaVersion(descriptor).compareTo("1.4") < 0) { // NOI18N
            AppClientDeploymentDescriptorFile ddf = new AppClientDeploymentDescriptorFile();
            File file = new File(getAbstractArchiveUri(descriptor),
                    ddf.getDeploymentDescriptorPath());
            FileInputStream is = new FileInputStream(file);
            try {
                if (is != null) {
                    Result result = new ParseDD().validateAppClientDescriptor(is);
                    result.setComponentName(getArchiveUri(descriptor));
                    setModuleName(result);
                    verifierFrameworkContext.getResultManager().add(result);
                }
            } finally {
                try {
                    if(is!=null)
                        is.close();
                } catch(Exception e) {}
            }
        }

        super.check(descriptor);
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
     * return the configuration file name for the list of tests pertinent to the
     * application client architecture
     *
     * @return <code>String</code> filename containing the list of tests
     */
    protected String getSunONETestsListFileName() {
        return sunONETestsListFileName;
    }

    protected String getSchemaVersion(Descriptor descriptor) {
        return ((RootDeploymentDescriptor) descriptor).getSpecVersion();
    }

    protected void setModuleName(Result r) {
        r.setModuleName(Result.APPCLIENT);
    }

    protected void checkWebServicesClient(Descriptor descriptor)
            throws Exception {
        if (verifierFrameworkContext.isPartition() &&
                !verifierFrameworkContext.isWebServicesClient())
            return;
        WebServiceClientCheckMgrImpl webServiceClientCheckMgr = 
                                new WebServiceClientCheckMgrImpl(verifierFrameworkContext);
        ApplicationClientDescriptor desc = (ApplicationClientDescriptor) descriptor;
        if (desc.hasWebServiceClients()) {
            Set serviceRefDescriptors = desc.getServiceReferenceDescriptors();
            Iterator it = serviceRefDescriptors.iterator();
            while (it.hasNext()) {
                webServiceClientCheckMgr.setVerifierContext(context);
                webServiceClientCheckMgr.check(
                        (ServiceReferenceDescriptor) it.next());
            }
        } else // set not applicable for all tests in WebServices for this Appclient Bundle 
            webServiceClientCheckMgr.setVerifierContext(context);
    }

    protected ComponentNameConstructor getComponentNameConstructor(
            Descriptor descriptor) {
        return new ComponentNameConstructor((ApplicationClientDescriptor)descriptor);
    }

}
