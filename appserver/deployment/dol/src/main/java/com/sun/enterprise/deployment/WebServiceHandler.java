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

package com.sun.enterprise.deployment;

import org.glassfish.deployment.common.Descriptor;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This class describes a web service message handler.
 *
 * @author Jerome Dochez
 * @author Kenneth Saks
 */
public class WebServiceHandler extends Descriptor {

    private String handlerName = null;

    private String handlerClass = null;

    private Collection initParams = new HashSet();

    private Collection soapHeaders = new HashSet();

    private Collection soapRoles = new HashSet();

    private Collection portNames = new HashSet();

    /**
    * copy constructor.
    */
    public WebServiceHandler(WebServiceHandler other) {
        super(other);
        handlerName = other.handlerName; // String
        handlerClass = other.handlerClass; // String
        portNames.addAll(other.portNames); // Set of String
        soapRoles.addAll(other.soapRoles); // Set of String
        soapHeaders.addAll(other.soapHeaders); // Set of QName (immutable)
        for (Iterator i = other.initParams.iterator(); i.hasNext();) {
            initParams.add(new NameValuePairDescriptor((NameValuePairDescriptor) i.next()));
        }
    }

    public WebServiceHandler() {
    }

    /**
     * Sets the class name for this handler
     * @param class name
     */
    public void setHandlerClass(String className) {
        handlerClass = className;
    }

    /**
     * @return the class name for this handler
     */
    public String getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerName(String name) {
        handlerName = name;

    }

    public String getHandlerName() {
        return handlerName;
    }

    /**
     * add an init param to this handler
     * @param the init param
     */
    public void addInitParam(NameValuePairDescriptor newInitParam) {
        initParams.add(newInitParam);

    }

    /**
     * remove an init param from this handler
     * @param the init param
     */
    public void removeInitParam(NameValuePairDescriptor initParamToRemove) {
        initParams.remove(initParamToRemove);

    }

    /**
     * @return the list of init params for this handler
     */
    public Collection getInitParams() {
        return initParams;
    }

    public void addSoapHeader(QName soapHeader) {
        soapHeaders.add(soapHeader);

    }

    public void removeSoapHeader(QName soapHeader) {
        soapHeaders.remove(soapHeader);

    }

    // Collection of soap header QNames
    public Collection getSoapHeaders() {
        return soapHeaders;
    }

    public void addSoapRole(String soapRole) {
        soapRoles.add(soapRole);

    }

    public void removeSoapRole(String soapRole) {
        soapRoles.remove(soapRole);

    }

    public Collection getSoapRoles() {
        return soapRoles;
    }

    public void addPortName(String portName) {
        portNames.add(portName);

    }

    public void removePortName(String portName) {
        portNames.remove(portName);

    }

    // Collection of port name Strings
    public Collection getPortNames() {
        return portNames;
    }

    /**
     * Appends a string describing the values I hold
     */
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("\nHandler name = ").append(handlerName).append(
            "Handler class name = ").append(handlerClass);
        for (Iterator i=getInitParams().iterator(); i.hasNext(); ) {
            toStringBuffer.append("\n").append(i.next().toString());
        }
    }

}
