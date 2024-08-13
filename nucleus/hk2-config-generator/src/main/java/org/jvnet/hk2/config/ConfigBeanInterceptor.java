/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.jvnet.hk2.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

/**
 * Interceptor interface to be notified of read/write operations on a ConfigBean.
 * Interceptor can be configured using the optional T interface.
 *
 * @Author Jerome Dochez
 */
public interface ConfigBeanInterceptor<T> {

    /**
     * Interceptor can usually be configured, allowing for customizing how
     * the interceptor should behave or do.
     *
     * @return interface implementing the configuration capability of this
     * interceptor
     */
    public T getConfiguration();

    /**
     * Notification that an attribute is about to be changed
     *
     * @param evt information about the forthcoming change
     * @throws PropertyVetoException if the change is unacceptable
     */
    public void beforeChange(PropertyChangeEvent evt) throws PropertyVetoException;

    /**
     * Notification that an attribute has changed
     *
     * @param evt information about the change
     * @param timestamp time of the change
     */
    public void afterChange(PropertyChangeEvent evt, long timestamp);

    /**
     * Notification of an attribute read
     *
     * @param source  object owning the attribute
     * @param xmlName name of the attribute
     * @param value value of the attribute
     */
    public void readValue(ConfigBean source, String xmlName, Object value);
}
