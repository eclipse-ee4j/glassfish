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

import java.sql.*;
import com.pointbase.jdbc.jdbcInOutIntWrapper;

public class SampleExternalMethods {

   private Connection m_conn;

   public SampleExternalMethods (Connection p_conn) {
     m_conn = p_conn;
   }

   /**
    * Counts rows in the coffee table.
    * This is the procedure body for COUNTCOFFEE procedure.
    */
   public void countCoffee (jdbcInOutIntWrapper p_count) throws Exception{
     Statement stmt=null;
     try {
       stmt = m_conn.createStatement();
       String query = "select count(*) from coffee";
       ResultSet rs = stmt.executeQuery (query);

       rs.next();
       int count = rs.getInt(1);

       p_count.set (count);
       rs.close();
       stmt.close();
     } catch (Exception e) {
       e.printStackTrace();
       throw e;
     }
   }

   /**
    * Inserts a row in the coffee table.
    * This is the procedure body for INSERTCOFFEE procedure.
    */
   public void insertCoffee (String p_name, int p_qty) throws Exception{
     PreparedStatement pstmt=null;
     try {
       String insertStr = "insert into coffee values (?, ?)";
       pstmt = m_conn.prepareStatement(insertStr);
       pstmt.setString (1, p_name);
       pstmt.setInt (2, p_qty);

       int cnt = pstmt.executeUpdate ();
       m_conn.commit();

       pstmt.close();
     } catch (Exception e) {
       e.printStackTrace();
       throw e;
     }
   }
}
