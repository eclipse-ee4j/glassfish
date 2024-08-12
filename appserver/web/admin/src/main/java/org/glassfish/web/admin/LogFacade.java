/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.admin;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 * Provides the logging facilities.
 *
 * @author Shing Wai Chan
 */
public class LogFacade {
    @LogMessagesResourceBundle
    private static final String SHARED_LOGMESSAGE_RESOURCE =
            "org.glassfish.web.admin.monitor.LogMessages";

    @LoggerInfo(subsystem="WEB", description="WEB Admin Logger", publish=true)
    private static final String WEB_ADMIN_LOGGER = "jakarta.enterprise.web.admin";

    private static final Logger LOGGER =
            Logger.getLogger(WEB_ADMIN_LOGGER, SHARED_LOGMESSAGE_RESOURCE);

    private LogFacade() {}

    public static Logger getLogger() {
        return LOGGER;
    }

    private static final String prefix = "AS-WEB-ADMIN-";

    @LogMessageInfo(
            message = "Unable to register StatsProvider {0} with Monitoring Infrastructure. No monitoring data will be collected for {1} and {2}",
            level = "SEVERE",
            cause = "Current server config is null",
            action = "Verify if the server instance is started correctly")
    public static final String UNABLE_TO_REGISTER_STATS_PROVIDERS = prefix + "00001";

    @LogMessageInfo(
            message = "Current server config is null",
            level = "INFO")
    public static final String NULL_CONFIG = prefix + "00002";

    @LogMessageInfo(
            message = "The acceptor threads must be at least 1",
            level = "INFO")
    public static final String ACCEPTOR_THREADS_TOO_LOW = prefix + "00003";

    @LogMessageInfo(
            message = "Listener {0} could not be created, actual reason: {1}",
            level = "INFO")
    public static final String CREATE_HTTP_LISTENER_FAIL = prefix + "00004";

    @LogMessageInfo(
            message = "A default virtual server is required.  Please use --default-virtual-server to specify this value.",
            level = "INFO")
    public static final String CREATE_HTTP_LISTENER_VS_BLANK = prefix + "00005";

    @LogMessageInfo(
            message = "--defaultVS and --default-virtual-server conflict.  Please use only --default-virtual-server to specify this value.",
            level = "INFO")
    public static final String CREATE_HTTP_LISTENER_VS_BOTH_PARAMS = prefix + "00006";

    @LogMessageInfo(
            message = "Attribute value (default-virtual-server = {0}) is not found in list of virtual servers defined in config.",
            level = "INFO")
    public static final String CREATE_HTTP_LISTENER_VS_NOTEXISTS = prefix + "00007";

    @LogMessageInfo(
            message = "Http Listener named {0} already exists.",
            level = "INFO")
    public static final String CREATE_HTTP_LISTENER_DUPLICATE = prefix + "00008";

    @LogMessageInfo(
            message = "Port [{0}] is already taken for address [{1}], please choose another port.",
            level = "INFO")
    public static final String PORT_IN_USE = prefix + "00009";

    @LogMessageInfo(
            message = "Network Listener named {0} already exists.",
            level = "INFO")
    public static final String CREATE_NETWORK_LISTENER_FAIL_DUPLICATE = prefix + "00010";

    @LogMessageInfo(
            message = "Protocol {0} has neither a protocol nor a port-unification configured.",
            level = "INFO")
    public static final String CREATE_NETWORK_LISTENER_FAIL_BAD_PROTOCOL = prefix + "00011";

    @LogMessageInfo(
            message = "{0} create failed:",
            level = "INFO")
    public static final String CREATE_NETWORK_LISTENER_FAIL = prefix + "00012";

    @LogMessageInfo(
            message = "The specified protocol {0} is not yet configured.",
            level = "INFO")
    public static final String CREATE_HTTP_FAIL_PROTOCOL_NOT_FOUND = prefix + "00013";

    @LogMessageInfo(
            message = "Failed to create http-redirect for {0}: {1}.",
            level = "INFO")
    public static final String CREATE_HTTP_REDIRECT_FAIL = prefix + "00014";

    @LogMessageInfo(
            message = "An http element for {0} already exists. Cannot add duplicate http.",
            level = "INFO")
    public static final String CREATE_HTTP_FAIL_DUPLICATE = prefix + "00015";

    @LogMessageInfo(
            message = "An http-redirect element for {0} already exists. Cannot add duplicate http-redirect.",
            level = "INFO")
    public static final String CREATE_HTTP_REDIRECT_FAIL_DUPLICATE = prefix + "00016";

    @LogMessageInfo(
            message = "{0} protocol already exists. Cannot add duplicate protocol.",
            level = "INFO")
    public static final String CREATE_PROTOCOL_FAIL_DUPLICATE = prefix + "00017";

    @LogMessageInfo(
            message = "Failed to create protocol {0}.",
            level = "INFO")
    public static final String CREATE_PROTOCOL_FAIL = prefix + "00018";

    @LogMessageInfo(
            message = "{0} create failed: {1}.",
            level = "INFO")
    public static final String CREATE_PORTUNIF_FAIL = prefix + "00019";

