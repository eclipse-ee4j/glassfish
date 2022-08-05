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

package org.glassfish.main.jul.cfg;

/**
 * Properties used directly by the {@link org.glassfish.main.jul.GlassFishLogManager}
 *
 * @author David Matejcek
 */
public enum GlassFishLogManagerProperty implements LogProperty {

    /** Property key for a list of root handler implementations */
    KEY_ROOT_HANDLERS("handlers"),
    /**
     * Property key for a level of user root logger. User root loggers children can have own level.
     */
    KEY_USR_ROOT_LOGGER_LEVEL(".level"),
    /**
     * Property key for a level of system root logger. System root loggers children are not
     * configurable.
     */
    KEY_SYS_ROOT_LOGGER_LEVEL("systemRootLogger.level"),
    /**
     * Property key for a boolean value enabling forgetting log record parameters right after
     * the message is resolved. If false, parameters are set in the log record until the record
     * is processed by a formatter which can still use them. However they can change their internal
     * state while the record was waiting for processing in some of buffers.
     * <p>
     * Releasing them when they are not used may help performance (depends on type of the load).
     */
    KEY_RELEASE_PARAMETERS_EARLY("org.glassfish.main.jul.record.releaseParametersEarly"),
    /**
     * Property key for a boolean value enabling log record level resolution even when the logging
     * is configured just partially.
     * It can save some time and memory, on the other hand some verbose log records not passing
     * currently set log levels will be lost (as in all releases before GlassFish 7)
     */
    KEY_RESOLVE_LEVEL_WITH_INCOMPLETE_CONFIGURATION("org.glassfish.main.jul.record.resolveLevelWithIncompleteConfiguration"),;

    private final String propertyName;

    GlassFishLogManagerProperty(final String propertyName) {
        this.propertyName = propertyName;
    }


    @Override
    public String getPropertyName() {
        return propertyName;
    }


    /**
     * @return the same as {@link #getPropertyName()}
     * @deprecated use {@link #getPropertyName()}, this enum cannot relativize to a class.
     */
    @Override
    @Deprecated
    public String getPropertyFullName(final Class<?> bussinessObjectClass) {
        return getPropertyName();
    }
}
