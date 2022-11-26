/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.cli.remote;

import java.util.ArrayList;
import java.util.List;

/**
 * An immutable class to analyze the exception stack trace of a given instance of {@link java.lang.Exception}. Can be
 * extended to handle throwables, but it is not done in this version on purpose. Takes the snapshot of given exception
 * at the time of instantiation.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 Prelude
 */
final class ExceptionAnalyzer {

    private final Exception exc;
    private final List<Throwable> chain;

    ExceptionAnalyzer(Exception e) {
        if (e == null) {
            throw new IllegalArgumentException("null arg");
        }
        this.exc = e;
        this.chain = new ArrayList<>();
        chain.add(exc);
        build();
    }

    private void build() {
        Throwable t = exc.getCause();
        while (t != null) {
            chain.add(t);
            t = t.getCause();
        }
    }

    /**
     * Returns the first instance of the given Exception class in the chain of causes. The counting starts from the instance
     * of the Exception that created the ExceptionAnalyzer class itself.
     *
     * @param ac the unknown subclass of Exception that needs the chain to be examined for
     * @return first instance of given Throwable (returned object will be an instance of the given class) or null if there
     * is no such instance
     */
    Throwable getFirstInstanceOf(Class<? extends Exception> ac) {
        for (Throwable t : chain) {
            try {
                ac.cast(t);
                return t;
            } catch (ClassCastException cce) {
                //ignore and continue
            }
        }
        return null;
    }
}
