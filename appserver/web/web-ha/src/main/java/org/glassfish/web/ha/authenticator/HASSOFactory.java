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

/*
 * HASSOFactory.java
 *
 * Created on August 24, 2004, 5:27 PM
 */

package org.glassfish.web.ha.authenticator;


import com.sun.enterprise.container.common.spi.util.JavaEEIOUtils;
import com.sun.enterprise.security.web.GlassFishSingleSignOn;
import com.sun.enterprise.web.SSOFactory;
import com.sun.enterprise.web.session.PersistenceType;

import jakarta.inject.Inject;

import org.glassfish.gms.bootstrap.GMSAdapterService;
import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreConfiguration;
import org.glassfish.ha.store.api.BackingStoreException;
import org.glassfish.ha.store.api.BackingStoreFactory;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author lwhite
 * @author Shing Wai Chan
 */
@Service
@PerLookup
public class HASSOFactory implements SSOFactory {
    private static final String STORE_NAME = "SSOStore";

    private static BackingStore<String, HASingleSignOnEntryMetadata> ssoEntryMetadataBackingStore = null;

    @Inject
    private ServiceLocator services;

    @Inject
    private JavaEEIOUtils ioUtils;

    /**
     * Create a SingleSignOn valve
     * HASingleSignOnValve is created is global availability-enabled
     * is true and sso-failover-enabled
     */
    @Override
    public GlassFishSingleSignOn createSingleSignOnValve(String virtualServerName) {
        String persistenceType = PersistenceType.REPLICATED.getType();
        return new HASingleSignOn(ioUtils,
                getSsoEntryMetadataBackingStore(persistenceType, STORE_NAME, services));
    }

    protected static synchronized BackingStore<String, HASingleSignOnEntryMetadata>
            getSsoEntryMetadataBackingStore(
            String persistenceType, String storeName, ServiceLocator services) {

        if (ssoEntryMetadataBackingStore == null) {
            BackingStoreFactory factory = services.getService(BackingStoreFactory.class, persistenceType);
            BackingStoreConfiguration<String, HASingleSignOnEntryMetadata> conf =
                    new BackingStoreConfiguration<String, HASingleSignOnEntryMetadata>();

            String clusterName = "";
            String instanceName = "";
            GMSAdapterService gmsAdapterService = services.getService(GMSAdapterService.class);
            if(gmsAdapterService.isGmsEnabled()) {
                clusterName = gmsAdapterService.getGMSAdapter().getClusterName();
                instanceName = gmsAdapterService.getGMSAdapter().getModule().getInstanceName();
            }

            conf.setStoreName(storeName)
                    .setClusterName(clusterName)
                    .setInstanceName(instanceName)
                    .setStoreType(persistenceType)
                    .setKeyClazz(String.class).setValueClazz(HASingleSignOnEntryMetadata.class);

            try {
                ssoEntryMetadataBackingStore = factory.createBackingStore(conf);
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
        }

        return ssoEntryMetadataBackingStore;
    }
}
