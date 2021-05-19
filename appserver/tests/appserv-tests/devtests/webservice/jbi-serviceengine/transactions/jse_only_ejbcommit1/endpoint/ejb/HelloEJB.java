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

package endpoint.ejb;

import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceRef;
import jakarta.ejb.Stateless;
import jakarta.ejb.SessionContext;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.annotation.Resource;
import java.sql.*;
import javax.sql.DataSource;

@WebService(endpointInterface="endpoint.ejb.Hello", targetNamespace="http://endpoint/ejb")
@Stateless
public class HelloEJB implements Hello {

    @Resource private SessionContext ctx;
    @Resource(mappedName="jdbc/__default") private DataSource ds;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String sayHello(String who) {
        System.out.println("**** EJB Called");
        Connection con=null;
        String tableName = "CUSTOMER_cm1";
        String nameEntry = "Vikas";
        String emailEntry= "vikas@sun.com";

        try {
        con = ds.getConnection();
        System.out.println("**** auto commit = " + con.getAutoCommit());

        updateTable(con, tableName, nameEntry, emailEntry);
        readData(con, tableName);
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
            if(con != null) con.close();
            } catch (SQLException se) {}
        }
        //ctx.setRollbackOnly();
        return "WebSvcTest-Hello " + who;
    }

    private void updateTable(Connection con, String tableName, String name, String email) throws Exception {
        PreparedStatement pStmt =
             con.prepareStatement("INSERT INTO "+ tableName +" (NAME, EMAIL) VALUES(?,?)");
        pStmt.setString(1, name);
        pStmt.setString(2, email);
        pStmt.executeUpdate();
    }

    private void readData(Connection con, String tableName) throws Exception {
        PreparedStatement pStmt =
             con.prepareStatement("SELECT NAME, EMAIL FROM "+tableName);
        ResultSet rs = pStmt.executeQuery();
        while(rs.next()){
            System.out.println("NAME="+rs.getString(1)+", EMAIL="+rs.getString(2));
        }
    }
}
