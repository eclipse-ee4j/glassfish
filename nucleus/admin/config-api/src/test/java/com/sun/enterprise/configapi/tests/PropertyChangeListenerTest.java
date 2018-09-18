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

package com.sun.enterprise.configapi.tests;

import org.jvnet.hk2.config.*;
import org.jvnet.hk2.config.types.Property;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.Utils;
import com.sun.enterprise.config.serverbeans.*;


import java.beans.PropertyVetoException;
import java.beans.PropertyChangeEvent;

/**
 * This test listen to a property change event only injecting the parent containing the property.
 *
 * @author Jerome Dochez
 */
public class PropertyChangeListenerTest  extends ConfigApiTest implements ConfigListener {

    ServiceLocator habitat;
    boolean result = false;

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setup() {
        habitat = Utils.instance.getHabitat(this);
    }

    @Test
    public void propertyChangeEventReceptionTest() throws TransactionFailure {

        HttpService  httpService = habitat.getService(HttpService.class);
        assertNotNull(httpService);

       // let's find a acceptable target.
        VirtualServer target =null;
        for (VirtualServer vs : httpService.getVirtualServer()) {
            if (!vs.getProperty().isEmpty()) {
                target = vs;
                break;
            }
        }

        assertNotNull(target);

        ((ObservableBean) ConfigSupport.getImpl(target)).addListener(this);
        final Property prop  = target.getProperty().get(0);

        ConfigSupport.apply(new SingleConfigCode<Property>() {

            public Object run(Property param) throws PropertyVetoException, TransactionFailure {
                // first one is fine...
                param.setValue(prop.getValue().toUpperCase());
                return null;
            }
        }, prop);

        getHabitat().<Transactions>getService(Transactions.class).waitForDrain();
        assertTrue(result);
        ((ObservableBean) ConfigSupport.getImpl(target)).removeListener(this);
    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        result = true;
        return null;
    }
}
