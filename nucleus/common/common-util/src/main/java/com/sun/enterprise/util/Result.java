/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util;

/**
 * Used to carry a result or an exception justifying why a result could not be produced
 *
 * @author Jerome Dochez
 */
public class Result<T> {

    final T result;

    final Throwable error;

    public Result(T result) {
        this.result = result;
        error = null;
    }

    public Result(Throwable t) {
        result = null;
        this.error = t;
    }

    public boolean isSuccess() {
        return error==null;
    }

    public boolean isFailure() {
        return result==null;
    }

    public T result() {
        return result;
    }

    public Throwable exception() {
        return error;
    }
}
