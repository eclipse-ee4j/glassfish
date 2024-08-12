/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Server;

import jakarta.inject.Inject;

import java.util.Collection;

import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.Dom;

/**
 * Resolver based on a supplied target parameter (with a possible default value).
 *
 * @author Jerome Dochez
 */
@Service
public class TargetBasedResolver implements CrudResolver {

    @Param(defaultValue = "server", optional = true)
    String target = "server";

    @Inject
    ServiceLocator habitat;

    @Override
    public <T extends ConfigBeanProxy> T resolve(AdminCommandContext context, Class<T> type) {
        try {
            ConfigBeanProxy proxy = getTarget(Config.class, type);
            if (proxy == null) {
                proxy = getTarget(Cluster.class, type);
            }
            if (proxy == null) {
                proxy = getTarget(Server.class, type);
            }
            return type.cast(proxy);
        } catch (ClassNotFoundException e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    private <T extends ConfigBeanProxy> T getTarget(Class<? extends ConfigBeanProxy> targetType, Class<T> type)
            throws ClassNotFoundException {

        // when using the target based parameter, we look first for a configuration of that name,
        // then we look for a cluster of that name and finally we look for a subelement of the right type

        final String name = getName();

        ConfigBeanProxy config = habitat.getService(targetType, target);
        if (config != null) {
            try {
                return type.cast(config);
            } catch (ClassCastException e) {
                // ok we need to do more work to find which object is really requested.
            }
            Dom parentDom = Dom.unwrap(config);

            String elementName = GenericCrudCommand.elementName(parentDom.document, targetType, type);
            if (elementName == null) {
                return null;
            }
            ConfigModel.Property property = parentDom.model.getElement(elementName);
            if (property.isCollection()) {
                Collection<Dom> collection = parentDom.nodeElements(elementName);
                if (collection == null) {
                    return null;
                }

                for (Dom child : collection) {
                    if (name.equals(child.attribute("ref"))) {
                        return type.cast(child.<ConfigBeanProxy>createProxy());
                    }
                }
            }
        }
        return null;
    }

    public String getName() {
        return "";
    }

}
