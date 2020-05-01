/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import test.beans.Bean1;
import test.beans.Bean2;

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;
import java.io.IOException;

@WebServlet(name = "TransactionScopedTestServlet", urlPatterns = {"/TransactionScopedTestServlet"})
public class TransactionScopedTestServlet extends HttpServlet {
    public static boolean bean1PreDestroyCalled = false;
    public static boolean bean2PreDestroyCalled = false;

    @Inject
    UserTransaction userTransaction;

    @Inject
    Bean1 bean1;

    @Inject
    Bean1 bean1_1;

    @Inject
    Bean2 bean2;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuffer msg = new StringBuffer();
        ServletOutputStream m_out = response.getOutputStream();

        msg.append( "@TransactionScoped Test");
        try {
            userTransaction.begin();
        } catch (Exception e) {
            e.printStackTrace();
        }

        bean1PreDestroyCalled = false;
        bean2PreDestroyCalled = false;
        msg.append(doActiveTransaction("First"));

        try {
            userTransaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        msg.append( checkPreDestroyCalled("First") );
        try {
            userTransaction.begin();
        } catch (Exception e) {
            e.printStackTrace();
        }

        bean1PreDestroyCalled = false;
        bean2PreDestroyCalled = false;
        msg.append(doActiveTransaction("Second"));
        try {
            userTransaction.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
        msg.append( checkPreDestroyCalled("Second") );

        try {
            bean1.foo();
            msg.append("Should have gotten a ContextNotActiveException.\n");
        } catch (ContextNotActiveException cnae) {
        }

        m_out.print( msg.toString() );
    }

    private String doActiveTransaction( String transNum ) {
        StringBuffer msg = new StringBuffer();
        String bean1Foo = bean1.foo();
        String bean1_1Foo = bean1_1.foo();
        String bean2Foo = bean2.foo();

        if ( bean1PreDestroyCalled ) {
            msg.append( transNum + " Transaction bean1.preDestroyCalled initialized incorrectly.\n");
        }

        if ( bean2PreDestroyCalled ) {
            msg.append( transNum + " Transaction bean2.preDestroyCalled initialized incorrectly.\n");
        }

        if (!bean1Foo.equals(bean1_1Foo)) {
            msg.append( transNum + " Transaction bean1 does not equal bean1_1.  It should.\n");
        }

        if (bean2Foo.equals(bean1Foo)) {
            msg.append(transNum + " Transaction bean2 equals bean1.  It should not.\n");
        }

        return msg.toString();
    }

    private String checkPreDestroyCalled( String transNum ) {
        StringBuffer msg = new StringBuffer();

        if ( ! bean1PreDestroyCalled ) {
            msg.append( transNum + " Transaction bean1.preDestroyCalled not called.\n");
        }

        if ( ! bean2PreDestroyCalled ) {
            msg.append( transNum + " bean2.preDestroyCalled not called.\n");
        }

        return msg.toString();
    }

}
