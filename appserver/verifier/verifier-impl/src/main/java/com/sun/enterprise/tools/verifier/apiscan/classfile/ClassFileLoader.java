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
 * ClassFileLoader.java
 *
 * Created on August 13, 2004, 9:05 PM
 */

package com.sun.enterprise.tools.verifier.apiscan.classfile;

import java.io.IOException;

/**
 * This is loader for ClassFile similar to what ClassLoader is for class in
 * Java.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface ClassFileLoader {
    /**
     * @param externalClassName class name in external form. This is same as
     *                          what is used in java.lang.ClassLoader.load() or
     *                          Class.forName(). It is upto the implementation
     *                          to decide if they want to use caching or not.
     * @return The {@link ClassFile} loaded.
     * @throws IOException if specified class could not be found.
     * @see ClassFile
     */
    ClassFile load(String externalClassName) throws IOException;
}
