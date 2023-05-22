/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.ThreadPool;
import org.glassfish.grizzly.config.dom.Transport;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

/**
 * This is a dummy implementation of the NetworkListener interface. This is used to create a fake network-listener
 * elements. This is used only to support lazyInit attribute of iiop and jms services through the light weight listener.
 * Ultimately, these services will move to their own network-listener element in domain.xml (at which point we have to
 * get rid of this fake object). But till the time IIOP and JMS service elements in domain.xml can move to use
 * network-listener element, we will create this "fake network-listener" which in turn will help start light weight
 * listener for these services.
 */
public class DummyNetworkListener implements NetworkListener {
    private String address = "0.0.0.0";
    private String enabled = "true";
    private String type = "standard";
    private String name;
    private String port;
    private String protocol;
    private String pool;
    private String transport;
    private String jkEnabled;
    private String jkConfigurationFile;
    private final List<Property> properties = new ArrayList<>();

    public DummyNetworkListener() {
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void setAddress(String value) {
        address = value;
    }

    @Override
    public String getEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String value) {
        name = value;
    }

    @Override
    public String getPort() {
        return port;
    }

    @Override
    public void setPort(String value) {
        port = value;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public void setProtocol(String value) {
        protocol = value;
    }

    @Override
    public String getThreadPool() {
        return pool;
    }

    @Override
    public void setThreadPool(String value) {
        pool = value;
    }

    @Override
    public String getTransport() {
        return transport;
    }

    @Override
    public void setTransport(String value) {
        transport = value;
    }

    @Override
    public String getJkEnabled() {
        return jkEnabled;
    }

    @Override
    public void setJkEnabled(String value) {
        jkEnabled = value;
    }

    @Override
    public String getJkConfigurationFile() {
        return jkConfigurationFile;
    }

    @Override
    public void setJkConfigurationFile(String jkConfigurationFile) {
        this.jkConfigurationFile = jkConfigurationFile;
    }

    public void injectedInto(Object target) {
    }

    @Override
    public <T extends ConfigBeanProxy> T createChild(Class<T> type) throws TransactionFailure {
        throw new UnsupportedOperationException();
    }

    @Override
    public Protocol findProtocol() {
        return null;
    }

    @Override
    public String findHttpProtocolName() {
        return null;
    }

    @Override
    public Protocol findHttpProtocol() {
        return null;
    }

    @Override
    public ThreadPool findThreadPool() {
        return null;
    }

    @Override
    public Transport findTransport() {
        return null;
    }

    @Override
    public NetworkListeners getParent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends ConfigBeanProxy> T getParent(Class<T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConfigBeanProxy deepCopy(ConfigBeanProxy parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Property> getProperty() {
        return properties;
    }

    @Override
    public Property getProperty(String name) {
        if (name == null) {
            return null;
        }

        for (Property property : properties) {
            if (name.equals(property.getName())) {
                return property;
            }
        }

        return null;
    }

    @Override
    public String getPropertyValue(String name) {
        return getPropertyValue(name, null);
    }

    @Override
    public String getPropertyValue(String name, String defaultValue) {
        final Property property = getProperty(name);
        if (property != null) {
            return property.getValue();
        }

        return defaultValue;
    }

    @Override
    public Property addProperty(Property prprt) {
        if (properties.add(prprt)) {
            return prprt;
        } else {
            return null;
        }
    }

    @Override
    public Property lookupProperty(String string) {
        return getProperty(name);
    }

    @Override
    public Property removeProperty(String string) {
        final Property prop = getProperty(name);
        if (prop == null) {
            return null;
        }
        return removeProperty(prop);
    }

    @Override
    public Property removeProperty(Property prprt) {
        if (properties.remove(prprt)) {
            return prprt;
        }
        return null;
    }
}
