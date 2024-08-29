/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.glassfish.bootstrap.log;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

public class LogFacade {

    @LoggerInfo(subsystem = "BOOTSTRAP", description="Main bootstrap logger.")
    public static final String BOOTSTRAP_LOGGER_NAME = "jakarta.enterprise.bootstrap";

    @LogMessagesResourceBundle()
    public static final String RB_NAME = "com.sun.enterprise.glassfish.bootstrap.LogMessages";

    public static final Logger BOOTSTRAP_LOGGER = Logger.getLogger(BOOTSTRAP_LOGGER_NAME, RB_NAME);

    @LogMessageInfo(
            message = "GlassFish requires JDK {0}, you are using JDK version {1}.",
            level = "SEVERE",
            cause="Incorrect JDK version is used.",
            action="Please use correct JDK version.")
    public static final String BOOTSTRAP_INCORRECT_JDKVERSION = "NCLS-BOOTSTRAP-00001";

    @LogMessageInfo(
            message = "Using {0} as the framework configuration file.",
            level = "INFO")
    public static final String BOOTSTRAP_FMWCONF = "NCLS-BOOTSTRAP-00002";

    @LogMessageInfo(
            message = "Can not start bundle {0} because it is not contained in the list of installed bundles.",
            level = "WARNING")
    public static final String CANT_START_BUNDLE = "NCLS-BOOTSTRAP-00005";

    @LogMessageInfo(
            message = "Failed to start {0}.",
            level = "WARNING")
    public static final String BUNDLE_START_FAILED = "NCLS-BOOTSTRAP-00006";

    @LogMessageInfo(
            message = "Can't uninstall bundle = {0} as it's already uninstalled.",
            level = "WARNING")
    public static final String BUNDLE_ALREADY_UNINSTALED = "NCLS-BOOTSTRAP-00007";

    @LogMessageInfo(
            message = "Uninstalled bundle {0} installed from {1}.",
            level = "INFO")
    public static final String UNINSTALLED_BUNDLE = "NCLS-BOOTSTRAP-00008";

    @LogMessageInfo(
            message = "Can't update bundle = {0} as it's already uninstalled.",
            level = "WARNING")
    public static final String CANT_UPDATE_ALREADY_INSTALLED = "NCLS-BOOTSTRAP-00009";

    @LogMessageInfo(
            message = "Updated bundle {0} from {1}.",
            level = "INFO")
    public static final String BUNDLE_UPDATED = "NCLS-BOOTSTRAP-00010";

    @LogMessageInfo(
            message = "Failed to uninstall bundle {0}.",
            level = "WARNING")
    public static final String BUNDLE_UNINSTALL_FAILED = "NCLS-BOOTSTRAP-00011";

    @LogMessageInfo(
            message = "Failed to update {0}.",
            level = "WARNING")
    public static final String UPDATE_FAILED = "NCLS-BOOTSTRAP-00012";

    @LogMessageInfo(
            message = "Failed to install {0}.",
            level = "WARNING")
    public static final String INSTALL_FAILED = "NCLS-BOOTSTRAP-00013";

    @LogMessageInfo(
            message = "Can not set the start level for {0} to {2} as it is already set to {1}.",
            level = "WARNING")
    public static final String CANT_SET_START_LEVEL = "NCLS-BOOTSTRAP-00014";

    @LogMessageInfo(
            message = "Skipping entry {0} because it is not an absolute URI.",
            level = "WARNING")
    public static final String ENTRY_SKIPPED = "NCLS-BOOTSTRAP-00015";

    @LogMessageInfo(
            message = "Skipping entry {0} due to exception: ",
            level = "WARNING")
    public static final String ENTRY_SKIPPED_DUE_TO = "NCLS-BOOTSTRAP-00016";

    @LogMessageInfo(
            message = "Starting BundleProvisioner.",
            level = "INFO")
    public static final String STARTING_BUNDLEPROVISIONER = "NCLS-BOOTSTRAP-00017";

    @LogMessageInfo(
            message = "Time taken to locate OSGi framework = {0} ms.",
            level = "INFO")
    public static final String OSGI_LOCATE_TIME = "NCLS-BOOTSTRAP-00018";

    @LogMessageInfo(
            message = "Time taken to initialize OSGi framework = {0} ms.",
            level = "INFO")
    public static final String OSGI_INIT_TIME = "NCLS-BOOTSTRAP-00020";

