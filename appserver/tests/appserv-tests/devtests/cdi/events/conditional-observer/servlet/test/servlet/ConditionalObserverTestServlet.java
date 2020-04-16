/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import test.beans.SecondTestEventConditionalObserver;
import test.beans.TestEventConditionalObserver;
import test.beans.TestEventProducer;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class ConditionalObserverTestServlet extends HttpServlet {

    @Inject
    TestEventProducer trsb;


    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        trsb.fireEvents();
        if (TestEventConditionalObserver.documentCreatedEvent != 1){
            msg += "Document created event not received by observer";
        }
        
        if (TestEventConditionalObserver.documentUpdatedEvent != 2){
            msg += "Document updated event not received by observer";
        }
        
        if (TestEventConditionalObserver.documentApprovedEvents != 1){
            msg += "Document updated and approved event not " +
            		"received by observer";
        }
        
        if (TestEventConditionalObserver.documentAnyEvents != 4){
            msg += "# of Document event received by observer does not " +
            		"match expected values. expected = 2. observed="+ TestEventConditionalObserver.documentAnyEvents;
        }
        
        if (SecondTestEventConditionalObserver.documentCreatedEvent > 0) 
            msg += "A conditional observer was notified when it did not exist";
        
        

        writer.write(msg + "\n");
    }

}
