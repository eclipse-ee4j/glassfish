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

package org.glassfish.main.jul.handler;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;

import org.glassfish.main.jul.cfg.LogProperty;

/**
 * Configuration property set of the {@link ConsoleHandler}.
 *
 * @author David Matejcek
 */
public enum ConsoleHandlerProperty implements LogProperty {
    /** Name of a Filter class to use (defaults to no Filter). */
    FILTER("filter"),
    /** Name of the character set encoding to use (defaults to the default platform encoding). */
    ENCODING("encoding"),
    /** Class of the {@link Formatter} used with this handler */
    FORMATTER(HandlerConfigurationHelper.FORMATTER.getPropertyName()),
    /** The default level for the Handler (defaults to {@link Level#ALL}). */
    LEVEL("level"),
    ;

    private final String propertyName;

    ConsoleHandlerProperty(final String propertyName) {
        this.propertyName = propertyName;
    }


    @Override
    public String getPropertyName() {
        return propertyName;
    }


    /**
     * @return full name using the {@link ConsoleHandler} class.
     */
    public String getPropertyFullName() {
        return getPropertyFullName(ConsoleHandler.class);
    }
}
