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

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;

import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Resolver for an un-named type. If the type instance is not found, it will create an instance of it under the domain
 * instance.
 *
 * @author Jerome Dochez
 */
@Service(name = "type")
public class TypeResolver implements CrudResolver {

    @Inject
    private ServiceLocator habitat;

    @Inject
    Domain domain;

    final protected static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(TypeResolver.class);

    @Override
    public <T extends ConfigBeanProxy> T resolve(AdminCommandContext context, final Class<T> type) {
        T proxy = habitat.getService(type);
        if (proxy == null) {
            try {
                proxy = type.cast(ConfigSupport.apply(new SingleConfigCode<Domain>() {
                    @Override
                    public Object run(Domain writeableDomain) throws PropertyVetoException, TransactionFailure {
                        ConfigBeanProxy child = writeableDomain.createChild(type);
                        Dom domDomain = Dom.unwrap(writeableDomain);
                        final String elementName;
                        try {
                            elementName = GenericCrudCommand.elementName(domDomain.document, Domain.class, type);
                        } catch (ClassNotFoundException e) {
                            throw new TransactionFailure(e.toString());
                        }
                        if (elementName == null) {
                            String msg = localStrings.getLocalString(TypeResolver.class, "TypeResolver.no_element_of_that_type",
                                    "The Domain configuration does not have a sub-element of the type {0}", type.getSimpleName());
                            throw new TransactionFailure(msg);
                        }
                        domDomain.setNodeElements(elementName, Dom.unwrap(child));

                        // add to the habitat
                        ServiceLocatorUtilities.addOneConstant(habitat, child, null, type);

                        return child;
                    }
                }, domain));
            } catch (TransactionFailure e) {
                throw new RuntimeException(e);
            }
            if (proxy == null) {
                String msg = localStrings.getLocalString(TypeResolver.class, "TypeResolver.target_object_not_found",
                        "Cannot find a single component instance of type {0}", type.getSimpleName());
                throw new RuntimeException(msg);
            }
        }
        return proxy;

    }
}
