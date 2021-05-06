/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.logging;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Class LogDomains
 */
public class LogDomains {

    /**
     * DOMAIN_ROOT the prefix for the logger name. This is public only
     * so it can be accessed w/in the ias package space.
     */
    public static final String DOMAIN_ROOT = "javax.";

    /**
     * Upgrade logger name.
     */
    public static final String UPGRADE_LOGGER = "upgradeLogger";

    /**
     * PACKAGE_ROOT the prefix for the packages where logger resource
     * bundles reside. This is public only so it can be accessed w/in
     * the ias package space.
     */
    public static final String PACKAGE_ROOT = "com.sun.logging.";

    /**
     * RESOURCE_BUNDLE the name of the logging resource bundles.
     */
    public static final String RESOURCE_BUNDLE = "LogStrings";

    /**
     * Field
     */
    public static final String STD_LOGGER = DOMAIN_ROOT + "enterprise.system.std";

    /**
     * Field
     */
    public static final String TOOLS_LOGGER = DOMAIN_ROOT + "enterprise.system.tools";

    /**
     * Field
     */
    public static final String EJB_LOGGER = DOMAIN_ROOT + "enterprise.system.container.ejb";

    /**
     * JavaMail Logger
     */
    public static final String JAVAMAIL_LOGGER = DOMAIN_ROOT + "enterprise.resource.javamail";

    /**
     * IIOP Logger
     public static final String IIOP_LOGGER = DOMAIN_ROOT + "enterprise.resource.iiop";
     */


    /**
     * JMS Logger
     */
    public static final String JMS_LOGGER = DOMAIN_ROOT + "enterprise.resource.jms";

    /**
     * Field
     */
    public static final String WEB_LOGGER = DOMAIN_ROOT + "enterprise.system.container.web";

    /**
     * Field
     */
    public static final String CMP_LOGGER = DOMAIN_ROOT + "enterprise.system.container.cmp";

    /**
     * Field
     */
    public static final String JDO_LOGGER = DOMAIN_ROOT + "enterprise.resource.jdo";

    /**
     * Field
     */
    public static final String ACC_LOGGER = DOMAIN_ROOT + "enterprise.system.container.appclient";

    /**
     * Field
     */
    public static final String MDB_LOGGER = DOMAIN_ROOT + "enterprise.system.container.ejb.mdb";

    /**
     * Field
     */
    public static final String SECURITY_LOGGER = DOMAIN_ROOT + "enterprise.system.core.security";

    /**
     * Field
     */
    public static final String SECURITY_SSL_LOGGER = DOMAIN_ROOT + "enterprise.system.ssl.security";

    /**
     * Field
     */
    public static final String TRANSACTION_LOGGER = DOMAIN_ROOT + "enterprise.system.core.transaction";

    /**
     * Field
     */
    public static final String CORBA_LOGGER = DOMAIN_ROOT + "enterprise.resource.corba";

    /**
     * Field
     */
    //START OF IASRI 4660742
    /**
     * Field
     */
    public static final String UTIL_LOGGER = DOMAIN_ROOT + "enterprise.system.util";
    /**
     * Field
     */
    public static final String NAMING_LOGGER = DOMAIN_ROOT + "enterprise.system.core.naming";

    /**
     * Field
     */
    public static final String JNDI_LOGGER = DOMAIN_ROOT + "enterprise.system.core.naming";
    /**
     * Field
     */
    public static final String ACTIVATION_LOGGER = DOMAIN_ROOT + "enterprise.system.activation";
    /**
     * Field
     */
    public static final String JTA_LOGGER = DOMAIN_ROOT + "enterprise.resource.jta";

    /**
     * Resource Logger
     */

    public static final String RSR_LOGGER = DOMAIN_ROOT + "enterprise.resource.resourceadapter";
    //END OF IASRI 4660742

    /**
     * Deployment Logger
     */
    public static final String DPL_LOGGER = DOMAIN_ROOT + "enterprise.system.tools.deployment";

    /**
     * Deployment audit logger
     */
    public static final String DPLAUDIT_LOGGER = DOMAIN_ROOT + "enterprise.system.tools.deployment.audit";

    /**
     * Field
     */
    public static final String DIAGNOSTICS_LOGGER = DOMAIN_ROOT + "enterprise.system.tools.diagnostics";

