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
import java.io.IOException;
import java.security.SecureRandom;

import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import org.glassfish.deployment.common.ModuleDescriptor;
import com.sun.enterprise.tools.verifier.BaseVerifier;
import com.sun.enterprise.tools.verifier.VerifierFrameworkContext;
import com.sun.enterprise.tools.verifier.SpecVersionMapper;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClassFileLoaderFactory;
import com.sun.enterprise.tools.verifier.apiscan.packaging.ClassPathBuilder;
import com.sun.enterprise.tools.verifier.apiscan.stdapis.WebClosureCompiler;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.loader.ASURLClassLoader;

/**
 * Responsible for verfying the j2ee war archive.
 *
 * @author Vikas Awasthi
 */
public class WebVerifier extends BaseVerifier {

    private WebBundleDescriptor webd = null;
    private String classPath;//this is lazily populated in getClassPath()
    private boolean isASMode = false;
    private File jspOutDir = null;

    public WebVerifier(VerifierFrameworkContext verifierFrameworkContext,
                       WebBundleDescriptor webd) {
        this.verifierFrameworkContext = verifierFrameworkContext;
        this.webd = webd;
        this.isASMode = !verifierFrameworkContext.isPortabilityMode();
    }

    /**
     * Responsible for running web based verifier tests on the the web archive.
     * Called from runVerifier in {@link BaseVerifier} class.
     *
     * @throws Exception
     */
    public void verify() throws Exception {
        if (areTestsNotRequired(verifierFrameworkContext.isWeb()) &&
                areTestsNotRequired(verifierFrameworkContext.isWebServices()) &&
                areTestsNotRequired(verifierFrameworkContext.isWebServicesClient()) &&
                areTestsNotRequired(verifierFrameworkContext.isPersistenceUnits()))
            return;

        jspOutDir = getJspOutDir();
        try {
            preVerification();
            context.setOutDir(jspOutDir);
            createClosureCompiler();
            verify(webd, new WebCheckMgrImpl(verifierFrameworkContext));
        } finally {
            // verificationContext.getJspOutDir() will be non-null only when the
            // call is from deployment backend and precompilejsp is set
            if(verifierFrameworkContext.getJspOutDir()==null)
                FileUtils.whack(jspOutDir);
        }
    }

    /**
     *
     * @return web bundle descriptor
     */
    public Descriptor getDescriptor() {
        return webd;
    }

    /**
     * Creates the ClassLoader for the war archive.
     *
     * @return ClassLoader
     * @throws IOException
     */
    protected ClassLoader createClassLoader()
            throws IOException {
        ASURLClassLoader ASURLClassLoader = new ASURLClassLoader(webd.getClassLoader());
        ASURLClassLoader.appendURL(jspOutDir);
        return ASURLClassLoader;
    }

    /**
     *
     * @return name of the war archive
     */
    protected String getArchiveUri() {
        return FileUtils.makeFriendlyFilename(webd.getModuleDescriptor().getArchiveUri());
    }

    /**
     * @return the array of deployment descriptor names
     */
    protected String[] getDDString() {
        String dd[] = {"WEB-INF/sun-web.xml", "WEB-INF/web.xml", // NOI18N
                "WEB-INF/glassfish-web.xml", "WEB-INF/webservices.xml"}; // NOI18N
        return dd;
    }

    /**
     * Creates and returns the class path associated with the web archive.
     * Uses the exploded location of the archive for generating the classpath.
     *
     * @return entire classpath string
     * @throws IOException
     */
    protected String getClassPath() throws IOException {
        if (classPath != null) return classPath;

        if(isASMode)
            return (classPath = getClassPath(verifierFrameworkContext.getClassPath()) +
                                File.pathSeparator + 
                                jspOutDir.getAbsolutePath());

        String cp;
        if (!webd.getModuleDescriptor().isStandalone()) {
            //take the cp from the enclosing ear file
            String ear_uri = verifierFrameworkContext.getExplodedArchivePath();
            File ear = new File(ear_uri);
            assert(ear.isDirectory());
            String earCP = ClassPathBuilder.buildClassPathForEar(ear);
            String libdir = webd.getApplication().getLibraryDirectory();
            if (libdir!=null) {
                earCP = getLibdirClasspath(ear_uri, libdir) + earCP;
            }
            String module_uri = webd.getModuleDescriptor().getArchiveUri();//this is a relative path
            File module = new File(module_uri);
            assert(module.isFile() && !module.isAbsolute());
            // exploder creates the directory replacing all dots by '_'
            File explodedModuleDir = new File(ear_uri,
                    FileUtils.makeFriendlyFilename(module_uri));
            String moduleCP = ClassPathBuilder.buildClassPathForWar(
                    explodedModuleDir);
            cp = moduleCP + File.pathSeparator + earCP;
        } else {
            String module_uri = verifierFrameworkContext.getExplodedArchivePath();//this is an absolute path
            File module = new File(module_uri);
            assert(module.isDirectory() && module.isAbsolute());
            cp = ClassPathBuilder.buildClassPathForWar(module);
        }
        String as_lib_root=System.getProperty("com.sun.aas.installRoot")+File.separator+"lib"+File.separator;
        if (verifierFrameworkContext.getJavaEEVersion().compareTo("5") >= 0) { // NOI18N
            cp += File.pathSeparator+as_lib_root+"javax.faces.jar"+File.pathSeparator+ // NOI18N
                  as_lib_root+"appserv-jstl.jar"+File.pathSeparator; // NOI18N
        }
        cp = cp + File.pathSeparator + jspOutDir.getAbsolutePath();
        return (classPath = cp);
    }

    /**
     * creates the ClosureCompiler for the web module and sets it to the
     * verifier context. This is used to compute the closure on the classes used
     * in the web archive.
     *
     * @throws IOException
     */
    protected void createClosureCompiler() throws IOException {
        String specVer = SpecVersionMapper.getWebAppVersion(
                verifierFrameworkContext.getJavaEEVersion());
        Object arg = (isASMode)?context.getClassLoader():(Object)getClassPath();
        WebClosureCompiler cc = new WebClosureCompiler(specVer,
                ClassFileLoaderFactory.newInstance(new Object[]{arg}));
        context.setClosureCompiler(cc);
    }

    /**
     * If precompilejsp is set in the backend then return the jspOutput 
     * directory set in the frameworkContext. Otherwise create a new unique 
     * directory and return it.
     * @return the output directory where compiled JSPs will be put.
     */ 
    private File getJspOutDir(){
        // verificationContext.getJspOutDir() will be non-null only when the
        // call is from deployment backend and precompilejsp is set
        File jspOutDir = verifierFrameworkContext.getJspOutDir();
        if(jspOutDir != null) {
            ModuleDescriptor moduleDescriptor = webd.getModuleDescriptor();
            if(moduleDescriptor.isStandalone())
                return jspOutDir;
            return new File(jspOutDir, FileUtils.makeFriendlyFilename(moduleDescriptor.getArchiveUri()));
        }
        SecureRandom random=new SecureRandom();
        String prefix=System.getProperty("java.io.tmpdir")+File.separator+".jspc";
        do{
            float f=random.nextFloat();
            String outDirPath=new String(prefix+f);
            File out=new File(outDirPath);
            if(out.mkdirs()) 
                return out;
        }while(true);
    }
}
