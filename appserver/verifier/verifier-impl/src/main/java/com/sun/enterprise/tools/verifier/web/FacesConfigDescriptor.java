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

import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import org.glassfish.deployment.common.ModuleDescriptor;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.deploy.shared.FileArchive;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.NodeList;

/**
 *
 * Class which represents WEB-INF/faces-config.xml
 *
 * @author bshankar@sun.com
 *
 */
public class FacesConfigDescriptor {
    
    private final String MANAGED_BEAN_CLASS = "managed-bean-class"; // NOI18N
    private final String facesConfigFileName = "WEB-INF/faces-config.xml"; // NOI18N
    
    private VerifierTestContext context;
    private Document facesConfigDocument;
    
    public FacesConfigDescriptor(VerifierTestContext context, WebBundleDescriptor descriptor) {
        try {
            this.context = context;
            readFacesConfigDocument(descriptor);
        } catch(Exception ex) {
            facesConfigDocument = null;
        }
    }
    
    private void readFacesConfigDocument(WebBundleDescriptor webd) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // TODO: Why are we turning off validation here?
        factory.setValidating(false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        ModuleDescriptor moduleDesc = webd.getModuleDescriptor();
        String archBase = context.getAbstractArchive().getURI().toString();
        String uri = null;
        if(moduleDesc.isStandalone()){
            uri = archBase;
        } else {
            uri = archBase + File.separator +
                    FileUtils.makeFriendlyFilename(moduleDesc.getArchiveUri());
        }
        FileArchive arch = new FileArchive();
        arch.open(URI.create(uri));
        InputStream is = arch.getEntry(facesConfigFileName);
        InputSource source = new InputSource(is);
        try {
            facesConfigDocument = builder.parse(source);
        } finally {
            try{
                if(is != null)
                    is.close();
            } catch(Exception e) {}
        }
    }
    
    public List<String> getManagedBeanClasses() {
        if (facesConfigDocument == null) {
            return new ArrayList<String>();
        }
        NodeList nl = facesConfigDocument.getElementsByTagName(MANAGED_BEAN_CLASS);
        List<String> classes = new ArrayList<String>();
        if (nl != null) {
            int size = nl.getLength();
            for (int i = 0; i < size; i++) {
                classes.add(nl.item(i).getFirstChild().getNodeValue().trim());
            }
        }
        return classes;
    }
    
}
