/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.osgi_resources_test;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.sql.DataSource;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Statement;
import java.util.logging.Logger;

import java.security.Principal;
import javax.security.auth.Subject;


public class ResourcesTestActivator implements BundleActivator {

    private static class UserNameAndPassword implements Principal {
        private final String name;

        private UserNameAndPassword(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static final Logger logger =
            Logger.getLogger(org.glassfish.osgi_resources_test.ResourcesTestActivator.class.getPackage().getName());


    private BundleContext bundleContext;
    ServiceReference ref;
    DataSource ds;
    Subject s;

    public void start(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;
        s = new Subject();
        s.getPrincipals().add(new UserNameAndPassword("asadmin"));
        s.getPrincipals().add(new UserNameAndPassword("_InternalSystemAdministrator_"));

        acquireTestJdbcResource();
        test();
        debug("Bundle activated");

    }


    private void acquireTestJdbcResource() {
        //debug("---------------------------[ "+filter+" ]---------------------------------");
        try {
            ServiceReference[] refs = bundleContext.getAllServiceReferences(javax.sql.DataSource.class.getName(),
                    "(&(jndi-name=jdbc/s1qeDB)(osgi.jdbc.driver.class=org.apache.derby.jdbc.ClientDataSource))");
            if (refs != null) {
                for (ServiceReference ref : refs) {
                    ds = (DataSource) ref.getBundle().getBundleContext().getService(ref);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        //debug("-----------------------------------------------------------------------");
    }

    private void test() {

/*
        testJdbcResources(null);

        testJdbcResources("(osgi.jdbc.driver.class=oracle.jdbc.pool.OracleDataSource)");

        testJdbcResources("(jndi-name=jdbc/oracle_type4_resource)");

        testJdbcResources("(&(jndi-name=jdbc/oracle_type4_resource)(osgi.jdbc.driver.class=oracle.jdbc.pool.OracleDataSource))");
        testJdbcResources("(&(jndi-name=jdbc/__TimerPool)(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedXADataSource))");
        testJdbcResources("(&(jndi-name=jdbc/__default)(osgi.jdbc.driver.class=org.apache.derby.jdbc.ClientDataSource))");

        testJdbcResources("(&(jndi-name=jdbc/oracle_type4_resource)(osgi.jdbc.driver.class=org.apache.derby.jdbc.ClientDataSource))");


*/
        testJdbcResources("(&(jndi-name=jdbc/__default)(osgi.jdbc.driver.class=org.apache.derby.jdbc.ClientDataSource))");
        testJdbcResources("(&(jndi-name=jdbc/__TimerPool)(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedXADataSource))");

        testJdbcResources("(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedXADataSource)");
        testJdbcResources("(osgi.jdbc.driver.class=org.apache.derby.jdbc.ClientDataSource)");

        testJdbcResources("(jndi-name=jdbc/__TimerPool)");
        testJdbcResources("(jndi-name=jdbc/__default)");

        createJdbcResource("jdbc/test-resource");
        testJdbcResources("(jndi-name=jdbc/test-resource)", false, "-trial-1");
        deleteJdbcResource("jdbc/test-resource");
        //test it again to avoid stale service references (Refer issue : GLASSFISH-15790)
        testJdbcResources("(jndi-name=jdbc/test-resource)", true, "-trial-2");

        //test it again to avoid stale service references (Refer issue : GLASSFISH-15790)
        createJdbcResource("jdbc/test-resource");
        testJdbcResources("(jndi-name=jdbc/test-resource)", false, "-trial-3");
        deleteJdbcResource("jdbc/test-resource");
        testJdbcResources("(jndi-name=jdbc/test-resource)", true, "-trial-4");

        //test reconfiguration.
        createJdbcResource("jdbc/test-resource");
        setAttribute("server.resources.jdbc-connection-pool.DerbyPool.property.PortNumber=1444");
        testJdbcResources("(jndi-name=jdbc/test-resource)", true, "-trial-5");
        setAttribute("server.resources.jdbc-connection-pool.DerbyPool.property.PortNumber=1527");
        testJdbcResources("(jndi-name=jdbc/test-resource)", false, "-trial-6");
        deleteJdbcResource("jdbc/test-resource");
        testJdbcResources("(jndi-name=jdbc/test-resource)", true, "-trial-7");

        testJdbcResources("(jndi-name=jdbc/test-resource-1)", true);

        createJmsResource("jms/osgi.ConnectionFactory", "jakarta.jms.QueueConnectionFactory");
        testJmsResources("(jndi-name=jms/osgi.ConnectionFactory)", jakarta.jms.QueueConnectionFactory.class, false, "-trial-1");
        deleteJmsResource("jms/osgi.ConnectionFactory");
        //test it again to avoid stale service references (Refer issue : GLASSFISH-15790)
        testJmsResources("(jndi-name=jms/osgi.ConnectionFactory)", jakarta.jms.QueueConnectionFactory.class, true, "-trial-2");

        //test it again to avoid stale service references (Refer issue : GLASSFISH-15790)
        createJmsResource("jms/osgi.ConnectionFactory", "jakarta.jms.QueueConnectionFactory");
        testJmsResources("(jndi-name=jms/osgi.ConnectionFactory)", jakarta.jms.QueueConnectionFactory.class, false, "-trial-3");
        deleteJmsResource("jms/osgi.ConnectionFactory");
        testJmsResources("(jndi-name=jms/osgi.ConnectionFactory)", jakarta.jms.QueueConnectionFactory.class, true, "-trial-4");

        createJmsResource("jms/osgi.Admin.Object", "jakarta.jms.QueueConnectionFactory");
        testJmsResources("(jndi-name=jms/osgi.Admin.Object)", jakarta.jms.QueueConnectionFactory.class, false, "-trial-1");
        deleteJmsResource("jms/osgi.Admin.Object");
        //test it again to avoid stale service references (Refer issue : GLASSFISH-15790)
        testJmsResources("(jndi-name=jms/osgi.Admin.Object)", jakarta.jms.QueueConnectionFactory.class, true, "-trial-2");

        createJmsResource("jms/osgi.Admin.Object", "jakarta.jms.Queue");
        testJmsResources("(jndi-name=jms/osgi.Admin.Object)", jakarta.jms.Queue.class, false, "-trial-3");
        deleteJmsResource("jms/osgi.Admin.Object");
        //test it again to avoid stale service references (Refer issue : GLASSFISH-15790)
        testJmsResources("(jndi-name=jms/osgi.Admin.Object)", jakarta.jms.Queue.class, true, "-trial-4");
    }

    private void setAttribute(String nameValue) {
        ServiceLocator habitat = Globals.getDefaultHabitat();
        CommandRunner cr = habitat.getService(CommandRunner.class);
        ActionReport ar = habitat.getService(ActionReport.class);
        ParameterMap params = new ParameterMap();
        params.add("DEFAULT", nameValue);
        cr.getCommandInvocation("set", ar, s).parameters(params).execute();
    }


    private void deleteJmsResource(String resourceName) {
        ServiceLocator habitat = Globals.getDefaultHabitat();
        CommandRunner cr = habitat.getService(CommandRunner.class);
        ActionReport ar = habitat.getService(ActionReport.class);
        ParameterMap params = new ParameterMap();
        params.add("DEFAULT", resourceName);
        cr.getCommandInvocation("delete-jms-resource", ar, s).parameters(params).execute();
    }

    private void createJmsResource(String resourceName, String resourceType) {
        ServiceLocator habitat = Globals.getDefaultHabitat();
        CommandRunner cr = habitat.getService(CommandRunner.class);
        ActionReport ar = habitat.getService(ActionReport.class);
        ParameterMap params = new ParameterMap();
        params.add("resType", resourceType);
        params.add("DEFAULT", resourceName);
        cr.getCommandInvocation("create-jms-resource", ar, s).parameters(params).execute();
    }

    private void deleteJdbcResource(String resourceName) {
        ServiceLocator habitat = Globals.getDefaultHabitat();
        CommandRunner cr = habitat.getService(CommandRunner.class);
        ActionReport ar = habitat.getService(ActionReport.class);
        ParameterMap params = new ParameterMap();
        params.add("DEFAULT", resourceName);
        cr.getCommandInvocation("delete-jdbc-resource", ar, s).parameters(params).execute();
    }

    private void createJdbcResource(String resourceName) {
        ServiceLocator habitat = Globals.getDefaultHabitat();
        CommandRunner cr = habitat.getService(CommandRunner.class);
        ActionReport ar = habitat.getService(ActionReport.class);
        ParameterMap params = new ParameterMap();
        params.add("poolName", "DerbyPool");
        params.add("DEFAULT", resourceName);
        cr.getCommandInvocation("create-jdbc-resource", ar, s).parameters(params).execute();
    }


    private void testJmsResources(String filter, Class claz, boolean expectFailure) {
        testJmsResources(filter, claz, expectFailure, null);
    }

    private void testJmsResources(String filter, Class claz) {
        testJmsResources(filter, claz, true);
    }

    private void testJmsResources(String filter, Class claz, boolean expectFailure, String suffix) {
        debug("---------------------------[ " + filter + " ]---------------------------------");
        try {
            ServiceReference[] refs = bundleContext.getAllServiceReferences(claz.getName(), filter);
            if (refs != null) {
                for (ServiceReference ref : refs) {
                    Object o = ref.getBundle().getBundleContext().getService(ref);
                    debug("testJmsResources : " + o);
                    logResult(filter + suffix, "pass");
                }
            } else {
                debug("testJmsResources, none found");
                if (expectFailure) {
                    logResult(filter + suffix, "pass");
                } else {
                    logResult(filter + suffix, "fail");
                }
            }
        } catch (Exception e) {
            System.err.println(e);
            if (expectFailure) {
                logResult(filter + suffix, "pass");
            } else {
                logResult(filter + suffix, "fail");
            }
        }
        debug("-----------------------------------------------------------------------");
    }


    private void testJdbcResources(String filter, boolean expectFailure) {
        testJdbcResources(filter, expectFailure, null);
    }

    private void testJdbcResources(String filter) {
        testJdbcResources(filter, true);
    }

    private void testJdbcResources(String filter, boolean expectFailure, String suffix) {
        if (suffix == null) {
            suffix = "";
        }
        debug("---------------------------[ " + filter + " ]---------------------------------");
        try {
            ServiceReference[] refs = bundleContext.getAllServiceReferences(javax.sql.DataSource.class.getName(), filter);
            if (refs != null) {
                for (ServiceReference ref : refs) {
                    DataSource ds = (DataSource) ref.getBundle().getBundleContext().getService(ref);
                    try {
                        Connection con = ds.getConnection();
                        debug("got connection [" + con + "] for resource [" + ref.getProperty("jndi-name") + "]");
                        logResult(filter + suffix, "pass");
                        con.close();
                    } catch (Exception e) {
                        if (expectFailure) {
                            logResult(filter + suffix, "pass");
                        } else {
                            logResult(filter + suffix, "fail");
                        }
                        System.err.println(e);
                    }
                }
            } else {
                if (expectFailure) {
                    logResult(filter + suffix, "pass");
                } else {
                    logResult(filter + suffix, "fail");
                }
                debug("testJdbcResources, none found");
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        debug("-----------------------------------------------------------------------");
    }

    private void logResult(String filter, String result) {
        Connection con = null;
        Statement stmt = null;
        try {
            con = ds.getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate("insert into OSGI_RESOURCES_TEST_RESULTS values ('" + URLEncoder.encode(filter, "UTF-8") + "','" + result + "')");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
        debug("Bundle de-activated");
    }

    private void debug(String s) {
        logger.info("[osgi-ee-resources-tester] : " + s);
    }
}