    @LogMessageInfo(
            message = "{0} create failed.  Given class is not a ProtocolFilter: {1}.",
            level = "INFO")
    public static final String CREATE_PORTUNIF_FAIL_NOTFILTER = prefix + "00020";

    @LogMessageInfo(
            message = "{0} create failed.  Given class is not a ProtocolFinder: {1}.",
            level = "INFO")
    public static final String CREATE_PORTUNIF_FAIL_NOTFINDER = prefix + "00021";

    @LogMessageInfo(
            message = "{0} transport already exists. Cannot add duplicate transport.",
            level = "INFO")
    public static final String CREATE_TRANSPORT_FAIL_DUPLICATE = prefix + "00022";

    @LogMessageInfo(
            message = "Failed to create transport {0}.",
            level = "INFO")
    public static final String CREATE_TRANSPORT_FAIL = prefix + "00023";

    @LogMessageInfo(
            message = "Please use only networklisteners.",
            level = "INFO")
    public static final String CREATE_VIRTUAL_SERVER_BOTH_HTTP_NETWORK = prefix + "00024";

    @LogMessageInfo(
            message = "Virtual Server named {0} already exists.",
            level = "INFO")
    public static final String CREATE_VIRTUAL_SERVER_DUPLICATE = prefix + "00025";

    @LogMessageInfo(
            message = "{0} create failed.",
            level = "INFO")
    public static final String CREATE_VIRTUAL_SERVER_FAIL = prefix + "00026";

    @LogMessageInfo(
            message = "Specified http listener, {0}, doesn''t exist.",
            level = "INFO")
    public static final String DELETE_HTTP_LISTENER_NOT_EXISTS = prefix + "00028";

    @LogMessageInfo(
            message = "{0} delete failed.",
            level = "INFO")
    public static final String DELETE_HTTP_LISTENER_FAIL = prefix + "00029";

    @LogMessageInfo(
            message = "{0} Network Listener doesn't exist.",
            level = "INFO")
    public static final String DELETE_NETWORK_LISTENER_NOT_EXISTS = prefix + "00030";

    @LogMessageInfo(
            message = "Deletion of NetworkListener {0} failed.",
            level = "INFO")
    public static final String DELETE_NETWORK_LISTENER_FAIL = prefix + "00031";

    @LogMessageInfo(
            message = "{0} http-redirect doesn't exist.",
            level = "INFO")
    public static final String DELETE_HTTP_NOTEXISTS = prefix + "00032";

    @LogMessageInfo(
            message = "Deletion of http {0} failed.",
            level = "INFO")
    public static final String DELETE_HTTP_FAIL = prefix + "00033";

    @LogMessageInfo(
            message = "Deletion of http-redirect {0} failed.",
            level = "INFO")
    public static final String DELETE_HTTP_REDIRECT_FAIL = prefix + "00034";

    @LogMessageInfo(
            message = "{0} protocol doesn't exist.",
            level = "INFO")
    public static final String DELETE_PROTOCOL_NOT_EXISTS = prefix + "00035";

    @LogMessageInfo(
            message = "{0} protocol is being used in the network listener {1}.",
            level = "INFO")
    public static final String DELETE_PROTOCOL_BEING_USED = prefix + "00036";

    @LogMessageInfo(
            message = "Deletion of Protocol {0} failed.",
            level = "INFO")
    public static final String DELETE_PROTOCOL_FAIL = prefix + "00037";

    @LogMessageInfo(
            message = "{0} delete failed: {1}.",
            level = "INFO")
    public static final String DELETE_FAIL = prefix + "00038";

    @LogMessageInfo(
            message = "No {0} element found with the name {1}.",
            level = "INFO")
    public static final String NOT_FOUND = prefix + "00039";

    @LogMessageInfo(
            message = "{0} transport is being used in the network listener {1}.",
            level = "INFO")
    public static final String DELETE_TRANSPORT_BEINGUSED = prefix + "00040";

    @LogMessageInfo(
            message = "Deletion of Transport {0} failed.",
            level = "INFO")
    public static final String DELETE_TRANSPORT_FAIL = prefix + "00041";

    @LogMessageInfo(
            message = "{0} transport doesn''t exist.",
            level = "INFO")
    public static final String DELETE_TRANSPORT_NOT_EXISTS = prefix + "00042";

    @LogMessageInfo(
            message = "{0} delete failed.",
            level = "INFO")
    public static final String DELETE_VIRTUAL_SERVER_FAIL = prefix + "00043";

    @LogMessageInfo(
            message = "Specified virtual server, {0}, doesn''t exist.",
            level = "INFO")
    public static final String DELETE_VIRTUAL_SERVER_NOT_EXISTS = prefix + "00044";

    @LogMessageInfo(
            message = "Specified virtual server, {0}, can not be deleted because it is referenced from http listener, {1}.",
            level = "INFO")
    public static final String DELETE_VIRTUAL_SERVER_REFERENCED = prefix + "00045";

    @LogMessageInfo(
            message = "Monitoring Registry does not exist. Possible causes are 1) Monitoring is not turned on or at a lower level 2) The corresponding container (web, ejb, etc.) is not loaded yet",
            level = "INFO")
    public static final String MRDR_NULL = prefix + "00046";
}
