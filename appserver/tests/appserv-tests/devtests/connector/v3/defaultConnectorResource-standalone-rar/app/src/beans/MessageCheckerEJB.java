/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package beans;

import connector.MyAdminObject;

import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBException;
import jakarta.ejb.CreateException;
import java.util.Properties;
import java.sql.*;

import javax.naming.*;
import javax.sql.*;

public class MessageCheckerEJB implements SessionBean {

    private int WAIT_TIME = 15;
    private String user = "j2ee";
    private String password = "j2ee";
    private Properties beanProps = null;
    private SessionContext sessionContext = null;
    private Connection heldCon = null;
    private MyAdminObject Controls;

    public MessageCheckerEJB() {
    }

    public void ejbCreate()
            throws CreateException {
        System.out.println("bean created");
        heldCon = null;
        /*
          if (holdConnection) {
          try {
          heldCon = getDBConnection();
          } catch (Exception ex) {
          ex.printStackTrace();
          throw new CreateException("Error in ejbCreate");
          }
          }
        */
    }

    public boolean done() {
        return Controls.done();
    }

    public int expectedResults() {
        return Controls.expectedResults();
    }

    public void notifyAndWait() {
        //done so as to inialize TransactionManager
        //hackGetConnection();
        try {
            synchronized (Controls.getLockObject()) {
                //Tell the resource adapter the client is ready to run
                Controls.getLockObject().notifyAll();

                debug("NOTIFIED... START WAITING");
                //Wait until being told to read from the database
                Controls.getLockObject().wait();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void hackGetConnection() {
        try {
            InitialContext ic = new InitialContext();
            DataSource timerDS = (DataSource) ic.lookup("jdbc/__TimerPool");
            Connection con = timerDS.getConnection();
            con.close();
        } catch (Exception e) {
            System.out.println("Hack to initialize tx-manager, failed to initialize");
        }
    }


    public int getMessageCount() {
        try {
            System.out.println("Getting Fresh connection");
            Connection con = getFreshConnection();
            System.out.println("Obtained Fresh connection" + con);
            int count1 = getCount(con);
            System.out.println("Got count" + count1);
            con.close();

            /*
               synchronized(Controls.getLockObject()) {
                   Controls.getLockObject().notify();
               }
           */

            return count1;
        } catch (Exception e) {
            e.printStackTrace();
            //e.printStackTrace(System.out);
            throw new EJBException(e);
        }
    }

    private int getCount(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        int count = 0;
        String messages = "";
        ResultSet result = stmt.executeQuery(
                "SELECT messageId, message " + "FROM messages");
        while (result.next()) {
            count++;
            messages = messages + " - " + result.getString("messageId") + " " +
                    result.getString("message") + "\n";
        }
        messages = messages + "count = " + count;
        System.out.println(messages);
        stmt.close();
        return count;
    }

    public void setSessionContext(SessionContext context) {
        sessionContext = context;
        try {
            Context ic = new InitialContext();
            user = (String) ic.lookup("java:comp/env/user");
            password = (String) ic.lookup("java:comp/env/password");
            Controls = (MyAdminObject) ic.lookup("java:comp/env/eis/testAdmin");
            System.out.println("CALLING INITILIZE ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]");
            Controls.initialize();
            System.out.println("CALLED INITILIZE ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void ejbRemove() {
        System.out.println("bean removed");
    }

    public void ejbActivate() {
        System.out.println("bean activated");
    }

    public void ejbPassivate() {
        System.out.println("bean passivated");
    }

    private Connection getFreshConnection() throws Exception {
        Connection oldHeldCon = heldCon;
        heldCon = null;
        Connection result = getDBConnection();
        heldCon = oldHeldCon;
        return result;
    }

    private Connection getDBConnection() throws Exception {
        if (heldCon != null) return heldCon;
        Connection con = null;
        try {
            Context ic = new InitialContext();
            com.sun.appserv.jdbcra.DataSource ds = (com.sun.appserv.jdbcra.DataSource) ic.lookup("java:comp/env/MyDB");
            debug("Looked up Datasource\n");
            debug("Get JDBC connection, auto sign on");
            con = ds.getConnection("dbuser","dbpassword");

            if (con != null) {
                return con;
            } else {
                throw new Exception("Unable to get database connection ");
            }
        } catch (SQLException ex1) {
            //ex1.printStackTrace();
            throw ex1;
        }
    }

    private void closeConnection(Connection con) throws SQLException {
        if (heldCon != null) {
            return;
        } else {
            con.close();
        }
    }

    private void debug(String msg) {
        System.out.println("[MessageCheckerEJB]:: -> " + msg);
    }
}
