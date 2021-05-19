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

package org.glassfish.ejb.deployment.descriptor;

import java.util.logging.Level;

import com.sun.enterprise.deployment.util.DOLUtils;

/**
 * A dummy implementation of the EjbDescriptor
 *
 */

public class DummyEjbDescriptor extends EjbDescriptor
{
    public DummyEjbDescriptor() {
    }

    @Override
    public void setTransactionType(String transactionType) {
        DOLUtils.getDefaultLogger().log(Level.WARNING, "enterprise.deployment_dummy_set_trans_type", new Object[] {getName()});
    }

    @Override
    public String getContainerFactoryQualifier() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType() {
        return "Dummy";
    }

    @Override
    public void setType(String type) {
        DOLUtils.getDefaultLogger().log(Level.WARNING, "enterprise.deployment_dummy_set_type", new Object[] {getName()});
    }

    @Override
    public String getEjbTypeForDisplay() {
        return "Dummy";
    }
}
