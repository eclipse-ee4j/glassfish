/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;

/**
 * <p>
 * This exception is thrown to signal the parser to stop processing.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class ProcessingCompleteException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * The constructor.
     * </p>
     */
    public ProcessingCompleteException(LayoutDefinition ld) {
        super("Processing completed early.");
        _layoutDef = ld;
    }

    /**
     * <p>
     * This method is here to prevent the superclass method from eating up time. This implementation does not require a
     * stack trace. This method simply returns <code>this</code>.
     * </p>
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    /**
     * <p>
     * Accessor for the {@link LayoutDefinition} to be used.
     * </p>
     */
    public LayoutDefinition getLayoutDefinition() {
        return _layoutDef;
    }

    /**
     * <p>
     * This hold the {@link LayoutDefinition} which should be used as the result of processing the file.
     * </p>
     */
    private LayoutDefinition _layoutDef = null;
}
