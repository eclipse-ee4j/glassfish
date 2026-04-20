/*
 * Copyright (c) 2023, 2026 Contributors to the Eclipse Foundation
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

package org.glassfish.loadbalancer.config;

import com.sun.enterprise.config.serverbeans.ClusterRef;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Ref;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.config.support.CreationDecorator;
import org.glassfish.config.support.DeletionDecorator;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.loadbalancer.config.customvalidators.RefConstraint;
import org.glassfish.loadbalancer.config.customvalidators.RefValidator;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.RetryableException;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.config.support.Constants.NAME_REGEX;

/**
 *
 */
@Configured
@RefConstraint(
    message = "lb-config can contain references to either server-ref or cluster-ref but not both.",
    payload = RefValidator.class)
public interface LbConfig extends ConfigBeanProxy, PropertyBag, Payload {

    String LAST_APPLIED_PROPERTY = "last-applied";

    String LAST_EXPORTED_PROPERTY = "last-exported";

    /**
     * Gets the value of the {@code name} property.
     *
     * <p>Name of the load balancer configuration.
     *
     * @return possible object is {@link String}
     */
    @Attribute(key = true)
    @Pattern(
        regexp = NAME_REGEX,
        message = """
            Invalid lb-config name. The name must start with a letter, number or underscore and may contain only
            letters, numbers, and these characters: hyphen, period, underscore, hash and semicolon."
            """,
        payload = LbConfig.class)
    @NotNull
    String getName();

    /**
     * Sets the value of the {@code name} property.
     *
     * @param name allowed object is {@link String}
     */
    @Param (name = "name", primary = true)
    void setName(String name) throws PropertyVetoException;

    /**
     * Gets the value of the {@code responseTimeoutInSeconds} property.
     *
     * <p>Period within which a server must return a response or otherwise it will
     * be considered unhealthy. Default value is {@code 60} seconds. Must be greater
     * than or equal to {@code 0}. A value of 0 effectively turns off this check
     * functionality, meaning the server will always be considered healthy.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "60")
    @Min(value = 0)
    String getResponseTimeoutInSeconds();

    /**
     * Sets the value of the {@code responseTimeoutInSeconds} property.
     *
     * @param responseTimeout allowed object is {@link String}
     */
    @Param(name = "responsetimeout", optional = true)
    void setResponseTimeoutInSeconds(String responseTimeout) throws PropertyVetoException;

    /**
     * Gets the value of the {@code httpsRouting} property.
     *
     * <p>Boolean flag indicating how load-balancer will route https requests.
     * If true then an https request to the load-balancer will result in an
     * https request to the server; if {@code false} then https requests to the
     * load-balancer result in http requests to the server.
     *
     * <p>Default is to use {@code http} (i.e. value of {@code false})
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "false",dataType = Boolean.class)
    String getHttpsRouting();

    /**
     * Sets the value of the {@code httpsRouting} property.
     *
     * @param httpsRouting allowed object is {@link String}
     */
    @Param(name = "httpsrouting", optional = true)
    void setHttpsRouting(String httpsRouting) throws PropertyVetoException;

    /**
     * Gets the value of the {@code reloadPollIntervalInSeconds} property.
     *
     * <p>Maximum period, in seconds, that a change to the load balancer
     * configuration file takes before it is detected by the load balancer and
     * the file reloaded. A value of 0 indicates that reloading is disabled.
     *
     * <p>Default period is {@code 1} minute (60 sec)
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "60")
    String getReloadPollIntervalInSeconds();

    /**
     * Sets the value of the {@code reloadPollIntervalInSeconds} property.
     *
     * @param reloadPollInterval allowed object is {@link String}
     */
    @Param(name = "reloadinterval", optional = true)
    void setReloadPollIntervalInSeconds(String reloadPollInterval) throws PropertyVetoException;

