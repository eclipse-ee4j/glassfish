/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.deployment.util;

import com.sun.enterprise.deployment.util.ComponentVisitor;

import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;


/**
 * This class is responsible for visiting DOL ejb related descriptors
 *
 * @author Jerome Dochez 2002
 */
public interface EjbVisitor extends ComponentVisitor {

    /**
     * Visits an ejb descriptor
     *
     * @param ejb descriptor
     */
    void accept(EjbDescriptor ejb);
}
