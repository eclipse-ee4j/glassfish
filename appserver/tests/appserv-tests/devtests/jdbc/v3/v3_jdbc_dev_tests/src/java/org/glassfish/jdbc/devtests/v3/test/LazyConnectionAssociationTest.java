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

package org.glassfish.jdbc.devtests.v3.test;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import jakarta.transaction.SystemException;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;

/**
 *
 * @author jagadish
 */
public class LazyConnectionAssociationTest implements SimpleTest {

    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    public Map<String, Boolean> runTest(DataSource ds1, PrintWriter out) {
        try {
            if (testLazyAssoc_1(ds1, out)) {
                resultsMap.put("lazy-connection-association", true);
            }else{
                resultsMap.put("lazy-connection-association", false);
            }
        } catch (Exception e) {
            resultsMap.put("lazy-connection-association", false);
        }
        return resultsMap;
    }

    /**
     * acquire specified number of connections and <b>do not</b> close it, so that further requests on this test
     * should still pass as lazy-assoc is <b>ON</b>
     * @param i
     * @param ds
     */
    private void acquireConnections(int count, DataSource ds, PrintWriter out)
            throws Exception{

            for(int i=0; i<count ; i++){
                ds.getConnection();
            }
    }

    private boolean testLazyAssoc_1(DataSource ds1, PrintWriter out)
            throws SystemException {

        boolean pass = false;
        HtmlUtil.printHR(out);
        out.println("<h4> Lazy connection association test </h4>");
        try{
            acquireConnections(32, ds1, out);
            pass = true;
        }catch(Exception e){
            HtmlUtil.printException(e, out);
        }
        HtmlUtil.printHR(out);

        return pass;
    }
}