    /**
     * Gets the value of the {@code monitoringEnabled} property.
     *
     * <p>Boolean flag that determines whether monitoring is switched on or not.
     *
     * <p>Default is that monitoring is switched off ({@code false}).
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "false",dataType = Boolean.class)
    String getMonitoringEnabled();

    /**
     * Sets the value of the {@code monitoringEnabled} property.
     *
     * @param monitoringEnabled allowed object is {@link String}
     */
    @Param(name = "monitor", optional = true)
    void setMonitoringEnabled(String monitoringEnabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code routeCookieEnabled} property.
     *
     * <p>Boolean flag that determines whether a route cookie is or is not enabled.
     *
     * <p>Default is enabled ({@code true}).
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "true",dataType = Boolean.class)
    String getRouteCookieEnabled();

    /**
     * Sets the value of the {@code routeCookieEnabled} property.
     *
     * @param routeCookieEnabled allowed object is {@link String}
     */
    @Param(name = "routecookie", optional = true)
    void setRouteCookieEnabled(String routeCookieEnabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code clusterRefOrServerRef} property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present inside
     * the JAXB object. This is why there is not a {@code set} method for
     * the {@code clusterRefOrServerRef} property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     *    getClusterRefOrServerRef().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list:
     * <ul>
     * <li>{@link ClusterRef}</li>
     * <li>{@link ServerRef}</li>
     * </ul>
     */
    @Element("*")
    List<Ref> getClusterRefOrServerRef();

    /**
     *  Properties as per {@link PropertyBag}.
     */
    @Override
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props" )
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();

    default <T> List<T> getRefs(Class<T> type) {
        List<T> refs = new ArrayList<>();
        for (Object ref : getClusterRefOrServerRef()) {
            if (type.isInstance(ref)) {
                refs.add(type.cast(ref));
            }
        }
        // you have to return an unmodifiable list since this list
        // is not the real list of elements as maintained by this config bean
        return Collections.unmodifiableList(refs);
    }

    default <T> T getRefByRef(Class<T> type, String refName) {
        if (refName == null) {
            return null;
        }

        for (Ref ref : getClusterRefOrServerRef()) {
            if (type.isInstance(ref) && ref.getRef().equals(refName)) {
                return type.cast(ref);
            }
        }
        return null;
    }

    default Date getLastExported() {
        return getInternalPropertyValue(LAST_EXPORTED_PROPERTY);
    }

    default Date getLastApplied() {
        return getInternalPropertyValue(LAST_APPLIED_PROPERTY);
    }

    default boolean setLastExported() {
        return setInternalProperty(LAST_EXPORTED_PROPERTY);
    }

    default boolean setLastApplied() {
        return setInternalProperty(LAST_APPLIED_PROPERTY);
    }

    private Date getInternalPropertyValue(String propertyName) {
        String propertyValue = getPropertyValue(propertyName);
        if(propertyValue == null){
            return null;
        }
        return new Date(Long.parseLong(propertyValue));
    }

    private boolean setInternalProperty(String propertyName) {
        Property property = getProperty(propertyName);
        Transaction transaction = new Transaction();
        try {
            if (property == null) {
                LbConfig lcProxy = transaction.enroll(this);
                property = lcProxy.createChild(Property.class);
                property.setName(propertyName);
                property.setValue(String.valueOf((new Date()).getTime()));
                lcProxy.getProperty().add(property);
            } else {
                Property propertyProxy = transaction.enroll(property);
                propertyProxy.setValue(String.valueOf((new Date()).getTime()));
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            System.getLogger(LbConfig.class.getName()).log(ERROR,
                () -> "Unable to set property '" + propertyName + "' in lbconfig with name '" + getName() + "'", e);
            return false;
        }
        return true;
    }

    @Service
    @PerLookup
    class Decorator implements CreationDecorator<LbConfig> {
        private static final Logger LOG = System.getLogger(Decorator.class.getName());

        @Param (name = "name", optional = true)
        String config_name;

        @Param(optional = true)
        String target;

        @Param (optional = true, defaultValue = "60")
        String responsetimeout;

        @Param (optional = true, defaultValue = "false")
        Boolean httpsrouting;

        @Param (optional = true, defaultValue = "60")
        String reloadinterval;

        @Param (optional = true, defaultValue = "false")
        Boolean monitor;

        @Param (optional = true, defaultValue = "true")
        Boolean routecookie;

        @Param(optional = true, name = "property", separator = ':')
        Properties properties;

        @Inject
        Domain domain;

        /**
         * Create lb-config entries.
         *
         * <p>Tasks :
         *      - ensures that it references an existing cluster

         * @param context administration command context
         * @param instance newly created configuration element
         *
         */
        @Override
        public void decorate(AdminCommandContext context, final LbConfig instance) throws TransactionFailure, PropertyVetoException {
            if (config_name == null && target == null) {
                throw new TransactionFailure(
                    "Either option --target or operand config_name is required for this command.");
            }

            // generate lb config name if not specified
            if (config_name == null) {
                config_name = target + "_LB_CONFIG";
            }

            LbConfigs lbconfigs = domain.getExtensionByType(LbConfigs.class);
            //create load-balancers parent element if it does not exist
            if (lbconfigs == null) {
                Transaction transaction = new Transaction();
                try {
                    Domain domainProxy = transaction.enroll(domain);
                    lbconfigs = domainProxy.createChild(LbConfigs.class);
                    domainProxy.getExtensions().add(lbconfigs);
                    transaction.commit();
                } catch (TransactionFailure | RetryableException ex) {
                    transaction.rollback();
                    throw new TransactionFailure("Creation of parent element lb-configs failed.", ex);
                }
            }

            if (lbconfigs.getLbConfig(config_name) != null) {
                throw new TransactionFailure("Load balancer configuration '" + config_name
                    + "' contains server refs or clusters refs. It must be empty in order to be removed.");
            }

            instance.setName(config_name);
            instance.setResponseTimeoutInSeconds(responsetimeout);
            instance.setReloadPollIntervalInSeconds(reloadinterval);
            instance.setMonitoringEnabled(monitor == null ? null : monitor.toString());
            instance.setRouteCookieEnabled(routecookie == null ? null : routecookie.toString());
            instance.setHttpsRouting(httpsrouting == null ? null : httpsrouting.toString());

            // creates a reference to the target
            if (target != null) {
                if (domain.getClusterNamed(target) != null) {
                   ClusterRef cRef = instance.createChild(ClusterRef.class);
                   cRef.setRef(target);
                   instance.getClusterRefOrServerRef().add(cRef);
                } else if (domain.isServer(target)) {
                    ServerRef sRef = instance.createChild(ServerRef.class);
                    sRef.setRef(target);
                    instance.getClusterRefOrServerRef().add(sRef);
                } else {
                    throw new TransactionFailure(
                        "Invalid argument. Target '" + target + "' is not a cluster or stand alone server instance.");
                }
            }

            // add properties
            if (properties != null) {
                for (Object propname: properties.keySet()) {
                    Property newprop = instance.createChild(Property.class);
                    newprop.setName((String) propname);
                    newprop.setValue(properties.getProperty((String) propname));
                    instance.getProperty().add(newprop);
                }
            }
            LOG.log(INFO, () -> "Load balancer configuration '" + config_name + "' created.");
        }
    }

    @Service
    @PerLookup
    class DeleteDecorator implements DeletionDecorator<LbConfigs, LbConfig> {

        @Inject
        private Domain domain;

        @Override
        public void decorate(AdminCommandContext context, LbConfigs parent, LbConfig child) throws TransactionFailure {

            String lbConfigName = child.getName();
            LbConfig lbConfig = domain.getExtensionByType(LbConfigs.class).getLbConfig(lbConfigName);
            //Ensure there are no refs
            if (!lbConfig.getClusterRefOrServerRef().isEmpty()) {
                throw new TransactionFailure(
                    new LocalStringManagerImpl(LbConfig.class).getLocalString("LbConfigNotEmpty", lbConfigName));
            }
            System.getLogger(DeleteDecorator.class.getName()).log(INFO,
                () -> "Load balancer configuration '" + lbConfigName + "' deleted.");
        }
   }
}
