/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.common.util;

/**
 * These are constants that can be used throughout the server
 *
 * @author jwells
 */
public class Constants {
    /**
     * This constant should be used whenever there are multiple implementations
     * of an &#64;Contract and one of them should be the default implementation
     * that people should get if they have no other distinguising information
     * in their lookup or &#64;Inject point.
     */
    public final static int DEFAULT_IMPLEMENTATION_RANK = 50;

    /**
     * This constant should be used as the rank of an important
     * start level service (one that should be initialized generally
     * ahead of other services at the same run-level)
     */
    public final static int IMPORTANT_RUN_LEVEL_SERVICE = 50;

}
