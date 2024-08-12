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

package com.sun.jdo.api.persistence.enhancer.impl;

//@olsen: added general support
import com.sun.jdo.api.persistence.enhancer.util.Support;

import java.util.ArrayList;
import java.util.Iterator;

//@olsen: disabled feature
/*
import com.sun.jdo.api.persistence.enhancer.classfile.ClassFile;
*/

//@olsen: disabled feature
/*
import com.sun.jdo.api.persistence.enhancer.impl.FieldMap;
*/

//lars: made the class public to access it from the root package
//lars: moved from root package into impl-subpackage
//@olsen: cosmetics
//@olsen: subst: [iI]Persistent -> [pP]ersistenceCapable
//@olsen: subst: /* ... */ -> // ...
//@olsen: moved: class FilterError -> package util
//@olsen: moved: OSCFP.addClass(ClassControl) -> impl.Environment
//@olsen: subst: filterEnv.classMap.elements() -> filterEnv.getClasses()
//@olsen: subst: filterEnv.classMap.get(name) -> filterEnv.getClass(name)
//@olsen: subst: filterEnv.translations -> filterEnv.translations()
//@olsen: subst: OSCFP -> Main
//@olsen: subst: filterEnv -> env
//@olsen: subst: FilterEnv -> Environment
//@olsen: dropped parameter 'Environment env', use association instead
//@olsen: subst: augment -> closeOver
//@olsen: subst: collectAllClasses -> collectClasses
//@olsen: subst: Vector -> Collection, List, ArrayList
//@olsen: subst: Hashtable -> Map, HashMap
//@olsen: subst: Enumeration,... -> Iterator, hasNext(), next()
//@olsen: removed: proprietary support for IndexableField


/**
 * Main is the starting point for the persistent filter tool.
 */
