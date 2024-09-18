/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.v3.common;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author dochez
 */
@Service(name = "html")
@PerLookup
public class HTMLActionReporter extends ActionReporter {

    /** Creates a new instance of HTMLActionReporter */
    public HTMLActionReporter() {
    }

    @Override
    public void writeReport(OutputStream os) throws IOException {
        PrintWriter writer = new PrintWriter(os);
        writer.print("<html><head/>");
        writer.println("<body>" +
                "<h1>GlassFish " + actionDescription + " command report</h1>" +
                "<br><br>");
        writer.println("Exit Code : " + this.exitCode);
        writer.println("<hr>");
        write(2, topMessage, writer);
        writer.println("<hr>");
        if (exception!=null) {
            writer.println("Exception raised during operation : <br>");
            writer.println("<pre>");
           exception.printStackTrace(writer);
            writer.println("</pre>");
        }
        if (!subActions.isEmpty()) {
            writer.println("There are " + subActions.size() + " sub operations");
        }
        writer.print("</body></html>");
        writer.flush();
    }

    private void write(int level, MessagePart part, PrintWriter writer) {
        String mess =  part.getMessage();
        if (mess==null){
            mess = "";//better than a null string output
        }
        if (level>6) {
            writer.println(mess);
        } else {
            writer.println("<h" + level + ">" + mess + "</h" + level + ">");
        }
        write(part.getProps(), writer);

        for (MessagePart child : part.getChildren()) {
            write(level+1, child, writer);
        }
    }

    private void write(Properties props, PrintWriter writer) {
        if (props==null || props.size()==0) {
            return;
        }
        writer.println("<table border=\"1\">");
        for (Map.Entry entry : props.entrySet()) {
            writer.println("<tr>");
            writer.println("<td>" + entry.getKey() + "</td>");
            writer.println("<td>" + entry.getValue() + "</td>");
            writer.println("</tr>");
        }
        writer.println("</table>");

    }
}
