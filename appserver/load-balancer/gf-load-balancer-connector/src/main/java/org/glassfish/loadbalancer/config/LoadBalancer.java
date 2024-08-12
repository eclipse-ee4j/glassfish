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

package org.glassfish.loadbalancer.config;

import com.sun.enterprise.config.modularity.annotation.HasNoDefaultConfiguration;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.config.support.DeletionDecorator;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;
/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */

@Configured
@HasNoDefaultConfiguration
public interface LoadBalancer extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(key=true)
    @NotNull
    public String getName();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    @Param(name="name", primary = true)
    public void setName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the lbConfigName property.
     *
     * Name of the lb-config used by this load balancer
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @NotNull
    public String getLbConfigName();

    /**
     * Sets the value of the lbConfigName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLbConfigName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the device host property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @NotNull
    public String getDeviceHost();

    /**
     * Sets the value of the device host property.
     *
     * @param value allowed object is
     *              {@link String }
     */

    public void setDeviceHost(String value) throws PropertyVetoException;

    /**
     * Gets the value of the device port property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @NotNull
    public String getDevicePort();

    /**
     * Sets the value of the device port property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDevicePort(String value) throws PropertyVetoException;

    /**
     * Gets the value of the auto apply enabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(dataType=Boolean.class)
    @Deprecated
    public String getAutoApplyEnabled();

    /**
     * Sets the value of the auto apply enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    @Deprecated
    public void setAutoApplyEnabled(String value) throws PropertyVetoException;

    /**
     *Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     *
     * Known properties:
     * ssl-proxy-host - proxy host used for outbound HTTP
     * ssl-proxy-port - proxy port used for outbound HTTP
     */
    @Override
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();

    @Service
    @PerLookup
    class DeleteDecorator implements DeletionDecorator<LoadBalancers, LoadBalancer> {
        @Inject
        private Domain domain;

        @Override
        public void decorate(AdminCommandContext context, LoadBalancers parent, LoadBalancer child) throws
                PropertyVetoException, TransactionFailure{
            ActionReport report = context.getActionReport();
            Logger logger = LogDomains.getLogger(LoadBalancer.class, LogDomains.ADMIN_LOGGER);
            LocalStringManagerImpl localStrings = new LocalStringManagerImpl(LoadBalancer.class);
            Transaction t = Transaction.getTransaction(parent);

            String lbName = child.getName();

            String lbConfigName = child.getLbConfigName();
            LbConfig lbConfig = domain.getExtensionByType(LbConfigs.class).getLbConfig(lbConfigName);

            //check if lb-config is used by any other load-balancer
            for (LoadBalancer lb:domain.getExtensionByType(LoadBalancers.class).getLoadBalancer()) {
                if (!lb.getName().equals(lbName) &&
                        lb.getLbConfigName().equals(lbConfigName)) {
                    String msg = localStrings.getLocalString("LbConfigIsInUse", lbConfigName);
                    report.setMessage(msg);
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    throw new TransactionFailure(msg);
                }
            }

            //remove lb-config corresponding to this load-balancer
            LbConfigs configs = domain.getExtensionByType(LbConfigs.class);
            try {
                if (t != null) {
                    LbConfigs c = t.enroll(configs);
                    List<LbConfig> configList = c.getLbConfig();
                    configList.remove(lbConfig);
                }
            } catch (TransactionFailure ex) {
                logger.log(Level.WARNING,
                        localStrings.getLocalString("DeleteLbConfigFailed",
                        "Unable to remove lb config {0}", lbConfigName), ex);
                String msg = ex.getMessage() != null ? ex.getMessage()
                        : localStrings.getLocalString("DeleteLbConfigFailed",
                        "Unable to remove lb config {0}", lbConfigName);
                report.setMessage(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setFailureCause(ex);
                throw ex;
            }
        }
    }
}
