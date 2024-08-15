/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.serverbeans;

import jakarta.inject.Inject;

import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.config.support.CrudResolver;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 * Resolver for an DomainExtension type. The type instance is accessed as a DomainExtension from the Domain.
 *
 * @author tmueller
 */
@Service
public class DomainExtensionResolver implements CrudResolver {

    @Inject
    Domain domain;

    @Override
    public <T extends ConfigBeanProxy> T resolve(AdminCommandContext context, final Class<T> type) {
        return type.cast(domain.getExtensionByType((Class<DomainExtension>) type));
    }
}
