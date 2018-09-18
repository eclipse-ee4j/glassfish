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

package com.sun.enterprise.tools.verifier.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.dd.ParseDD;
import com.sun.enterprise.tools.verifier.wsclient.WebServiceClientCheckMgrImpl;

import org.glassfish.web.deployment.io.WebDeploymentDescriptorFile;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Web harness
 */
public class WebCheckMgrImpl extends CheckMgr implements JarCheck {

    /**
     * name of the file containing the list of tests for the web architecture
     */
    private static final String testsListFileName = "TestNamesWeb.xml"; // NOI18N
    private static final String sunONETestsListFileName = getSunPrefix()
            .concat(testsListFileName);
    private static TagLibDescriptor[] tlds;

    public WebCheckMgrImpl(VerifierFrameworkContext verifierFrameworkContext) {
        this.verifierFrameworkContext = verifierFrameworkContext;
    }

    /**
     * Check method introduced for WebServices integration
     *
     * @param descriptor Web descriptor
     */
    public void check(Descriptor descriptor) throws Exception {
        // run persistence tests first.
        checkPersistenceUnits(WebBundleDescriptor.class.cast(descriptor));
        // a WebBundleDescriptor can have an WebServicesDescriptor
        checkWebServices(descriptor);
        // a WebBundleDescriptor can have  WebService References
        checkWebServicesClient(descriptor);

        if (verifierFrameworkContext.isPartition() &&
                !verifierFrameworkContext.isWeb())
            return;

        createTaglibDescriptors(descriptor); //create document obj for all tld's defined in the war
        
        createFacesConfigDescriptor(descriptor);
        
        // run the ParseDD test
        if (getSchemaVersion(descriptor).compareTo("2.4") < 0) { // NOI18N
            WebDeploymentDescriptorFile ddf = new WebDeploymentDescriptorFile();
            File file = new File(getAbstractArchiveUri(descriptor),
                    ddf.getDeploymentDescriptorPath());
            FileInputStream is = new FileInputStream(file);
            try {
                if (is != null) {
                    Result result = new ParseDD().validateWebDescriptor(is);
                    result.setComponentName(getArchiveUri(descriptor));
                    setModuleName(result);
                    verifierFrameworkContext.getResultManager().add(result);
                    is.close();
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
     * <p/>
     * return the configuration file name for the list of tests pertinent to the
     * web app space (jsp and servlet) </p>
     *
     * @return <code>String</code> filename containing the list of tests
     */
    protected String getTestsListFileName() {
        return testsListFileName;
    }

    /**
     * @return <code>String</code> filename containing the  SunONE tests
     */
    protected String getSunONETestsListFileName() {
        return sunONETestsListFileName;
    }

    /**
     * Create array of TagLibDescriptors for all the jsp tag lib files defined
     * in the war. Set the array in the verifier Context
     *
     * @param descriptor
     */
    protected void createTaglibDescriptors(Descriptor descriptor) {
        TagLibFactory tlf = new TagLibFactory(context, verifierFrameworkContext);
        tlds = tlf.getTagLibDescriptors((WebBundleDescriptor) descriptor);
        if (tlds != null) {
            context.setTagLibDescriptors(tlds);
            setVerifierContext(context);
        }
    }

    /**
     * Create FacesConfigDescriptor
     *
     * @param descriptor
     */
    protected void createFacesConfigDescriptor(Descriptor descriptor) {
        FacesConfigDescriptor d = new FacesConfigDescriptor(context, (WebBundleDescriptor)descriptor);
        context.setFacesConfigDescriptor(d);
    }
    
    protected void checkWebServicesClient(Descriptor descriptor)
            throws Exception {
        if (verifierFrameworkContext.isPartition() &&
                !verifierFrameworkContext.isWebServicesClient())
            return;

        WebBundleDescriptor desc = (WebBundleDescriptor) descriptor;
        WebServiceClientCheckMgrImpl webServiceClientCheckMgr = new WebServiceClientCheckMgrImpl(
                verifierFrameworkContext);
        if (desc.hasWebServiceClients()) {
            Set serviceRefDescriptors = desc.getServiceReferenceDescriptors();
            Iterator it = serviceRefDescriptors.iterator();

            while (it.hasNext()) {
                webServiceClientCheckMgr.setVerifierContext(context);
                webServiceClientCheckMgr.check(
                        (ServiceReferenceDescriptor) it.next());
            }
        }
    }

    protected String getSchemaVersion(Descriptor descriptor) {
        return ((WebBundleDescriptor) descriptor).getSpecVersion();
    }

    protected void setModuleName(Result r) {
        r.setModuleName(Result.WEB);
    }

    /**
     * If the call is from deployment backend and precompilejsp option is set 
     * then there is no need to run the AllJSPsMustBeCompilable test. 
     * @return list of excluded tests
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */ 
    protected Vector<TestInformation> getTestFromExcludeList() throws ParserConfigurationException, SAXException, IOException {
        Vector<TestInformation> tests = super.getTestFromExcludeList();
        if(verifierFrameworkContext.getJspOutDir() !=null) { // pre-compile jsp flag set
            TestInformation ti = new TestInformation();
            ti.setClassName("com.sun.enterprise.tools.verifier.tests.web.AllJSPsMustBeCompilable"); // NOI18N
            tests.addElement(ti);
        }
        return tests;
    }

    protected ComponentNameConstructor getComponentNameConstructor(
            Descriptor descriptor) {
        return new ComponentNameConstructor((WebBundleDescriptor)descriptor);
    }

}
