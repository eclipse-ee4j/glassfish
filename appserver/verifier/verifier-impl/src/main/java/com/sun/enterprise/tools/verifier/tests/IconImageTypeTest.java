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

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.SpecVersionMapper;
import com.sun.enterprise.deployment.*;
import org.glassfish.web.deployment.descriptor.ServletFilterDescriptor;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Enumeration;

import org.glassfish.api.deployment.archive.Archive;
import org.glassfish.deployment.common.Descriptor;

/**
 * This test is deried from Java EE platform spec.
 * See javaee_5.xsd
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class IconImageTypeTest extends VerifierTest implements VerifierCheck{
    private Collection<String> smallIconUris = new ArrayList<String>();
    private Collection<String> largeIconUris = new ArrayList<String>();
    private Descriptor descriptor;
    private Result result;
    ComponentNameConstructor compName;
    private static final String[] allowableImageTypesForJavaEEOlderThan5 = {".gif", ".jpg"};
    private static final String[] allowableImageTypesForJavaEE5 = {".gif", ".jpg", ".png"};
    public Result check(Descriptor descriptor) {
        this.descriptor = descriptor;
        compName = getVerifierContext().getComponentNameConstructor();
        result = getInitializedResult();
        result.setStatus(Result.PASSED);
        addGoodDetails(result, compName);
        result.passed(smh.getLocalString
                      (getClass().getName() + ".passed", //NOI18N
                       "No errors were detected.")); // NOI18N

        // Now collect all the Icon URIs that we are going to test
        collectIconURIs();
        testIconURIType();
        testIconURIExistence();
        return result;
    }

    private void testIconURIType() {
        String[] allowableImageTypes;
        String JavaEESchemaVersion = getVerifierContext().getJavaEEVersion();
        if (JavaEESchemaVersion.compareTo(SpecVersionMapper.JavaEEVersion_5) < 0){
            allowableImageTypes = allowableImageTypesForJavaEEOlderThan5;
        } else {
            allowableImageTypes = allowableImageTypesForJavaEE5;
        }

        Collection<String> allURIs = new ArrayList<String>(smallIconUris);
        allURIs.addAll(largeIconUris);
        for(String uri : allURIs){
            boolean passed = false;
            for(String allowableType : allowableImageTypes) {
                if(uri.endsWith(allowableType)) {
                    passed = true;
                    break;
                }
            }
            if(!passed){
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                        (getClass().getName() + ".failedImageType",
                                "Error: Unsupported image type used in icon image URI [ {0} ].",
                                new Object[]{uri}));
            }
        }
    }

    private void testIconURIExistence() {
        Collection<String> allURIs = new ArrayList<String>(smallIconUris);
        allURIs.addAll(largeIconUris);
        for(String uri : allURIs){
            Archive moduleArchive = getVerifierContext().getModuleArchive();
            boolean passed = false;
            for(Enumeration entries = moduleArchive.entries(); entries.hasMoreElements();){
                if(uri.equals(entries.nextElement())) {
                    passed = true;
                    break;
                }
            }
            if(!passed){
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                        (getClass().getName() + ".failedExistence",
                                "Error: icon image URI [ {0} ] is not packaged inside [ {1} ].",
                                new Object[]{uri, getVerifierContext().getModuleArchive().getURI()}));
            }
        }
    }

    private void collectIconURIs(){
        // in the absence of a proper Visitor pattern I am left with
        // little option but to use instanceof
        if(descriptor instanceof Application)
            collectIconURIs((Application)descriptor);
        else if(descriptor instanceof ApplicationClientDescriptor)
            collectIconURIs((ApplicationClientDescriptor)descriptor);
        else if(descriptor instanceof EjbDescriptor)
            collectIconURIs((EjbDescriptor)descriptor);
        else if(descriptor instanceof ConnectorDescriptor)
            collectIconURIs((ConnectorDescriptor)descriptor);
        else if(descriptor instanceof WebBundleDescriptor)
            collectIconURIs((WebBundleDescriptor)descriptor);
        else if(descriptor instanceof WebServiceEndpoint)
            collectIconURIs((WebServiceEndpoint)descriptor);
        else if(descriptor instanceof ServiceReferenceDescriptor)
            collectIconURIs((ServiceReferenceDescriptor)descriptor);
        else {
            // every time we introduce a new CheckMgrImpl, this will fail
            // that way we can be notified of the fact that this method needs
            // to be modified as well.
            throw new RuntimeException("Unexpected descriptor type.");
        }
    }

    // implementation that is common to descriptors that only contain
    // icon element at top level.
    private void collectIconURIs(Descriptor desc){
        String uri=desc.getSmallIconUri();
        if(uri!=null && uri.length()>0) smallIconUris.add(uri);
        uri = desc.getLargeIconUri();
        if(uri!=null && uri.length()>0) largeIconUris.add(uri);
    }

    private void collectIconURIs(WebBundleDescriptor webBundleDescriptor) {
        // this is for itself
        collectIconURIs((Descriptor)webBundleDescriptor);
        // now collect for each servlet
        for (WebComponentDescriptor o : webBundleDescriptor.getWebComponentDescriptors()){
            collectIconURIs(o);
        }
        // now collect for each servlet filter
        for (Object o : webBundleDescriptor.getServletFilterDescriptors()) {
            collectIconURIs(ServletFilterDescriptor.class.cast(o));
        }
    }

    private void collectIconURIs(WebServiceEndpoint webServiceEndpoint) {
        // WebService.xml is organised like this:
        // WebServicesDescriptor->WebService->WebServiceEndpoint
        // Since we don't have a CheckMgr that runs test for WebService.xml,
        // a work around would be to collect all Icons for all the parents
        // and test them here.
        // This means a problem there would be as many times as there are
        // end points.
        collectIconURIs(webServiceEndpoint.getWebService().getWebServicesDescriptor());
        collectIconURIs(webServiceEndpoint.getWebService());

        // this is for itself
        collectIconURIs((Descriptor)webServiceEndpoint);
        // now collect for each port-compont_handler in handler-chain
        for (Object o : webServiceEndpoint.getHandlers()){
            collectIconURIs(WebServiceHandler.class.cast(o));
        }
    }

    private void collectIconURIs(EjbDescriptor desc){
        // Since we don't have a CheckMgr that runs test for ejb-jar.xml,
        // a work around would be to collect all Icons for the parent
        // and test them here.
        // This means a problem there would be as many times as there are
        // beans.
        collectIconURIs(desc.getEjbBundleDescriptor());
        // this is for itself
        collectIconURIs((Descriptor)descriptor);
    }
}
