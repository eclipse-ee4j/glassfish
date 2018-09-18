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
 * JarFileDependsOnOutsidePackage.java
 *
 * Created on August 30, 2004, 10:16 AM
 */

package com.sun.enterprise.tools.verifier.tests.util;

import com.sun.enterprise.tools.verifier.Result;

import java.io.File;
import java.io.FileInputStream;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 *
 * @author  ss141213
 * This as per J2EE 1.4 spec section#8.2. Contents from spec is given below...
 * Only Jar format files containing class files or resources to be loaded directly 
 * by a standard ClassLoader should be the target of a Class-Path reference; 
 * such files are always named with a .jar extension. 
 * Top level Jar files that are processed by a deployment tool 
 * should not contain Class-Path entries; 
 * such entries would, by definition, 
 * reference other files external to the deployment unit. 
 * A deployment tool is not required to process such external references.
  */
public class BundledOptPkgHasDependencies {
    public static void test(String explodedJarPath, Result result){
        try (FileInputStream fis = new FileInputStream(new File(explodedJarPath+File.separator+JarFile.MANIFEST_NAME))){
            boolean failed=false;
            Manifest manifest=new Manifest(fis);
            String depClassPath=manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
            if(depClassPath!=null){
                for(StringTokenizer st=new StringTokenizer(depClassPath);st.hasMoreTokens();){
                    String entry=st.nextToken();
                    String entryPath=new File(explodedJarPath).getParent()+File.separator+entry;
                    File bundledOptPkg=new File(entryPath);
                    if(!bundledOptPkg.isDirectory()){
                        try (JarFile jf = new JarFile(bundledOptPkg)) {
                            Manifest bundledManifest = jf.getManifest();
                            String bundledCP = bundledManifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
                            if (bundledCP != null && bundledCP.length() != 0) {
                                failed = true;
                                result.failed(entry + " contains Class-Path in it's manifest.");
                            }
                        }
                    }
                }
            }//if
            if(!failed){
                result.setStatus(Result.PASSED);
            }
        }catch(Exception e){
            result.failed(e.toString());
        }
    }
}
