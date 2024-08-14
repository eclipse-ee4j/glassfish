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

package org.glassfish.loadbalancer.admin.cli.reader.impl;

import java.util.Iterator;
import java.util.Properties;

import org.glassfish.loadbalancer.admin.cli.reader.api.LbReaderException;
import org.glassfish.loadbalancer.admin.cli.reader.api.LoadbalancerReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.PropertyReader;
import org.glassfish.loadbalancer.admin.cli.transform.PropertyVisitor;
import org.glassfish.loadbalancer.admin.cli.transform.Visitor;
import org.glassfish.loadbalancer.config.LbConfig;
import org.jvnet.hk2.config.types.Property;

/**
 * Provides property information relavant to Load balancer tier.
 *
 * @author Kshitiz Saxena
 */
public class PropertyReaderImpl implements PropertyReader {

    public static PropertyReader[] getPropertyReaders(Properties properties) {
        if (properties == null) {
            properties = new Properties();
        }

        if (properties.getProperty(
                LoadbalancerReader.ACTIVE_HEALTH_CHECK) == null) {
            properties.setProperty(
                    LoadbalancerReader.ACTIVE_HEALTH_CHECK,
                    LoadbalancerReader.ACTIVE_HEALTH_CHECK_VALUE);
        }

        if (properties.getProperty(
                LoadbalancerReader.NUM_HEALTH_CHECK) == null) {
            properties.setProperty(
                    LoadbalancerReader.NUM_HEALTH_CHECK,
                    LoadbalancerReader.NUM_HEALTH_CHECK_VALUE);
        }

        if (properties.getProperty(
                LoadbalancerReader.REWRITE_LOCATION) == null) {
            properties.setProperty(
                    LoadbalancerReader.REWRITE_LOCATION,
                    LoadbalancerReader.REWRITE_LOCATION_VALUE);
        }

        if (properties.getProperty(
                LoadbalancerReader.REWRITE_COOKIES) == null) {
            properties.setProperty(
                    LoadbalancerReader.REWRITE_COOKIES,
                    LoadbalancerReader.REWRITE_COOKIES_VALUE);
        }

        if (properties.getProperty(
                LoadbalancerReader.RESP_TIMEOUT) == null) {
            properties.setProperty(
                    LoadbalancerReader.RESP_TIMEOUT,
                    LoadbalancerReader.RESP_TIMEOUT_VALUE);
        }

        if (properties.getProperty(
                LoadbalancerReader.RELOAD_INTERVAL) == null) {
            properties.setProperty(
                    LoadbalancerReader.RELOAD_INTERVAL,
                    LoadbalancerReader.RELOAD_INTERVAL_VALUE);
        }

        if (properties.getProperty(
                LoadbalancerReader.HTTPS_ROUTING) == null) {
            properties.setProperty(
                    LoadbalancerReader.HTTPS_ROUTING,
                    LoadbalancerReader.HTTPS_ROUTING_VALUE);
        }

        if (properties.getProperty(
                LoadbalancerReader.REQ_MONITOR_DATA) == null) {
            properties.setProperty(
                    LoadbalancerReader.REQ_MONITOR_DATA,
                    LoadbalancerReader.REQ_MONITOR_DATA_VALUE);
        }

        if (properties.getProperty(
                LoadbalancerReader.PREFERRED_FAILOVER_INSTANCE) == null) {
            properties.setProperty(
                    LoadbalancerReader.PREFERRED_FAILOVER_INSTANCE,
                    LoadbalancerReader.PREFERRED_FAILOVER_INSTANCE_VALUE);
        }

        int i = 0;
        int propSize = properties.size();
        PropertyReaderImpl[] props = new PropertyReaderImpl[propSize];


        Iterator iter = properties.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            props[i++] = new PropertyReaderImpl(key,
                    properties.getProperty(key));
        }
        return props;
    }

    public static PropertyReader[] getPropertyReaders(LbConfig _lbConfig) {
        Properties properties = new Properties();
        properties.setProperty(LoadbalancerReader.HTTPS_ROUTING, _lbConfig.getHttpsRouting());
        properties.setProperty(LoadbalancerReader.REQ_MONITOR_DATA, _lbConfig.getMonitoringEnabled());
        properties.setProperty(LoadbalancerReader.RELOAD_INTERVAL, _lbConfig.getReloadPollIntervalInSeconds());
        properties.setProperty(LoadbalancerReader.RESP_TIMEOUT, _lbConfig.getResponseTimeoutInSeconds());
        Iterator<Property> propertyList = _lbConfig.getProperty().iterator();
        while(propertyList.hasNext()){
            Property property = propertyList.next();
            if(property.getName().equals(LbConfig.LAST_APPLIED_PROPERTY) ||
                    property.getName().equals(LbConfig.LAST_EXPORTED_PROPERTY)){
                continue;
            }
            properties.setProperty(property.getName(), property.getValue());
        }
        return getPropertyReaders(properties);
    }

    // --- CTOR METHOD ------
    private PropertyReaderImpl(String name, String value) {
        _name = name;
        _value = value;
    }

    // -- READER IMPLEMENTATION ----
    /**
     * Returns name of the property
     *
     * @return String           name of the property
     */
    @Override
    public String getName() throws LbReaderException {
        return _name;
    }

    /**
     * Returns value of the property
     *
     * @return String           name of the value
     */
    @Override
    public String getValue() throws LbReaderException {
        return _value;
    }

    /**
     * Returns description of the property
     *
     * @return String           description of the property
     */
    @Override
    public String getDescription() throws LbReaderException {
        return _description;
    }

    // --- VISITOR IMPLEMENTATION ---
    @Override
    public void accept(Visitor v) throws Exception {
        if (v instanceof PropertyVisitor) {
            PropertyVisitor pv = (PropertyVisitor) v;
            pv.visit(this);
        }
    }

    // -- PRIVATE VARS ---
    private String _value = null;
    private String _name = null;
    private String _description = null;
}
