/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.faces.integration;

import jakarta.servlet.ServletContext;

import java.util.logging.Logger;

import org.glassfish.mojarra.config.WebConfiguration;
import org.glassfish.mojarra.spi.HighAvailabilityEnabler;
import org.glassfish.mojarra.util.FacesLogger;

import static com.sun.enterprise.web.Constants.ENABLE_HA_ATTRIBUTE;
import static com.sun.enterprise.web.Constants.IS_DISTRIBUTABLE_ATTRIBUTE;
import static java.lang.Boolean.TRUE;
import static java.util.logging.Level.FINE;
import static org.glassfish.mojarra.config.WebConfiguration.BooleanWebContextInitParameter.EnableDistributable;

/**
 * This <code>HighAvailabilityEnabler</code> is specific to GlassFish.
 */
public class GlassFishHighAvailabilityEnabler implements HighAvailabilityEnabler {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    /**
     * Method to test with HA has been enabled. If so, then set the JSF context param
     * org.glassfish.mojarra.enableAgressiveSessionDirtying to true
     *
     * @param ctx
     */
    @Override
    public void enableHighAvailability(ServletContext ctx) {
        // look at the following values for the web app
        // 1> has <distributable /> in the web.xml
        // 2> Was deployed with --availabilityenabled --target <clustername>

        WebConfiguration config = WebConfiguration.getInstance(ctx);

        if (!config.isSet(EnableDistributable)) {
            Object isDistributableObj = ctx.getAttribute(IS_DISTRIBUTABLE_ATTRIBUTE);
            Object enableHAObj = ctx.getAttribute(ENABLE_HA_ATTRIBUTE);

            if (isDistributableObj instanceof Boolean isDistributable && enableHAObj instanceof Boolean enableHA) {
                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.log(FINE, "isDistributable = {0} enableHA = {1}", new Object[] { isDistributable, enableHA });
                }

                if (isDistributable && enableHA) {
                    LOGGER.fine("setting the EnableAgressiveSessionDirtying to true");
                    config.overrideContextInitParameter(EnableDistributable, TRUE);
                }
            }
        }
    }
}
