/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.deployment.util.DOLUtils;
import java.util.Properties;

import static org.glassfish.deployment.common.JavaEEResourceType.*;

/**
 * Represents the data from a @MailSessionDefinition annotation.
 */
public class MailSessionDescriptor extends ResourceDescriptor {

    private String name;
    private String storeProtocol;
    private String transportProtocol;
    private String user;
    private String password;
    private String host;
    private String from;
    private Properties properties = new Properties();

    private static final String JAVA_URL = "java:";
    private static final String JAVA_COMP_URL = "java:comp/";

    private boolean deployed = false;

    public MailSessionDescriptor(){
        super.setResourceType(MSD);
    }


    public String getName() {
        return name;
    }

    public static String getName(String thisName) {
        if (!thisName.contains(JAVA_URL)) {
            thisName = JAVA_COMP_URL + thisName;
        }
        return thisName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return (String) properties.get(key);
    }

    public Properties getProperties() {
        return properties;
    }

    public String getStoreProtocol() {
        return storeProtocol;
    }

    public void setStoreProtocol(String storeProtocol) {
        this.storeProtocol = storeProtocol;
    }

    public String getTransportProtocol() {
        return transportProtocol;
    }

    public void setTransportProtocol(String transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void addMailSessionPropertyDescriptor(ResourcePropertyDescriptor propertyDescriptor) {
        properties.put(propertyDescriptor.getName(), propertyDescriptor.getValue());
    }

    public void setDeployed(boolean deployed) {
        this.deployed = deployed;
    }

    public boolean isDeployed() {
        return deployed;
    }

    public boolean equals(Object object) {
        if (object instanceof MailSessionDescriptor) {
            MailSessionDescriptor reference = (MailSessionDescriptor) object;
            return getJavaName(this.getName()).equals(getJavaName(reference.getName()));
        }
        return false;
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + getName().hashCode();
        return result;
    }

    public static String getJavaName(String thisName) {
        if (!thisName.startsWith(JAVA_URL)) {
            thisName = JAVA_COMP_URL + thisName;
        }
        return thisName;
    }

    public boolean isConflict(MailSessionDescriptor other) {
            return (getName().equals(other.getName())) &&
                !(
                    DOLUtils.equals(getUser(), other.getUser()) &&
                    DOLUtils.equals(getPassword(), other.getPassword()) &&
                    DOLUtils.equals(getFrom(), other.getFrom()) &&
                    DOLUtils.equals(getHost(), other.getHost()) &&
                    DOLUtils.equals(getPassword(), other.getPassword()) &&
                    DOLUtils.equals(getStoreProtocol(),
                                                other.getStoreProtocol()) &&
                    DOLUtils.equals(getTransportProtocol(),
                                                other.getTransportProtocol()) &&
                    DOLUtils.equals(getDescription(), other.getDescription()) &&
                    properties.equals(other.properties)
                );
        }
}
