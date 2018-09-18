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

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.*;
import java.io.IOException;
import javax.management.JMException;
import com.sun.enterprise.util.*;

/**
 *
 * @author bnevins
 */

public class GetResourceTest extends LifeCycle
{
    public GetResourceTest()
    {
    }
    
    String testInternal() throws JMException, IOException
    {
        long msec = System.currentTimeMillis();
        title("GetResourceTest");
        title("Stage I -- deploy ");
        String name = "getResourceTest";
        String classname = "testmbeans.AWT";
        String objname = "user:foo=" + name;
        create(classname, objname, name);
        msec = System.currentTimeMillis() - msec;
        System.out.println("Created CMB, name: " + name + ", impl class: " 
                + classname + ", obj-name: " + objname + ", Time(msec): " 
                + msec + ", Memory Usage: " + getMemoryInfo());

        title("Stage II -- check ");
        List<String> list = list();
        if(!list.contains(name))
            throw new RuntimeException("Could not find " + name + " in list of deployed MBeans");
        System.out.println("Found: " + name);
        title("All MBeans were registered OK");

        if(interactive)
            Console.readLine("Go look at the AWT window and hit ENTER: ");

        title("Stage III -- delete ");
        
        if(interactive)
            Console.readLine("Shall I delete the MBeans? [y]: ");
            delete(name);
            System.out.println("Deleted: " + name);
        
        title("Stage IV  -- check deletion");
        
        list = list();
        if(list.contains(name))
            throw new RuntimeException(name + " is still in the list of deployed MBeans");
        System.out.println("Verified Deletion of " + name);
        title("All Done!");
        return ( SimpleReporterAdapter.PASS );
    }

    private static final String[] classnames = {
            "testmbeans.OneClassDynamicMBean", 
            "testmbeans.MicrowaveOvenImpl", 
            "testmbeans.SimpleStandard", 
            "testmbeans.PrimitiveStandard", 
    };
    
    private int numIter = 500;
    private String namePrefix;
    private String alphabet = "abcdefghijklmnopqrstuvwxyz";
    private Random rnd;
    private boolean interactive = false;
}
