/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.stringsubs;

import com.sun.enterprise.admin.servermgmt.stringsubs.impl.StringSubstitutionEngine;

import java.io.InputStream;

/**
 * Factory class to create {@link StringSubstitutor} object.
 */
public abstract class StringSubstitutionFactory {
    /**
     * Create a {@link StringSubstitutor} object.
     *
     * @param stringsubs An input stream of string substitution file.
     * @return An object facilitate string substitution process.
     * @throws StringSubstitutionException If the input stream is invalid or any exception occurs during parsing or in
     * validation.
     */
    public static StringSubstitutor createStringSubstitutor(InputStream stringsubs) throws StringSubstitutionException {
        return new StringSubstitutionEngine(stringsubs);
    }
}
