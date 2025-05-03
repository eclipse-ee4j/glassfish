/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2025 Contributors to the Eclipse Foundation. All rights reserved
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

package org.glassfish.osgijdbc_test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;


public class JDBCActivator implements BundleActivator {

    private static final Logger logger = Logger.getLogger(JDBCActivator.class.getPackage().getName());

    private BundleContext bundleContext;

    public void start(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;
        test();
        debug("Bundle activated");
    }

    private void test(){

        testJdbcResources(null);

        testJdbcResources("(osgi.jdbc.driver.class=oracle.jdbc.pool.OracleDataSource)");
        testJdbcResources("(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedXADataSource)");
        testJdbcResources("(osgi.jdbc.driver.class=org.apache.derby.jdbc.ClientDataSource)");

        testJdbcResources("(jndi-name=jdbc/oracle_type4_resource)");
        testJdbcResources("(jndi-name=jdbc/__TimerPool)");
        testJdbcResources("(jndi-name=jdbc/__default)");

        testJdbcResources("(&(jndi-name=jdbc/oracle_type4_resource)(osgi.jdbc.driver.class=oracle.jdbc.pool.OracleDataSource))");
        testJdbcResources("(&(jndi-name=jdbc/__TimerPool)(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedXADataSource))");
        testJdbcResources("(&(jndi-name=jdbc/__default)(osgi.jdbc.driver.class=org.apache.derby.jdbc.ClientDataSource))");

        testJdbcResources("(&(jndi-name=jdbc/oracle_type4_resource)(osgi.jdbc.driver.class=org.apache.derby.jdbc.ClientDataSource))");


    }

    private void testJdbcResources(String filter) {
        debug("---------------------------[ "+filter+" ]---------------------------------");
        try {
            ServiceReference[] refs = bundleContext.getAllServiceReferences(javax.sql.DataSource.class.getName(), filter);
            if (refs != null) {
                for (ServiceReference ref : refs) {
                    DataSource ds = (DataSource) ref.getBundle().getBundleContext().getService(ref);
                    try {
                        Connection con = ds.getConnection();
                        debug("got connection [" + con + "] for resource [" + ref.getProperty("jndi-name") + "]");
                        con.close();
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                }
            } else {
                debug("testJdbcResources, none found");
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        debug("-----------------------------------------------------------------------");
    }


    public void stop(BundleContext bundleContext) throws Exception {
        debug("Bundle de-activated");
    }

    private void debug(String s) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("[osgi-jdbc-tester] : " + s);
        }
    }
}
