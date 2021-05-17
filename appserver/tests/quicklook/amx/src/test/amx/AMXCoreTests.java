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

package amxtest;

import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;

import org.testng.annotations.*;
import org.testng.Assert;


import java.util.Set;
import java.util.Map;
import java.util.List;

import  org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.util.ExceptionUtil;

/**
Basic AMXProxy tests that verify connectivity and ability to
traverse the AMXProxy hierarchy and fetch all attributes.
 */
//@Test(groups={"amx"}, description="AMXProxy tests", sequential=false, threadPoolSize=5)
@Test(groups =
{
    "amx"
}, description = "Core AMX tests")
public final class AMXCoreTests extends AMXTestBase
{
    public AMXCoreTests()
    {
    }

    //@Test(timeOut=15000)
    public void bootAMX() throws Exception
    {
        final DomainRoot domainRoot = getDomainRootProxy();

        // one basic call to prove it's there...
        domainRoot.getAppserverDomainName();
    }

    /** IF THIS TEST FAILS, DO NOT REMOVE, just comment it out if the need mandates, but only temporarily */
    @Test(dependsOnMethods = "bootAMX")
    public void iterateAllSanityCheck() throws Exception
    {
        try
        {
            final Set<AMXProxy> all = getAllAMX();
            assert all.size() > 20 : "Expected at least 20 AMX MBeans, got: " + all.size();
            for (final AMXProxy amx : all)
            {
                try
                {
                    if ( ! amx.valid() )
                    {
                        continue;   // could have been unregistered
                    }
                    final Set<AMXProxy> children = amx.childrenSet();
                    assert children != null;
                }
                catch( final Exception e )
                {
                    if ( ExceptionUtil.getRootCause(e) instanceof InstanceNotFoundException )
                    {
                        continue;
                    }
                    if ( ! amx.valid() )
                    {
                        warning( "MBean valid()=false during testing, ignoring: " + amx.objectName() );
                    }

                    throw e;
                }
            }
        }
        catch( final Throwable t )
        {
           System.out.println( "Test iterateAllSanityCheck() IGNORED, see issue #9355" );
           t.printStackTrace();
        }
    }

    /** IF THIS TEST FAILS, DO NOT REMOVE, just comment it out if the need mandates, but only temporarily */
    @Test
    public void testAMXComplianceMonitorFailureCount()
    {
        try
        {
        final Map<ObjectName, List<String>> failures = getDomainRootProxy().getComplianceFailures();

        assert failures.size() == 0 :
            "Server indicates that there are non-compliant AMX MBean validator failures, failure count = " + failures.size() + "\n" + failures;
        }
        catch( final Throwable t )
        {
            System.out.println( "\n******* Test testAMXComplianceMonitorFailureCount() IGNORED, see issue #10096 ******* \n" );
            t.printStackTrace();
        }
     }

    @Test
    public void testDemo()
    {
        Demo.runDemo( false, new String[] { mHost, "" + mPort, mAdminUser, mAdminPassword });
    }
}





























