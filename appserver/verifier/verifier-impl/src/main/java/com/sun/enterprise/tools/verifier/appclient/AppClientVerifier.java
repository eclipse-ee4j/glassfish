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
import java.io.IOException;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.tools.verifier.BaseVerifier;
import com.sun.enterprise.tools.verifier.VerifierFrameworkContext;
import com.sun.enterprise.tools.verifier.SpecVersionMapper;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClassFileLoaderFactory;
import com.sun.enterprise.tools.verifier.apiscan.packaging.ClassPathBuilder;
import com.sun.enterprise.tools.verifier.apiscan.stdapis.AppClientClosureCompiler;
import com.sun.enterprise.util.io.FileUtils;

/**
 * @author Vikas Awasthi
 */
public class AppClientVerifier extends BaseVerifier {

    private ApplicationClientDescriptor appclientd = null;
    private String classPath;//this is lazily populated in getClassPath()
    private boolean isASMode = false;

    public AppClientVerifier(VerifierFrameworkContext verifierFrameworkContext,
                             ApplicationClientDescriptor appClientDescriptor) {
        this.verifierFrameworkContext = verifierFrameworkContext;
        this.appclientd = appClientDescriptor;
        this.isASMode = !verifierFrameworkContext.isPortabilityMode();
    }

    /**
     * Responsible for running application client based verifier tests on the the web archive.
     * Called from runVerifier in {@link BaseVerifier} class.
     *
     * @throws Exception
     */
    public void verify() throws Exception {
        if (areTestsNotRequired(verifierFrameworkContext.isAppClient()) &&
                areTestsNotRequired(verifierFrameworkContext.isWebServicesClient()) &&
                areTestsNotRequired(verifierFrameworkContext.isPersistenceUnits()))
            return;

        preVerification();
        createClosureCompiler();//this can be moved up to base verifier in future.
        verify(appclientd, new AppClientCheckMgrImpl(verifierFrameworkContext));
    }

    public Descriptor getDescriptor() {
        return appclientd;
    }

    protected ClassLoader createClassLoader()
            throws IOException {
        return appclientd.getClassLoader();
    }

    protected String getArchiveUri() {
        return FileUtils.makeFriendlyFilename(appclientd.getModuleDescriptor().getArchiveUri());
    }

    protected String[] getDDString() {
        String dd[] = {"META-INF/sun-application-client.xml", // NOI18N
                       "META-INF/application-client.xml", 
                       "META-INF/glassfish-application-client.xml"}; // NOI18N
        return dd;
    }

    /**
     * Creates and returns the class path associated with the client jar.
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
        if (!appclientd.getModuleDescriptor().isStandalone()) {
            //take the cp from the enclosing ear file
            String ear_uri = verifierFrameworkContext.getExplodedArchivePath();
            File ear = new File(ear_uri);
            assert(ear.isDirectory());
            String earCP = ClassPathBuilder.buildClassPathForEar(ear);
            String libdir = appclientd.getApplication().getLibraryDirectory();
            if (libdir!=null) {
                earCP = getLibdirClasspath(ear_uri, libdir) + earCP;
            }
            String module_uri = appclientd.getModuleDescriptor().getArchiveUri();//this is a relative path
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
     * creates the ClosureCompiler for the client jar and sets it to the
     * verifier context. This is used to compute the closure on the classes used
     * in the client jar.
     *
     * @throws IOException
     */
    protected void createClosureCompiler() throws IOException {
        String specVer = SpecVersionMapper.getAppClientVersion(
                verifierFrameworkContext.getJavaEEVersion());
        Object arg = (isASMode)?appclientd.getClassLoader():(Object)getClassPath();
        AppClientClosureCompiler cc = new AppClientClosureCompiler(specVer,
                ClassFileLoaderFactory.newInstance(new Object[]{arg}));
        context.setClosureCompiler(cc);
    }

}
