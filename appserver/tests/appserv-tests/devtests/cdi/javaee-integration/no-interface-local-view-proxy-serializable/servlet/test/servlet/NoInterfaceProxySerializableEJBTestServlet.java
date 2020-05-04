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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import jakarta.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.beans.TestBeanInterface;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class NoInterfaceProxySerializableEJBTestServlet extends HttpServlet {

    @Inject
    TestBeanInterface tbi;

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException,
            IOException {
        PrintWriter writer = response.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        // set state
        tbi.setState("TEST");

        // Test serializability of EJB in TestBean.
        try {
            File tmpFile = File.createTempFile("SerializableProxyTest", null);
            FileOutputStream fout = new FileOutputStream(tmpFile);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            System.out.println("Writing " + tbi + " to file-" + tmpFile);
            oos.writeObject(tbi);
            oos.close();

            FileInputStream fin = new FileInputStream(tmpFile);
            ObjectInputStream ois = new ObjectInputStream(fin);
            System.out.println("Attempting to read " + tbi + " from file-"
                    + tmpFile);

            TestBeanInterface tb = (TestBeanInterface) ois.readObject();
            // check if we have access to the same stateful session bean
            System.out.println(tb.getState().equals("TEST"));
            if (!tb.getState().equals("TEST"))
                msg += "Failed to serialize stateful bean";
            //check if we can invoke stateless EJB
            tb.method1();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
            msg += "Failed to serialize/deserialize proxy to EJB";
        }

        writer.write(msg + "\n");
    }
}
