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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides helper methods for logging messages.
 * <p>
 * It uses standard J2SE logging. However, using these API's is abstracts this
 * away from our code in case we want to go back to Apache commons logging or
 * some other logging API in the future.
 * </p>
 *
 * The logging levels follow the J2SE log level names, they are as follows:
 *
 * <UL><LI>FINEST -- Highly detailed tracing message</LI>
 * <LI>FINER -- Fairly detailed tracing message</LI>
 * <LI>FINE -- Coarse tracing message</LI>
 * <LI>CONFIG -- Static configuration messages</LI>
 * <LI>INFO -- Informational messages (logged by default)</LI>
 * <LI>WARNING -- Potentially problematic messages</LI>
 * <LI>SEVERE -- Serious failure messages</LI>
 * </UL>
 */
public final class LogUtil {

    /**
     * Cannot be instanciated.
     */
    private LogUtil() {
    }

    /**
     * This is the bundle name for the {@code ResourceBundle} that contains all
     * the message strings.
     */
    public static final String BUNDLE_NAME
            = "com.sun.webui.jsf.resources.LogMessages";

    /**
     * This is the default log key.
     */
    public static final String DEFAULT_LOG_KEY = "WEBUI0001";

    /**
     * This is the default logger name.
     */
    public static final String DEFAULT_LOGGER_NAME = "com.sun.webui.jsf";

    /**
     * This key is used when the requested key is not found to inform the
     * developer they forgot to add a key.
     */
    public static final String KEY_NOT_FOUND_KEY = "WEBUI0002";

    /**
     * The default Logger.
     */
    private static final Logger DEFAULT_LOGGER = getLogger(DEFAULT_LOGGER_NAME);

    /**
     * This is the separator between the the log key and log message.
     */
    private static final String LOG_KEY_MESSAGE_SEPARATOR = ": ";

    /**
     * Method to check if this log level is enabled for the default logger.
     *
     * @return true if the log level is enabled, false otherwise.
     */
    public static boolean finestEnabled() {
        return getLogger().isLoggable(Level.FINEST);
    }

    /**
     * Method to check if this log level is enabled for the given logger.
     *
     * @param loggerId The logger to check. This may be specified as a String or
     * Class Object.
     *
     * @return true if the log level is enabled, false otherwise.
     */
    public static boolean finestEnabled(final Object loggerId) {
        return getLogger(loggerId).isLoggable(Level.FINEST);
    }

    /**
     * Logging method supporting a localized message key and a single
     * substitution parameter. It will use the default Logger.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param param Value to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void finest(final String msgId, final Object param) {
        finest(msgId, new Object[]{param});
    }

    /**
     * Logging method supporting a localized message key and zero or more
     * substitution parameters. It will use the default Logger.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param params Value(s) to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void finest(final String msgId, final Object[] params) {
        getLogger().log(Level.FINEST, getMessage(msgId, params, false));
    }

    /**
     * Logging method supporting a localized message key, a single substitution
     * parameter, and the ability to specify the Logger.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param param Value to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void finest(final Object loggerId, final String msgId,
            final Object param) {

        finest(loggerId, msgId, new Object[]{param});
    }

    /**
     * Logging method supporting a localized message key, zero or more
     * substitution parameters, and the ability to specify the Logger.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param params Value(s) to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void finest(final Object loggerId, final String msgId,
            final Object[] params) {

        getLogger(loggerId).log(Level.FINEST, getMessage(msgId, params, false));
    }

    /**
     * Logging method to log a simple localized or non-localized message. This
     * method will first attempt to find {@code msg} in the properties file, if
     * not found it will print the given message.
     *
     * @param msg The message (or {@code ResourceBundle} key).
     */
    public static void finest(final String msg) {
        finest(DEFAULT_LOGGER, msg);
    }

    /**
     * Logging method to log a simple localized or non-localized message. This
     * method will first attempt to find {@code msg} in the properties file, if
     * not found it will print the given message. The specified Logger will be
     * used.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msg The message (or {@code ResourceBundle} key).
     */
    public static void finest(final Object loggerId, final String msg) {
        getLogger(loggerId).log(Level.FINEST, getMessage(msg, false));
    }

