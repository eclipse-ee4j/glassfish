/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.enterprise.iiop.impl;

import com.sun.logging.LogDomains;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 * @author David Matejcek
 */
public class IIOPImplLogFacade {

    @LogMessagesResourceBundle
    private static final String BUNDLE_NAME = "org.glassfish.enterprise.iiop.impl.LogMessages";

    @LoggerInfo(subsystem = "AS-ORB", description = "Main IIOP/ORB/CORBA Logger", publish = true)
    public static final String LOGGER_NAME_PREFIX = LogDomains.CORBA_LOGGER;

    private IIOPImplLogFacade() {
        // hidden
    }

    public static Logger getLogger(final Class<?> clazz) {
        return Logger.getLogger(clazz.getName(), BUNDLE_NAME);
    }

    @LogMessageInfo(
        message = "Invalid or unavailable RootPOA service name",
        cause = "Check server.log for details",
        action = "Check network configuration",
        level = "SEVERE")
    public static final String INVALID_ROOT_POA_NAME = "AS-ORB-0011";

    @LogMessageInfo(
        message = "Exception occurred when resolving {0}",
        cause = "org.omg.CORBA.ORBPackage.InvalidName when trying to resolve GroupInfoService",
        action = "Check server.log for details")
    public static final String FAILED_TO_RESOLVE_GROUPINFOSERVICE = "AS-ORB-00101";

    @LogMessageInfo(message = "No Endpoints selected in com.sun.appserv.iiop.endpoints property. Using {0}:{1} instead")
    public static final String NO_ENDPOINT_SELECTED = "AS-ORB-00102";

    @LogMessageInfo(
        message = "Problem with membership change notification. Exception occurred.",
        cause = "Check server.log for details",
        action = "Check network configuration and cluster setup")
    public static final String GROUPINFOSERVICE_MEMBERSHIP_NOTIFICATION_PROBLEM = "AS-ORB-00103";


    @LogMessageInfo(message = "Could not find an endpoint to send request to.")
    public static final String COULD_NOT_FIND_ENDPOINT = "AS-ORB-00204";

    @LogMessageInfo(message = "Unknown host: {0} Exception thrown: {1}")
    public static final String UNKNOWN_HOST = "AS-ORB-00205";

    @LogMessageInfo(message = "No Endpoints selected in com.sun.appserv.iiop.endpoints property. Using JNDI Provider URL {0} instead")
    public static final String NO_ENDPOINTS_SELECTED_PROVIDER = "AS-ORB-00206";

    @LogMessageInfo(message = "Exception : {0} thrown for bad provider URL String: {1}")
    public static final String PROVIDER_EXCEPTION = "AS-ORB-00207";

}
