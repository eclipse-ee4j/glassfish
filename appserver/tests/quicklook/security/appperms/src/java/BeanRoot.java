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

package myapp;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.DependsOn;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import java.io.*;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

@Singleton
@Startup
public class BeanRoot implements BeanRootInterface {

    @EJB
    private BeanLeaf bl;

    @EJB
    private BeanMessageInterface msg;

    String MESSAGE_POST = "PostBeanRoot";
    String MESSAGE_HELLO = "HelloBeanRoot";

    @PostConstruct
    public void afterConstruct() {
        if (msg != null && !msg.getMessage().contains(MESSAGE_POST)) {
            msg.appendMessage(MESSAGE_POST);
        }
        String h = bl.sayHello();
        System.out.println("** BeanRoot: Hello from beanLeaf: " + h);
    }

    public String sayHello() {
        if (msg != null && !msg.getMessage().contains(MESSAGE_HELLO)) {
            msg.appendMessage(MESSAGE_HELLO);
        }

        StringBuffer check = new StringBuffer(" -EJB test-");


        FilePermission fp = new FilePermission(
                "/scratch/spei/bug/test/war.txt", "delete");
        try {
            if (System.getSecurityManager() != null) {
                AccessController.checkPermission(fp);
                check.append("BeanRoot - success for WAR.txt; ");
            } else
                check.append("BeanRoot - bypass for WAR.txt; ");
        } catch (AccessControlException e) {
            check.append("BeanRoot - failed for WAR.txt; ");
        }

        fp = new FilePermission("/scratch/spei/bug/test/ear.txt", "delete");
        try {
            if (System.getSecurityManager() != null) {
                AccessController.checkPermission(fp);
                check.append("BeanRoot - success for EAR.txt; ");
            } else
                check.append("BeanRoot - bypass for EAR.txt; ");
        } catch (AccessControlException e) {
            check.append("BeanRoot - failed for EAR.txt; ");
        }

        fp = new FilePermission("/scratch/spei/bug/test/ejb.txt", "delete");
        final FilePermission p1 = fp;
        try {
            if (System.getSecurityManager() != null) {
                AccessController.checkPermission(p1);
                check.append("BeanRoot - success for EJB.txt; ");
            } else
                check.append("BeanRoot - bypass for EJB.txt; ");
        } catch (AccessControlException e) {
            check.append("BeanRoot - failed for EJB.txt; " + e.getMessage());
        }

        return "Hello from: " + this.getClass().getName() + "; "
                + check.toString() + " , code= "
                + System.identityHashCode(this);
    }

}
