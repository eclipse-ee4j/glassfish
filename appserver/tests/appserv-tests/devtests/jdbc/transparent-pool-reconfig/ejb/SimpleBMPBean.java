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

package com.sun.s1asdev.jdbc.transparent_pool_reconfig.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.*;
import org.glassfish.internal.api.Globals;
import org.glassfish.hk2.api.ServiceLocator;
import javax.security.auth.Subject;
import org.glassfish.security.common.UserNameAndPassword;


public class SimpleBMPBean implements EntityBean {

    protected DataSource ds;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context = new InitialContext();
            if (ds == null) {
                ds = (DataSource) context.lookup("java:comp/env/DataSource");
            }
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
        System.out.println("[**SimpleBMPBean**] Done with setEntityContext....");
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    public boolean acquireConnectionsTest(boolean expectFailure, long sleep) {
        boolean result;
        if (expectFailure) {
            result = false;
        } else {
            result = true;
        }

        try {
            Connection cons[] = new Connection[4];
            for (int i = 0; i < 4; i++) {
                try {
                    System.out.println("[DRC-TEST] : Using data-source : " + ds);

                    cons[i] = ds.getConnection();
                    System.out.println("[DRC-TEST] : " + cons[i].getMetaData().getUserName());
                    //introduce sleep in the middle of transaction (2 connections are acquired, 2 need to be acquired)
                    if (i == 2 & sleep > 0) {
                        try {
                            System.out.println("[DRC-TEST] : sleeping for " + sleep / 1000 + " seconds");
                            Thread.currentThread().sleep(sleep);
                            System.out.println("[DRC-TEST] : wokeup after " + sleep / 1000 + " seconds");
                        } catch (Exception e) {

                        }
                    }

                } catch (Exception e) {
                    if (expectFailure) {
                        result = true;
                    } else {
                        result = false;
                        System.out.println("[DRC-TEST] : " + e.getMessage());
                    }
                }
            }
            for (int i = 0; i < 4; i++) {
                try {
                    if (cons[i] != null) {
                        cons[i].close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        } catch (Exception sqe) {
            sqe.printStackTrace();
        }
        return result;
    }


    public void ejbLoad() {
    }

    public void ejbStore() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void unsetEntityContext() {
    }

    public void ejbPostCreate() {
    }

    public void setProperty(String property, String value) {
        ParameterMap params = new ParameterMap();
        params.add("DEFAULT", "domain.resources.jdbc-connection-pool.ql-jdbc-pool.property." + property + "=" + value);
        runCommand("set", params);
        System.out.println("Property set : " + property + " - value : " + value);
    }

    public void setAttribute(String attribute, String value) {
        ParameterMap params = new ParameterMap();
        params.add("DEFAULT", "domain.resources.jdbc-connection-pool.ql-jdbc-pool." + attribute + "=" + value);
        runCommand("set", params);
        System.out.println("attribute set : " + attribute + " - value : " + value);
    }

    private static ActionReport runCommand(String commandName, ParameterMap parameters) {
        Subject subject = new Subject();
        UserNameAndPassword rp =  new UserNameAndPassword("asadmin", "");
        subject.getPrincipals().add(rp);

        ServiceLocator serviceLocator = Globals.getDefaultHabitat();
        CommandRunner cr = serviceLocator.getService(CommandRunner.class);
        ActionReport ar = serviceLocator.getService(ActionReport.class);
        cr.getCommandInvocation(commandName, ar, subject).parameters(parameters).execute();
        return ar;
    }
}
