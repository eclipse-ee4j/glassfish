/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package myapp.test;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import myapp.util.HtmlUtil;

/**
 *
 * @author jagadish
 */
public class LeakTest implements SimpleTest{

    
       Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    public Map<String, Boolean> runTest(DataSource ds1, PrintWriter out) {
        try {
            if (checkForNoLeak(ds1, out)) {
                resultsMap.put("no-leak-test", true);
            }else{
                resultsMap.put("no-leak-test", false);
            }
        } catch (Exception e) {
            resultsMap.put("no-leak-test", false);
        }

        return resultsMap;

    }
        private boolean checkForNoLeak(DataSource ds1, PrintWriter out) {
        int count = 32;
        boolean result = false;
        Connection[] connections = new Connection[count];

        HtmlUtil.printHR(out);
        out.println("<h4> no leak test </h4>");
        try {
            for (int i = 0; i < count; i++) {
                connections[i] = ds1.getConnection();
            }
            out.println("able to retrieve all 32 connections<br>");
            result = true;
        } catch (Exception e) {
            HtmlUtil.printException(e, out);
            result = false;
        } finally {

            try {
                for (Connection con : connections) {
                    try {
                        if (con != null) {
                            con.close();
                        }
                    } catch (Exception e) {

                    }
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }
            HtmlUtil.printHR(out);
            return result;
        }
     }
}
