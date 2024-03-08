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

import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.glassfish.api.admin.AdminCommandContext;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * A config resolver is responsible for finding the target object of a specified type on which a creation command
 * invocation will be processed.
 *
 * Implementation of these interfaces can be injected with the command invocation parameters in order to determine which
 * object should be returned
 *
 * @author Jerome Dochez
 */
@Contract
public interface CrudResolver {

    /**
     * Retrieves the existing configuration object a command invocation is intented to mutate.
     *
     * @param context the command invocation context
     * @param type the type of the expected instance
     * @return the instance or null if not found
     */
    <T extends ConfigBeanProxy> T resolve(AdminCommandContext context, Class<T> type);

    @Service
    public static final class DefaultResolver implements CrudResolver {

        @Inject
        @Named("type")
        @Optional
        CrudResolver defaultResolver = null;

        @Override
        public <T extends ConfigBeanProxy> T resolve(AdminCommandContext context, Class<T> type) {
            if (defaultResolver != null) {
                return defaultResolver.resolve(context, type);
            }
            return null;
        }
    }
}
