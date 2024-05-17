/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.annotation.handlers;

import java.util.Properties;

/**
 * @author David Matejcek
 */
public class ManagedScheduledExecutorDefinitionData implements ContextualResourceDefinition {

    private static final long serialVersionUID = -5569777558325563130L;
    private String name;
    private String context;
    private Class<?>[] qualifiers;
    private boolean virtual;
    private long hungTaskThreshold;
    private int maxAsync = Integer.MAX_VALUE;
    private Properties properties = new Properties();

    @Override
    public String getName() {
        return name;
    }


    @Override
    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String getContext() {
        return context;
    }


    @Override
    public void setContext(String context) {
        this.context = context;
    }


    @Override
    public Class<?>[] getQualifiers() {
        return qualifiers;
    }


    @Override
    public void setQualifiers(Class<?>[] qualifiers) {
        this.qualifiers = qualifiers;
    }


    @Override
    public boolean isVirtual() {
        return virtual;
    }


    @Override
    public void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }


    public long getHungTaskThreshold() {
        return hungTaskThreshold;
    }


    public void setHungTaskThreshold(long hungTaskThreshold) {
        this.hungTaskThreshold = hungTaskThreshold;
    }


    public int getMaxAsync() {
        return maxAsync;
    }


    public void setMaxAsync(int maxAsync) {
        this.maxAsync = maxAsync;
    }


    public Properties getProperties() {
        return properties;
    }


    public void setProperties(Properties properties) {
        this.properties = properties;
    }


    public void addManagedScheduledExecutorDefinitionDescriptor(String name, String value) {
        properties.put(name, value);
    }


    @Override
    public String toString() {
        return super.toString() + "[" + getName() + "]";
    }
}
