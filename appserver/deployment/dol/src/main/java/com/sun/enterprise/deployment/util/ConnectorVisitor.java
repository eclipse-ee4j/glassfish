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

/*
 * ConnectorVisitor.java
 *
 */

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.ConnectorDescriptor;

import org.jvnet.hk2.annotations.Contract;

/**
 * This class defines a Visitor API for the DOL connector descriptor.
 *
 * @author  Sheetal Vartak
 * @version
 */
@Contract
public interface ConnectorVisitor extends ComponentVisitor {

    /**
     * visits an connector descriptor
     * @param an connector descriptor
     */
    public void accept(ConnectorDescriptor descriptor);

}

