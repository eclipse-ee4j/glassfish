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


import com.sun.enterprise.tools.verifier.web.FacesConfigDescriptor;
import org.w3c.dom.Document;
import org.glassfish.api.deployment.archive.Archive;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClosureCompiler;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.jdo.spi.persistence.support.ejb.ejbc.JDOCodeGenerator;

import java.io.File;

/**
 * <p/>
 * Context for each test execution
 * </p>
 *
 * @author Sheetal Vartak
 */
public class VerifierTestContext
{

    private ClassLoader classLoader = null;

    private ClassLoader altClassLoader = null;

    //start IASRI 4725528
    private boolean isAppserverMode = false;
    //end IASRI 4725528

    // provides the abstract archive for use by tests which cannot get the
    // physical EAR file
    private Archive archive = null;

    // this represents the archive that represents the module jar/war/rar.
    private Archive moduleArchive;

    // added for webservice clients to store current ejbdescriptor
    // which contains the service-ref
    private EjbDescriptor ejbdesc = null;

    private boolean isXMLBasedOnSchema = false;
    private String stdAloneUri = null;
    private Document runtimeDoc = null;
    private Document doc = null;
    private Document webservicedoc = null;
    private Throwable JDOExceptionObject = null;
    private TagLibDescriptor[] taglibDescriptors = null;
    private FacesConfigDescriptor facesConfigDescriptor = null;
//    private Verifier verifier;
    // the JDO codegenerator instance to be used by tests
    // set from EJBCheckMgr
    private JDOCodeGenerator jdc = null;
    private ClosureCompiler cc;
    private String classPath;//used by JspC. See AllJSPsMustBeCompilable test
    private File outDir;//used by AllJSPsMustBeCompilable & WebArchiveClassesLoadable
    // This represent's component's schema version.
    private String schemaVersion="";

    // this denotes the corresponding Java EE version.
    private String javaEEVersion;

    // denotes the name of the component. Used in each test for reporting
    private ComponentNameConstructor compName;

    public VerifierTestContext() {
        classLoader = null;
    }

    public VerifierTestContext(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getRarClassLoader() {
        return classLoader;
    }

    public void setRarClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    //start IASRI 4725528
    public void setAppserverMode(boolean isBackend) {
        this.isAppserverMode = isBackend;
    }

    public boolean isAppserverMode() {
        return isAppserverMode;
    }
    //end IASRI 4725528

    public void setAbstractArchive(Archive arch) {
        archive = arch;
    }

    /**
     * Retrieve the Abstract Archive file in test
     *
     * @return <code>String</code> handle to current application .ear file
     */
    public Archive getAbstractArchive() {
        return archive;
    }

    /**
     * Added for WebServices Client verification
     *
     * @return The <code>EjbDescriptor</code> for the current ServiceReferenceDescriptor
     *         being validated by tests
     */
    public EjbDescriptor getEjbDescriptorForServiceRef() {
        return ejbdesc;
    }

    /**
     * Added for WebServices Client verification
     * SET The <code>EjbDescriptor</code> for the current ServiceReferenceDescriptor
     * being validated by tests
     */
    public void setEjbDescriptorForServiceRef(EjbDescriptor desc) {
        ejbdesc = desc;
    }

    public void setAlternateClassLoader(ClassLoader l) {
        altClassLoader = l;
    }

    public ClassLoader getAlternateClassLoader() {
        return altClassLoader;
    }

    public boolean getisXMLBasedOnSchema() {
        return isXMLBasedOnSchema;
    }
//
//     public void setisXMLBasedOnSchema(boolean value) {
//         isXMLBasedOnSchema = value;
//     }

    /*
     *getRuntimeDocument()
     *    returns the document object created for runtime descriptor
     */
    public Document getRuntimeDocument() {
        return runtimeDoc;
    }

    /*
     *setRuntimeDocement(Document value)
     */
    public void setRuntimeDocument(Document value) {
        runtimeDoc = value;
    }

    /*
     *getDocument()
     *    returns the document object created for runtime descriptor
     */
    public Document getDocument() {
        return doc;
    }

    /*
     *setDocement(Document value)
     */
    public void setDocument(Document value) {
        doc = value;
    }

    /* get the document object for webservices.xml
     * setWebServiceDocement(Document value)
     */

    public Document getWebServiceDocument() {
        return webservicedoc;
    }

    /*Set the document object of webservices.xml
     *setWebServiceDocement(Document value)
     */
    public void setWebServiceDocument(Document value) {
        webservicedoc = value;
    }

    public String getStdAloneArchiveURI() {
        return stdAloneUri;

    }

    public void setStdAloneArchiveURI(String uri) {
        stdAloneUri = uri;
    }

    public void setJDOCodeGenerator(JDOCodeGenerator gen) {
        jdc = gen;
    }

    public JDOCodeGenerator getJDOCodeGenerator() {
        return jdc;
    }

    public void setJDOException(Throwable ex) {
        JDOExceptionObject = ex;
    }

    public Throwable getJDOException() {
        return JDOExceptionObject;
    }

    public void setTagLibDescriptors(TagLibDescriptor[] tld) {
        taglibDescriptors = tld;
    }

    public TagLibDescriptor[] getTagLibDescriptors() {
        return taglibDescriptors;
    }

    public void setFacesConfigDescriptor(FacesConfigDescriptor d) {
        facesConfigDescriptor = d;
    }

    public FacesConfigDescriptor getFacesConfigDescriptor() {
        return facesConfigDescriptor;
    }
    
//    public Verifier getVerifier () {
//        return verifier;
//    }
//
//    public void setVerifier(Verifier verifier) {
//        this.verifier = verifier;
//    }
    
    public void setClosureCompiler(ClosureCompiler cc) {
        this.cc = cc;
    }

    public ClosureCompiler getClosureCompiler() {
        return cc;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String cp) {
        classPath = cp;
    }

    public File getOutDir() {
        return outDir;
    }

    public void setOutDir(File outDir) {
        this.outDir = outDir;
    }
    
    public void setSchemaVersion(String ver) {
        this.schemaVersion = ver;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public ComponentNameConstructor getComponentNameConstructor(){
        return compName;
    }

    public void setComponentNameConstructor(ComponentNameConstructor compName){
        this.compName = compName;
    }

    public String getJavaEEVersion(){
        return javaEEVersion;
    }

    public void setJavaEEVersion(String v){
        javaEEVersion = v;
    }

    /**
     * This method returns the module archive while
     * {@link #getAbstractArchive()} returns an Archive representing the
     * application that this module is embedded in.
     * For standalone jar/war they will return same archive
     * The caller MUST close the archive.
     * @return the archive for the module archive.
     */
    public Archive getModuleArchive() {
        return moduleArchive;
    }

    public void setModuleArchive(Archive a) {
        moduleArchive = a;
    }
}
