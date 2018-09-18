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

package com.sun.enterprise.tools.verifier.tests.web;

import java.util.*;
import java.lang.reflect.Modifier;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.deploy.shared.FileArchive;

/**
 * All Servlet class of an war bundle should be declared in the deployment
 * descriptor for portability
 * 
 * @author  Jerome Dochez
 * @version 
 */
public class ServletClassDeclared extends WebTest implements WebCheck { 

    final String servletClassPath = "WEB-INF/classes";
    
    /** 
     *  All Servlet class of an war bundle should be declared in the deployment
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {
        
	Result result = getInitializedResult();
        // See bug #6332745
        if(getVerifierContext().getJavaEEVersion().compareTo(SpecVersionMapper.JavaEEVersion_5) >=0){
            result.setStatus(Result.NOT_APPLICABLE);
            return result;
        }
//	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        boolean oneWarning = false;
        boolean foundOne=false;
        
//        File f =  Verifier.getArchiveFile(descriptor.getModuleDescriptor().getArchiveUri());
        result = loadWarFile(descriptor);
        
//        ZipFile zip = null;
        FileArchive arch = null;
        Enumeration entries= null;
        //ZipEntry entry;
        Object entry;

        try {
//            if (f == null) {
              String uri = getAbstractArchiveUri(descriptor);
              try {
                 arch = new FileArchive();
                 arch.open(uri);
                 entries = arch.entries();
               }catch (Exception e) { throw e; }
//            }
//            else {
//              zip = new ZipFile(f);
//              entries = zip.entries();
//            }
        } catch(Exception e) {
            e.printStackTrace();
	    result.failed(smh.getLocalString
				 (getClass().getName() + ".exception",
                                 "IOException while loading the war file [ {0} ]",
				  new Object[] {descriptor.getName()}));
            
            return result;
        }
        while (entries.hasMoreElements()) {
            entry  = entries.nextElement();
//            if (f == null) {
            String name = (String)entry;
//            }
//            else {
//               name = ((ZipEntry)entry).getName();
//            }
            if (name.startsWith(servletClassPath)) {
                if (name.endsWith(".class")) {
                    String classEntryName = name.substring(0, name.length()-".class".length());
                    classEntryName = classEntryName.substring(servletClassPath.length()+1, classEntryName.length());
                    String className = classEntryName.replace('/','.');
                    Class servletClass = loadClass(result, className);
                    if (!Modifier.isAbstract(servletClass.getModifiers()) &&
                            isImplementorOf(servletClass, "javax.servlet.Servlet")) {
                        foundOne=true;
                        // let's find out if this servlet has associated deployment descriptors...
                        Set servlets = descriptor.getServletDescriptors();
                        boolean foundDD = false;
                        for (Iterator itr = servlets.iterator();itr.hasNext();) {
                            WebComponentDescriptor servlet = (WebComponentDescriptor)itr.next();
                            String servletClassName = servlet.getWebComponentImplementation();
                            if (servletClassName.equals(className)) {
                                foundDD=true;
                                break;
                            }
                        }
                        if (foundDD) {
                            result.addGoodDetails(smh.getLocalString
                                (getClass().getName() + ".passed",
                                "Servlet class [ {0} ] found in war file is defined in the Deployement Descriptors",
                                new Object[] {className}));
                        } else {
                            oneWarning=true;                            
                            result.addWarningDetails(smh.getLocalString
                                (getClass().getName() + ".warning",
                                "Servlet class [ {0} ] found in war file is not defined in the Deployement Descriptors",
                                new Object[] {className}));
                        }
                    }
                }
            }
        }
        if (!foundOne) {
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no servlet implementation within the web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
        } else {
            if (oneWarning) {
                result.setStatus(Result.WARNING);
            } else {
                result.setStatus(Result.PASSED);
            }
        }
        return result;
    }
}
