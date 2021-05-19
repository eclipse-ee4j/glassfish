/*
 * Copyright (c) 2002, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ejb.*;
import java.util.Properties;
import java.sql.*;
import java.rmi.RemoteException;

import javax.naming.*;
import javax.sql.*;

import jakarta.annotation.Resource;
import jakarta.validation.*;

@Stateless
public class MessageCheckerEJB implements SessionBean {

    @Resource
    Validator beanValidator ;

    @Resource
    ValidatorFactory validatorFactory;

    @Resource(mappedName="java:app/jdbc/sr"/*, lookup="java:app/jdbc/sr"*/, name="java:app/jdbc/test")
    DataSource ds;


    private int WAIT_TIME = 15;
    private String user = "j2ee";
    private String password = "j2ee";
    private Properties beanProps = null;
    private SessionContext sessionContext = null;
    private Connection heldCon = null;
    private MyAdminObject Controls;

    public MessageCheckerEJB() {
    }

    private void initialize(){

        System.out.println("java:app/jdbc/sr : " + ds);

         try{
        InitialContext ic = new InitialContext();
        DataSource datasource = (javax.sql.DataSource)ic.lookup(
               "java:app/jdbc/sr");
        System.out.println("DataSource: " + datasource );
        }catch(Exception e){
            System.out.println("java:app/jdbc/sr lookup failure" + e);
        }


        try{
        InitialContext ic = new InitialContext();
        validatorFactory = (jakarta.validation.ValidatorFactory)ic.lookup(
               "java:comp/env/ValidatorFactory");
        System.out.println("Bean Validator Factory : " + validatorFactory);
        }catch(Exception e){
            System.out.println("Bean Validator Factory setup failure " + e);
        }

        try{
        InitialContext ic = new InitialContext();
        beanValidator = (jakarta.validation.Validator)ic.lookup(
               "java:comp/env/Validator");
        System.out.println("Bean Validator : " + beanValidator);
        }catch(Exception e){
            System.out.println("Bean Validator setup failure " + e);
        }
    }

    public void ejbCreate()
        throws CreateException {
        System.out.println("bean created");
        heldCon = null;
    }

    public boolean done() {
        return Controls.done();
    }

    public int expectedResults() {
        return Controls.expectedResults();
    }

    public boolean testAdminObject(String jndiName, boolean expectLookupSuccess) {
        boolean failed = false;
        try{
            Context ic = new InitialContext();
            ic.lookup(jndiName);
        }catch(NameNotFoundException e){
            failed = true;
        }catch(NamingException ne){
            failed = isConstraintViolationException(ne.getCause(), jndiName);
        }
        return !failed==expectLookupSuccess;
    }

    public boolean testRA(int intValue){
        try{
            initialize();
            InitialContext ic = new InitialContext();
            MyAdminObject mao = (MyAdminObject)ic.lookup("java:app/eis/testAdmin");
            return mao.testRA(intValue, beanValidator);
        }catch(NamingException ne){
     //       ne.printStackTrace();
            return false;
        }
    }

    public void notifyAndWait() {
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

    public int getMessageCount() {
        try {
            Connection con = getFreshConnection();
            int count1 = getCount(con);
            con.close();

            /*
            synchronized(Controls.getLockObject()) {
                Controls.getLockObject().notify();
            }
            */

            return count1;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new EJBException(e);
        }
    }

    private int getCount(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        int count = 0;
        String messages = "";
        ResultSet result = stmt.executeQuery(
                "SELECT messageId, message "+ "FROM messages");
        while (result.next()) {
            count++;
            messages = messages + " - " + result.getString("messageId")+" "+
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
            Controls.initialize();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private boolean isConstraintViolationException(Throwable t, String jndiName){
       boolean result = false;
            if(t != null){
                    if(t instanceof jakarta.validation.ConstraintViolationException){
                        System.out.println("Found Constraint Violation for resource ["+jndiName+"]" + t.getMessage());
                         result = true;
                    }else{
                         result = isConstraintViolationException(t.getCause(), jndiName);
                    }
            }
            return result;
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
            DataSource ds = (DataSource) ic.lookup("java:comp/env/MyDB");
            debug("Looked up Datasource\n");
            debug("Get JDBC connection, auto sign on");
            con = ds.getConnection();

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
