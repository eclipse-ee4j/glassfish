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
 * ASMClosureCompilerImpl.java
 *
 * Created on August 13, 2004, 3:34 PM
 */

package com.sun.enterprise.tools.verifier.apiscan.classfile;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * An implementation of {@link ClosureCompilerImplBase} based on
 * ASM.
 *
 * It is not a public class because I expect users to use this class via
 * the bridge setup from {@link ClosureCompilerImpl}.
 * It extends the common implementation from ClosureCompilerImplBase.
 * @author Sanjeeb.Sahoo@Sun.COM
 */
class ASMClosureCompilerImpl extends ClosureCompilerImplBase {

    private Collection<MethodRef> methodReferences = new HashSet<MethodRef>();

    private HashSet<MethodRef> visitedMethods = new HashSet<MethodRef>();

    private Stack<MethodRef> callStack = new Stack<MethodRef>();

    // holds result of current buildClosure() method
    private boolean result;

    private HashSet<String> closure = new HashSet<String>();

    //map of refencing path to list of not found classes.
    private Map<String, List<String>> failed = new HashMap<String, List<String>>();

    private static final String myClassName = "ASMClosureCompilerImpl"; // NOI18N

    /**
     * @param loader the ClassFileLoader that is used to load the referenced
     *               classes.
     */
    public ASMClosureCompilerImpl(ClassFileLoader loader) {
        super(loader);
    }

    private void resetResult() {
        logger.entering("ASMClosureCompilerImpl", "resetResult", // NOI18N
                new Object[]{});
        result = true;
    }

    private void setResult(boolean newresult){
        result = result && newresult;
    }

