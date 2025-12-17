/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import java.util.Set;

import jakarta.annotation.Resource;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;

import test.beans.TestBean;
import test.beans.TestBeanInterface;
import test.beans.artifacts.Preferred;
import test.util.JpaTest;

import test.beans.wbinflib.TestBeanInWebInfLib;
import test.beans.artifacts.TestDatabase;


@WebServlet(name="mytest",
        urlPatterns={"/myurl"},
        initParams={ @WebInitParam(name="n1", value="v1"), @WebInitParam(name="n2", value="v2") } )
public class TestServlet extends HttpServlet {

    /* Normal injection of Beans */
    @Inject
    private transient org.jboss.logging.Logger log;
    @Inject BeanManager bm_at_inj;

    /*Injection of Jakarta EE resources*/
    @PersistenceUnit(unitName = "pu1")
    private EntityManagerFactory emf_at_pu;

    @Inject @TestDatabase
    private EntityManager emf_at_inj;

    private @Resource
    UserTransaction utx;

    @Inject @Preferred
    TestBeanInterface tbi;

    /* Injection of Beans from WEB-INF/lib */
    @Inject TestBeanInWebInfLib tbiwil;

    /* Test lookup of BeanManager*/
    BeanManager bm_lookup;


    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0. ");
        String msg = "n1=" + getInitParameter("n1") +
            ", n2=" + getInitParameter("n2");

        if (tbi == null) msg += "Bean injection into Servlet failed";
        if (tbiwil == null) msg += "Bean injection of a TestBean in WEB-INF/lib into Servlet failed";
        System.out.println("Test Bean from WEB-INF/lib=" + tbiwil);

        System.out.println("BeanManager is " + bm_at_inj);
        System.out.println("BeanManager via lookup is " + bm_lookup);
        if (bm_at_inj == null) msg += "BeanManager Injection via @Inject failed";
        try {
            bm_lookup = (BeanManager)((new InitialContext()).lookup("java:comp/BeanManager"));
        } catch (Exception ex) {
            ex.printStackTrace();
            msg += "BeanManager Injection via component environment lookup failed";
        }
        if (bm_lookup == null) msg += "BeanManager Injection via component environment lookup failed";

        //Check if Beans in WAR(WEB-INF/classes) and WEB-INF/lib/*.jar are visible
        //via BeanManager of WAR
        Set warBeans = bm_at_inj.getBeans(TestBean.class,new AnnotationLiteral<Any>() {});
        if (warBeans.size() != 1) msg += "TestBean in WAR is not available via the WAR BeanManager";

        Set webinfLibBeans = bm_at_inj.getBeans(TestBeanInWebInfLib.class,new AnnotationLiteral<Any>() {});
        if (webinfLibBeans.size() != 1) msg += "TestBean in WEB-INF/lib is not available via the WAR BeanManager";
        System.out.println("Test Bean from WEB-INF/lib via BeanManager:" + webinfLibBeans);

        //Test injection into WEB-INF/lib beans
        msg += tbiwil.testInjection();

        msg += testEMInjection(req);

        writer.write("initParams: " + msg + "\n");
    }


    private String testEMInjection(HttpServletRequest request) {
        String msg = "";
        EntityManager em = emf_at_inj;
        System.out.println("JPAResourceInjectionServlet::createEM" +
                "EntityManager=" + em);
        String testcase = request.getParameter("testcase");
        System.out.println("testcase=" + testcase);

        if (testcase != null) {
            JpaTest jt = new JpaTest(em, utx);
            boolean status = false;
            if ("llinit".equals(testcase)) {
                status = jt.lazyLoadingInit();
            } else if ("llfind".equals(testcase)) {
                status = jt.lazyLoadingByFind(1);
            } else if ("llquery".equals(testcase)) {
                status = jt.lazyLoadingByQuery("Carla");
            } else if ("llinj".equals(testcase)){
                status = ((tbi != null) &&
                        (tbi.testDatasourceInjection().trim().length()==0));
            }
            if (status) {
                msg += "";// pass
            } else {
                msg += (testcase + ":fail");
            }
        }
        return msg;
    }
}
