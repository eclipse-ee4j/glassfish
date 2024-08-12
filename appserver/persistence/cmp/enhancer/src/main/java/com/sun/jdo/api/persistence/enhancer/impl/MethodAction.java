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

import com.sun.jdo.api.persistence.enhancer.classfile.ClassMethod;
import com.sun.jdo.api.persistence.enhancer.util.Support;

//@olsen: cosmetics
//@olsen: moved: this class -> package impl
//@olsen: subst: /* ... */ -> // ...
//@olsen: subst: FilterEnv -> Environment
//@olsen: dropped parameter 'Environment env', use association instead
//@olsen: subst: Hashtable -> Map, Set, HashSet
//@olsen: subst: theClass, classAction -> ca
//@olsen: added: support for I18N
//@olsen: subst: FilterError -> UserException, assert()
//@olsen: removed: old, disabled ODI code


/**
 * MethodAction controls the annotation actions applied to a single
 * method of a class.
 */
class MethodAction
    //@olsen: not needed
    //implements AnnotationConstants
    extends Support {

    /* hash table for lookup of known ok Generic attributes */
//@olsen: disabled feature
/*
    private static Set safeGenericAttributes;

    private static void addSafeAttribute(String attrName) {
        safeGenericAttributes.add(attrName);
    }

    static {
        safeGenericAttributes = new HashSet();

        // Microsoft COM attributes
        addSafeAttribute("COM_Class_type");
        addSafeAttribute("COM_DispMethod");
        addSafeAttribute("COM_ExposedAs");
        addSafeAttribute("COM_ExposedAs_Group");
        addSafeAttribute("COM_FuncDesc");
        addSafeAttribute("COM_Guid");
        addSafeAttribute("COM_GuidPool");
        addSafeAttribute("COM_MapsTo");
        addSafeAttribute("COM_MethodPool");
        addSafeAttribute("COM_ProxiesTo");
        addSafeAttribute("COM_Safety");
        addSafeAttribute("COM_TypeDesc");
        addSafeAttribute("COM_VarTypeDesc");
        addSafeAttribute("COM_VtblMethod");
    }
*/

    /* The parent ClassAction of this MethodAction */
    //@olsen: made final
    private final ClassAction ca;

    /* The method to which the actions apply */
    //@olsen: made final
    private final ClassMethod theMethod;

    /* The code annotater for the method */
    //@olsen: made final
    private final MethodAnnotater annotater;

    /* Central repository for the options and classes */
    //@olsen: added association
    //@olsen: made final
    private final Environment env;

    /**
     * Returns true if any code annotations need to be performed on
     * this method.
     */
    boolean needsAnnotation() {
        return annotater.needsAnnotation();
    }

    /**
     * Returns the method for which this MethodAction applies
     */
    ClassMethod method() {
        return theMethod;
    }

    /**
     * Constructor
     */
    //@olsen: added parameter 'env' for association
    MethodAction(ClassAction ca,
                 ClassMethod method,
                 Environment env) {
        this.ca = ca;
        theMethod = method;
        this.env = env;
        annotater = new MethodAnnotater(ca, method, env);
    }

    /**
     * Examine the method to determine what actions are required
     */
    void check() {
        annotater.checkMethod();
//@olsen: disabled feature
/*
        if (env.verbose()) {
            CodeAttribute codeAttr = theMethod.codeAttribute();
            if (codeAttr != null) {
                Enumeration e = codeAttr.attributes().elements();
                while (e.hasMoreElements()) {
                    ClassAttribute attr = (ClassAttribute) e.nextElement();
                    if ((attr instanceof GenericAttribute) &&
                        safeGenericAttributes.contains(attr.attrName().asString())) {
                        String userClass = ca.classControl().userClassName();
                        String msg = "method "  + userClass +
                            "." + theMethod.name().asString() +
                            Descriptor.userMethodArgs(theMethod.signature().asString()) +
                            " contains an unrecognized attribute of type " +
                            attr.attrName().asString() + ".  " +
                            "Please check with Object Design support to see " +
                            "whether this is a problem.";
                        env.warning(msg, userClass);
                    }
                }
            }
        }
*/
    }

    /**
     * Retarget class references according to the class name mapping
     * table.
     */
//@olsen: disabled feature
/*
    void retarget(Map classTranslations) {
        // No action needed currently
    }
*/

    /**
     * Perform annotations
     */
    void annotate() {
        annotater.annotateMethod();
    }
}
