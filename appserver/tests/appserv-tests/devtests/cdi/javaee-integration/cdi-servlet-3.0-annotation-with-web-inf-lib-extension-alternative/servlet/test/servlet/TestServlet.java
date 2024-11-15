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
import test.beans.artifacts.TestDatabase;
import test.beans.wbinflib.AnotherTestBeanInWebInfLib;
import test.beans.wbinflib.TestAlternativeBeanInWebInfLib;
import test.beans.wbinflib.TestBeanInWebInfLib;
import test.util.JpaTest;

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
    //We are injecting TestBeanInWebInfLib directly above. Since the alternative
    //TestBean is not enabled in the WAR's BDA(beans.xml),
    //TestBeanInWebInfLib must be injected


    @Inject AnotherTestBeanInWebInfLib atbiwil;
    //However in this case, when AnotherTestBeanInWebInfLib tries to inject
    //TestBeanInWebInfLib in its bean, it must inject TestAlternativeBeanInWebInfLib
    //as the alternative bean is enabled in the WEB-INF/lib's BDA (beans.xml)

    /* Test lookup of BeanManager*/
    BeanManager bm_lookup;


    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        String msg = "";
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

        //Ensure Alternative Beans enabled only in the context of web-inf/lib is
        //not visible in WAR's BM
        Set webinfLibAltBeans = bm_at_inj.getBeans(TestAlternativeBeanInWebInfLib.class,new AnnotationLiteral<Any>() {});
        if (webinfLibAltBeans.size() != 0) msg += "TestAlternativeBean in WEB-INF/lib is available via the WAR BeanManager";
        System.out.println("Test Bean from WEB-INF/lib via BeanManager:" + webinfLibAltBeans);

        //Test injection of a Bean in WEB-INF/lib beans into Servlet
        //and check that the Alternative bean is not called.
        //The alternative bean in web-inf/lib is not enabled in the WAR's beans.xml
        //and hence must not be visible.
        TestAlternativeBeanInWebInfLib.clearStatus(); //clear status

        String injectionOfBeanInWebInfLibResult = tbiwil.testInjection();
        System.out.println("injectionWithAlternative returned: " + injectionOfBeanInWebInfLibResult);
        if (injectionOfBeanInWebInfLibResult.equals ("Alternative")) {
            msg += "Expected that the original TestBeanInWebInfLib is called, " +
                            "but instead got " + injectionOfBeanInWebInfLibResult + " instead";
        }

        if(TestAlternativeBeanInWebInfLib.ALTERNATIVE_BEAN_HAS_BEEN_CALLED) {
            msg += "Alternate Bean is called even though it is not enabled in the WAR's beans.xml";
        }

        //Test injection into a bean in web-inf/lib
        //In this case the alternative bean must be called, as it is enabled
        //in the library jar's beans.xml and the injection of the Bean
        //happens in the context of the library jar
        TestAlternativeBeanInWebInfLib.clearStatus(); //clear status
        String injectionWithAlternative2 = atbiwil.testInjection();
        System.out.println("injectionWithAlternative returned: " + injectionWithAlternative2);
        if (injectionWithAlternative2.equals ("Alternative")) {
            //test injection successful
        } else {
            msg += "Expected alternative, but got " + injectionWithAlternative2 + " instead";
        }

        if (!TestAlternativeBeanInWebInfLib.ALTERNATIVE_BEAN_HAS_BEEN_CALLED) {
            msg += "Alternative Bean enabled in WEB-INF/lib was not called " +
                            "when the injection happened in the context of a " +
                            "Bean in WEB-INF/lib where the alternative Bean was enabled";
        }


        msg += testEMInjection(req);

        writer.write(msg + "\n");
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