//@olsen: added local class
public class EnhancerControl
//extends Support
{

    /* Central repository for the options selected by
     * the user and the current state of the Filter execution */
    private Environment env;

    /**
     * Create an instance.
     */
    public EnhancerControl(Environment env) {
        this.env = env;
    }

    /**
     * Extend the class map so that persistent classes are closed over
     */
//@olsen: inlined method
/*
    //@olsen: moved: Main.closeOverClasses() -> EnhancerControl
    //@olsen: made public
    public void closeOverClasses() {
        ArrayList v = env.collectClasses(ClassControl.PersistCapable);
        for (Iterator e = v.iterator(); e.hasNext();) {
            ClassControl cc = (ClassControl)e.next();
            closeOverClass(cc);
        }
    }
*/

    /**
     * Extend the class map so that all base classes of the specified
     * class are included, if possible.  The specified class is assumed
     * to already be on the persistent class list.
     */
//@olsen: disabled feature
/*
    //@olsen: moved: Main.closeOverClass(ClassControl startCC) -> EnhancerControl
    //@olsen: made public
    public void closeOverClass(ClassControl startCC) {
        final JDOMetaData jdoMetaData = env.getJDOMetaData();
        String className = startCC.classFile().className().asString();
        String baseClassName = startCC.classFile().superName().asString();
        while (true) {
            //@olsen: added final
            final ClassControl cc = env.findClass(baseClassName);

            if (cc == null) {
                // We can't find the class file
                // perhaps a mechanism to only produce the error would be nice
                env.error("Unable to locate class " +
                          ClassControl.userClassFromVMClass(baseClassName));
                return;
            }

            if (baseClassName.equals("java/lang/Object"))
                //@olsen: subst: break -> return
                return;

//@olsen: disabled feature
///
            //@olsen: not used
            //String ccPkg = cc.pkg();
            if (baseClassName.startsWith("java/") &&
                !cc.implementsPersistenceCapable() &&
                !env.modifyJavaClasses()) {
                env.error("Sorry, java types can not be made persistent.  " +
                          "Class " +
                          ClassControl.userClassFromVMClass(className) +
                          " extends " +
                          ClassControl.userClassFromVMClass(baseClassName) +
                          ".  See the -modifyjava option if you really " +
                          "want to do this.");
                return;
            }

            if (baseClassName.startsWith("com/ms/com/")) {
                env.error("Sorry, Microsoft COM types can not be made persistent.  " +
                          "Class " +
                          ClassControl.userClassFromVMClass(className) +
                          " extends " +
                          ClassControl.userClassFromVMClass(baseClassName) +
                          ".");
                return;
            }
///

            //cc.setPersistType(ClassControl.PersistCapable);

            //@olsen: set persisence type of class by JDO meta-data
            if (jdoMetaData.isPersistenceCapableClass(baseClassName)) {
                //cc.setPersistType(ClassControl.PersistCapable);
                cc.setInitialPersistType(ClassControl.PersistCapable);

                //@olsen: impose limitation
                env.error("Sorry, in this release a persistent class cannot have a persistent super-class.  " +
                          "Persistent class " +
                          ClassControl.userClassFromVMClass(className) +
                          " (indirectly) extends persistent class " +
                          ClassControl.userClassFromVMClass(baseClassName) +
                          ".");
                return;
            } else if (baseClassName.startsWith("java/")) {
                //cc.setPersistType(ClassControl.TransientOnly);
                cc.setInitialPersistType(ClassControl.TransientOnly);

                // may stop when transient root class found
                return;
            }

//@olsen: disabled feature
///
            if (cc.persistType() == ClassControl.PersistCapable) {
                // The base class is already on the persistent-capable list
                // so we're all done here
                return;
            }
            if (cc.implementsPersistenceCapable()) {
                // The class already inherits from Persistent.  It's probably
                // ok to update if necessary
                env.message("Promoting " + cc.userClassName() +
                            " to persistence-capable.");
                cc.setPersistType(ClassControl.PersistCapable);
                cc.setImplicitlyPersistent(true);
            } else if (cc.persistType() == ClassControl.PersistUnknown) {
                if (cc.pkg().equals(startCC.pkg())) {
                    // It's ok to make this base class persistent too.
                    env.message("Including class " + cc.userClassName() +
                                " as a persistence capable class.");
                    cc.setPersistType(ClassControl.PersistCapable);
                    cc.setImplicitlyPersistent(true);
                } else {
                    // Ought to be more clever here as this class may be
                    // pulled in through another persistence path later on
                    env.error("Class " + cc.userClassName() +
                              " must be persistent to allow " +
                              startCC.userClassName() +
                              " to be persistent.");
                    return;
                }
            } else {
                // Ought to be more clever here as this class may be pulled in
                // through another persistence path later on
                env.error("Class " + cc.userClassName() +
                          " must be persistent to allow " +
                          startCC.userClassName() +
                          " to be persistent.");
                return;
            }
///

            // Move on to the next base class
            baseClassName = cc.classFile().superName().asString();
        }
    }
*/

    /**
     * Dumps a class' signature and byte-code.
     */
    //@olsen: added method for debugging
    static protected void dumpClass(ClassControl cc) {
        final String name = cc.userClassName();
        System.out.println();
        System.out.println("dumping class " + name + " {");//NOI18N
        cc.classFile().print(System.out);
        System.out.println("} // end of class " + name);
        System.out.println();
    }


    /**
     * Determine what modifications are needed and perform them
     */
    //@olsen: moved: Main.modifyClasses() -> EnhancerControl
    //@olsen: made public
    //@olsen: improved output
    public void modifyClasses() {
        //@olsen: added support for timing statistics
        try{
            if (env.doTimingStatistics()) {
                Support.timer.push("EnhancerControl.modifyClasses()");//NOI18N
            }
            final ArrayList classes = env.collectClasses();

            if (classes.size() > 1) {
                env.messageNL("scanning classes");//NOI18N
            }

            // First examine the classes, noting the class characteristics
            for (Iterator e = classes.iterator(); e.hasNext();) {
                ClassControl cc = (ClassControl)e.next();
                cc.scan1();

                if (false) {
                    dumpClass(cc);
                }
            }

//@olsen: disabled feature
/*
            // Possibly update package names
            retargetClasses();
*/

            if (env.errorCount() > 0)
                return;

            if (classes.size() > 1) {
                env.messageNL("augmenting classes");//NOI18N
            }

            // Change class inheritance
            for (Iterator e = classes.iterator(); e.hasNext();) {
                ClassControl cc = (ClassControl)e.next();
                //@olsen: subst: augmentInterfaces -> augment
                cc.augment();

                if (false) {
                    dumpClass(cc);
                }
            }

            if (env.errorCount() > 0)
                return;

            if (classes.size() > 1) {
                env.messageNL("annotating classes");//NOI18N
            }

            // Then perform the annotation actions
            for (Iterator e = classes.iterator(); e.hasNext();) {
                ClassControl cc = (ClassControl)e.next();
                cc.annotate();

                if (false) {
                    dumpClass(cc);
                }
            }
        } finally {
            if (env.doTimingStatistics()) {
                Support.timer.pop();
            }
        }
    }

    /**
     * Build a ArrayList of strings which are the names of the
     * persistent classes in this enhancer's run.
     */
//@olsen: disabled feature
/*
    private ArrayList computePersistentClasses() {
        ArrayList v = new ArrayList();
        Iterator allClasses = env.getClasses();
        while (allClasses.hasNext()) {
            ClassControl cc = (ClassControl)allClasses.next();
            if (cc.isExplicitlyNamed() && cc.persistCapable())
                v.add(cc.userClassName());
        }
        return v;
    }
*/

    /**
     * For each class in the class map which isn't transient-only,
     * apply package name translations to update class references
     */
//@olsen: disabled feature
/*
    //@olsen: moved: Main.retargetClasses() -> EnhancerControl
    private void retargetClasses() {

        if (env.translations().size() == 0)
            return;

        locateTranslatedClasses();

        //@olsen: made classMap local in Environment
        //if (env.classMap.size() > 0) {
        {
            ArrayList translatable = new ArrayList();
            Map classTranslations = new HashMap();

            // Compute the full set of class translations
            for (Iterator e = env.getClasses(); e.hasNext();) {
                ClassControl cc = (ClassControl)e.next();
                String pkg = cc.pkg();
                String xlat = (String)env.translations().get(pkg);
                if (xlat != null || cc.annotateable()) {
                    translatable.add(cc);

                    if (xlat != null) {
                        String newName;
                        if (xlat.length() == 0)
                            newName = cc.unpackagedName();
                        else
                            newName = xlat + "/" + cc.unpackagedName();
                        if (!newName.equals(cc.className())) {
                            ClassControl existingCC =
                                (ClassControl)env.getClass(newName);

                            if (existingCC != null) {
                                env.error("The package translations specified would " +
                                          "cause " + cc.userClassName() +
                                          " to be translated to " +
                                          existingCC.userClassName() +
                                          " which already exists.");
                            } else
                                classTranslations.put(cc.className(), newName);
                        }
                    }
                }
            }

            if (env.errorCount() > 0)
                return;

            if (classTranslations.size() == 0) {
                env.warning("No package name translations are being applied");
            }
            else {
                env.message();
                env.message("doing package name translations");

                for (Iterator e = translatable.iterator(); e.hasNext();) {
                    final ClassControl cc = (ClassControl)e.next();
                    final String className = cc.className();
                    cc.retarget(classTranslations);

                    //@olsen: use added method
                    env.renameClass(className);
//
                    // Add the modified name to the class map if the class
                    // name has changed.
                    String newClassName = cc.className();
                    env.classMap.remove(className);
                    env.renamedMap.put(className, cc);
                    env.classMap.put(newClassName, cc);
//
                }
            }
        }
    }
*/

    /**
     * For each package name translation selected, find all classes in the
     * package and add to the class map.
     */
//@olsen: disabled feature
/*
    //@olsen: moved: Main.locateTranslatedClasses() -> EnhancerControl
    private void locateTranslatedClasses() {
        // Compute the full set of class translations
        for (Iterator e = env.translations().keySet().iterator();
             e.hasNext();) {
            String pkg = (String)e.next();

            for (Enumeration pe = env.classPathOption().classesInPackage(pkg);
                 pe.hasMoreElements();) {
                String className = (String)pe.nextElement();
                env.findClass(className);
            }
        }
    }
*/
}
