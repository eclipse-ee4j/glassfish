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
import org.jvnet.hk2.config.types.*;
import org.jvnet.hk2.component.*;
import org.junit.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.*;

import com.sun.enterprise.config.serverbeans.*;

import java.beans.*;

/**
 * This test registers an vetoable change listener on a config bean and vetoes
 * any change on that object.
 *
 * @author Jerome Dochez
 */
public class VetoableChangeListenerTest extends ConfigApiTest implements VetoableChangeListener {

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

        HttpService httpService = habitat.getService(HttpService.class);
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

        ((ConfigBean) ConfigSupport.getImpl(target)).getOptionalFeature(ConstrainedBeanListener.class).addVetoableChangeListener(this);

        try {
            ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {

                public Object run(VirtualServer param) throws PropertyVetoException, TransactionFailure {
                    param.setId("foo");
                    param.setAccessLog("Foo");
                    return null;
                }
            }, target);
        } catch(TransactionFailure e) {
            //e.printStackTrace();
            System.out.println("Got exception: " + e.getClass().getName() + " as expected, with message: " + e.getMessage());
            result=true;
        }

        assertTrue(result);

        result=false;
        // let's do it again.
        try {
            ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {

                public Object run(VirtualServer param) throws PropertyVetoException, TransactionFailure {
                    param.setId("foo");
                    param.setAccessLog("Foo");
                    return null;
                }
            }, target);
        } catch(TransactionFailure e) {
            //e.printStackTrace();
            System.out.println("Got exception: " + e.getClass().getName() + " as expected, with message: " + e.getMessage());
            result=true;
        }

        ((ConfigBean) ConfigSupport.getImpl(target)).getOptionalFeature(ConstrainedBeanListener.class).removeVetoableChangeListener(this);
        assertTrue(result);


        // this time it should work !
        try {
            ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {

                public Object run(VirtualServer param) throws PropertyVetoException, TransactionFailure {
                    // first one is fine...
                    param.setAccessLog("Foo");
                    return null;
                }
            }, target);
        } catch(TransactionFailure e) {
            //e.printStackTrace();
            System.out.println("Got exception: " + e.getClass().getName() + " as expected, with message: " + e.getMessage());
            result=false;
        }

        assertTrue(result);
    }


    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        throw new PropertyVetoException("I don't think so !", evt);
    }
}
