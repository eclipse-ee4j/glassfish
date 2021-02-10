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

package org.glassfish.api.admin;

/**
 * Defines a server process type.
 *
 * @author Jerome Dochez
 */
public enum RuntimeType {
    /**
     * the Domain Administration Server
     */
    DAS,

    /**
     * the node agent process
     */
    NODE_AGENT,

    /**
     * a single instance or a clustered instance ?need to dissambiguate?
     */
    INSTANCE,

    /**
     * Embedded
     */

    EMBEDDED,

    /**
     * The local single instance on which the command will run
     */
    SINGLE_INSTANCE,

    /**
     * All instances in the domain
     */
    ALL;

    public final boolean isInstance() {
        return this == INSTANCE;
    }

    public final boolean isDas() {
        return this == DAS;
    }

    public final boolean isNodeAgent() {
        return this == NODE_AGENT;
    }

    public final boolean isEmbedded() {
        return this == EMBEDDED;
    }

    public final boolean isBroadcast() {
        return this == ALL;
    }

    public final boolean isSingleInstance() {
        return this == SINGLE_INSTANCE;
    }

    public final static RuntimeType getDefault() {
        return DAS;
    }
}
