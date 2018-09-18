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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.v3.common.HTMLActionReporter;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.api.admin.*;
import org.glassfish.tests.utils.ConfigApiTest;
import org.glassfish.tests.utils.Utils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import java.beans.PropertyChangeEvent;

/**
 * test the set command
 * @author Jerome Dochez
 */
// Ignored temporarily because it fails to inject CommandRunnerImpl as ModulesRegistry is not available
@Ignore 
public class ConfigAttributeSetTest  extends ConfigApiTest implements ConfigListener {

    ServiceLocator habitat = Utils.instance.getHabitat(this);
    PropertyChangeEvent event = null;

    public DomDocument getDocument(ServiceLocator habitat) {
        return new TestDocument(habitat);
    }

    /**
     * Returns the DomainTest file name without the .xml extension to load the test configuration
     * from.
     *
     * @return the configuration file name
     */
    public String getFileName() {
        return "DomainTest";
    }     

    @Test
     public void simpleAttributeSetTest() {

        CommandRunnerImpl runner = habitat.getService(CommandRunnerImpl.class);
        assertNotNull(runner);

        // let's find our target
        NetworkListener listener = null;
        NetworkListeners service = habitat.getService(NetworkListeners.class);
        for (NetworkListener l : service.getNetworkListener()) {
            if ("http-listener-1".equals(l.getName())) {
                listener = l;
                break;
            }
        }
        assertNotNull(listener);        

        // Let's register a listener
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(listener);
        bean.addListener(this);

        // parameters to the command
        ParameterMap parameters = new ParameterMap();
        parameters.set("value", "8090");
        parameters.set("DEFAULT", "configs.config.server-config.http-service.http-listener.http-listener-1.port");

        // execute the set command.
        runner.getCommandInvocation("set", new HTMLActionReporter(), adminSubject()).parameters(parameters).execute();
                                                                                                                                                                                                                           
        // check the result.
        String port = listener.getPort();
        assertEquals(port, "8090");

        // ensure events are delivered.
        habitat.<Transactions>getService(Transactions.class).waitForDrain();
        
        // finally
        bean.removeListener(this);

        // check we recevied the event
        assertNotNull(event);
        assertEquals("8080", event.getOldValue());
        assertEquals("8090", event.getNewValue());
        assertEquals("port", event.getPropertyName());
        
    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
        assertEquals("Array size", propertyChangeEvents.length, 1 );
        event = propertyChangeEvents[0];
        return null;
    }
}
