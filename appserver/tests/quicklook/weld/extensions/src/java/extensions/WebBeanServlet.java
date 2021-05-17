/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package extensions;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Test that WebBean is deployed and accessible.
 *
 * @author paulsandoz
 */
public class WebBeanServlet extends HttpServlet {

    @Resource(name="injectedResource") int injectedResource;

    private boolean inject = true;
    private boolean postConstruct = true;

    @PostConstruct
    public void postConstruct() {
      // This is not valid anymore.  See
      // https://issues.jboss.org/browse/CDI-425 and
      // https://issues.jboss.org/browse/CDITCK-408
//        for (final ProcessInjectionTarget pit : extensions.ExtensionBean.l) {
//            final InjectionTarget it = pit.getInjectionTarget();
//            final InjectionTarget nit = new InjectionTarget() {
//
//                public void inject(Object t, CreationalContext cc) {
//                    inject = true;
//                    it.inject(t, cc);
//                }
//
//                public void postConstruct(Object t) {
//                    postConstruct = true;
//                    it.postConstruct(t);
//                }
//
//                public void preDestroy(Object t) {
//                    it.preDestroy(t);
//                }
//
//                public Object produce(CreationalContext cc) {
//                    return it.produce(cc);
//                }
//
//                public void dispose(Object t) {
//                    it.dispose(t);
//                }
//
//                public Set getInjectionPoints() {
//                    return it.getInjectionPoints();
//                }
//            };
//            pit.setInjectionTarget(nit);
//        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebBean wb = get(WebBean.class, getBM());

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<html>");
            out.println("<body>");
            out.println("<h1>WEB BEAN " + wb.get() + "</h1>");
            out.println("<h1>" + inject + " " + postConstruct + "</h1>");
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>


    public <T> T get(Class<T> c, BeanManager bm) {
        Set<Bean<?>> bs = bm.getBeans(c);
        if (bs.isEmpty()) {
            System.out.println("CANNOT GET SET OF BEANS FOR " + c);
            return null;
        }
        Bean<?> b = bm.resolve(bs);

        CreationalContext<?> cc = bm.createCreationalContext(b);

        return c.cast(bm.getReference(b, c, cc));
    }

    public BeanManager getBM() {
        try {
            return (BeanManager) new InitialContext().
                lookup("java:comp/BeanManager");
        } catch (NamingException ex) {
            Logger.getLogger(WebBeanServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
