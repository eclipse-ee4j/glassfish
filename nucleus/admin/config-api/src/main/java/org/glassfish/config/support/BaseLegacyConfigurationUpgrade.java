/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.config.support;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.config.LegacyConfigurationUpgrade;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import static org.glassfish.config.support.IntrospectionUtils.setProperty;

public abstract class BaseLegacyConfigurationUpgrade implements LegacyConfigurationUpgrade {
    protected void report(AdminCommandContext context, final String message) {
        context.getActionReport().setMessage("DEPRECATION WARNING: " + message);
    }

    protected void updatePropertyToAttribute(final AdminCommandContext context, final ConfigBeanProxy target, final String property,
            final String attribute) throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<ConfigBeanProxy>() {
            public Object run(ConfigBeanProxy param) {
                PropertyBag bag = (PropertyBag) param;
                final List<Property> propertyList = new ArrayList<Property>(bag.getProperty());
                setProperty(target, attribute, getValue(propertyList, property));
                final String message = MessageFormat.format("Moved {0}.property.{1} to {0}.{2}",
                        Dom.convertName(Dom.unwrap(target).getProxyType().getSimpleName()), property, Dom.convertName(attribute));
                report(context, message);
                bag.getProperty().clear();
                bag.getProperty().addAll(propertyList);
                return param;
            }
        }, target);
    }

    protected void removeProperty(final ConfigBeanProxy target, final String property) throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<ConfigBeanProxy>() {
            public Object run(ConfigBeanProxy param) {
                PropertyBag bag = (PropertyBag) param;
                final List<Property> propertyList = new ArrayList<Property>(bag.getProperty());
                final Iterator<Property> it = propertyList.iterator();
                boolean done = false;
                while (!done && it.hasNext()) {
                    Property prop = it.next();
                    if (property.equals(prop.getName())) {
                        done = true;
                        it.remove();
                    }
                }
                bag.getProperty().clear();
                bag.getProperty().addAll(propertyList);
                return param;
            }
        }, target);
    }

    private String getValue(List<Property> list, String property) {
        final Iterator<Property> iterator = list.iterator();
        while (iterator.hasNext()) {
            Property prop = iterator.next();
            if (property.equals(prop.getName())) {
                iterator.remove();
                return prop.getValue();
            }
        }
        return null;
    }

}
