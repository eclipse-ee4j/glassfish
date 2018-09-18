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
 * QueryMBeansTest.java
 *
 * Created on July 22, 2005, 12:53 AM
 */
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.admin.mbeans.jvm.JVMInformationMBean;
import com.sun.enterprise.admin.server.core.AdminService;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.QueryExp;

/**
 */
public class QueryMBeansTest implements RemoteAdminQuicklookTest {

    private MBeanServerConnection mbsc;
    
    private long start, end;

    public QueryMBeansTest() {}

    public void setMBeanServerConnection(MBeanServerConnection c) {
        this.mbsc = c;
    }

    public String test() {
        try {
            start = System.currentTimeMillis();
            queryAllMBeans();
            System.out.println("Gets all MBeans");
            return SimpleReporterAdapter.PASS;
        } catch(final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            end = System.currentTimeMillis();
        }
    }

    public String getName() {
        return ( this.getClass().getName() );
    }

    public long getExecutionTime() {
        return ( end - start );
    }
    
    private File getFile() {
        String path = null; //set it to user passed arg
        if (path != null) 
            return new File(path);
        else return new File("./ListOfMbeans.txt");
    }
    
    private void queryAllMBeans() throws Exception {
        ObjectName name = null;
        QueryExp query = null;
        
        String[] domains = mbsc.getDomains();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(getFile()));
            for (String domain : domains) {
                name = new ObjectName(domain+":*");
                Set<ObjectName> mbeanObjNames = mbsc.queryNames(name, query);
                logDomainMBeansTofile(pw, domain, mbeanObjNames);
            }
            
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            if (pw != null) pw.close();
        }
    }
    
    private void logDomainMBeansTofile(PrintWriter pw, String domain, Set<ObjectName> mbeanObjNames) 
    throws IOException {
        pw.println("/***********************************************************/");
        pw.println("                     " + domain + "                         ");
        pw.println("/***********************************************************/");
        
        for (ObjectName objName : mbeanObjNames) 
            pw.println(objName.toString());
        pw.flush();
    }
    
}