    private boolean getResult() {
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public boolean buildClosure(String className) {
        logger.entering(myClassName, "buildClosure", className); // NOI18N
        resetResult(); // at the entry of this public method, we reset result.
        if (needToBuildClosure(className)) {
            visitedClasses.add(className); // yes, we add before visting
            ClassFile cf = load(className);
            if(cf==null){
                return false;
            }
            // top level methods whose closure needs to be calculated.
            // TODO: We must get precise list of methods.
            Collection<? extends Method> methods = cf.getMethods();
            for(Method next:methods){
                methodReferences.add(next.getSelfReference());
                buildClosure(next);
            }
        }
        return getResult();
    }

    private void buildClosure(Method m) {
        MethodRef methodReference = m.getSelfReference();
        logger.entering("ASMClosureCompilerImpl", "buildClosure", // NOI18N
                new Object[]{methodReference});
        callStack.push(methodReference);
        if (needToBuildClosure(methodReference)) {
            visitedMethods.add(methodReference);
            Collection<MethodRef> methodRefs = m.getReferencedMethods();
            methodReferences.addAll(methodRefs);
            for(MethodRef nextMethodRef : methodRefs) {
                if (!needToBuildClosure(nextMethodRef)) continue;
                ClassFile cf = load(Util.convertToExternalClassName
                        (nextMethodRef.getOwningClassName()));
                if (cf == null) {
                    continue; // no need to call handleFailure() as it is done by this.load()
                }
                Method nextMethod = cf.getMethod(nextMethodRef);
                if(nextMethod==null){
                    handleFailure(nextMethodRef);
                    continue;
                }
                buildClosure(nextMethod);//recurssive call
            } // for
            // now go thru' all the classes that will be loaded when this
            // method is called.
            for(String className : m.getReferencedClasses()){
                String externalClassName = Util.convertToExternalClassName(className);
                if(!needToBuildClosure(externalClassName)) continue;
                load(externalClassName);
            }
        }
        callStack.pop();
    }

    /**
     * load class with given name. It also recurssively loads all classes in the
     * super class chain and interfaces implemented by this class and
     * its super classes, until it reaches a class which is found in the
     * excluded list. It also builds closure of each of the class's clinit method.
     *
     * @param className name of class in external format.
     * @return ClassFile representing this class name,
     * It returns null if class has been already loaded or need not be loaded
     * because it is an excluded class or could not be loaded. If class could
     * could not be loaded, it calls appropriate handleFailure().
     * @see #needToLoad(String)
     */
    protected ClassFile load(String className) {
        logger.entering("ASMClosureCompilerImpl", "load", // NOI18N
                new Object[]{className});
        ClassFile cf = null;
        try{
            cf = loader.load(className);
        }catch(IOException e){
            handleFailure(className);
        }
        if((cf!=null) && needToLoad(className)) {
            closure.add(className); // yes we add before actually loading.
            MethodRef clinitMethodRef =
                    new MethodRef(Util.convertToInternalClassName(className),
                            MethodRef.CLINIT_NAME,
                            MethodRef.CLINIT_DESC);
            Method clinitMethod  = cf.getMethod(clinitMethodRef);
            try{
                // See we push clinitMethodRef here so that we can display
                // useful error to user even during class loading.
                callStack.push(clinitMethodRef);
                String superClassName = cf.getNameOfSuperClass();
                if(superClassName!=null && needToBuildClosure(superClassName)) { // super class of java.lang.Object is null
                    load(superClassName); //recurssive call
                }
                for(String interfaceClassName : cf.getNamesOfInterfaces()){
                    if(needToBuildClosure(interfaceClassName)) {
                        load(interfaceClassName); // recurssive call
                    }
                }
            }finally{
                // pop callStack before buildClosure is called for clinit
                callStack.pop();
            }
            if(clinitMethod!=null) {
                methodReferences.add(clinitMethodRef);
                buildClosure(clinitMethod); // recurssive call
            }
        }
        logger.exiting("ASMClosureCompilerImpl", "load", cf==null?"null":cf.getName()); // NOI18N
        return cf;
    }

    private void handleFailure(MethodRef mr){
        logger.logp(Level.WARNING, "ASMClosureCompilerImpl", "handleFailure", 
                getClass().getName() + ".exception1", new Object[]{mr.toString()});
//        setResult(false);
        // TODO: We should look for base class methods
    }

    /**
     * @param referencedClass is the name of the class in external format.
     */
    private void handleFailure(String referencedClass) {
        logger.entering("ASMClosureCompilerImpl", "handleFailure", // NOI18N
                new Object[]{referencedClass});
        setResult(false);
        String referencingPath = "";
        try {
            StringBuilder referencingPathBuffer = new StringBuilder();
            for (MethodRef m : callStack) {
                if (referencingPathBuffer.length() != 0)
                    referencingPathBuffer.append("->"); // NOI18N
                referencingPathBuffer.append(m);
            }
            referencingPath = referencingPathBuffer.toString();
        } catch (EmptyStackException e) {
        }
        List<String> failedList = failed.get(referencingPath);
        if (failedList == null) {
            failedList = new ArrayList<String>();
            failed.put(referencingPath, failedList);
        }
        failedList.add(referencedClass);
    }

    /**
     * {@inheritDoc}
     */
    public Collection getClosure() {
        return Collections.unmodifiableCollection(methodReferences);
    }

    /**
     * {@inheritDoc}
     */
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
        methodReferences.clear();
        visitedClasses.clear();
        failed.clear();
        closure.clear();
    }

    /**
     * @param className name of class in external format.
     * @return
     */
    protected boolean needToLoad(String className) {
        return !closure.contains(className);
    }

    protected boolean needToBuildClosure(MethodRef methodRef) {
        boolean result = true;
        final String owningClassName = methodRef.getOwningClassName();
        if (visitedMethods.contains(methodRef))
            result = false;
        else if (excludedClasses.contains(owningClassName)) {
            result = false;
        } else if (excludedPackages.contains(getPackageName(owningClassName))) {
            result = false;
        } else {
            for (Iterator i = excludedPatterns.iterator(); i.hasNext();) {
                String pattern = (String) i.next();
                if (owningClassName.startsWith(pattern)) {
                    result = false;
                    break;
                }
            }
        }
        logger.logp(Level.FINEST, myClassName, "needToBuildClosure", // NOI18N
                methodRef + " " + result); // NOI18N
        return result;
    }

    // used for pretty printing
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
            
            sb.append("\n\t<Methods>"); // NOI18N
            for(MethodRef m : methodReferences){
                sb.append("\n\t\t"); // NOI18N
                sb.append(m);
            }
            sb.append("\n\t</Methods>"); // NOI18N

            sb.append("\n\t<Closure>"); // NOI18N
            for(String c : closure){
                sb.append("\n\t\t"); // NOI18N
                sb.append(c);
            }
            sb.append("\n\t</Closure>"); // NOI18N
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
        if(logger.isLoggable(Level.FINER)){
            sb.append("\n</Closure>"); // NOI18N
        }
        return sb.toString();        
    }

}
