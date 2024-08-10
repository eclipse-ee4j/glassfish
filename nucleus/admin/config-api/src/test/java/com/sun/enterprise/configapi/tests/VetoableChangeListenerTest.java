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
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConstrainedBeanListener;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This test registers an vetoable change listener on a config bean and vetoes
 * any change on that object.
 *
 * @author Jerome Dochez
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class VetoableChangeListenerTest implements VetoableChangeListener {

    @Inject
    private ServiceLocator locator;

    @Test
    public void propertyChangeEventReceptionTest() throws TransactionFailure {
        final VirtualServer target = findTargetWithProperties();
        assertNotNull(target);

        SingleConfigCode<VirtualServer> configCode = vs -> {
            vs.setId("foo");
            vs.setAccessLog("Foo");
            return null;
        };
        ConfigBean configBean1 = (ConfigBean) ConfigSupport.getImpl(target);
        configBean1.getOptionalFeature(ConstrainedBeanListener.class).addVetoableChangeListener(this);
        assertThrows(TransactionFailure.class, () -> ConfigSupport.apply(configCode, target));
        // let's do it again.
        assertThrows(TransactionFailure.class, () -> ConfigSupport.apply(configCode, target));
        ConfigBean configBean2 = (ConfigBean) ConfigSupport.getImpl(target);
        configBean2.getOptionalFeature(ConstrainedBeanListener.class).removeVetoableChangeListener(this);

        // this time it should work!
        SingleConfigCode<VirtualServer> configCode2 = vs -> {
            // first one is fine...
            vs.setAccessLog("Foo");
            return null;
        };
        ConfigSupport.apply(configCode2, target);
    }


    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        throw new PropertyVetoException("I don't think so !", evt);
    }


    private VirtualServer findTargetWithProperties() {
        HttpService httpService = locator.getService(HttpService.class);
        assertNotNull(httpService);
        for (VirtualServer vs : httpService.getVirtualServer()) {
            if (!vs.getProperty().isEmpty()) {
                return vs;
            }
        }
        return null;
    }
}
