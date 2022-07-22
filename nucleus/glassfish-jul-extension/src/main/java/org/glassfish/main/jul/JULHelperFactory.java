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

package org.glassfish.main.jul;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.glassfish.main.jul.handler.GlassFishLogHandler;

/**
 * Tool for accessing GlassFish related parts under control of any available {@link LogManager}
 * supported. It was made to make life of developers easier.
 *
 * @author David Matejcek
 */
public class JULHelperFactory {

    /**
     * @return {@link JULHelper}.
     */
    public static JULHelper getHelper() {
        LogManager manager = LogManager.getLogManager();
        if (manager instanceof GlassFishLogManager) {
            return new GJULEHelper((GlassFishLogManager) manager);
        }
        return new JDKJULHelper(manager);
    }


    /**
     * Definition of usual helper capabilities.
     */
    public interface JULHelper {

        /**
         * @param clazz - must not be null
         * @return {@link java.lang.System.Logger} based on the class name
         */
        default System.Logger getSystemLogger(Class<?> clazz) {
            return System.getLogger(clazz.getName());
        }


        /**
         * @param clazz - must not be null
         * @return {@link Logger} based on the class name
         */
        default Logger getJULLogger(Class<?> clazz) {
            return Logger.getLogger(clazz.getName());
        }


        /**
         * Asks the root logger for his {@link GlassFishLogHandler} instance. If no such handler
         * used, returns null.
         *
         * @return null or {@link GlassFishLogHandler} instance
         */
        GlassFishLogHandler findGlassFishLogHandler();
    }

}
