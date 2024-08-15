/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.upgrade;

import com.sun.ejb.containers.EjbContainerUtil;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.ejb.config.EjbContainer;
import org.glassfish.ejb.config.EjbTimerService;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

/**
 * Upgrade EJB Timer Service table from v2 to v3
 * @author Marina Vatkina
 */

@Service(name="ejbTimerServiceUpgrade")
public class EJBTimerServiceUpgrade implements PostConstruct, ConfigurationUpgrade {

    @Inject
    Configs configs;

    public void postConstruct() {
        for (Config config : configs.getConfig()) {
            EjbContainer container = config.getExtensionByType(EjbContainer.class);
            if (container != null && container.getEjbTimerService() != null) {
                doUpgrade(container.getEjbTimerService());
            }
        }
    }

    private void doUpgrade(EjbTimerService ts) {
        String value = ts.getMinimumDeliveryIntervalInMillis();
        if (value == null || "7000".equals(value)) {
            value = "" + EjbContainerUtil.MINIMUM_TIMER_DELIVERY_INTERVAL;
        }

        List<Property> properties = ts.getProperty();
        if (properties != null) {
            for (Property p : properties) {
                if (p.getName().equals(EjbContainerUtil.TIMER_SERVICE_UPGRADED)) {
                    return; // Already set
                }
            }
        }
        try {
            final String minDelivery = value;
            ConfigSupport.apply(new SingleConfigCode<EjbTimerService>() {

                public Object run(EjbTimerService ts) throws PropertyVetoException, TransactionFailure {
                    Property prop = ts.createChild(Property.class);
                    ts.getProperty().add(prop);
                    prop.setName(EjbContainerUtil.TIMER_SERVICE_UPGRADED);
                    prop.setValue("false");
                    ts.setMinimumDeliveryIntervalInMillis(minDelivery);
                    return null;
                }
            }, ts);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
