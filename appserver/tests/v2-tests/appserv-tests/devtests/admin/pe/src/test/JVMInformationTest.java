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
 * JVMInformationTest.java
 *
 * Created on July 22, 2005, 12:53 AM
 */
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.admin.mbeans.jvm.JVMInformationMBean;
import com.sun.enterprise.admin.server.core.AdminService;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

/**
 */
public class JVMInformationTest implements RemoteAdminQuicklookTest {

    private MBeanServerConnection mbsc;
    
    private static final String BACKEND_MBEAN_ON = AdminService.PRIVATE_MBEAN_DOMAIN_NAME + ":" + "category=monitor,type=JVMInformationCollector,server=server";
    private long start, end;
    /** Creates a new instance of JVMInformationTest */
    public JVMInformationTest() {
    }

    public void setMBeanServerConnection(MBeanServerConnection c) {
        this.mbsc = c;
    }

    public String test() {
            try {
            start = System.currentTimeMillis();
            dumpDASInfo();
            System.out.println("Gets the DAS VM Information");
            return ( SimpleReporterAdapter.PASS );
        } catch(final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            end = System.currentTimeMillis();
        }
}

    public String getName() {
        return ( this.getClass().getName() );
    }

    public long getExecutionTime() {
        return ( end - start );
    }
    
    String getMemoryInfo() throws Exception 
    {
        final ObjectName on = new ObjectName(BACKEND_MBEAN_ON);
        final JVMInformationMBean mbean = (JVMInformationMBean)MBeanServerInvocationHandler.newProxyInstance(mbsc, on, com.sun.enterprise.admin.mbeans.jvm.JVMInformationMBean.class, false);
        return mbean.getMemoryInformation(null);
    }
    private void dumpDASInfo() throws Exception {
        final ObjectName on = new ObjectName(BACKEND_MBEAN_ON);
        final JVMInformationMBean mbean = (JVMInformationMBean)MBeanServerInvocationHandler.newProxyInstance(mbsc, on, com.sun.enterprise.admin.mbeans.jvm.JVMInformationMBean.class, false);
        System.out.println(mbean.getThreadDump(null));
        System.out.println(mbean.getMemoryInformation(null));
        System.out.println(mbean.getClassInformation(null));
        System.out.println(mbean.getSummary(null));
        
    }
}
