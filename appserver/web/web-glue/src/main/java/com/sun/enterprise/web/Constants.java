/*
 * Copyright (c) 2021, 2024 Contributors to Eclipse Foundation.
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

package com.sun.enterprise.web;

/**
 * Static constants for this package.
 */

public final class Constants {

    public static final String Package = "com.sun.enterprise.web";

    /**
     * Path to web application context.xml configuration file.
     */

    public static final String WEB_CONTEXT_XML = "META-INF/context.xml";

    /**
     * The default location of the global context.xml file.
     *
     * Path to global context.xml (relative to instance root).
     */
    public static final String DEFAULT_CONTEXT_XML = "config/context.xml";

    /**
     * The default web application's deployment descriptor location.
     *
     * This path is relative to catalina.home i.e. the instance root directory.
     */
    public static final String DEFAULT_WEB_XML = "config/default-web.xml";

    /**
     * The system-assigned default web module's name/identifier.
     *
     * This has to be the same value as is in j2ee/WebModule.cpp.
     */
    public static final String DEFAULT_WEB_MODULE_NAME = "__default-web-module";

    /**
     * The separator character between an application name and the web
     * module name within the application.
     */
    public static final String NAME_SEPARATOR = ":";

    /**
     * The string to prefix to the name of the web module when a web module
     * is designated to be the default-web-module for a virtual server.
     *
     * This serves as a way to differentiate the web module from the
     * variant that is deployed as a 'default web module' at a context root
     * of "".
     */
    public static final String DEFAULT_WEB_MODULE_PREFIX = "__default-";

    /**
     * The Eclipse WASP Pages servlet class name.
     */
    public static final String APACHE_JSP_SERVLET_CLASS = "org.glassfish.wasp.servlet.JspServlet";

    public static final String JSP_URL_PATTERN="*.jsp";


    public static final String REQUEST_START_TIME_NOTE =
        "com.sun.enterprise.web.request.startTime";

    public static final String ACCESS_LOG_PROPERTY = "accesslog";

    public static final String ACCESS_LOG_BUFFER_SIZE_PROPERTY = "accessLogBufferSize";

    public static final String ACCESS_LOG_WRITE_INTERVAL_PROPERTY = "accessLogWriteInterval";

    public static final String ACCESS_LOGGING_ENABLED = "accessLoggingEnabled";

    public static final String SSO_ENABLED = "sso-enabled";

    public static final String ERROR_REPORT_VALVE = "errorReportValve";

    // Services attribute in ServletContext
    public static final String SERVICE_LOCATOR_ATTRIBUTE = "org.glassfish.servlet.habitat";

    // WebModule attributes in ServletContext
    // available only during ServletContextListener.contextInitialized
    public static final String DEPLOYMENT_CONTEXT_ATTRIBUTE = "com.sun.enterprise.web.WebModule.DeploymentContext";
    public static final String IS_DISTRIBUTABLE_ATTRIBUTE = "com.sun.enterprise.web.WebModule.isDistributable";
    public static final String ENABLE_HA_ATTRIBUTE = "com.sun.enterprise.web.WebModule.enableHA";
}
