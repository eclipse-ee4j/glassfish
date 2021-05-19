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

package org.glassfish.loadbalancer.config;

import org.glassfish.api.I18n;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.*;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.DuckTyped;

import java.util.List;

import com.sun.enterprise.config.serverbeans.DomainExtension;

/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "lbConfig"
}) */

@Configured
public interface LbConfigs extends ConfigBeanProxy, DomainExtension {


    /**
     * Gets the value of the lbConfig property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lbConfig property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLbConfig().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link LbConfig }
     */
    @Element
    @Create(value="create-http-lb-config", decorator=LbConfig.Decorator.class,
            cluster=@org.glassfish.api.admin.ExecuteOn(value = RuntimeType.DAS),
            i18n=@I18n("create.http.lb.config.command"))
    @Delete(value="delete-http-lb-config", resolver= TypeAndNameResolver.class,
            decorator=LbConfig.DeleteDecorator.class,
            i18n=@I18n("delete.http.lb.config.command"))
    public List<LbConfig> getLbConfig();

    /**
     * Return the lb config with the given name, or null if no such lb config exists.
     *
     * @param   name    the name of the lb config
     * @return          the LbConfig object, or null if no such lb config
     */

    @DuckTyped
    public LbConfig getLbConfig(String name);

    class Duck {
        public static LbConfig getLbConfig(LbConfigs instance, String name) {
            for (LbConfig lbconfig : instance.getLbConfig()) {
                if (lbconfig.getName().equals(name)) {
                    return lbconfig;
                }
            }
            return null;
        }
    }
}