    /**
     * Logging method to log a {@code Throwable} with a message.
     *
     * @param msg The message.
     * @param ex The {@code Throwable} to log.
     */
    public static void finest(final String msg, final Throwable ex) {
        getLogger().log(Level.FINEST, DEFAULT_LOG_KEY
                + LOG_KEY_MESSAGE_SEPARATOR + msg, ex);
    }

    /**
     * Logging method to log a {@code Throwable} with a message. The specified
     * Logger will be used.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msg The message.
     * @param ex The {@code Throwable} to log.
     */
    public static void finest(final Object loggerId, final String msg,
            final Throwable ex) {

        getLogger(loggerId).
                log(Level.FINEST, DEFAULT_LOG_KEY
                        + LOG_KEY_MESSAGE_SEPARATOR + msg, ex);
    }

    /**
     * Method to check if this log level is enabled for the default logger.
     *
     * @return true if the log level is enabled, false otherwise.
     */
    public static boolean finerEnabled() {
        return getLogger().isLoggable(Level.FINER);
    }

    /**
     * Method to check if this log level is enabled for the given logger.
     *
     * @param loggerId The logger to check. This may be specified as a String or
     * Class Object.
     *
     * @return true if the log level is enabled, false otherwise.
     */
    public static boolean finerEnabled(final Object loggerId) {
        return getLogger(loggerId).isLoggable(Level.FINER);
    }

    /**
     * Logging method supporting a localized message key and a single
     * substitution parameter. It will use the default Logger.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param param Value to substitute into the message.
     *
     * @see LogUtil#BUNDLE_NAME
     */
    public static void finer(final String msgId, final Object param) {
        finer(msgId, new Object[]{param});
    }

    /**
     * Logging method supporting a localized message key and zero or more
     * substitution parameters. It will use the default Logger.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param params Value(s) to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void finer(final String msgId, final Object[] params) {
        getLogger().log(Level.FINER, getMessage(msgId, params, false));
    }

    /**
     * Logging method supporting a localized message key, a single substitution
     * parameter, and the ability to specify the Logger.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param param Value to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void finer(final Object loggerId, final String msgId,
            final Object param) {

        finer(loggerId, msgId, new Object[]{param});
    }

    /**
     * Logging method supporting a localized message key, zero or more
     * substitution parameters, and the ability to specify the Logger.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param params Value(s) to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void finer(final Object loggerId, final String msgId,
            final Object[] params) {

        getLogger(loggerId).log(Level.FINER, getMessage(msgId, params, false));
    }

    /**
     * Logging method to log a simple localized or non-localized message. This
     * method will first attempt to find {@code msg} in the properties file, if
     * not found it will print the given message.
     *
     * @param msg The message (or {@code ResourceBundle} key).
     */
    public static void finer(final String msg) {
        finer(DEFAULT_LOGGER, msg);
    }

    /**
     * Logging method to log a simple localized or non-localized message. This
     * method will first attempt to find {@code msg} in the properties file, if
     * not found it will print the given message. The specified Logger will be
     * used.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msg The message (or {@code ResourceBundle} key).
     */
    public static void finer(final Object loggerId, final String msg) {
        getLogger(loggerId).log(Level.FINER, getMessage(msg, false));
    }

    /**
     * Logging method to log a {@code Throwable} with a message.
     *
     * @param msg The message.
     * @param ex The {@code Throwable} to log.
     */
    public static void finer(final String msg, final Throwable ex) {
        getLogger().
                log(Level.FINER, DEFAULT_LOG_KEY
                        + LOG_KEY_MESSAGE_SEPARATOR + msg, ex);
    }

    /**
     * Logging method to log a {@code Throwable} with a message. The specified
     * Logger will be used.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msg The message.
     * @param ex The {@code Throwable} to log.
     */
    public static void finer(final Object loggerId, final String msg,
            final Throwable ex) {

        getLogger(loggerId).
                log(Level.FINER, DEFAULT_LOG_KEY
                        + LOG_KEY_MESSAGE_SEPARATOR + msg, ex);
    }

    /**
     * Method to check if this log level is enabled for the default logger.
     *
     * @return true if the log level is enabled, false otherwise.
     */
    public static boolean fineEnabled() {
        return getLogger().isLoggable(Level.FINE);
    }

    /**
     * Method to check if this log level is enabled for the given logger.
     *
     * @param loggerId The logger to check. This may be specified as a String or
     * Class Object.
     *
     * @return true if the log level is enabled, false otherwise.
     */
    public static boolean fineEnabled(final Object loggerId) {
        return getLogger(loggerId).isLoggable(Level.FINE);
    }

