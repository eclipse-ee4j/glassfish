/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.logging;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Class providing helper APIs for logging purposes.
 *
 */
public final class LogHelper {

    /**
     * Logs a message with the given level, message, parameters and <code>Throwable</code>.
     *
     * @param logger the <code>Logger</code> object to be used for logging the message.
     * @param level the <code>Level</code> of the message to be logged.
     * @param messageId the key in the resource bundle of the <code>Logger</code> containing the localized text.
     * @param thrown the <code>Throwable</code> associated with the message to be logged.
     * @param params the parameters to the localized text.
     */
    public static void log(Logger logger, Level level, String messageId, Throwable thrown, Object... params) {
        LogRecord rec = new LogRecord(level, messageId);
        rec.setLoggerName(logger.getName());
        rec.setResourceBundleName(logger.getResourceBundleName());
        rec.setResourceBundle(logger.getResourceBundle());
        rec.setParameters(params);
        rec.setThrown(thrown);
        logger.log(rec);
    }

    /**
     * Gets the formatted message given the message key and parameters. The ResourceBundle associated with the logger is
     * searched for the specified key.
     *
     * @param logger
     * @param msgKey
     * @param params
     * @return
     */
    public static String getFormattedMessage(Logger logger, String msgKey, Object... params) {
        ResourceBundle rb = logger.getResourceBundle();
        if (rb != null) {
            try {
                return MessageFormat.format(rb.getString(msgKey), params);
            } catch (java.util.MissingResourceException e) {
                return msgKey;
            }
        }
        return msgKey;
    }

}
