/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.gms;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.junit.DomainXml;
import org.glassfish.tests.utils.junit.HK2JUnit5Extension;
import org.jvnet.hk2.config.types.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(HK2JUnit5Extension.class)
public class GmsUpgradeTestBase {

    @Inject
    private ServiceLocator locator;

    @BeforeEach
    public void runUpgrades() {
        Domain domain = locator.getService(Domain.class);
        assertNotNull(domain);

        assertNotNull(locator.getService(GMSConfigUpgrade.class), "GMSConfigUpgrade is missing");
    }

    @Test
    public void gmsListenerPortCorrectlyUpdated() {
        Clusters clusters = locator.getService(Clusters.class);
        assertNotNull(clusters);

        Cluster cluster = clusters.getCluster().stream()
            .filter(c -> "cluster1".equals(c.getName()))
            .findFirst()
            .orElseThrow();

        long gmsListenerPortCount = cluster.getProperty().stream()
            .map(Property::getName)
            .filter("GMS_LISTENER_PORT"::equals)
            .count();
        assertEquals(1L, gmsListenerPortCount);

        Property gmsListenerPort = cluster.getProperty("GMS_LISTENER_PORT");
        assertNotNull(gmsListenerPort);
        assertEquals("${GMS_LISTENER_PORT-cluster1}", gmsListenerPort.getValue());
    }
}