    /**
     * Logging method supporting a localized message key and a single
     * substitution parameter. It will use the default Logger.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param param Value to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void fine(final String msgId, final Object param) {
        fine(msgId, new Object[]{param});
    }

    /**
     * Logging method supporting a localized message key and zero or more
     * substitution parameters. It will use the default Logger.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param params Value(s) to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void fine(final String msgId, final Object[] params) {
        getLogger().log(Level.FINE, getMessage(msgId, params, false));
    }

    /**
     * Logging method supporting a localized message key, a single substitution
     * parameter, and the ability to specify the Logger.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param param Value to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void fine(final Object loggerId, final String msgId,
            final Object param) {

        fine(loggerId, msgId, new Object[]{param});
    }

    /**
     * Logging method supporting a localized message key, zero or more
     * substitution parameters, and the ability to specify the Logger.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param params Value(s) to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void fine(final Object loggerId, final String msgId,
            final Object[] params) {

        getLogger(loggerId).log(Level.FINE, getMessage(msgId, params, false));
    }

    /**
     * Logging method to log a simple localized or non-localized message. This
     * method will first attempt to find {@code msg} in the properties file, if
     * not found it will print the given message.
     *
     * @param msg The message (or {@code ResourceBundle} key).
     */
    public static void fine(final String msg) {
        fine(DEFAULT_LOGGER, msg);
    }

    /**
     * Logging method to log a simple localized or non-localized message. This
     * method will first attempt to find {@code msg} in the properties file, if
     * not found it will print the given message. The specified Logger will be
     * used.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     *
     * @param msg The message (or {@code ResourceBundle} key).
     */
    public static void fine(final Object loggerId, final String msg) {
        getLogger(loggerId).log(Level.FINE, getMessage(msg, false));
    }

    /**
     * Logging method to log a {@code Throwable} with a message.
     *
     * @param msg The message.
     * @param ex The {@code Throwable} to log.
     */
    public static void fine(final String msg, final Throwable ex) {
        getLogger().log(Level.FINE, getMessage(msg, false), ex);
    }

    /**
     * Logging method to log a {@code Throwable} with a message. The specified
     * Logger will be used.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msg The message.
     * @param ex The {@code Throwable} to log.
     */
    public static void fine(final Object loggerId, final String msg,
            final Throwable ex) {

        getLogger(loggerId).log(Level.FINE, getMessage(msg, false), ex);
    }

    /**
     * Method to check if this log level is enabled for the default logger.
     *
     * @return true if the log level is enabled, false otherwise.
     */
    public static boolean configEnabled() {
        return getLogger().isLoggable(Level.CONFIG);
    }

    /**
     * Method to check if this log level is enabled for the given logger.
     *
     * @param loggerId The logger to check. This may be specified as a String or
     * Class Object.
     * @return true if the log level is enabled, false otherwise.
     */
    public static boolean configEnabled(final Object loggerId) {
        return getLogger(loggerId).isLoggable(Level.CONFIG);
    }

    /**
     * Logging method supporting a localized message key and a single
     * substitution parameter. It will use the default Logger.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param param Value to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void config(final String msgId, final Object param) {
        config(msgId, new Object[]{param});
    }

    /**
     * Logging method supporting a localized message key and zero or more
     * substitution parameters. It will use the default Logger.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     *
     * @param params Value(s) to substitute into the message.
     *
     * @see LogUtil#BUNDLE_NAME
     */
    public static void config(final String msgId, final Object[] params) {
        getLogger().log(Level.CONFIG, getMessage(msgId, params, false));
    }

    /**
     * Logging method supporting a localized message key, a single substitution
     * parameter, and the ability to specify the Logger.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param param Value to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void config(final Object loggerId, final String msgId,
            final Object param) {

        config(loggerId, msgId, new Object[]{param});
    }

    /**
     * Logging method supporting a localized message key, zero or more
     * substitution parameters, and the ability to specify the Logger.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param params Value(s) to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void config(final Object loggerId, final String msgId,
            final Object[] params) {

        getLogger(loggerId).log(Level.CONFIG, getMessage(msgId, params, false));
    }

    /**
     * Logging method to log a simple localized or non-localized message. This
     * method will first attempt to find {@code msg} in the properties file, if
     * not found it will print the given msg.
     *
     * @param msg The message (or {@code ResourceBundle} key).
     */
    public static void config(final String msg) {
        config(DEFAULT_LOGGER, msg);
    }

