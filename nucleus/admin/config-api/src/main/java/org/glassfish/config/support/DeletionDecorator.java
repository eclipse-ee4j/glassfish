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
 * A decorator for acting upon a configuration element deletion.
 *
 * @param <T> the deleted element parent type
 * @param <U> the deleted element
 *
 * @author Jerome Dochez
 */
@PerLookup
public interface DeletionDecorator<T extends ConfigBeanProxy, U extends ConfigBeanProxy> {

    /**
     * notification of a configuration element of type U deletion.
     *
     * Note that this notification is called within the boundaries of the configuration transaction, therefore the parent
     * instance is a writable copy and further changes to the parent can be made without enrolling it inside a transaction.
     *
     * @param context the command context to lead to the element deletion
     * @param parent the parent instance the element was removed from
     * @param child the deleted instance
     */
    public void decorate(AdminCommandContext context, T parent, U child) throws TransactionFailure, PropertyVetoException;

    @Service
    public static class NoDecoration implements DeletionDecorator<ConfigBeanProxy, ConfigBeanProxy> {
        @Override
        public void decorate(AdminCommandContext context, ConfigBeanProxy parent, ConfigBeanProxy child)
                throws TransactionFailure, PropertyVetoException {
            // do nothing.
        }
    }
}
