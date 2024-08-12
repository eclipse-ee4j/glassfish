/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.inbound;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.connectors.ActiveResourceAdapter;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.runtime.BeanPoolDescriptor;

import jakarta.resource.spi.ActivationSpec;


public interface ActiveInboundResourceAdapter extends ActiveResourceAdapter {

    /**
     * Adds endpoint factory information.
     *
     * @param id   Unique identifier of the endpoint factory.
     * @param info <code>MessageEndpointFactoryInfo</code> object.
     */
    public void addEndpointFactoryInfo(String id, MessageEndpointFactoryInfo info) ;

    /**
     * Removes information about an endpoint factory
     *
     * @param id Unique identifier of the endpoint factory to be
     *           removed.
     */
    public void removeEndpointFactoryInfo(String id);

    /**
     * Returns information about endpoint factory.
     *
     * @param id Id of the endpoint factory.
     * @return <code>MessageEndpointFactoryIndo</code> object.
     */
    MessageEndpointFactoryInfo getEndpointFactoryInfo(String id);


    /**
     * update MDB container runtime properties
     * @param descriptor_ MessageBean Descriptor
     * @param poolDescriptor Bean pool descriptor
     * @throws ConnectorRuntimeException
     */
    public void updateMDBRuntimeInfo(EjbMessageBeanDescriptor descriptor_,
           BeanPoolDescriptor poolDescriptor) throws ConnectorRuntimeException ;


    /**
     * validate the activation-spec
     * @param spec activation-spec
     */
    public void validateActivationSpec(ActivationSpec spec);
}