    /**
     * Logging method to log a simple localized or non-localized message. This
     * method will first attempt to find {@code msg} in the properties file, if
     * not found it will print the given msg. The specified Logger will be used.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msg The message (or {@code ResourceBundle} key).
     */
    public static void config(final Object loggerId, final String msg) {
        getLogger(loggerId).log(Level.CONFIG, getMessage(msg, false));
    }

    /**
     * Logging method to log a {@code Throwable} with a message.
     *
     * @param msg The message.
     * @param ex The {@code Throwable} to log.
     */
    public static void config(final String msg, final Throwable ex) {
        getLogger().log(Level.CONFIG, getMessage(msg, false), ex);
    }

    /**
     * Logging method to log a {@code Throwable} with a message. The specified
     * Logger will be used.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msg The message.
     * @param ex The {@code Throwable} to log.
     */
    public static void config(final Object loggerId, final String msg,
            final Throwable ex) {

        getLogger(loggerId).log(Level.CONFIG, getMessage(msg, false), ex);
    }

    /**
     * Method to check if this log level is enabled for the default logger.
     *
     * @return true if the log level is enabled, false otherwise.
     */
    public static boolean infoEnabled() {
        return getLogger().isLoggable(Level.INFO);
    }

    /**
     * Method to check if this log level is enabled for the given logger.
     *
     * @param loggerId The logger to check. This may be specified as a String or
     * Class Object.
     * @return true if the log level is enabled, false otherwise.
     */
    public static boolean infoEnabled(final Object loggerId) {
        return getLogger(loggerId).isLoggable(Level.INFO);
    }

    /**
     * Logging method to log a simple localized message. The default Logger will
     * be used.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void info(final String msgId) {
        getLogger().log(Level.INFO, getMessage(msgId, true));
    }

    /**
     * Logging method to log a simple localized message. The specified Logger
     * will be used.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void info(final Object loggerId, final String msgId) {
        getLogger(loggerId).log(Level.INFO, getMessage(msgId, true));
    }

    /**
     * Logging method supporting a localized message key and a single
     * substitution parameter. It will use the default Logger.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param param Value to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void info(final String msgId, final Object param) {
        info(msgId, new Object[]{param});
    }

    /**
     * Logging method supporting a localized message key and zero or more
     * substitution parameters. It will use the default Logger.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param params Value(s) to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void info(final String msgId, final Object[] params) {
        getLogger().log(Level.INFO, getMessage(msgId, params, true));
    }

    /**
     * Logging method supporting a localized message key, a single substitution
     * parameter, and the ability to specify the Logger.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param param Value to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void info(final Object loggerId, final String msgId,
            final Object param) {

        info(loggerId, msgId, new Object[]{param});
    }

    /**
     * Logging method supporting a localized message key, zero or more
     * substitution parameters, and the ability to specify the Logger.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param params Value(s) to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void info(final Object loggerId, final String msgId,
            final Object[] params) {

        getLogger(loggerId).log(Level.INFO, getMessage(msgId, params, true));
    }

    /**
     * Logging method to log a {@code Throwable} with a localized message.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param ex The {@code Throwable} to log.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void info(final String msgId, final Throwable ex) {
        getLogger().log(Level.INFO, getMessage(msgId, false), ex);
    }

    /**
     * Logging method to log a {@code Throwable} with a localized message. The
     * specified Logger will be used.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param ex The {@code Throwable} to log.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void info(final Object loggerId, final String msgId,
            final Throwable ex) {

        getLogger(loggerId).log(Level.INFO, getMessage(msgId, false), ex);
    }

    /**
     * Method to check if this log level is enabled for the default logger.
     *
     * @return true if the log level is enabled, false otherwise.
     */
    public static boolean warningEnabled() {
        return getLogger().isLoggable(Level.WARNING);
    }

    /**
     * Method to check if this log level is enabled for the given logger.
     *
     * @param loggerId The logger to check. This may be specified as a String or
     * Class Object.
     * @return true if the log level is enabled, false otherwise.
     */
    public static boolean warningEnabled(final Object loggerId) {
        return getLogger(loggerId).isLoggable(Level.WARNING);
    }

