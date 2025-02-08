/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.main.jul.record;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.main.jul.env.LoggingSystemEnvironment;

/**
 * This class is used to resolve {@link LogRecord}'s message:
 * <ol>
 * <li>to internationalize the message if the resource bundle is set
 * <li>to construct final message using record's parameters
 * <li>to return {@link GlassFishLogRecord} providing additional items usable in logs
 * </ol>
 *
 * @author David Matejcek
 */
public final class MessageResolver {

    private final LogManager manager;

    /**
     * Searches for {@link LogManager} valid in current context.
     * Does not have any other internal state.
     */
    public MessageResolver() {
        this.manager = LogManager.getLogManager();
    }


    /**
     * Resolves the original record to {@link GlassFishLogRecord} so it contains the final log
     * message. Then sets record's resource bundle and parameters to null to avoid repeating same
     * resolution again.
     * <br>
     * It also detects that the record is already resolved, then returns it without any change.
     *
     * @param record
     * @return {@link GlassFishLogRecord} with final log message.
     */
    public GlassFishLogRecord resolve(final LogRecord record) {
        if (record == null) {
            return null;
        }
        final GlassFishLogRecord enhancedLogRecord = toGlassFishLogRecord(record);
        if (isAlreadyResolved(enhancedLogRecord)) {
            return enhancedLogRecord;
        }
        final ResolvedLogMessage message = resolveMessage(record);
        enhancedLogRecord.setMessageKey(message.key);
        enhancedLogRecord.setMessage(message.message);
        // values were used and they are not required any more.
        // not only this, it is good to even avoid their usage as JUL implementation does it.
        // We also touch them in isAlreadyResolved to avoid redundant work.
        enhancedLogRecord.setResourceBundle(null);
        enhancedLogRecord.setResourceBundleName(null);
        if (LoggingSystemEnvironment.isReleaseParametersEarly()) {
            enhancedLogRecord.setParameters(null);
        }
        return enhancedLogRecord;
    }


    private GlassFishLogRecord toGlassFishLogRecord(final LogRecord record) {
        if (GlassFishLogRecord.class.isInstance(record)) {
            return (GlassFishLogRecord) record;
        }
        return new GlassFishLogRecord(record);
    }


    private boolean isAlreadyResolved(final GlassFishLogRecord record) {
        // can be set only in resolve method
        if (record.getMessageKey() != null) {
            return true;
        }
        // resolve method sets all of them to null
        return record.getResourceBundle() == null && record.getResourceBundleName() == null
            && record.getParameters() == null;
    }


    /**
     * This is a mechanism extracted from the StreamHandler and extended.
     * If the message is loggable should be decided before creation of this instance to avoid
     * resolving a message which would not be used. And it is in done - in
     * {@link Logger#log(LogRecord)} and in {@link #publish(LogRecord)}
     */
    private ResolvedLogMessage resolveMessage(final LogRecord record) {
        final String originalMessage = record.getMessage();
        if (originalMessage == null || originalMessage.isEmpty()) {
            return new ResolvedLogMessage(null, originalMessage);
        }
        final ResourceBundle bundle = getResourceBundle(record.getResourceBundle(), record.getLoggerName());
        final ResolvedLogMessage localizedTemplate = tryToLocalizeTemplate(originalMessage, bundle);
        final Object[] parameters = record.getParameters();
        if (parameters == null || parameters.length == 0) {
            return localizedTemplate;
        }
        final String localizedMessage = toMessage(localizedTemplate.message, parameters);
        return new ResolvedLogMessage(localizedTemplate.key, localizedMessage);
    }


    private ResourceBundle getResourceBundle(final ResourceBundle bundle, final String loggerName) {
        // anonymous logger does not have any rb, and even causes NPE in JUL's LogManager
        if (bundle != null || loggerName == null) {
            return bundle;
        }
        final Logger logger = this.manager.getLogger(loggerName);
        return logger == null ? null : logger.getResourceBundle();
    }


    private ResolvedLogMessage tryToLocalizeTemplate(final String originalMessage, final ResourceBundle bundle) {
        if (bundle == null) {
            return new ResolvedLogMessage(null, originalMessage);
        }
        try {
            final String localizedMessage = bundle.getString(originalMessage);
            return new ResolvedLogMessage(originalMessage, localizedMessage);
        } catch (final MissingResourceException e) {
            return new ResolvedLogMessage(null, originalMessage);
        }
    }


    private String toMessage(final String template, final Object[] parameters) {
        try {
            return MessageFormat.format(template, parameters);
        } catch (final Exception e) {
            return template;
        }
    }

    /**
     * Bind the message and it's bundle key.
     */
    private static final class ResolvedLogMessage {

        private final String key;
        private final String message;

        ResolvedLogMessage(final String key, final String message) {
            this.key = key;
            this.message = message;
        }


        /**
         * Returns key:message
         */
        @Override
        public String toString() {
            return key + ":" + message;
        }
    }

}
