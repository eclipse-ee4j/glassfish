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

package com.sun.enterprise.tools.verifier.util;

import java.util.Hashtable;
import java.util.logging.Logger;

/**
 * Class LogDomains
 */
public class LogDomains {

    
    private static final String DOMAIN_ROOT = "jakarta.enterprise.";

    public static final String AVK_VERIFIER_LOGGER =
        DOMAIN_ROOT + "system.tools.avk.tools.verifier";

    public static final String AVK_APPVERIFICATION_LOGGER =
        DOMAIN_ROOT + "system.tools.avk.appverification";

    public static final String AVK_APPVERIFICATION_TOOLS_LOGGER =
        DOMAIN_ROOT + "system.tools.avk.appverification.tools";

    public static final String AVK_APPVERIFICATION_XML_LOGGER =
        DOMAIN_ROOT + "system.tools.avk.appverification.xml";

    // RESOURCE_BUNDLES the name of the logging resource bundles.

    private static final String PACKAGE_ROOT = "com.sun.enterprise.";

    private static final String AVK_VERIFIER_BUNDLE =
        PACKAGE_ROOT + "tools.verifier.LocalStrings";

    // Note that these 3 bundles are packaged only in javke.jar and
    // they are not present in appserv-rt.jar
    private static final String AVK_APPVERIFICATION_BUNDLE =
        PACKAGE_ROOT + "appverification.LocalStrings";

    private static final String AVK_APPVERIFICATION_TOOLS_BUNDLE =
        PACKAGE_ROOT + "appverification.tools.LocalStrings";

    private static final String AVK_APPVERIFICATION_XML_BUNDLE =
        PACKAGE_ROOT + "appverification.xml.LocalStrings";

    // static field
    private static Hashtable<String, Logger> loggers = null;

    // static initializer
    static {
      loggers = new Hashtable<String, Logger>();
      loggers.put(AVK_VERIFIER_LOGGER,
                  Logger.getLogger(AVK_VERIFIER_LOGGER,
                                   AVK_VERIFIER_BUNDLE));
      // When run in instrumentation mode, with javke.jar in classpath
      // the calls below will succeed
      try {
      loggers.put(AVK_APPVERIFICATION_LOGGER,
                  Logger.getLogger(AVK_APPVERIFICATION_LOGGER,
                                   AVK_APPVERIFICATION_BUNDLE));
      loggers.put(AVK_APPVERIFICATION_TOOLS_LOGGER,
                  Logger.getLogger(AVK_APPVERIFICATION_TOOLS_LOGGER,
                                   AVK_APPVERIFICATION_TOOLS_BUNDLE));
      loggers.put(AVK_APPVERIFICATION_XML_LOGGER,
                  Logger.getLogger(AVK_APPVERIFICATION_XML_LOGGER,
                                   AVK_APPVERIFICATION_XML_BUNDLE));
      }catch(Exception e) {
         // during normal appserver-run, these 3 initializations will fail
      }
    }

    private LogDomains() {} // prevent instance creation

    public static Logger getLogger(String name) {
        return loggers.get(name);
    }

    public static Logger getDefaultLogger() {
        return loggers.get(AVK_VERIFIER_LOGGER);
    }
}
