/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.beans.VetoableChangeSupport;

/**
 * Support class for sending vetoable events.
 *
 * @author Jerome Dochez
 */
public class TypedVetoableChangeSupport extends VetoableChangeSupport {

    final private Object source;

    public TypedVetoableChangeSupport(Object sourceBean) {
        super(sourceBean);
        source = sourceBean;
    }

    public void fireVetoableChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException {

        super.fireVetoableChange(new TypedChangeEvent(source, propertyName, oldValue, newValue));
    }
}
