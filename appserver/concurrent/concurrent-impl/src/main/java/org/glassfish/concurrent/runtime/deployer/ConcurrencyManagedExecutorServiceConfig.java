/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.runtime.deployer;

import com.sun.enterprise.deployment.ManagedExecutorDefinitionDescriptor;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.concurrent.config.ManagedExecutorService;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static com.sun.enterprise.universal.JavaLangUtils.nonNull;


/**
 * @author David Matejcek
 */
public class ConcurrencyManagedExecutorServiceConfig implements ManagedExecutorService {
    private final ManagedExecutorDefinitionDescriptor descriptor;

    public ConcurrencyManagedExecutorServiceConfig(ManagedExecutorDefinitionDescriptor descriptor) {
        this.descriptor = descriptor;
    }


    @Override
    public ConfigBeanProxy getParent() {
        return null;
    }


    @Override
    public <T extends ConfigBeanProxy> T getParent(Class<T> type) {
        return null;
    }


    @Override
    public <T extends ConfigBeanProxy> T createChild(Class<T> type) throws TransactionFailure {
        return null;
    }


    @Override
    public ConfigBeanProxy deepCopy(ConfigBeanProxy parent) throws TransactionFailure {
        return null;
    }


    @Override
    public String getObjectType() {
        return null;
    }


    @Override
    public void setObjectType(String value) throws PropertyVetoException {
    }


    @Override
    public String getDeploymentOrder() {
        return null;
    }


    @Override
    public void setDeploymentOrder(String value) throws PropertyVetoException {
    }


    @Override
    public String getJndiName() {
        return descriptor.getJndiName().toString();
    }


    @Override
    public void setJndiName(String value) throws PropertyVetoException {
        descriptor.setName(value);
    }


    @Override
    public String getEnabled() {
        return Boolean.TRUE.toString();
    }


    @Override
    public void setEnabled(String value) throws PropertyVetoException {
    }


    @Override
    public String getContextInfoEnabled() {
        return null;
    }


    @Override
    public void setContextInfoEnabled(String value) throws PropertyVetoException {
    }


    @Override
    public String getContextInfo() {
        return null;
    }


    @Override
    public void setContextInfo(String value) throws PropertyVetoException {
    }


    @Override
    public String getDescription() {
        return descriptor.getDescription();
    }


    @Override
    public void setDescription(String value) throws PropertyVetoException {
        descriptor.setDescription(value);
    }


    @Override
    public List<Property> getProperty() {
        return null;
    }


    @Override
    public Property addProperty(Property property) {
        return null;
    }


    @Override
    public Property lookupProperty(String name) {
        return null;
    }


    @Override
    public Property removeProperty(String name) {
        return null;
    }


    @Override
    public Property removeProperty(Property removeMe) {
        return null;
    }


    @Override
    public Property getProperty(String name) {
        return null;
    }


    @Override
    public String getPropertyValue(String name) {
        return null;
    }


    @Override
    public String getPropertyValue(String name, String defaultValue) {
        return null;
    }


    @Override
    public String getThreadPriority() {
        return String.valueOf(Thread.NORM_PRIORITY);
    }


    @Override
    public void setThreadPriority(String value) throws PropertyVetoException {
    }


    @Override
    public String getLongRunningTasks() {
        return Boolean.FALSE.toString();
    }


    @Override
    public void setLongRunningTasks(String value) throws PropertyVetoException {
    }


    @Override
    public String getHungAfterSeconds() {
        return nonNull(descriptor.getHungAfterSeconds(), String::valueOf, null);
    }


    @Override
    public void setHungAfterSeconds(String value) throws PropertyVetoException {
        descriptor.setHungAfterSeconds(nonNull(value, Long::valueOf, null));
    }


    @Override
    public String getHungLoggerPrintOnce() {
        // TODO: implement Fujitsu features here again
        return Boolean.TRUE.toString();
    }


    @Override
    public void setHungLoggerPrintOnce(String value) throws PropertyVetoException {
    }


    @Override
    public String getHungLoggerInitialDelaySeconds() {
        // TODO: implement Fujitsu features here again
        return getHungAfterSeconds();
    }


    @Override
    public void setHungLoggerInitialDelaySeconds(String value) throws PropertyVetoException {
    }


    @Override
    public String getHungLoggerIntervalSeconds() {
        // TODO: implement Fujitsu features here again
        return getHungAfterSeconds();
    }


    @Override
    public void setHungLoggerIntervalSeconds(String value) throws PropertyVetoException {
    }


    @Override
    public String getCorePoolSize() {
        return String.valueOf(descriptor.getMaximumPoolSize());
    }


    @Override
    public void setCorePoolSize(String value) throws PropertyVetoException {
    }


    @Override
    public String getKeepAliveSeconds() {
        return "60";
    }


    @Override
    public void setKeepAliveSeconds(String value) throws PropertyVetoException {
    }


    @Override
    public String getThreadLifetimeSeconds() {
        return "0";
    }


    @Override
    public void setThreadLifetimeSeconds(String value) throws PropertyVetoException {
    }


    @Override
    public String getContext() {
        return descriptor.getContext();
    }


    @Override
    public void setContext(String value) throws PropertyVetoException {
        descriptor.setContext(value);
    }


    @Override
    public String getMaximumPoolSize() {
        return String.valueOf(descriptor.getMaximumPoolSize());
    }


    @Override
    public void setMaximumPoolSize(String value) throws PropertyVetoException {
        descriptor.setMaximumPoolSize(Integer.parseInt(value));
    }


    @Override
    public String getTaskQueueCapacity() {
        return String.valueOf(Integer.MAX_VALUE);
    }


    @Override
    public void setTaskQueueCapacity(String value) throws PropertyVetoException {
    }


    @Override
    public String getIdentity() {
        return null;
    }

    @Override
    public String getUseVirtualThreads() {
        return String.valueOf(descriptor.getUseVirtualThreads());
    }

    @Override
    public void setUseVirtualThreads(String value) throws PropertyVetoException {
        descriptor.setUseVirtualThreads(Boolean.parseBoolean(value));
    }

}
