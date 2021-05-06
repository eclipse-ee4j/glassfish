/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.mq.cmt.excpt.ejb;

import jakarta.ejb.MessageDrivenBean;
import jakarta.ejb.MessageDrivenContext;
import javax.naming.*;
import jakarta.jms.*;
import java.sql.*;

public class SimpleMessageBean implements MessageDrivenBean,
    MessageListener {

    private transient MessageDrivenContext mdc = null;
    private Context context;
    private static int i;
    private final static Integer lock = new Integer("1");

    public SimpleMessageBean() {
        System.out.println("In SimpleMessageBean.SimpleMessageBean()");
    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
        System.out.println("In "
            + "SimpleMessageBean.setMessageDrivenContext()");
        this.mdc = mdc;
    }

    public void ejbCreate() {
        System.out.println("In SimpleMessageBean.ejbCreate()");
    }

    public void onMessage(Message inMessage) {
        TextMessage msg = null;

        System.out.println("MQ-CMT about to updateDB");
        updateDB();
        System.out.println("MQ-CMT after updateDB");

        try {
            if (inMessage instanceof TextMessage) {
                msg = (TextMessage) inMessage;
                System.out.println("MQ-CMT MESSAGE BEAN: Message received: "
                    + msg.getText());
            } else {
                System.out.println("Message of wrong type: "
                    + inMessage.getClass().getName());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (Throwable te) {
            te.printStackTrace();
        }
        throw new RuntimeException("Test exception");
    }  // onMessage

    private void updateDB() {
        synchronized(lock){
        try {
            //Class.forName("com.inet.ora.OraDriver");
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            //String url = "jdbc:inetora::wrx.india.sun.com:1521:dbsmpl1";
            String url = "jdbc:derby://localhost:1527/testdb;create=true;";
            java.sql.Connection con = DriverManager.getConnection(url,"dbuser", "dbpassword");
            ResultSet rs = con.createStatement().executeQuery("select exCount from mq_cmt_excpt");
            int count = 0;
            while (rs.next()){
                count = rs.getInt(1);
                System.out.println("MQ-CMT updateDB : " + count);
            }
            rs.close();
            count++;
            String qry = "update mq_cmt_excpt set exCount="+ count ;
            System.out.println("MQ-CMT updateDB : query : " + qry);
            con.createStatement().executeUpdate(qry);
            con.close();
        } catch(Exception e) {
           System.out.println("Error:" + e.getMessage());
        }
        }
    }

    public void ejbRemove() {
        System.out.println("In SimpleMessageBean.remove()");
    }
} // class
