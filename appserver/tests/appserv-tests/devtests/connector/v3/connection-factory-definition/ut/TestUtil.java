/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.cfd;

import com.sun.enterprise.deployment.ConnectionFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.ResourceDescriptor;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.internal.api.Globals;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static junit.framework.Assert.*;

public class TestUtil {

    public static void compareCFDD(Map<String,ConnectionFactoryDefinitionDescriptor> expectedCFDDs,
            Set<ResourceDescriptor> actualCFDDs) throws Exception{

        for(Descriptor descriptor : actualCFDDs){
            ConnectionFactoryDefinitionDescriptor actualDesc = (ConnectionFactoryDefinitionDescriptor)descriptor;
            assertNotNull("the name of connector resource cannot be null.", actualDesc.getName());

            ConnectionFactoryDefinitionDescriptor expectedDesc = expectedCFDDs.get(actualDesc.getName());
            assertNotNull("The CFD of the name ["+actualDesc.getName()+"] is not expected.", expectedDesc);

            assertEquals("Fail to verify class-name of the CFDD:"+actualDesc.getName(),
                    expectedDesc.getInterfaceName(), actualDesc.getInterfaceName());

            assertEquals("Fail to verify resource-adapter of the CFDD:"+actualDesc.getName(),
                    expectedDesc.getResourceAdapter(), actualDesc.getResourceAdapter());

            assertEquals("Fail to verify transaction-support of the CFDD:"+actualDesc.getName(),
                    expectedDesc.getTransactionSupport(), actualDesc.getTransactionSupport());

            assertEquals("Fail to verify max-pool-size of the CFDD:"+actualDesc.getName(),
                    expectedDesc.getMaxPoolSize(), actualDesc.getMaxPoolSize());

            assertEquals("Fail to verify min-pool-size of the CFDD:"+actualDesc.getName(),
                    expectedDesc.getMinPoolSize(), actualDesc.getMinPoolSize());

            assertEquals("Fail to verify description of the CFDD:"+actualDesc.getName(),
                    expectedDesc.getDescription(), actualDesc.getDescription());

            Properties expectedProps = expectedDesc.getProperties();
            Properties actualProps = actualDesc.getProperties();

            for(Object name : actualProps.keySet()){
                assertEquals("Fail to verify property ("+name+") of the CFDD:"+actualDesc.getName(),
                        expectedProps.get(name), actualProps.get(name));
            }

            assertEquals("Fail to verify size of properties of the CFDD:"+actualDesc.getName(),
                    expectedProps.size(), actualProps.size());

            expectedCFDDs.remove(actualDesc.getName());
        }
        if(expectedCFDDs.size()>0){
            StringBuilder sb = new StringBuilder();
            for(String name : expectedCFDDs.keySet()){
                sb.append("  "+name+"\n");
            }
            fail("Still has expected "+ expectedCFDDs.size()+" CFDs: \n"+sb.toString());
        }
    }

    public static void setupHK2() throws Exception{
        Globals.getStaticHabitat();
        assertNotNull("The global habitat is not initialized.", Globals.getDefaultHabitat());

    }
    public static Object getByType(Class clz) throws Exception{
        setupHK2();
        return Globals.getDefaultHabitat().getService(clz);
    }
}
