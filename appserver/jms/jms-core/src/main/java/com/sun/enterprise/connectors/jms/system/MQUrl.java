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

package com.sun.enterprise.connectors.jms.system;

/**
 * Represents one of the MQ address list elements.
 *
 * @author Binod P.G
 */
public class MQUrl {
    private String host = null;
    private String port = null;
    private String scheme = "mq";
    private String service = "";
    private String id = null;

    /**
     * Constructs the MQUrl with the id. Id is actually
     * the name of JmsHost element in the domain.xml.
     *
     * @param id Logical name of the MQUrl
     */
    public MQUrl(String id) {
        this.id = id;
    }

    /**
     * Sets the host name of the Url.
     *
     * @param host Host Name of the Url.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Sets the port number of the Url.
     *
     * @param port Port number of the Url.
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Sets the Scheme of MQ connection for this Url.
     * Eg> mq, mtcp, mqssl ...
     *
     * @param scheme scheme of the connection.
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * Sets the type of service offered by MQ broker.
     * Eg> jms, jmsssl etc.
     *
     * @param service Name of service.
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * String representation of the Url.
     * i.e> scheme://host:port/service
     * Eg> mq://javasoft12:7676/jmsssl
     *
     * @return String representation of Url.
     */
    public String toString() {
        if ( host.equals("")) {
            return "";
        }

        if ( port.equals("") && service.equals("")) {
           return scheme + "://" + host;
        }

        if (service.equals("")) {
           return scheme + "://" + host + ":" + port + "/";
        }

        return scheme + "://" + host + ":" + port + "/" + service;
    }

    /**
     * Two MQUrls are identified by their id (name).
     *
     * @param obj another MQUrl object.
     * @return a boolean indicating whether MQUrls are equal.
     */
    public boolean equals(Object obj) {
        if (obj instanceof MQUrl) {
            return this.id.equals(((MQUrl)obj).id);
        } else {
            return false;
        }
    }

    /**
     * Hashcode of MQurl is the same as the Hashcode of its name.
     *
     * @return  hashcode of MQUrl
     */
    public int hashCode() {
        return id.hashCode();
    }
}
