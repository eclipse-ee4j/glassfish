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

package com.sun.enterprise.tools.verifier.apiscan.classfile;

import java.util.Collection;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface Method {
    /**
     * @return the {@link ClassFile} this method belongs to.
     */
    ClassFile getOwningClass();

    /**
     * @return the name of the method.
     */
    String getName();

    /**
     * @return return the descriptor, e.g. ([Ljava.lang.String;)V
     */
    String getDescriptor();

    /**
     * @return return the access flags.
     */
    int getAccess();

    /**
     * Used only when method's parameters or return type use generics.
     * @return
     */
    String getSignature();

    /**
     * @return the internal names of the method's exception classes. May be null.
     */
    String[] getExceptions();

    /**
     *
     * @return an unmodifiable collection of method references representing
     * the methods that are invoked directly (i.e. not recurssively) from this
     * method.
     */
    Collection<MethodRef> getReferencedMethods();

    /**
     *
     * @return an unmodifiable collection of class names in external format
     * representing the classes that are directly (i.e. not recurssively)
     * referenced by this method.
     */
    Collection<String> getReferencedClasses();

    /**
     * @return a reference that represents this method.
     */
    MethodRef getSelfReference();

    /**
     *
     * @return true if this is a native method else false
     */
    boolean isNative();

}
