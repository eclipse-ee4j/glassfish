/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package test;

/*
 * SMFTest.java
 *
 * Created on August 22, 2005, 5:40 PM
 */
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.admin.servermgmt.SMFService;
import com.sun.enterprise.admin.servermgmt.SMFService.AppserverServiceType;
import com.sun.enterprise.admin.servermgmt.SMFServiceHandler;
import com.sun.enterprise.admin.servermgmt.ServiceHandler;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;
import javax.management.MBeanServerConnection;
/**
 */
public class SMFTest implements RemoteAdminQuicklookTest {
    private final static String CONFIG_FILE="smftest.properties";
    //look at this file first
    
    private long start, end;
    public SMFTest() throws Exception {
        setProperties();
        start = System.currentTimeMillis();
    }

    public long getExecutionTime() {
        return ( end - start );
    }

    public void setMBeanServerConnection(final MBeanServerConnection c) {
	}

    public String getName() {
	    return ( this.getClass().getName() );
    }

    public String test() {
        try {
            start = System.currentTimeMillis();
            testPlatform();
            createSMFService();
            System.out.println("This test just creates the SMF service");
            return ( SimpleReporterAdapter.PASS );
        } catch(final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            end = System.currentTimeMillis();
        }
        
    }
    private void setProperties() throws Exception {
        final Properties additional = new Properties();
        additional.load(new FileInputStream(CONFIG_FILE));
        final Properties existing = System.getProperties();
        existing.putAll(additional);
        System.setProperties(existing);
        existing.list(System.out);
    }
    private void testPlatform() throws Exception {
        final String OS_NAME = "SunOS";
        final String OS_VERS = "5.10";
        System.out.println(System.getProperty("os.name"));
        System.out.println(System.getProperty("os.version"));
        final boolean ok = OS_NAME.equals(System.getProperty("os.name")) && 
                           OS_VERS.equals(System.getProperty("os.version"));
        if (!ok)
            throw new RuntimeException("Runs only on Solaris 10");
    }
    
    private void createSMFService() {
        final ServiceHandler smfsh = new SMFServiceHandler();
        final SMFService ss = new SMFService();
        ss.setDate(new Date().toString());
        ss.setAsadminPath(System.getProperty("AS_ADMIN_PATH"));
        ss.setName(System.getProperty("SERVICE_NAME"));
        ss.setLocation(System.getProperty("SERVICE_LOCATION"));
        ss.setFQSN();
        ss.setOSUser();
        ss.setPasswordFilePath(System.getProperty("PASSWORD_FILE_PATH"));
        ss.setType(SMFService.AppserverServiceType.valueOf(System.getProperty("SERVICE_TYPE")));
        System.out.println(ss.toString());
        final boolean v = ss.isConfigValid();
        smfsh.createService(ss.tokensAndValues());
    }
}
