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

package com.sun.enterprise.tools.verifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.tools.verifier.util.LogDomains;
import com.sun.enterprise.tools.verifier.util.XMLValidationHandler;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.deploy.shared.FileArchive;

/**
 * The base class of all the verifiers. It has all the common
 * logic required in the verification process.
 *
 * @author Vikas Awasthi
 */
public abstract class BaseVerifier {

    protected Logger logger = LogDomains.getLogger(LogDomains.AVK_VERIFIER_LOGGER);
    protected VerifierFrameworkContext verifierFrameworkContext = null;
    protected VerifierTestContext context = null;

    public abstract void verify() throws Exception;

    public abstract Descriptor getDescriptor();

    protected abstract ClassLoader createClassLoader()
            throws IOException;

    protected abstract String getArchiveUri();

    protected abstract String getClassPath() throws IOException;

    protected void createDocumentObject(Descriptor descriptor)
            throws IOException, ParserConfigurationException, SAXException {
        InputStream is = null;
        InputSource source = null;

        String archBase = new File(context.getAbstractArchive().getURI()).getAbsolutePath();
        String uri = null;
        if(descriptor instanceof Application) {
            uri = archBase;
        } else {
            BundleDescriptor bundleDesc =
                BundleDescriptor.class.cast(descriptor);
            if(bundleDesc.getModuleDescriptor().isStandalone()) {
                uri = archBase;
            } else {
                uri = archBase + File.separator + getArchiveUri();
            }
        }
        String dd[] = getDDString();
        for (int i = 0; i < dd.length; i++) {
            try{
                is = getInputStreamFromAbstractArchive(uri, dd[i]);
                if (is != null) {
                    source = new InputSource(is);
                    createDOMObject(source, dd[i]);
                    is.close();
                }
            } finally {
                try {
                    if(is != null) {
                        is.close();
                    }
                } catch(Exception e) {}
            }
        }
    }

    protected boolean areTestsNotRequired(
            boolean isModuleGivenInPartiotioningArgs) {
        return (verifierFrameworkContext.isPartition() &&
                !isModuleGivenInPartiotioningArgs);
    }

    protected void preVerification() throws Exception {
        logger.log(Level.INFO, "Verifying: [ " + getArchiveUri() + " ]");
        ClassLoader loader = createClassLoader();
        context = new VerifierTestContext(loader);
        context.setAppserverMode(!verifierFrameworkContext.isPortabilityMode());
        context.setAbstractArchive(verifierFrameworkContext.getArchive());
        context.setClassPath(getClassPath());
        logger.log(Level.FINE, "Using CLASSPATH: " + getClassPath());
    }

    protected void verify(Descriptor descriptor, CheckMgr checkMgrImpl)
            throws Exception {
        //hs for creating DOM object for runtime tests
        createDocumentObject(descriptor);
        // now start calling each individual test, as per spec
        checkMgrImpl.setVerifierContext(context);
        checkMgrImpl.check(descriptor);
        logger.log(Level.FINE,
                getClass().getName() + ".debug.endStaticVerification"); // NOI18N
    }

    protected void createDOMObject(InputSource source, String dd)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        EntityResolver dh = new XMLValidationHandler(false);
        builder.setEntityResolver(dh);
        Document document = builder.parse(source);

        if ((dd.indexOf("sun-")) < 0) { // NOI18N
            if ((dd.indexOf("webservices")) < 0) { // NOI18N
                context.setDocument(document);
            } else {
                context.setWebServiceDocument(document);
            }
        } else
            context.setRuntimeDocument(document);
    }

    protected abstract String[] getDDString();

    protected InputStream getInputStreamFromAbstractArchive(String uri,
                                                            String ddName)
            throws IOException {
        FileArchive arch = new FileArchive();
        arch.open(new File(uri).toURI());
        InputStream deploymentEntry = arch.getEntry(ddName);
        return deploymentEntry;
    }

    /**
     *
     * @param appRoot The location of the exploded archive
     * @param libdirPath library-directory location as specified in aplication.xml
     * @return Classpath string containing list of jar files in the library directory
     * separated by File.separator char
     * @throws IOException
     */
    protected String getLibdirClasspath(String appRoot, String libdirPath) throws IOException{
        StringBuilder classpath=new StringBuilder();
        libdirPath = libdirPath.replace('/', File.separatorChar);
        List<String> jars = getAllJars(new File(appRoot, libdirPath));
        for (String s : jars) {
            classpath.append(s);
            classpath.append(File.pathSeparator);
        }
        return classpath.toString();
    }

    /**
     *
     * @param file
     * @return returns a list of jars in the a directory
     * @throws IOException
     */
    private List<String> getAllJars(File file) throws IOException{
        List<String> list = new ArrayList<String>();
        if (file.isDirectory() || file.canRead()) {
            File[] files = file.listFiles();
            for (int i=0; i<files.length; i++) {
                File jar = files[i];
                if ( FileUtils.isJar(jar)) {
                    list.add( jar.getCanonicalPath() );
                }
            }
        }
        return list;
    }

    /** converts list of paths to a string of paths separated by pathSeparator*/
    protected String getClassPath(List<String> classPath) {
        if (classPath == null) return "";
        StringBuilder cp = new StringBuilder("");
        for (int i = 0; i < classPath.size(); i++) {
            cp.append(classPath.get(i));
            cp.append(File.pathSeparator);
        }
        return cp.toString();
    }
}
