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
 * BCELClosureCompilerImpl.java
 *
 * Created on August 13, 2004, 3:34 PM
 */

package com.sun.enterprise.tools.verifier.apiscan.classfile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * An implementation of {@link ClosureCompilerImplBase} based on
 * BCEL.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class BCELClosureCompilerImpl extends ClosureCompilerImplBase {

    /*
     * Earlier this class used to be called as ClosureCompilerImpl.
     * So to get the history of this file, use above name in CVS
     */

    private Stack<ClassFile> callStack = new Stack<ClassFile>();

    private HashSet<String> closure = new HashSet<String>();

    private HashSet<String> nativeMethods = new HashSet<String>();

    //map of refencing path to list of not found classes.
    private Map<String, List<String>> failed = new HashMap<String, List<String>>();

    private static final String myClassName = "BCELClosureCompilerImpl"; // NOI18N

    /**
     * @param loader the ClassFileLoader that is used to load the referenced
     *               classes.
     */
    public BCELClosureCompilerImpl(ClassFileLoader loader) {
        super(loader);
    }

    //See corresponding method of ClosureCompiler for javadocs
    public boolean buildClosure(String className) {
        logger.entering(myClassName, "buildClosure", className); // NOI18N
        ClassFile cf;
        if (!needToBuildClosure(className))
            return true;
        try {
            cf = loader.load(className);
        } catch (IOException e) {
            handleFailure(className);
            return false;
        }
        return buildClosure(cf);
    }

    /**
     * @param cf class file whose closure needs to be computed. The behavior is
     *           same as the other buildClosure() method.
     */
    private boolean buildClosure(ClassFile cf) {
        boolean result = true;
        callStack.push(cf);
        if (needToBuildClosure(cf.getName())) {
            visitedClasses.add(cf.getName());
            Collection<String> names = cf.getAllReferencedClassNames();
            closure.addAll(names);
            // TODO: We should not be doing this here. Its just a quick &
            // dirty solution.
            for(Method m : cf.getMethods()) {
                if(m.isNative()) {
                    final String methodDesc =
                            m.getOwningClass().getName()+ "." + m.getName(); // NOI18N
                    nativeMethods.add(methodDesc);
                }
            }
            for (Iterator i = names.iterator(); i.hasNext();) {
                String nextExternalName = (String) i.next();
                if (!needToBuildClosure(nextExternalName)) continue;
                ClassFile next;
                try {
                    next = loader.load(nextExternalName);
                } catch (IOException e) {
                    result = false;
                    handleFailure(nextExternalName);
                    continue;
                }
                boolean newresult = buildClosure(next);//recurssive call
                result = newresult && result;
            }
        }
        callStack.pop();
        return result;
    }

    private void handleFailure(String referencedClass) {
        String referencingPath = "";
        try {
            StringBuilder referencingPathBuffer = new StringBuilder();
            for (Iterator i = callStack.iterator(); i.hasNext();) {
                if (referencingPathBuffer.length() != 0)
                    referencingPathBuffer.append(File.separator);
                referencingPathBuffer.append(((ClassFile) i.next()).getName());
            }
            referencingPath = referencingPathBuffer.toString();
        } catch (EmptyStackException e) {
        }
        logger.finer(
                "Could not locate " + referencingPath + File.separator + // NOI18N
                referencedClass);
        List<String> failedList = failed.get(referencingPath);
        if (failedList == null) {
            failedList = new ArrayList<String>();
            failed.put(referencingPath, failedList);
        }
        failedList.add(referencedClass);
    }

    //See corresponding method of ClosureCompiler for javadocs
    public Collection getClosure() {
        return Collections.unmodifiableCollection(closure);
    }

    //See corresponding method of ClosureCompiler for javadocs
    public Map getFailed() {
        return Collections.unmodifiableMap(failed);
    }
    
    /**
     * Reset the closure for next closure computation.
     * Clear the internal cache. It includes the result it has collected since
     * last reset(). But it does not clear the excludedd list. If you want to
     * reset the excluded list, create a new ClosureCompiler.
     */
    public void reset() {
        closure.clear();
        visitedClasses.clear();
        failed.clear();
        nativeMethods.clear();
    }

    public Collection<String> getNativeMethods() {
        return Collections.unmodifiableCollection(nativeMethods);
    }

    public String toString() {
        StringBuilder sb=new StringBuilder();
        if(logger.isLoggable(Level.FINER)){
            sb.append("\n<Closure>"); // NOI18N

            sb.append("\n\t<ExcludedClasses>"); // NOI18N
            for(Iterator i=excludedClasses.iterator(); i.hasNext();) {
                sb.append("\n\t\t"); // NOI18N
                sb.append((String)i.next());
            }
            sb.append("\n\t</ExcludedClasses>"); // NOI18N

            sb.append("\n\t<ExcludedPackages>"); // NOI18N
            for(Iterator i=excludedPackages.iterator(); i.hasNext();){
                sb.append("\n\t\t"); // NOI18N
                sb.append((String)i.next());
            }
            sb.append("\n\t</ExcludedPackages>"); // NOI18N
            
            sb.append("\n\t<ExcludedPatterns>"); // NOI18N
            for(Iterator i=excludedPatterns.iterator(); i.hasNext();){
                sb.append("\n\t\t"); // NOI18N
                sb.append((String)i.next());
            }
            sb.append("\n\t</ExcludedPatterns>"); // NOI18N
            
            sb.append("\n\t<Classes>"); // NOI18N
            for(Iterator i=closure.iterator(); i.hasNext();){
                sb.append("\n\t\t"); // NOI18N
                sb.append((String)i.next());
            }
            sb.append("\n\t</Classes>"); // NOI18N
        }
        sb.append("\n\t<Failed>"); // NOI18N
        for(Iterator i=failed.entrySet().iterator(); i.hasNext();) {
            Map.Entry referencingPathToFailedList=(Map.Entry)i.next();
            sb.append("\n\t\t"); // NOI18N
            sb.append("<ReferencingPath>"); // NOI18N
            sb.append("\n\t\t\t"); // NOI18N
            sb.append(referencingPathToFailedList.getKey());
            sb.append("\n\t\t"); // NOI18N
            sb.append("</ReferencingPath>"); // NOI18N
            sb.append("\n\t\t"); // NOI18N
            sb.append("<Classes>"); // NOI18N
            for(Iterator iii=((List)referencingPathToFailedList.getValue()).iterator(); iii.hasNext();){
                sb.append("\n\t\t\t"); // NOI18N
                sb.append((String)iii.next());
            }
            sb.append("\n\t\t"); // NOI18N
            sb.append("</Classes>"); // NOI18N
        }
        sb.append("\n\t</Failed>"); // NOI18N

        sb.append("\n\t<NativeMethods>"); // NOI18N
        for(String s : nativeMethods) {
            sb.append("\n\t\t"); // NOI18N
            sb.append(s);
        }
        sb.append("\n\t</NativeMethods>"); // NOI18N

        if(logger.isLoggable(Level.FINER)){
            sb.append("\n</Closure>"); // NOI18N
        }
        return sb.toString();        
    }

}
