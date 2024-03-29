/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment;

import org.glassfish.api.naming.SimpleJndiName;

/**
 * Objects implementing this interface have a JNDI
 * name property.
 *
 * @author Danny Coward
 */
public interface NamedDescriptor {

    /**
     * The name of this descriptor.
     *
     * @return java.lang.String name
     */
    String getName();

    /**
     * Returns the JNDI name property of the receiver.
     *
     * @return java.lang.String JNDI name
     */
    SimpleJndiName getJndiName();

    /**
     * Sets the JNDI name property of the reciever as a String.
     *
     * @param jndiName the new JNDI name of the receiver.
     */
    void setJndiName(SimpleJndiName jndiName);

}
