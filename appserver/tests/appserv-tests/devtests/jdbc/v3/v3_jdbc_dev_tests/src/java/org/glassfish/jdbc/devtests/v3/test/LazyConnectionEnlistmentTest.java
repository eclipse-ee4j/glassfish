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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.jdbc.devtests.v3.test;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;

/**
 *
 * @author jagadish
 */
public class LazyConnectionEnlistmentTest implements SimpleTest {

    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();
    UserTransaction uTx;
    InitialContext ic;

    public Map<String, Boolean> runTest(DataSource ds1, PrintWriter out) {
        try {
            if (testLazyEnlist_1(ds1, out)) {
                resultsMap.put("lazy-connection-enlistment", true);
            }else{
                resultsMap.put("lazy-connection-enlistment", false);
            }
        } catch (Exception e) {
            resultsMap.put("lazy-connection-enlistment", false);
        }
        return resultsMap;
    }

    private boolean testLazyEnlist_1(DataSource ds1, PrintWriter out) {
        Connection con1 = null;
        Connection con2 = null;
        boolean result = false;
        try{
            ic = new InitialContext();
            uTx = (UserTransaction)ic.lookup("java:comp/UserTransaction");

            out.println("got UserTransaction") ;
            uTx.begin();
            con1 = ds1.getConnection();

            //this is a lazy-enlist resource, only when the connection is used, it should be enlisted in transaction.
            //if it had been non-lazy-enlist, exception will be thrown stating not more than one non-xa resource can be
            //enlisted in a transaction.
            DataSource ds2 = (DataSource)ic.lookup("jdbc/jdbc-lazy-enlist-resource-2");
            con2 = ds2.getConnection();

            uTx.commit();
            out.println("able to commit") ;
            result = true;
        }catch(Exception e){
            HtmlUtil.printException(e, out);
        }finally{
            if(con1 != null){
                try{
                    con1.close();
                }catch(Exception e){}
            }

            if(con2 != null){
                try{
                    con2.close();
                }catch(Exception e){}
            }
        }
        return result;
    }
}
