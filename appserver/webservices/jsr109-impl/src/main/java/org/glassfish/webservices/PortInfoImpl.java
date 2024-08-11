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

package org.glassfish.webservices;

import com.sun.xml.ws.api.BindingID;

import jakarta.xml.ws.handler.PortInfo;

import javax.xml.namespace.QName;

/**
 * Implementation of the PortInfo interface. This is just a simple
 * class used to hold the info necessary to uniquely identify a port,
 * including the port name, service name, and binding ID. This class
 * is only used on the client side.
 */
public class PortInfoImpl implements PortInfo {

    private BindingID bindingId;
    private QName portName;
    private QName serviceName;

    public PortInfoImpl(BindingID bindingId, QName portName, QName serviceName) {
        this.bindingId = bindingId;
        this.portName = portName;
        this.serviceName = serviceName;
    }

    public String getBindingID() {
        return bindingId.toString();
    }

    public QName getPortName() {
        return portName;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public boolean equals(Object obj) {
        if (obj instanceof PortInfo) {
            PortInfo info = (PortInfo) obj;
            if (bindingId.toString().equals(info.getBindingID()) &&
                portName.equals(info.getPortName()) &&
                serviceName.equals(info.getServiceName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Needed by JAXWS so PortInfoImpl can be used as a key in a map..
     */
    public int hashCode() {
        return bindingId.toString().hashCode();
    }
}
