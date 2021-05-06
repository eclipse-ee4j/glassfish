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

package util;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.*;

import jakarta.persistence.*;
import jakarta.transaction.*;
import jakarta.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/** ServletUtil.java
  * This program is generic servlet which process the
  * HTTP request and invoke the
  * proper methods based on the request parameters.
  *
  * @author      Sarada Kommalapati
  */


public class ServletUtil extends HttpServlet{

    public String tc;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
        throws ServletException, IOException {
        processAction(request, response);
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
        throws ServletException, IOException {
        processAction(request, response);
    }

    public boolean processParams(HttpServletRequest request) {
      try {
        if (request.getParameter("case") != null) {
           tc = request.getParameter("case");
        }
        return true;
     } catch(Exception ex) {
        System.err.println("Exception when processing the request params");
        ex.printStackTrace();
        return false;
     }

    }


    public void processAction(HttpServletRequest request,
                               HttpServletResponse response)
      throws IOException{

        System.out.println("processing test driver request ... ");

        processParams(request);
        boolean status = false;
        System.out.println("tc:"+tc);

        response.setContentType("text/plain");
        ServletOutputStream out = response.getOutputStream();
        out.println("TestCase: "+tc);

        if (tc != null) {

           try {
               Class<?> c = getClass();
               Object t = this;

               Method[] allMethods = c.getDeclaredMethods();
               for ( Method m: allMethods ) {
                   String mname= m.getName();
                   if ( !mname.equals(tc.trim() ) ) {
                       continue;
                   }

                System.out.println("Invoking : " + mname );
                try {
                   m.setAccessible( true);
                   Object o = m.invoke( t );
                   System.out.println("Returned => " + (Boolean)o );
                   status = new Boolean((Boolean)o).booleanValue();
                   //Handle any methods thrown by method to be invoked
                } catch ( InvocationTargetException x ) {
                   Throwable cause = x.getCause();

                   System.err.format("invocation of %s failed: %s%n", mname, cause.getMessage() );
            } catch ( IllegalAccessException x ) {
               x.printStackTrace();
            }

           }
           } catch ( Exception ex ) {
              ex.printStackTrace();
            }

          if (status) {
            out.println(tc+":pass");
          } else {
            out.println(tc+":fail");
          }

      }
    }

}
