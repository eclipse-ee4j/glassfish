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

package com.sun.jdo.api.persistence.enhancer.classfile;

/**
 * ClassMember is a common base class for ClassMethod and ClassField
 */
abstract public class ClassMember implements VMConstants {

    /* public accessors */

    /**
     * Is the member static?
     */
    final public boolean isStatic() {
        return (access() & ACCStatic) != 0;
    }

    /**
     * Is the member final?
     */
    final public boolean isFinal() {
        return (access() & ACCFinal) != 0;
    }

    /**
     * Turn on or off the final qualifier for the member.
     */
    public void setIsFinal(boolean newFinal) {
        if (newFinal) {
            setAccess(access() | ACCFinal);
        } else {
            setAccess(access() & ~ACCFinal);
        }
    }

    /**
     * Is the member private?
     */
    final public boolean isPrivate() {
        return (access() & ACCPrivate) != 0;
    }

    /**
     * Is the member protected?
     */
    final public boolean isProtected() {
        return (access() & ACCProtected) != 0;
    }

    /**
     * Is the member public?
     */
    final public boolean isPublic() {
        return (access() & ACCPublic) != 0;
    }

    /* These are expected to be implemented by subtypes */

    /**
     * Return the access flags for the method - see VMConstants
     */
    abstract public int access();

    /**
     * Set the access flags for the method - see VMConstants
     */
    abstract public void setAccess(int newAccess);

    /**
     * Return the name of the member
     */
    abstract public ConstUtf8 name();

    /**
     * Return the type signature of the method
     */
    abstract public ConstUtf8 signature();

    /**
     * Return the attributes associated with the member
     */
    abstract public AttributeVector attributes();

}
