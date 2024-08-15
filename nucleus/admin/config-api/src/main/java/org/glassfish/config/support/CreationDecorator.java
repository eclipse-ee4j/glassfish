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

import java.beans.PropertyVetoException;

import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * An element decorator decorates a newly added configuration element, usually added through the generic create command
 * implementation.
 *
 * @author Jerome Dochez
 */
@PerLookup
public interface CreationDecorator<T extends ConfigBeanProxy> {

    /**
     * The element instance has been created and added to the parent, it can be customized. This method is called within a
     * {@link org.jvnet.hk2.config.Transaction} and instance is therefore a writeable view on the configuration component.
     *
     * @param context administration command context
     * @param instance newly created configuration element
     * @throws TransactionFailure if the transaction should be rollbacked
     * @throws PropertyVetoException if one of the listener of <T> is throwing a veto exception
     */
    public void decorate(AdminCommandContext context, T instance) throws TransactionFailure, PropertyVetoException;

    /**
     * Default implementation of a decorator that does nothing.
     */
    @Service
    public class NoDecoration implements CreationDecorator<ConfigBeanProxy> {
        @Override
        public void decorate(AdminCommandContext context, ConfigBeanProxy instance) throws TransactionFailure, PropertyVetoException {
            // do nothing
        }
    }
}
