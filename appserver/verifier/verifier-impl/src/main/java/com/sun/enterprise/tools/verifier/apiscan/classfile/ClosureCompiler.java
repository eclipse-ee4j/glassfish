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
 * ClosureCompiler.java
 *
 * Created on September 7, 2004, 5:02 PM
 */

package com.sun.enterprise.tools.verifier.apiscan.classfile;

import java.util.Collection;
import java.util.Map;

/**
 * This is single most important interface of the apiscan package. This class is
 * used to compute the complete closure of a set of classes.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @see ClosureCompilerImpl
 */
public interface ClosureCompiler {
    /**
     * @param externalClsName class name (in external format) whose closure will
     *                        be computed.
     * @return true if it can compute the closure for all the classes that are
     *         new to it, else false. In other words, this operation is not
     *         idempotent. e.g. ClosureCompiler cc; cc.reset(); boolean
     *         first=cc.buildClosure("a.B"); boolean second=cc.buildClosure("a.B");
     *         second will always be true irrespective of value of first. This
     *         is because it uses list of classes that it has already visited.
     *         That list gets cleared when {@link #reset()} is called).
     */
    boolean buildClosure(String externalClsName);

    /**
     * @return unmodifiable collection of class names which it visited during
     *         closure computation. e.g. Let's say a.class references b1.class
     *         and b2.class. b1.class references c1.class, c2.class and c3.class
     *         b2.class references d1.class, d2.class and d3.class.
     *         c1/c2/d1/d2.class are all not loadable where as c3 and d3.class
     *         are loadable. When we build the closure of a.class, closure will
     *         contain the following... {"a", "b1", "b2", "c3", "d3"}
     */
    Collection getClosure();

    /**
     * @return unmodifiable collection of class names whose closure could not be
     *         computed. The typical reason for not able to build closure for a
     *         class is that class not being found in loader's search path. See
     *         it returns a map which is keyed by the access path and the value
     *         is a list of class names which could not be loaded. e.g. Let's
     *         say a.class references b1.class and b2.class. b1.class references
     *         c1.class, c2.class and c3.class b2.class references d1.class,
     *         d2.class and d3.class. c1/c2/d1/d2.class are all not loadable
     *         where as c3 and d3.class are loadable. When we build the closure
     *         of a.class, failed map will contain the following... {("a:b1",
     *         {"c1","c2"}), ("a.b2", {"d1","d2"})}
     */
    Map getFailed();

    /**
     * Clear the internal cache. It includes the result it has collected since
     * last reset(). But it does not clear the excludedd list. If you want to
     * reset the excluded list, create a new ClosureCompiler.
     */
    void reset();

}
