/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jdbcra.spi;

import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.NotSupportedException;
import javax.transaction.xa.XAResource;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapterInternalException;

/**
 * <code>ResourceAdapter</code> implementation for Generic JDBC Connector.
 *
 * @version        1.0, 02/08/05
 * @author        Evani Sai Surya Kiran
 */
public class ResourceAdapter implements jakarta.resource.spi.ResourceAdapter {
public static final int VERSION = 2;
    public String raProp = null;

    /**
     * Empty method implementation for endpointActivation
     * which just throws <code>NotSupportedException</code>
     *
     * @param        mef        <code>MessageEndpointFactory</code>
     * @param        as        <code>ActivationSpec</code>
     * @throws        <code>NotSupportedException</code>
     */
    public void endpointActivation(MessageEndpointFactory mef, ActivationSpec as) throws NotSupportedException {
        throw new NotSupportedException("This method is not supported for this JDBC connector");
    }

    /**
     * Empty method implementation for endpointDeactivation
     *
     * @param        mef        <code>MessageEndpointFactory</code>
     * @param        as        <code>ActivationSpec</code>
     */
    public void endpointDeactivation(MessageEndpointFactory mef, ActivationSpec as) {

    }

    /**
     * Empty method implementation for getXAResources
     * which just throws <code>NotSupportedException</code>
     *
     * @param        specs        <code>ActivationSpec</code> array
     * @throws        <code>NotSupportedException</code>
     */
    public XAResource[] getXAResources(ActivationSpec[] specs) throws NotSupportedException {
        throw new NotSupportedException("This method is not supported for this JDBC connector");
    }

    /**
     * Empty implementation of start method
     *
     * @param        ctx        <code>BootstrapContext</code>
     */
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
/*
NO NEED TO CHECK THIS AS THE TEST's PURPOSE IS TO CHECK THE VERSION ALONE

        System.out.println("Resource Adapter is starting with configuration :" + raProp);
        if (raProp == null || !raProp.equals("VALID")) {
            throw new ResourceAdapterInternalException("Resource adapter cannot start. It is configured as : " + raProp);
        }
*/
    }

    /**
     * Empty implementation of stop method
     */
    public void stop() {

    }

    public void setRAProperty(String s) {
        raProp = s;
    }

    public String getRAProperty() {
        return raProp;
    }

}
