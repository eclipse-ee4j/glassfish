/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jdbc.devtests.v3.util;

import java.io.PrintWriter;

/**
 *
 * @author jagadish
 */
public class HtmlUtil {

    /**
     * Prints the exceptions generated.
     * @param e
     * @param out
     */
    public static void printException(Throwable e, PrintWriter out) {
        StackTraceElement elements[] = e.getStackTrace();
        out.println("Following exception occurred :<br>");
        out.println(e.getMessage() + "<br>");
        for (StackTraceElement element : elements) {
            out.println(element.toString() + "<br>");
        }
    }

    /**
     * Prints a horizontal ruler.
     * @param out
     */
    public static void printHR(PrintWriter out){
        out.println("<hr>");
    }
}
