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

package org.glassfish.admin.amxtest.helper;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.ClusterRefConfig;
import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.config.RefConfig;
import com.sun.appserv.management.config.ResourceRefConfig;
import com.sun.appserv.management.config.ServerRefConfig;
import com.sun.appserv.management.helper.RefHelper;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.HashMap;
import java.util.HashSet;
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
public final class RefHelperTest
        extends AMXTestBase {
    public RefHelperTest() {
    }

    public void
    testFindResourceRefs() {
        final Set<ResourceRefConfig> refs1 =
                RefHelper.findAllRefConfigsByJ2EEType(getQueryMgr(), XTypes.RESOURCE_REF_CONFIG);

        final Set<ResourceRefConfig> refs2 =
                RefHelper.findAllResourceRefConfigs(getQueryMgr());

        assertEquals(refs1, refs2);
    }

    public void
    testFindDeployedItemRefs() {
        final Set<RefConfig> refs1 =
                RefHelper.findAllRefConfigsByJ2EEType(getQueryMgr(), XTypes.DEPLOYED_ITEM_REF_CONFIG);

        final Set<DeployedItemRefConfig> refs2 =
                RefHelper.findAllDeployedItemRefConfigs(getQueryMgr());

        assertEquals(refs1, refs2);
    }


    public void
    testFindServerRefs() {
        final Set<RefConfig> refs1 =
                RefHelper.findAllRefConfigsByJ2EEType(getQueryMgr(), XTypes.SERVER_REF_CONFIG);

        final Set<ServerRefConfig> refs2 =
                RefHelper.findAllServerRefConfigs(getQueryMgr());

        assertEquals(refs1, refs2);
    }

    public void
    testFindClusterRefs() {
        final Set<RefConfig> refs1 =
                RefHelper.findAllRefConfigsByJ2EEType(getQueryMgr(), XTypes.CLUSTER_REF_CONFIG);

        final Set<ClusterRefConfig> refs2 =
                RefHelper.findAllClusterRefConfigs(getQueryMgr());

        assertEquals(refs1, refs2);
    }

    public void
    testFindAllRefConfigs() {
        final Set<RefConfig> all =
                RefHelper.findAllRefConfigs(getQueryMgr());

        final Set<String> referentJ2EETypes = RefHelper.getReferentJ2EETypes();
        assert (referentJ2EETypes.size() >= 4);

        final Set<RefConfig> allSeparately = new HashSet<RefConfig>();

        for (final String j2eeType : referentJ2EETypes) {
            final Set<RefConfig> refs =
                    RefHelper.findAllRefConfigsByJ2EEType(getQueryMgr(), j2eeType);

            for (final RefConfig ref : refs) {
                allSeparately.add(ref);
            }
        }

        assertEquals(all, allSeparately);
    }


    public void
    testFindAllRefConfigsByName() {
        final Set<RefConfig> refs =
                RefHelper.findAllRefConfigs(getQueryMgr());

        final Set<String> names = Util.getNames(refs);
        final Map<String, Set<RefConfig>> byName =
                new HashMap<String, Set<RefConfig>>();

        // initialize map, keyed by name
        for (final String name : names) {
            byName.put(name, new HashSet<RefConfig>());
        }

        // populate each Set with RefConfig of the same name
        for (final RefConfig ref : refs) {
            final Set<RefConfig> s = byName.get(ref.getName());

            s.add(ref);
        }

        // verify that the names all refer to the same type
        for (final String name : byName.keySet()) {
            final Set<RefConfig> s = byName.get(name);

            final String lastJ2EEType = s.iterator().next().getJ2EEType();
            for (final RefConfig ref : s) {
                assertEquals(ref.getJ2EEType(), lastJ2EEType);
            }
        }

        // now verify that we can get the same set another way
        for (final String name : byName.keySet()) {
            final Set<RefConfig> s = byName.get(name);

            final String j2eeType = s.iterator().next().getJ2EEType();
            final Set<RefConfig> s2 =
                    RefHelper.findAllRefConfigsWithName(getQueryMgr(), j2eeType, name);

            assertEquals(s, s2);
        }
    }

}

















