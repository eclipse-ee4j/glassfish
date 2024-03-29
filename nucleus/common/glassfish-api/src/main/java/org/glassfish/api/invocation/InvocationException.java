/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.invocation;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;

@Service
@Contract
public class InvocationException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Exception ex;

    public InvocationException() {
    }

    public InvocationException(String s) {
        super(s);
    }

    public InvocationException(String s, Throwable cause) {
        super(s, cause);
    }

    public InvocationException(Exception ex) {
        this.ex = ex;
    }

    public Exception getNestedException() {
        return ex;
    }

}
