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

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.web.ServletFilter;
import com.sun.enterprise.deployment.web.AppListenerDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClosureCompiler;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClosureCompilerImpl;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.util.WebArchiveLoadableHelper;
import com.sun.enterprise.deploy.shared.FileArchive;
import org.glassfish.web.deployment.descriptor.ErrorPageDescriptor;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;

import java.util.*;
import java.io.File;

/**
 * A j2ee archive should be self sufficient and should not depend on any classes to be 
 * available at runtime.
 * The test checks whether all the classes found in the web archive are loadable and the
 * classes that are referenced inside their code are also loadable within the jar. 
 * 
 * @author Vikas Awasthi
 */
public class WebArchiveClassesLoadable extends WebTest implements WebCheck { 
    public Result check(WebBundleDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String archiveUri = getAbstractArchiveUri(descriptor);
        
        Iterator entries;
        try{
            entries=getClassNames(descriptor).iterator();
        } catch(Exception e) {
//            e.printStackTrace();
            result.failed(smh.getLocalString(getClass().getName() + ".exception",
                                             "Error: [ {0} ] exception while loading the archive [ {1} ].",
                                              new Object[] {e, descriptor.getName()}));
            return result;
        }
        
        boolean allPassed = true;
        ClosureCompiler closureCompiler=getVerifierContext().getClosureCompiler();

        // org.apache.jasper takes care of internal JSP stuff
        ((ClosureCompilerImpl)closureCompiler).addExcludedPattern("org.apache.jasper");

        // DefaultServlet takes care of the default servlet in GlassFish.
        // For some reason, for every web app, this is returned as a component
        ((ClosureCompilerImpl)closureCompiler).addExcludedClass("org.apache.catalina.servlets.DefaultServlet");
        if(getVerifierContext().isAppserverMode())
        	((ClosureCompilerImpl)closureCompiler).addExcludedPattern("com.sun.enterprise");

        while (entries.hasNext()) {
                String className=(String)entries.next();
                boolean status=closureCompiler.buildClosure(className);
                allPassed=status && allPassed;
        }
        if (allPassed) {
            result.setStatus(Result.PASSED);
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                (getClass().getName() + ".passed",
                "All the classes are loadable within [ {0} ] without any linkage error.",
                new Object[] {archiveUri}));
        } else {
            result.setStatus(Result.FAILED);
            addErrorDetails(result, compName);
            result.addErrorDetails(WebArchiveLoadableHelper.getFailedResults(closureCompiler, getVerifierContext().getOutDir()));
            result.addErrorDetails(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.loadableError",
                    "Please either bundle the above mentioned classes in the application " +
                    "or use optional packaging support for them."));
        }
        return result;
    }  
    
    /**
     * Looks for Servlet classes, ServletFilter classes, Listener classes and 
     * Exception classes in the webBundleDescriptor. The closure is computed
     * starting from these classes. 
     * @param descriptor
     * @return returns a list of class names in the form that can be used in 
     * classloader.load()
     * @throws Exception
     */ 
    private List getClassNames(WebBundleDescriptor descriptor) throws Exception{
        final List<String> results=new LinkedList<String>();
        for(Object obj : descriptor.getServletDescriptors()) {
            String servletClassName = (WebComponentDescriptor.class.cast(obj))
                    .getWebComponentImplementation();
            results.add(servletClassName);
        }
        
        for (Object obj : descriptor.getServletFilterDescriptors()) {
            String filterClassName = (ServletFilter.class.cast(obj)).getClassName();
            results.add(filterClassName);
        }
        
        for (Object obj : descriptor.getAppListenerDescriptors()) {
            String listenerClassName = (AppListenerDescriptor.class.cast(obj)).getListener();
            results.add(listenerClassName);
        }
        
        results.addAll(getVerifierContext().getFacesConfigDescriptor().getManagedBeanClasses());
        
        Enumeration en = ((WebBundleDescriptorImpl)descriptor).getErrorPageDescriptors();
        while (en.hasMoreElements()) {
            ErrorPageDescriptor errorPageDescriptor = (ErrorPageDescriptor) en.nextElement();
            String exceptionType = errorPageDescriptor.getExceptionType();
            if (exceptionType != null && !exceptionType.equals(""))
                results.add(exceptionType);
        }
        
        File file = getVerifierContext().getOutDir();
        if(!file.exists())
            return results;

        FileArchive arch= new FileArchive();
        arch.open(file.toURI());
        Enumeration entries = arch.entries();
        while(entries.hasMoreElements()){
            String name=(String)entries.nextElement();
            if(name.startsWith("org/apache/jsp") && name.endsWith(".class"))
                results.add(name.substring(0, name.lastIndexOf(".")).replace('/','.'));
        }
        return results;
    }
    
}
