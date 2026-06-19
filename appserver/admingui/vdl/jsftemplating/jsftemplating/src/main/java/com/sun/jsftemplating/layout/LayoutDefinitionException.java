/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout;

import com.sun.jsftemplating.TemplatingException;

/**
 * <p>
 * This exception is thrown when a {@link LayoutDefinitionManager} is unable to locate a
 * {@link com.sun.jsftemplating.layout.descriptors.LayoutDefinition}. This exception should not be used to indicate that
 * a syntax error has occurred, see {@link SyntaxException}. This exception is meant for file not found, i/o problems,
 * etc.
 * </p>
 */
public class LayoutDefinitionException extends TemplatingException {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public LayoutDefinitionException(String msg, Throwable ex) {
        super(msg, ex);
    }

    /**
     *
     */
    public LayoutDefinitionException() {
        super();
    }

    /**
     *
     */
    public LayoutDefinitionException(Throwable ex) {
        super(ex);
    }

    /**
     *
     */
    public LayoutDefinitionException(String msg) {
        super(msg);
    }
}
