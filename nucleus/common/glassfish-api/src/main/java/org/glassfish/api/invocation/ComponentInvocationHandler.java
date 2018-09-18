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

package org.glassfish.api.invocation;

import static org.glassfish.api.invocation.ComponentInvocation.ComponentInvocationType;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * ComponentInvocationHandler handles pre and post activities for a particular
 *  type of ComponentInvocation
 *
 * @author Mahesh Kannan
 *
 */
@Contract
public interface ComponentInvocationHandler {

    /**
     * Called <b>before</b> the curInv is pushed into the invocation stack.
     *
     * @param invType
     * @param prevInv
     * @param newInv
     * @throws InvocationException
     */
    public void beforePreInvoke(ComponentInvocationType invType,
                          ComponentInvocation prevInv, ComponentInvocation newInv)
        throws InvocationException;

    /**
     * Called <b>after</b> the curInv has been pushed into the invocation stack.
     *
     * @param invType
     * @param prevInv
     * @param curInv
     * @throws InvocationException
     */
    public void afterPreInvoke(ComponentInvocationType invType,
                          ComponentInvocation prevInv, ComponentInvocation curInv)
        throws InvocationException;

    /**
     * Called <b>before</b> the curInv has been popped from the invocation stack.
     *
     * @param invType
     * @param prevInv
     * @param curInv
     * @throws InvocationException
     */
    public void beforePostInvoke(ComponentInvocationType invType,
            ComponentInvocation prevInv, ComponentInvocation curInv)
        throws InvocationException;

    /**
     * Called <b>after</b> the curInv has been popped from the invocation stack.
     *
     * @param invType
     * @param prevInv
     * @param curInv
     * @throws InvocationException
     */
    public void afterPostInvoke(ComponentInvocationType invType,
            ComponentInvocation prevInv, ComponentInvocation curInv)
        throws InvocationException;

}
