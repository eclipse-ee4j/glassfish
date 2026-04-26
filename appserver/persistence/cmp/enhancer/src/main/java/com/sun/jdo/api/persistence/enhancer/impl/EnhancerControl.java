/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import com.sun.jdo.api.persistence.enhancer.util.Support;

import java.util.Iterator;
import java.util.List;

/**
 * Main is the starting point for the persistent filter tool.
 */
public class EnhancerControl {

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
            final List<ClassControl> classes = env.collectClasses();

            if (classes.size() > 1) {
                env.messageNL("scanning classes");//NOI18N
            }

            // First examine the classes, noting the class characteristics
            for (Iterator<ClassControl> e = classes.iterator(); e.hasNext();) {
                ClassControl cc = e.next();
                cc.scan1();

                if (false) {
                    dumpClass(cc);
                }
            }

            if (env.errorCount() > 0) {
                return;
            }

            if (classes.size() > 1) {
                env.messageNL("augmenting classes");//NOI18N
            }

            // Change class inheritance
            for (Iterator<ClassControl> e = classes.iterator(); e.hasNext();) {
                ClassControl cc = e.next();
                //@olsen: subst: augmentInterfaces -> augment
                cc.augment();

                if (false) {
                    dumpClass(cc);
                }
            }

            if (env.errorCount() > 0) {
                return;
            }

            if (classes.size() > 1) {
                env.messageNL("annotating classes");//NOI18N
            }

            // Then perform the annotation actions
            for (Iterator<ClassControl> e = classes.iterator(); e.hasNext();) {
                ClassControl cc = e.next();
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
}
