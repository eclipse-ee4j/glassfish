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

package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.ResourceConfig;
import com.sun.appserv.management.config.ResourceRefConfig;
import com.sun.appserv.management.config.ResourceRefConfigCR;
import com.sun.appserv.management.helper.RefHelper;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.Map;
import java.util.Set;


/**
 This test should normally be run before the generic tests
 so that it can set up default items for many of the config elements
 so that the generic tests will actually test them. Otherwise,
 when the generic tests are run, they won't see any instances
 of many of the AMXConfig MBeans.
 <p/>
 If there are errors doing this, disable this test in amxtest.classes,
 fix the error in the specific place it's occurring, then re-enabled
 this test.
 */
public final class DanglingRefsTest
        extends AMXTestBase {
    public DanglingRefsTest() {
    }

    public void
    testAllDangling()
            throws ClassNotFoundException {
        _testDanglingResourceRefConfigs();
        //_testDanglingDeployedItemRefConfigs();
    }

    private void
    _testDanglingResourceRefConfigs()
            throws ClassNotFoundException {
        final DomainConfig domainConfig = getDomainConfig();
        final Set<ResourceConfig> resourcesSet =
                getQueryMgr().queryInterfaceSet(ResourceConfig.class.getName(), null);

        final Set<ResourceRefConfig>
                refs = RefHelper.findAllResourceRefConfigs(getQueryMgr());

        final Map<String, ResourceConfig> resourcesMap = Util.createNameMap(resourcesSet);

        for (final ResourceRefConfig ref : refs) {
            final String name = ref.getName();

            final ResourceConfig resourceConfig = resourcesMap.get(name);
            if (resourceConfig == null) {
                String msg =
                        "Resource reference '" +
                                Util.getObjectName(ref) + "' refers to a non-existent resource";

                boolean removedOK = false;
                try {
                    final ResourceRefConfigCR container =
                            (ResourceRefConfigCR) ref.getContainer();

                    container.removeResourceRefConfig(name);
                    removedOK = true;
                }
                catch (Exception e) {
                    msg = msg + ", and trying to remove it throws an Exception " +
                            "(remove it manually from domain.xml)" +
                            ", see bug #6298512";
                }

                if (!removedOK) {
                    warning(msg);
                }
            } else {
                //printVerbose( "ResourceRefConfig '" + name + "' is OK" );
            }
        }
    }

    /*
        private void
     _testDanglingDeployedItemRefConfigs()
         throws ClassNotFoundException
     {
         final DomainConfig  domainConfig    = getDomainConfig();

         final Set<DeployedItemRefConfig> s    = (Set<DeployedItemRefConfig>)
             getQueryMgr().queryInterfaceSet( DeployedItemRefConfig.class.getName(), null );

         final Set<DeployedItemRefConfig>
             refs = RefHelper.findAllDeployedItemRefConfigRefs( getQueryMgr() );

         final Map<String,DeployedItem> deployedItemsMap    = Util.createNameMap( s );

         for( final DeployedItemRefConfig ref : refs )
         {
             final String    name    = ref.getName();

             final DeployedItem    deployedItem  = deployedItemsMap.get( name );
             assert( deployedItem != null ) :
                 "Deployed item reference '" + Util.getObjectName( ref ) +
                     "' refers to a non-existent item";
         }
     }
     */
}

















