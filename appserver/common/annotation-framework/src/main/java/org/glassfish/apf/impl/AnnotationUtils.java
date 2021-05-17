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

package org.glassfish.apf.impl;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.MessageFormat;

import org.glassfish.apf.AnnotationHandler;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.ServiceLocator;


/**
 * Bag for utility methods
 *
 * @author Jerome Dochez
 */
public class AnnotationUtils {

    private static Logger logger;
    private static String whatToLog="";

    public static Logger getLogger() {
        if (logger==null) {
            logger = Logger.global;
        }
        return logger;
    }

    public static void setLogger(Logger lg) {
        logger = lg;
    }

    public static void setLoggerTarget(String what) {
        whatToLog = what;
    }

    public static String getLoggerTarget() {
        return whatToLog;
    }

    public static boolean shouldLog(String what) {

        if (logger.isLoggable(Level.FINER)) {
            if (whatToLog.indexOf(what)!=-1)
                return true;
            if ("*".equals(whatToLog))
                return true;
        }
        return false;
    }

    public static String getLocalString(String key, String defaultString, Object... arguments){
        return MessageFormat.format(defaultString, arguments);
    }

    /**
     * Gets the annotation handler for the given class (without causing any of the annotation handlers
     * to be classloaded)
     *
     * @param locator The locator to find the annotation handler for
     * @param forThis The class to find the annotation handler for
     * @return
     */
    @SuppressWarnings("unchecked")
    public static ActiveDescriptor<AnnotationHandler> getAnnotationHandlerForDescriptor(ServiceLocator locator, final Class<?> forThis) {
        ActiveDescriptor<?> retVal = locator.getBestDescriptor(new IndexedFilter() {

            @Override
            public boolean matches(Descriptor d) {
                Map<String, List<String>> metadata = d.getMetadata();
                List<String> handlerForList = metadata.get(AnnotationHandler.ANNOTATION_HANDLER_METADATA);
                if (handlerForList == null || handlerForList.isEmpty()) return false;

                String descriptorForThis = handlerForList.get(0);

                return descriptorForThis.equals(forThis.getName());
            }

            @Override
            public String getAdvertisedContract() {
                return AnnotationHandler.class.getName();
            }

            @Override
            public String getName() {
                return null;
            }

        });

        return (ActiveDescriptor<AnnotationHandler>) retVal;
    }
}
