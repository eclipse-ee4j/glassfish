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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.inject.Inject;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.naming.InitialContext;

@WebServlet(name="mytest",
        urlPatterns={"/myurl"},
        initParams={ @WebInitParam(name="n1", value="v1"), @WebInitParam(name="n2", value="v2") } )
public class TestServlet extends HttpServlet {
    @Inject TestBean tb;
    @Inject BeanManager bm;
    BeanManager bm1;
    
    @Inject 
    private transient org.jboss.logging.Logger log;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0. ");
        String msg = "n1=" + getInitParameter("n1") +
            ", n2=" + getInitParameter("n2");
        if (tb == null) msg += "Bean injection into Servlet failed";
        if (bm == null) msg += "BeanManager Injection via @Inject failed";
        try {
            bm1 = (BeanManager)((new InitialContext()).lookup("java:comp/BeanManager"));
        } catch (Exception ex) {
            ex.printStackTrace();
            msg += "BeanManager Injection via component environment lookup failed";
        }
        if (bm1 == null) msg += "BeanManager Injection via component environment lookup failed";
        
        //Ensure that the OptionalService Bean is not registered.
        Set<Bean<?>> optionalServiceBeans = bm.getBeans(TestService.class,new AnnotationLiteral<Any>() {});
        boolean optionalServiceAvailable = false;
        for (Iterator<Bean<?>> iterator = optionalServiceBeans.iterator(); iterator
                .hasNext();) {
            Bean<?> b =  iterator.next();
            System.out.println("-----" + b + " " + b.getClass() + " " + b.getBeanClass().getCanonicalName());
            if (b.getBeanClass().getCanonicalName().contains("Optional")) optionalServiceAvailable = true; 
        }
        
        //The OptionalService Bean was not added to the WAR and hence must not available through the BM.
        if (optionalServiceAvailable) msg += "OptionalServiceBean is registered, though it was not included in the WAR";
        
        System.out.println("BeanManager is " + bm);
        System.out.println("BeanManager via lookup is " + bm1);
        writer.write("initParams: " + msg + "\n");
    }
}
