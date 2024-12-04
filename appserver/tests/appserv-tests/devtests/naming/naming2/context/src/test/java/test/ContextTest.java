/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package test;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

import jakarta.ejb.embeddable.EJBContainer;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ContextTest {

    private static final String NL = System.getProperty("line.separator");
    private static EJBContainer ejbContainer;
    private TestBean testBean;
    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", null);

    @Rule
    public TestWatcher reportWatcher = new ReportWatcher(stat, "Naming::naming2::ContextTest");

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void setUpClass() {
        ejbContainer = EJBContainer.createEJBContainer();
    }


    @AfterClass
    public static void tearDownClass() {
        if (ejbContainer != null)
            ejbContainer.close();
    }


    @AfterClass
    public static void printSummary() {
        stat.printSummary();
    }


    @Before
    public void setUp() throws NamingException {
        testBean = (TestBean) ejbContainer.getContext().lookup("java:global/classes/TestBean");
        System.out.printf("%n----------------- Starting test %s -------------------%n", testName.getMethodName());
    }


    @After
    public void tearDown() {
        System.out.printf("%n================= Finishing test   ================================================%n%n");
    }


    @Test
    public void lookupWithWLInitialContextFactory() throws NamingException {
        TestBean b = testBean.lookupWithWLInitialContextFactory("java:global/classes/TestBean");
        DataSource ds = testBean.lookupWithWLInitialContextFactory("jdbc/__default");
        System.out.println("TestBean from lookup: " + b);
        System.out.println("DataSource from lookup: " + ds);
    }


    @Test
    public void listEmptyString2() throws NamingException {
        System.out.println(testBean.listEmptyString().toString());
    }


    @Test
    public void listEmptyString() throws NamingException {
        Context context = ejbContainer.getContext();
        NamingEnumeration<NameClassPair> list = context.list("");
        assertNotNull(list);
        System.out.println("Got NameClassPair: " + toString(list));
    }


    @Test
    public void listBindingsEmptyString2() throws NamingException {
        System.out.println(testBean.listBindingsEmptyString().toString());
    }


    @Test
    public void listBindingsEmptyString() throws NamingException {
        Context context = ejbContainer.getContext();
        NamingEnumeration<Binding> list = context.listBindings("");
        assertNotNull(list);
        System.out.println("Got Binding: " + toString(list));
    }


    // @Ignore
    @Test
    public void listGlobal2() throws NamingException {
        System.out.println(testBean.listGlobal().toString());
    }


    // @Ignore //got null componentId
    @Test
    public void listGlobal() throws NamingException {
        Context context = ejbContainer.getContext();
        NamingEnumeration<NameClassPair> list = context.list("java:global");
        assertNotNull(list);
        System.out.println("Got NameClassPair: " + toString(list));
    }


    // @Ignore
    @Test
    public void listBindingsGlobal2() throws NamingException {
        System.out.println(testBean.listBindingsGlobal().toString());
    }


    // @Ignore
    @Test
    public void listBindingsGlobal() throws NamingException {
        Context context = ejbContainer.getContext();
        NamingEnumeration<Binding> list = context.listBindings("java:global");
        assertNotNull(list);
        System.out.println("Got Binding: " + toString(list));
    }


    @Test
    public void listJavaComp() throws NamingException {
        System.out.println(testBean.listJavaComp().toString());
    }


    @Test
    public void listBindingsJavaComp() throws NamingException {
        System.out.println(testBean.listBindingsJavaComp().toString());
    }


    @Test
    public void listJavaModule() throws NamingException {
        System.out.println(testBean.listJavaModule().toString());
    }


    @Test
    public void listBindingsJavaModule() throws NamingException {
        System.out.println(testBean.listBindingsJavaModule().toString());
    }


    @Test
    public void listJavaApp() throws NamingException {
        System.out.println(testBean.listJavaApp().toString());
    }


    @Test
    public void listBindingsJavaApp() throws NamingException {
        System.out.println(testBean.listBindingsJavaApp().toString());
    }


    @Test
    public void closeNamingEnumerations() throws NamingException {
        testBean.closeNamingEnumerations();
    }


    @Test
    public void getIsInAppClientContainerFromEJB() throws NamingException {
        Boolean isACC = testBean.getIsInAppClientContainer();
        assertFalse(isACC);
        System.out.println("get java:comp/InAppClientContainer from EJB:" + isACC);
    }


    @Test
    public void getIsInAppClientContainerFromSEClient() throws NamingException {
        String jndiname = "java:comp/InAppClientContainer";
        Context context = new InitialContext();
        Boolean isACC = (Boolean) context.lookup(jndiname);
        assertFalse(isACC);
        System.out.println("get " + jndiname + " from java SE client:" + isACC);
    }


    private String toString(NamingEnumeration<? extends NameClassPair> n) throws NamingException {
        StringBuilder sb = new StringBuilder();
        sb.append(n.toString()).append(NL);
        while (n.hasMore()) { // test will fail with NPE if null
            NameClassPair x = n.next();
            sb.append(x).append(NL);
        }
        return sb.toString();
    }

}
