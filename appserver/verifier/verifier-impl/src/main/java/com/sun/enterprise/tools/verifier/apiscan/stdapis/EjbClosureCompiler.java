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

/*
 * EjbClosure.java
 *
 * Created on August 23, 2004, 2:05 PM
 */

package com.sun.enterprise.tools.verifier.apiscan.stdapis;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.tools.verifier.apiscan.classfile.ClassFileLoader;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClassFileLoaderFactory;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClosureCompilerImpl;
import com.sun.enterprise.tools.verifier.apiscan.packaging.ClassPathBuilder;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class EjbClosureCompiler extends ClosureCompilerImpl {
    private static Logger logger = Logger.getLogger("apiscan.stdapis"); // NOI18N
    private static final String myClassName = "EjbClosureCompiler"; // NOI18N
    private String specVersion;

    /**
     * Creates a new instance of EjbClosure
     */
    public EjbClosureCompiler(String specVersion, ClassFileLoader cfl) {
        super(cfl);
        logger.entering(myClassName, "init<>", specVersion); // NOI18N
        this.specVersion = specVersion;
        addStandardAPIs();
    }

    //this method adds APIs specific to versions.
    protected void addStandardAPIs() {
        String apiName = "ejb_jar_" + specVersion; // NOI18N
        Collection classes = APIRepository.Instance().getClassesFor(apiName);
        for (Iterator i = classes.iterator(); i.hasNext();) {
            addExcludedClass((String) i.next());
        }
        Collection pkgs = APIRepository.Instance().getPackagesFor(apiName);
        for (Iterator i = pkgs.iterator(); i.hasNext();) {
            addExcludedPackage((String) i.next());
        }
        Collection patterns = APIRepository.Instance().getPatternsFor(apiName);
        for (Iterator i = patterns.iterator(); i.hasNext();) {
            addExcludedPattern((String) i.next());
        }
    }

    public static void main(String[] args) {
        Handler h = new ConsoleHandler();
        h.setLevel(Level.ALL);
        Logger.getLogger("apiscan").addHandler(h); // NOI18N
        Logger.getLogger("apiscan").setLevel(Level.ALL); // NOI18N

        int j = 0;
        String pcp = "";
        String specVer = "";
        for (j = 0; j < args.length; ++j) {
            if (args[j].equals("-prefixClassPath")) { // NOI18N
                pcp = args[++j];
                continue;
            } else if (args[j].equals("-specVer")) { // NOI18N
                specVer = args[++j];
                continue;
            }
        }
        if (args.length < 5) {
            System.out.println(
                    "Usage: " + EjbClosureCompiler.class.getName() + // NOI18N
                    " <-prefixClassPath> <prefix classpath> <-specVer> <something like ejb_2.1> <jar file name(s)>"); // NOI18N
            return;
        }

        for (int i = 4; i < args.length; ++i) {
            try (JarFile jar = new JarFile(args[i])) {
                String cp = pcp + File.pathSeparator +
                        ClassPathBuilder.buildClassPathForJar(jar);
                System.out.println("Using CLASSPATH " + cp); // NOI18N
                ClassFileLoader cfl = ClassFileLoaderFactory.newInstance(
                        new Object[]{cp});
                EjbClosureCompiler ejbClosureCompiler = new EjbClosureCompiler(
                        specVer, cfl);
                boolean result = ejbClosureCompiler.buildClosure(jar);
                jar.close();
                System.out.println("The closure is [\n"); // NOI18N
                System.out.println(ejbClosureCompiler);
                if (result) {
                    System.out.println("Did not find any non-standard APIs "); // NOI18N
                } else {
                    System.out.println("Found non-standard APIs for " + // NOI18N
                            args[i]);
                }
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            System.out.println("\n]"); // NOI18N
        }//args[i]
    }

}
