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

/*
 * CustomerDetails.java
 *
 * Created on March 17, 2008, 1:00 AM
 */

package web;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.transaction.UserTransaction;
import persistence.*;

/**
 *
 * @author adminuser
 * @version
 */
@PersistenceContext(name = "persistence/LogicalName", unitName = "webappPU")
public class CustomerDetails extends HttpServlet {

    @Resource
    private UserTransaction utx;

    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        // TODO output your page here
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet CustomerDetails</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Servlet CustomerDetails at " + request.getContextPath () + "</h1>");

        out.println("<h2>Search Customer Information</h2>");
        out.println("<p>Pl. select from 1,2,3,4,5 as a customer number</p>");
        String customerNr = request.getParameter("customer_nr");
        if((customerNr != null) && !(customerNr.equals(""))) {

            WebCustomer customer = findByID(new Integer(customerNr));
            if(customer != null){
                out.println("Customer's info for nr. " + customerNr + ": " + customer.getCustname());
            }else{
                out.println("Customer not found.");
            }
        }
        out.println("<form>");
        out.println("Customer number: <input type='text' name='customer_nr' />");
        out.println("<input type=submit value=Select />");
        out.println("</form>");


        out.println("</body>");
        out.println("</html>");

        out.close();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>

    protected WebCustomer findByID(Integer customerNr) {
        WebCustomer customer=null;
        try {
            Context ctx = (Context) new InitialContext().lookup("java:comp/env");
            utx.begin();
            EntityManager em =  (EntityManager) ctx.lookup("persistence/LogicalName");
            customer = em.find(WebCustomer.class, customerNr);
            utx.commit();


            // TODO:
            // em.persist(object);    utx.commit();
        } catch(Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,"exception caught", e);
            throw new RuntimeException(e);
        }

        return customer;
    }


}
