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

import java.util.logging.Formatter;

import org.glassfish.main.jul.cfg.ConfigurationHelper;


/**
 * This is a tool to help with parsing the logging.properties file to configure formatters.
 * <p>
 * It respects JUL configuration standards, so ie. each formatter knows best how to configure
 * itself, but still can use this helper to parse properties directly to objects instead of plain
 * strings.
 * <p>
 * Helper also supports custom error handlers.
 *
 * @author David Matejcek
 */
public class FormatterConfigurationHelper extends ConfigurationHelper {

    /**
     * Creates helper for parsing formatter settings using the handler's formatter attribute.
     *
     * @param handlerId
     * @return instance of the helper which prints errors to the standard error output.
     */
    public static FormatterConfigurationHelper forHandlerId(final HandlerId handlerId) {
        return new FormatterConfigurationHelper(handlerId.getPropertyPrefix() + ".formatter",
            ERROR_HANDLER_PRINT_TO_STDERR);
    }


    /**
     * Creates helper for parsing formatter settings using formatter class name as a prefix.
     *
     * @param formatterClass
     * @return instance of the helper which prints errors to the standard error output.
     */
    public static FormatterConfigurationHelper forFormatterClass(final Class<? extends Formatter> formatterClass) {
        return new FormatterConfigurationHelper(formatterClass.getName(), ERROR_HANDLER_PRINT_TO_STDERR);
    }


    /**
     * @param prefix Usually a canonical class name
     * @param errorHandler
     */
    public FormatterConfigurationHelper(final String prefix, final LoggingPropertyErrorHandler errorHandler) {
        super(prefix, errorHandler);
    }
}