    /**
     * Logging method to log a simple localized message. The default Logger will
     * be used.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void warning(final String msgId) {
        getLogger().log(Level.WARNING, getMessage(msgId, true));
    }

    /**
     * Logging method to log a simple localized message. The specified Logger
     * will be used.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void warning(final Object loggerId, final String msgId) {
        getLogger(loggerId).log(Level.WARNING, getMessage(msgId, true));
    }

    /**
     * Logging method supporting a localized message key and a single
     * substitution parameter. It will use the default Logger.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param param Value to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void warning(final String msgId, final Object param) {
        warning(msgId, new Object[]{param});
    }

    /**
     * Logging method supporting a localized message key and zero or more
     * substitution parameters. It will use the default Logger.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param params Value(s) to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void warning(final String msgId, final Object[] params) {
        getLogger().log(Level.WARNING, getMessage(msgId, params, true));
    }

    /**
     * Logging method supporting a localized message key, a single substitution
     * parameter, and the ability to specify the Logger.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param param Value to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void warning(final Object loggerId, final String msgId,
            final Object param) {

        warning(loggerId, msgId, new Object[]{param});
    }

    /**
     * Logging method supporting a localized message key, zero or more
     * substitution parameters, and the ability to specify the Logger.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param params Value(s) to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void warning(final Object loggerId, final String msgId,
            final Object[] params) {

        getLogger(loggerId).log(Level.WARNING, getMessage(msgId, params, true));
    }

    /**
     * Logging method to log a {@code Throwable} with a localized message.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param ex The {@code Throwable} to log.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void warning(final String msgId, final Throwable ex) {
        getLogger().log(Level.WARNING, getMessage(msgId, false), ex);
    }

    /**
     * Logging method to log a {@code Throwable} with a localized message. The
     * specified Logger will be used.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param ex The {@code Throwable} to log.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void warning(final Object loggerId, final String msgId,
            final Throwable ex) {

        getLogger(loggerId).log(Level.WARNING, getMessage(msgId, false), ex);
    }

    /**
     * Method to check if this log level is enabled for the default logger.
     *
     * @return true if the log level is enabled, false otherwise.
     */
    public static boolean severeEnabled() {
        return getLogger().isLoggable(Level.SEVERE);
    }

    /**
     * Method to check if this log level is enabled for the given logger.
     *
     * @param loggerId The logger to check. This may be specified as a String or
     * Class Object.
     * @return true if the log level is enabled, false otherwise.
     */
    public static boolean severeEnabled(final Object loggerId) {
        return getLogger(loggerId).isLoggable(Level.SEVERE);
    }

    /**
     * Logging method to log a simple localized message. The default Logger will
     * be used.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void severe(final String msgId) {
        getLogger().log(Level.SEVERE, getMessage(msgId, true));
    }

    /**
     * Logging method to log a simple localized message. The specified Logger
     * will be used.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void severe(final Object loggerId, final String msgId) {
        getLogger(loggerId).log(Level.SEVERE, getMessage(msgId, true));
    }

    /**
     * Logging method supporting a localized message key and a single
     * substitution parameter. It will use the default Logger.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param param Value to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void severe(final String msgId, final Object param) {
        severe(msgId, new Object[]{param});
    }

    /**
     * Logging method supporting a localized message key and zero or more
     * substitution parameters. It will use the default Logger.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param params Value(s) to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void severe(final String msgId, final Object[] params) {
        getLogger().log(Level.SEVERE, getMessage(msgId, params, true));
    }

    /**
     * Logging method supporting a localized message key, a single substitution
     * parameter, and the ability to specify the Logger.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param param Value to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void severe(final Object loggerId, final String msgId,
            final Object param) {

        severe(loggerId, msgId, new Object[]{param});
    }

    /**
     * Logging method supporting a localized message key, zero or more
     * substitution parameters, and the ability to specify the Logger.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param params Value(s) to substitute into the message.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void severe(final Object loggerId, final String msgId,
            final Object[] params) {

        getLogger(loggerId).log(Level.SEVERE, getMessage(msgId, params, true));
    }

    /**
     * Logging method to log a {@code Throwable} with a localized message.
     *
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param ex The {@code Throwable} to log.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void severe(final String msgId, final Throwable ex) {
        getLogger().log(Level.SEVERE, getMessage(msgId, false), ex);
    }

    /**
     * Logging method to log a {@code Throwable} with a localized message. The
     * specified Logger will be used.
     *
     * @param loggerId The logger to use. This may be specified as a String or
     * Class Object.
     * @param msgId The {@code ResourceBundle} key used to lookup the message.
     * @param ex The {@code Throwable} to log.
     * @see LogUtil#BUNDLE_NAME
     */
    public static void severe(final Object loggerId, final String msgId,
            final Throwable ex) {

        getLogger(loggerId).log(Level.SEVERE, getMessage(msgId, false), ex);
    }