    @LogMessageInfo(
            message = "Time taken to finish installation of bundles = {0} ms.",
            level = "INFO")
    public static final String BUNDLE_INSTALLATION_TIME = "NCLS-BOOTSTRAP-00021";

    @LogMessageInfo(
            message = "Time taken to finish starting bundles = {0} ms.",
            level = "INFO")
    public static final String BUNDLE_STARTING_TIME = "NCLS-BOOTSTRAP-00022";

    @LogMessageInfo(
            message = "Total time taken to start = {0} ms.",
            level = "INFO")
    public static final String TOTAL_START_TIME = "NCLS-BOOTSTRAP-00023";

    @LogMessageInfo(
            message = "Time taken to stop = {0} ms.",
            level = "INFO")
    public static final String BUNDLE_STOP_TIME = "NCLS-BOOTSTRAP-00024";

    @LogMessageInfo(
            message = "Total time taken = {0}.",
            level = "INFO")
    public static final String TOTAL_TIME = "NCLS-BOOTSTRAP-00025";

    @LogMessageInfo(
            message = "Create bundle provisioner class = {0}.",
            level = "CONFIG")
    public static final String CREATE_BUNDLE_PROVISIONER = "NCLS-BOOTSTRAP-00026";

    @LogMessageInfo(
            message = "Registered {0} as OSGi service registration: {1}.",
            level = "CONFIG")
    public static final String SERVICE_REGISTERED = "NCLS-BOOTSTRAP-00027";

    @LogMessageInfo(
            message = "Unregistered {0} from service registry.",
            level = "CONFIG")
    public static final String SERVICE_UNREGISTERED = "NCLS-BOOTSTRAP-00028";

    @LogMessageInfo(
            message = "Exception while unregistering: ",
            level = "WARNING")
    public static final String SERVICE_UNREGISTRATION_EXCEPTION = "NCLS-BOOTSTRAP-00029";

    @LogMessageInfo(
            message = "installLocations = {0}.",
            level = "INFO")
    public static final String SHOW_INSTALL_LOCATIONS = "NCLS-BOOTSTRAP-00030";

    @LogMessageInfo(
            message = "Unable to determine if {0} is a fragment or not due to ",
            level = "INFO")
    public static final String CANT_TELL_IF_FRAGMENT = "NCLS-BOOTSTRAP-00031";

    @LogMessageInfo(
            message = "Skipping starting of bundles bundles have been provisioned already.",
            level = "INFO")
    public static final String SKIP_STARTING_ALREADY_PROVISIONED_BUNDLES = "NCLS-BOOTSTRAP-00032";

    @LogMessageInfo(
            message = "{0} : latest file in installation location = {1} and latest installed bundle = {2} ",
            level = "INFO")
    public static final String LATEST_FILE_IN_INSTALL_LOCATION = "NCLS-BOOTSTRAP-00033";

    @LogMessageInfo(
            message = "Updating system bundle.",
            level = "INFO")
    public static final String UPDATING_SYSTEM_BUNDLE = "NCLS-BOOTSTRAP-00034";

    @LogMessageInfo(
            message = "Provisioning options have changed, recreating the framework with a clean OSGi storage(aka cache).",
            level = "INFO")
    public static final String PROVISIONING_OPTIONS_CHANGED = "NCLS-BOOTSTRAP-00035";

    @LogMessageInfo(
            message = "Storage support not available in framework bundle, so can't store bundle ids. This may lead to slower start up time.",
            level = "WARNING")
    public static final String CANT_STORE_BUNDLEIDS = "NCLS-BOOTSTRAP-00037";

    @LogMessageInfo(
            message = "Storage support not available in framework bundle, so can't store provisioning options. This may lead to slower start up time.",
            level = "WARNING")
    public static final String CANT_STORE_PROVISIONING_OPTIONS = "NCLS-BOOTSTRAP-00038";

    @LogMessageInfo(
            message = "Got an unexpected exception.",
            level = "WARNING")
    public static final String CAUGHT_EXCEPTION = "NCLS-BOOTSTRAP-00039";

    /**
     * This helper method is duplicated from org.glassfish.api.logging.LogHelper to avoid adding
     * dependency on the glassfish-api bundle.
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
}
