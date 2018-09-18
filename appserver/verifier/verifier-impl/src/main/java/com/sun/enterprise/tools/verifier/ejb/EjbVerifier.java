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

package com.sun.enterprise.tools.verifier.ejb;

import java.io.File;
import java.io.IOException;

import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.tools.verifier.BaseVerifier;
import com.sun.enterprise.tools.verifier.VerifierFrameworkContext;
import com.sun.enterprise.tools.verifier.SpecVersionMapper;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClassFileLoaderFactory;
import com.sun.enterprise.tools.verifier.apiscan.packaging.ClassPathBuilder;
import com.sun.enterprise.tools.verifier.apiscan.stdapis.EjbClosureCompiler;
import com.sun.enterprise.util.io.FileUtils;

/**
 * The class handles the EJB Verification.
 *
 * @author Vikas Awasthi
 */
public class EjbVerifier extends BaseVerifier {

    private EjbBundleDescriptor ejbd = null;
    private String classPath;//this is lazily populated in getClassPath()
    private boolean isASMode = false;

    public EjbVerifier(VerifierFrameworkContext verifierFrameworkContext,
                       EjbBundleDescriptor ejbd) {
        this.verifierFrameworkContext = verifierFrameworkContext;
        this.ejbd = ejbd;
        this.isASMode = !verifierFrameworkContext.isPortabilityMode();
    }

    public void verify() throws Exception {
        if (areTestsNotRequired(verifierFrameworkContext.isEjb()) &&
                areTestsNotRequired(verifierFrameworkContext.isWebServices()) &&
                areTestsNotRequired(verifierFrameworkContext.isWebServicesClient()) &&
                areTestsNotRequired(verifierFrameworkContext.isPersistenceUnits()))
            return;

        preVerification();
        createClosureCompiler();//this can be moved up to base verifier in future.
        verify(ejbd, new EjbCheckMgrImpl(verifierFrameworkContext));
    }

    public Descriptor getDescriptor() {
        return ejbd;
    }

    /**
     * Create the classloader from the extracted directory
     */
    protected ClassLoader createClassLoader()
            throws IOException {
        return ejbd.getClassLoader();
    }

    protected String getArchiveUri() {
        return FileUtils.makeFriendlyFilename(ejbd.getModuleDescriptor().getArchiveUri());
    }

    protected String[] getDDString() {
        String dd[] = {"META-INF/sun-ejb-jar.xml", "META-INF/ejb-jar.xml", "META-INF/glassfish-ejb-jar.xml", // NOI18N
                       "META-INF/webservices.xml"}; // NOI18N
        return dd;
    }

    /**
     * Creates and returns the class path associated with the ejb archive.
     * Uses the exploded location of the archive for generating the classpath.
     *
     * @return entire classpath string
     * @throws IOException
     */
    protected String getClassPath() throws IOException {
        if (classPath != null) return classPath;

        if(isASMode)
            return (classPath = getClassPath(verifierFrameworkContext.getClassPath()));

        String cp;
        if (!ejbd.getModuleDescriptor().isStandalone()) {
            //take the cp from the enclosing ear file
            String ear_uri = verifierFrameworkContext.getExplodedArchivePath();
            File ear = new File(ear_uri);
            assert(ear.isDirectory());
            String earCP = ClassPathBuilder.buildClassPathForEar(ear);
            String libdir = ejbd.getApplication().getLibraryDirectory();
            if (libdir!=null) {
                earCP = getLibdirClasspath(ear_uri, libdir) + earCP;
            }
            String module_uri = ejbd.getModuleDescriptor().getArchiveUri();//this is a relative path
            File module = new File(module_uri);
            assert(module.isFile() && !module.isAbsolute());
            // exploder creates the directory replacing all dots by '_'
            File explodedModuleDir = new File(ear_uri,
                    FileUtils.makeFriendlyFilename(module_uri));
            String moduleCP = ClassPathBuilder.buildClassPathForJar(
                    explodedModuleDir);
            cp = moduleCP + File.pathSeparator + earCP;
        } else {
            String module_uri = verifierFrameworkContext.getExplodedArchivePath();//this is an absolute path
            File module = new File(module_uri);
            assert(module.isDirectory() && module.isAbsolute());
            cp = ClassPathBuilder.buildClassPathForJar(module);
        }
        return (classPath = cp);
    }

    /**
     * creates the ClosureCompiler for the ejb module and sets it to the
     * verifier context. This is used to compute the closure on the classes used
     * in the ejb archive.
     *
     * @throws IOException
     */
    protected void createClosureCompiler() throws IOException {
        String specVer = SpecVersionMapper.getEJBVersion(
                verifierFrameworkContext.getJavaEEVersion());
        Object arg = (isASMode)?ejbd.getClassLoader():(Object)getClassPath();
        EjbClosureCompiler cc = new EjbClosureCompiler(specVer,
                ClassFileLoaderFactory.newInstance(new Object[]{arg}));
        context.setClosureCompiler(cc);
    }
}
