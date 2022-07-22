/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul.formatter;

import java.util.logging.Handler;

/**
 * HandlerId holds a handler id used in logging.properties. The id is used as a key prefix of all
 * handler's properties. Usually it is a full class name of the handler implementation.
 *
 * @author David Matejcek
 */
public class HandlerId {

    private final String name;

    private HandlerId(final String name) {
        this.name = name;
    }


    /**
     * @return name of the handler, usually same as the class name
     */
    public String getName() {
        return this.name;
    }


    /**
     * @return prefix of all properties opf the handler, usually handler's class name.
     */
    public String getPropertyPrefix() {
        return this.name;
    }


    /**
     * Returns name of the handler.
     */
    @Override
    public String toString() {
        return getName();
    }


    /**
     * Creates a {@link HandlerId} instance for handler's class.
     *
     * @param handlerClass
     * @return {@link HandlerId}, never null.
     */
    public static HandlerId forHandlerClass(final Class<? extends Handler> handlerClass) {
        return new HandlerId(handlerClass.getName());
    }
}