    /**
     * JAXRPC Logger
     */
    public static final String JAXRPC_LOGGER = DOMAIN_ROOT + "enterprise.system.webservices.rpc";

    /**
     * JAXR Logger
     */
    public static final String JAXR_LOGGER = DOMAIN_ROOT + "enterprise.system.webservices.registry";

    /**
     * SAAJ Logger
     */
    public static final String SAAJ_LOGGER = DOMAIN_ROOT + "enterprise.system.webservices.saaj";

    /**
     * Self Management Logger
     */
    public static final String SELF_MANAGEMENT_LOGGER = DOMAIN_ROOT + "enterprise.system.core.selfmanagement";

    /**
     * SQL Tracing Logger
     */
    public static final String SQL_TRACE_LOGGER = DOMAIN_ROOT + "enterprise.resource.sqltrace";

    /**
     * Admin Logger
     */
    public static final String ADMIN_LOGGER =
            DOMAIN_ROOT + "enterprise.system.tools.admin";
    /**
     * Server Logger
     */
    public static final String SERVER_LOGGER = DOMAIN_ROOT + "enterprise.system";
    /**
     * core Logger
     */
    public static final String CORE_LOGGER = DOMAIN_ROOT + "enterprise.system.core";
    /**
     * classloader Logger
     */
    public static final String LOADER_LOGGER = DOMAIN_ROOT + "enterprise.system.core.classloading";

    /**
     * Config Logger
     */
    public static final String CONFIG_LOGGER = DOMAIN_ROOT + "enterprise.system.core.config";

    /**
     * Process Launcher Logger
     */
    public static final String PROCESS_LAUNCHER_LOGGER = DOMAIN_ROOT + "enterprise.tools.launcher";

    /**
     * GMS Logger
     */
    public static final String GMS_LOGGER = DOMAIN_ROOT + "org.glassfish.gms";

    /**
     * AMX Logger
     */
    public static final String AMX_LOGGER = DOMAIN_ROOT + "enterprise.system.amx";

    /**
     * JMX Logger
     */
    public static final String JMX_LOGGER = DOMAIN_ROOT + "enterprise.system.jmx";

    /**
     * core/kernel Logger
     */
    public static final String SERVICES_LOGGER = DOMAIN_ROOT + "enterprise.system.core.services";

    /**
     * webservices logger
     */
    public static final String WEBSERVICES_LOGGER = DOMAIN_ROOT + "enterprise.webservices";

    /**
     * monitoring logger
     */
    public static final String MONITORING_LOGGER = DOMAIN_ROOT + "enterprise.system.tools.monitor";

    /**
     * persistence logger
     */
    public static final String PERSISTENCE_LOGGER = DOMAIN_ROOT + "org.glassfish.persistence";

    /**
     * virtualization logger
     */
    public static final String VIRTUALIZATION_LOGGER = DOMAIN_ROOT + "org.glassfish.virtualization";

    /**
     * PaaS logger
     */
    public static final String PAAS_LOGGER = DOMAIN_ROOT + "org.glassfish.paas";

    // Lock to ensure the Logger creation is synchronized (JDK 6U10 and before can deadlock)
    static Lock lock = new ReentrantLock();

    // Use to store clazz name for which resource bundle is not found.
    static Vector<String> vectorClazz = new Vector<String>();

    /**
     * This is temporary and needed so that IAS can run with or without
     * the com.sun.enterprise.server.logging.ServerLogger. The subclassed
     * addLogger() method there automatically appends the logger name.
     */

    private static String getLoggerResourceBundleName(String loggerName) {
        String result = loggerName + "." + RESOURCE_BUNDLE;
        // System.out.println("looking for bundle "+ result.replaceFirst(DOMAIN_ROOT, PACKAGE_ROOT));
        return result.replaceFirst(DOMAIN_ROOT, PACKAGE_ROOT);
    }


    /**
     * Method getLogger
     *
     * @param clazz
     * @param name
     * @return
     */

