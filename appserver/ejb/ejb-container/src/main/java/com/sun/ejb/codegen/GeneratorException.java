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

package com.sun.ejb.codegen;

/**
 * The Generator exception is thrown whenever there is an error in
 * generating the stubs and skeletons and other related files.
 */
public class GeneratorException extends java.lang.Exception {

    /**
     * Constructs the Generator exception with the specified string.
     *
     * @param the string description
     */
    public GeneratorException(String s) {
        super(s);
        this.reason = s;
    }


    /**
     * Return the string representation of the exception.
     *
     * @return the string representation of the exception.
     */
    @Override
    public String toString() {
        return reason;
    }

    /**
     * @serial XXX needs doc
     */
    private final String reason;
}
