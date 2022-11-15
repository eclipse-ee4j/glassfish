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

package org.glassfish.api.naming;

import org.jvnet.hk2.annotations.Contract;

/**
 * A contract that describes a binding in the component namespace
 */

@Contract
public interface JNDIBinding {

    /**
     * Returns the logical name in the component namespace
     *
     * @return the logical name in the component namespace
     */
    SimpleJndiName getName();

    /**
     * Returns the value to be bound against the logical name
     *
     * @return the value to be bound against the logical name
     */
    Object getValue();

}
