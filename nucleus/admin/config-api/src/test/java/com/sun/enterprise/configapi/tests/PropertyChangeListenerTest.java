/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.config.types.Property;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test listen to a property change event only injecting the parent containing the property.
 *
 * @author Jerome Dochez
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class PropertyChangeListenerTest implements ConfigListener {

    @Inject
    private ServiceLocator locator;
    private boolean result;

    @Test
    public void propertyChangeEventReceptionTest() throws TransactionFailure {

        HttpService  httpService = locator.getService(HttpService.class);
        assertNotNull(httpService);

       // let's find a acceptable target.
       VirtualServer target = null;
       for (VirtualServer vs : httpService.getVirtualServer()) {
           if (!vs.getProperty().isEmpty()) {
               target = vs;
               break;
           }
       }

       assertNotNull(target);

       ((ObservableBean) ConfigSupport.getImpl(target)).addListener(this);
       final Property prop = target.getProperty().get(0);

       SingleConfigCode<Property> configCode = property -> {
           // first one is fine...
           property.setValue(prop.getValue().toUpperCase());
           return null;
       };
       ConfigSupport.apply(configCode, prop);

       locator.<Transactions> getService(Transactions.class).waitForDrain();
       assertTrue(result);
       ((ObservableBean) ConfigSupport.getImpl(target)).removeListener(this);
   }


   @Override
   public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
       result = true;
       return null;
   }
}
