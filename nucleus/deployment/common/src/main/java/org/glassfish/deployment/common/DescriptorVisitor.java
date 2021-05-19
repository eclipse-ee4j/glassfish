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

package org.glassfish.deployment.common;


/**
 * This class defines the minimum visitor API each descriptor
 * is required to implement.
 *
 * @author  Jerome Dochez
 */
public interface DescriptorVisitor {

    /**
     * visits a J2EE descriptor
     * @param descriptor descriptor to visit
     */
    public void accept(Descriptor descriptor);

    /**
     * get the visitor for its sub descriptor
     * @param sub descriptor to return visitor for
     */
    public DescriptorVisitor getSubDescriptorVisitor(Descriptor subDescriptor);
}
