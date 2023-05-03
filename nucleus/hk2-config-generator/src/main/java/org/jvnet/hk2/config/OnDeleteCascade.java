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
import java.lang.reflect.Proxy;

/**
 * Usage:
 * <code>
 * class ExampleDocument extends DomDocument<ConfigBean> {
 *   @Override
 *   public ConfigBean make(final ServiceLocator serviceLocator, XMLStreamReader xmlStreamReader,
 *       ConfigBean dom, ConfigModel configModel) {
 *     ConfigBean configBean = new ConfigBean(habitat,this, dom, configModel, xmlStreamReader);
 *     configBean.addInterceptor(Object.class, new OnDeleteCascade());
 *     return configBean;
 *   }
 * }
 * </code>
 *
 * @author Andriy Zhdanov
 *
 */
public class OnDeleteCascade implements ConfigBeanInterceptor<Object> {

    @Override
    public Object getConfiguration() {
        return new Object();
    }

    @Override
    public void beforeChange(PropertyChangeEvent evt)
            throws PropertyVetoException {

        Object oldValue = evt.getOldValue();
        if (oldValue != null && oldValue instanceof ConfigBeanProxy) {
            ConfigBean bean = ((ConfigBean) Proxy.getInvocationHandler(evt.getSource()));
            WriteableView writeableSource = bean.getWriteableView();
            writeableSource.removeNestedElements(oldValue);
        }
    }

    @Override
    public void afterChange(PropertyChangeEvent evt, long timestamp) {
    }

    @Override
    public void readValue(ConfigBean source, String xmlName, Object value) {
    }

}