    public static synchronized Logger getLogger(final Class clazz, final String name) {
        final ClassLoader cloader = clazz.getClassLoader();
        if(LogManager.getLogManager().getLogger(name) == null) {
            //first time through for this logger.  create it and find the resource bundle

            // should be pass in a resource bundle?
            Logger.getLogger(name);
        }

        // now create the real logger which is the logger name with the package name
        // this is what will be returned.
        //look for the resource bundle only in the package if not there then the resource
        // bundle from the parent above will be used.
        String pkgName = clazz.getPackage().getName();
        String loggerName = name + "." + pkgName;
        Logger cLogger = LogManager.getLogManager().getLogger(loggerName);

        // we should only add a logger of the same name at time.

        if (cLogger == null) {
            //first time through for this logger.  create it and find the resource bundle
            // Byron Sez:  warning: super-long complex anonymous class -- very hard to understand.
            cLogger = new Logger(loggerName, null) {

                // cache it to avoid having to use the class loader later. see GLASSFISH-17256
                final ResourceBundle rb = initResourceBundle();

                /* override this method to set the the thread id so all handlers get the same info*/
                @Override
                public void log(LogRecord record) {
                    record.getSourceMethodName();
                    if(record.getResourceBundle()==null) {
                        if(rb!=null) {
                            record.setResourceBundle(rb);
                        }
                    }
                    //record.setThreadID((int) Thread.currentThread().getId());
                                        if(record.getMessage()==null) {
                                                record.setMessage("");
                                        }
                    super.log(record);
                }


                /**
                 * Retrieve the localization resource bundle for this
                 * logger for the current default locale.  Note that if
                 * the result is null, then the Logger will use a resource
                 * bundle inherited from its parent.
                 *
                 * @return localization bundle (may be null)
                 *
                 */
                @Override
                public ResourceBundle getResourceBundle() {
                    return rb;
                }

                private ResourceBundle initResourceBundle() {
                    //call routine to add resource bundle if not already added
                    // the return needs to go through all known resource bundles
                    try {

                        return ResourceBundle.getBundle(getLoggerResourceBundleName(name), Locale.getDefault(), cloader);
                    } catch (MissingResourceException e) {

                        //try the parent
                        String root = clazz.getPackage().getName();
                        try {

                            return ResourceBundle.getBundle(root + "." + RESOURCE_BUNDLE, Locale.getDefault(), cloader);
                        } catch (MissingResourceException me) {
                            //walk the parents to find the bundle

                            String p = root;
                            while (p != null) {
                                try {
                                    int i = p.lastIndexOf(".");
                                    if (i != -1) {
                                        p = p.substring(0, i);
                                        return ResourceBundle.getBundle(p + "." + RESOURCE_BUNDLE, Locale.getDefault(), cloader);
                                    } else {
                                        p = null;
                                    }
                                } catch (MissingResourceException mre) {
                                }
                            }
                        }
                        // look in this package for the file
                        try {
                            return ResourceBundle.getBundle(getLoggerResourceBundleName(name), Locale.getDefault(),
                                    LogDomains.class.getClassLoader());

                        } catch (MissingResourceException me) {
                        }

                        if(!vectorClazz.contains(clazz.getName())) {
                            Logger l = LogManager.getLogManager().getLogger(name);
                            if(l!=null) {
                                l.log(Level.FINE, "Can not find resource bundle for this logger. " + " class name that failed: {0}", clazz.getName());
                            }
                            vectorClazz.add(clazz.getName());
                        }


                        //throw e;
                        return null;
                    }
                }
            };

            // let's make sure we are the only

            // We must not return an orphan logger (the one we just created) if
            // a race condition has already created one
            if (!addLoggerToLogManager(cLogger)) {
                final Logger existing = LogManager.getLogManager().getLogger(name);
                if (existing == null) {
                    // Can loggers be removed?  If not, this should be impossible
                    // this time, make the call and hope for the best.
                    addLoggerToLogManager(cLogger);
                } else {
                    cLogger = existing;
                }

            }
        }
        return cLogger;
    }

    private static boolean addLoggerToLogManager(Logger logger) {
        // bnevins April 30, 2009 -- there is a bug in the JDK having to do with
        // the ordering of synchronization in the logger package.
        // The work-around is to ALWAYS lock in the order that the JDK bug
        // is assuming.  That means lock A-B-A instead of B-A
        // A == Logger.class, B == LogManager.class
        // I created this method to make it very very clear what is going on

        synchronized (Logger.class) {
            synchronized (LogManager.getLogManager()) {
                return LogManager.getLogManager().addLogger(logger);
            }
        }
    }
}