    /**
     * This method provides direct access to the default Logger. It is private
     * because the internals of what type of logger is being used should not be
     * exposed outside this class.
     * @return Logger
     */
    private static Logger getLogger() {
        return DEFAULT_LOGGER;
    }

    /**
     * This method provides direct access to the Logger. It is private because
     * the internals of what type of logger is being used should not be exposed
     * outside this class.
     *
     * This method will return the default logger (if INFO), or delegate to
     * {@link getLogger(String)} or {@link getLogger(Class)}.
     *
     * @param key The logger to use as specified by the Object.
     * @return Logger
     */
    private static Logger getLogger(final Object key) {
        // If null, use default
        if (key == null) {
            return getLogger();
        }

        // If string, use it as logger name
        if (key instanceof String) {
            return getLogger((String) key);
        }

        // If Log, return it
        if (key instanceof Logger) {
            return (Logger) key;
        }

        // If class, use it
        if (key instanceof Class) {
            return getLogger((Class) key);
        }

        // else, use the class name
        return getLogger(key.getClass());
    }

    /**
     * This method provides direct access to the Logger. It is private because
     * the internals of what type of logger is being used should not be exposed
     * outside this class.
     *
     * @param key The logger to use as specified by the String.
     * @return Logger
     */
    private static Logger getLogger(final String key) {
        if (key.trim().length() == 0) {
            return DEFAULT_LOGGER;
        }
        return Logger.getLogger(key);
    }

    /**
     * This method provides direct access to the Logger. It is private because
     * the internals of what type of logger is being used should not be exposed
     * outside this class.
     *
     * @param key The logger to use as specified by the Class.
     * @return Logger
     */
    private static Logger getLogger(final Class key) {
        if (key == null) {
            return DEFAULT_LOGGER;
        }
        return Logger.getLogger(key.getName());
    }

    /**
     * This method gets the appropriate message to display. It will attempt to
     * resolve it from the {@code ResourceBundle}. If not found and it will
     * either use the message id as the key, or it will return an error message
     * depending on whether {@code strict} is true or false.
     *
     * @param msgId The message key
     * @param strict True if key not found should be an error
     * @return The message to write to the log file.
     */
    private static String getMessage(final String msgId, final boolean strict) {
        return getMessage(msgId, new Object[0], strict);
    }

    /**
     * This method gets the appropriate message to display. It will attempt to
     * resolve it from the {@code ResourceBundle}. If not found and it will
     * either use the message id as the key, or it will return an error message
     * depending on whether {@code strict} is true or false.
     *
     * @param msgId The message key
     * @param params The parameters
     * @param strict True if key not found should be an error
     * @return The message to write to the log file.
     */
    private static String getMessage(final String msgId, final Object[] params,
            final boolean strict) {

        String result = MessageUtil.getMessage(BUNDLE_NAME, msgId, params);
        if (result.equals(msgId)) {
            // We didn't find the key...
            if (strict) {
                // A key is required, return an error message
                if (msgId.equals(KEY_NOT_FOUND_KEY)) {
                    // This is here to prevent and infinite loop
                    result = KEY_NOT_FOUND_KEY + LOG_KEY_MESSAGE_SEPARATOR
                            + "'" + params[0]
                            + "' not found in ResourceBundle: '"
                            + BUNDLE_NAME + "'";
                } else {
                    result = getMessage(
                            KEY_NOT_FOUND_KEY, new Object[]{msgId}, strict);
                }
            } else {
                // Use the msgId as the message, use the default key
                result = DEFAULT_LOG_KEY + LOG_KEY_MESSAGE_SEPARATOR + msgId;
            }
        } else {
            // We found the key, construct the log format...
            result = msgId + LOG_KEY_MESSAGE_SEPARATOR + result;
        }

        // Return the formatted result
        return result;
    }

}
