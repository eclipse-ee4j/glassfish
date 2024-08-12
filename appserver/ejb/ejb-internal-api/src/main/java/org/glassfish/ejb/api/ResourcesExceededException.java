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

package org.glassfish.ejb.api;

import java.io.PrintStream;
import java.io.PrintWriter;

public class ResourcesExceededException extends Exception {

    private Exception ex;

    public ResourcesExceededException() {
    }

    public ResourcesExceededException(String s) {
        super(s);
    }

    public ResourcesExceededException(Exception ex) {
        super(ex.getMessage());
        this.ex = ex;
    }

    public ResourcesExceededException(String s, Exception ex) {
        super(s);
        this.ex = ex;
    }

    public Exception getNestedException() {
        return ex;
    }

    public void printStackTrace() {
        super.printStackTrace();
        if (ex != null) {
            ex.printStackTrace();
        }
    }

    public void printStackTrace(PrintStream ps) {
        super.printStackTrace(ps);
        if (ex != null) {
            ex.printStackTrace(ps);
        }
    }

    public void printStackTrace(PrintWriter pw) {
        super.printStackTrace(pw);
        if (ex != null) {
            ex.printStackTrace(pw);
        }
    }

}
