/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.webui.jsf.util;

/**
 * This class provides a type-safe enumeration of value types (see also
 * ClientTypeEvaluator). The ClientTypeEvaluator and the ClientTypes are helper
 * classes for UIComponents which accept value bindings that can be either
 * single objects or a collection of objects (for example, an array). Typically,
 * these components have to process input differently depending on the type of
 * the value object.
 *
 * @see com.sun.webui.jsf.util.ClientSniffer
 */
public final class ClientType {

    /**
     * Client type.
     */
    private final String type;

    /**
     * Client type is Mozilla 6 or higher.
     */
    public static final ClientType GECKO = new ClientType("gecko");

    /**
     * Client type is IE7 or higher.
     */
    public static final ClientType IE7 = new ClientType("ie7");

    /**
     * Client type is IE6.
     */
    public static final ClientType IE6 = new ClientType("ie6");

    /**
     * Client type is IE 5, version 5.5 or higher.
     */
    public static final ClientType IE5_5 = new ClientType("ie5.5");

    /**
     * Client type is safari.
     */
    public static final ClientType SAFARI = new ClientType("safari");

    /**
     * Client type is not IE 5.5+ or gecko.
     */
    public static final ClientType OTHER = new ClientType("default");

    /**
     * Create a new instance.
     *
     * @param clientType client type
     */
    private ClientType(final String clientType) {
        type = clientType;
    }

    /**
     * Get a String representation of the action.
     *
     * @return A String representation of the value type.
     */
    @Override
    public String toString() {
        return type;
    }
}
