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

package com.sun.enterprise.tools.verifier.connector;

import java.io.File;
import java.io.IOException;

import com.sun.enterprise.deployment.ConnectorDescriptor;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.tools.verifier.BaseVerifier;
import com.sun.enterprise.tools.verifier.VerifierFrameworkContext;
import com.sun.enterprise.tools.verifier.SpecVersionMapper;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClassFileLoaderFactory;
import com.sun.enterprise.tools.verifier.apiscan.packaging.ClassPathBuilder;
import com.sun.enterprise.tools.verifier.apiscan.stdapis.ConnectorClosureCompiler;
import com.sun.enterprise.util.io.FileUtils;

/**
 * @author Vikas Awasthi
 */
public class ConnectorVerifier extends BaseVerifier {

    private ConnectorDescriptor cond = null;
    private String classPath;//this is lazily populated in getClassPath()
    private boolean isASMode = false;

    public ConnectorVerifier(VerifierFrameworkContext verifierFrameworkContext,
                             ConnectorDescriptor cond) {
        this.verifierFrameworkContext = verifierFrameworkContext;
        this.cond = cond;
        this.isASMode = !verifierFrameworkContext.isPortabilityMode();
    }

    public void verify() throws Exception {
        if (areTestsNotRequired(verifierFrameworkContext.isConnector()))
            return;

        preVerification();
        createClosureCompiler();//this can be moved up to base verifier in future.
        verify(cond, new ConnectorCheckMgrImpl(verifierFrameworkContext));
    }

    public Descriptor getDescriptor() {
        return cond;
    }

    protected ClassLoader createClassLoader()
            throws IOException {
        return cond.getClassLoader();
    }

    protected String getArchiveUri() {
        return FileUtils.makeFriendlyFilename(cond.getModuleDescriptor().getArchiveUri());
    }

    protected String[] getDDString() {
        String dd[] = {"META-INF/sun-ra.xml", "META-INF/ra.xml"}; // NOI18N
        return dd;
    }

    /**
     * Creates and returns the class path associated with the rar.
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
        if (!cond.getModuleDescriptor().isStandalone()) {
            //take the cp from the enclosing ear file
            String ear_uri = verifierFrameworkContext.getExplodedArchivePath();
            File ear = new File(ear_uri);
            assert(ear.isDirectory());
            cp = ClassPathBuilder.buildClassPathForEar(ear);
            String libdir = cond.getApplication().getLibraryDirectory();
            if (libdir!=null) {
                cp = getLibdirClasspath(ear_uri, libdir) + cp;
            }
            /** buildClasspathForEar takes care of embedded rars.*/
/*
            //this is a relative path
            String module_uri = cond.getModuleDescriptor().getArchiveUri();
            File module = new File(module_uri);
            assert(module.isFile() && !module.isAbsolute());
            // exploder creates the directory replacing all dots by '_'
            File explodedModuleDir = new File(ear_uri,
                    FileUtils.makeFriendlyFilename(module_uri));
            String moduleCP = ClassPathBuilder.buildClassPathForRar(
                    explodedModuleDir);
            cp = moduleCP + File.pathSeparator + earCP;
*/
        } else {
            //this is an absolute path
            String module_uri = verifierFrameworkContext.getExplodedArchivePath();
            File module = new File(module_uri);
            assert(module.isDirectory() && module.isAbsolute());
            cp = ClassPathBuilder.buildClassPathForRar(module);
        }
        return (classPath = cp);
    }

    /**
     * creates the ClosureCompiler for the rar module and sets it to the
     * verifier context. This is used to compute the closure on the classes used
     * in the rar.
     *
     * @throws IOException
     */
    protected void createClosureCompiler() throws IOException {
        String specVer = SpecVersionMapper.getJCAVersion(
                verifierFrameworkContext.getJavaEEVersion());
        Object arg = (isASMode)?cond.getClassLoader():(Object)getClassPath();
        ConnectorClosureCompiler cc = new ConnectorClosureCompiler(specVer,
                ClassFileLoaderFactory.newInstance(new Object[]{arg}));
        context.setClosureCompiler(cc);
    }
}
