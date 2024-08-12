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

import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 * Resolver based on type + name.
 *
 * @author Jerome Dochez
 */
@Service
public class TypeAndNameResolver implements CrudResolver {

    @Param(primary = true)
    String name;

    @Inject
    ServiceLocator habitat;

    final protected static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(GenericCrudCommand.class);

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ConfigBeanProxy> T resolve(AdminCommandContext context, Class<T> type) {
        T proxy = (T) habitat.getService(type, name);
        return proxy;
    }

    public String name() {
        return name;
    }
}
