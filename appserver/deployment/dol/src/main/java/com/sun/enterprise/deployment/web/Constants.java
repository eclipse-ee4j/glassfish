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

package com.sun.enterprise.deployment.web;

/**
 * Constants used by the various web descriptors.
 * @author James Todd [gonzo@eng.sun.com]
 * @author Danny Coward
 */

public final class Constants {
    private Constants() { /* disallow instantiation */ }

    public static final String ConfigFile = "web.xml";

    public static final String WebApp = "web-app";
    public static final String Servlet = "servlet";
    public static final String ServletName = "servlet-name";
    public static final String ServletClass = "servlet-class";
    public static final String JSP_FILENAME = "jsp-file";
    public static final String LOAD_ON_START_UP = "load-on-startup";

    public static final String Filter = "filter";
    public static final String FilterMapping = "filter-mapping";
    public static final String FilterClass = "filter-class";
    public static final String FilterName = "filter-name";

    public static final String Parameter = "init-param";
    public static final String CONTEXT_PARAMETER = "context-param";
    public static final String ParameterName = "param-name";
    public static final String ParameterValue = "param-value";
    public static final String MIMEMapping = "mime-mapping";
    public static final String MIMEMappingExtension = "extension";
    public static final String MIMEMappingType = "mime-type";
    public static final String ServletMapping = "servlet-mapping";
    public static final String URLPattern = "url-pattern";
    public static final String SessionTimeOut = "session-timeout";
    public static final String WelcomeFileList = "welcome-file-list";
    public static final String WelcomeFile = "welcome-file";

    public static final String DISPLAY_NAME = "display-name";
    public static final String DESCRIPTION = "description";
    public static final String ICON = "icon";
    public static final String LARGE_ICON = "large-icon";
    public static final String SMALL_ICON = "small-icon";
    public static final String DISTRIBUTABLE = "distributable";

    public static final String ERROR_PAGE = "error-page";
    public static final String ERROR_CODE = "error-code";
    public static final String EXCEPTION_TYPE = "exception-type";
    public static final String LOCATION = "location";

    public static final String LISTENER = "listener";
    public static final String LISTENER_CLASS = "listener-class";

    public static final String ENVIRONMENT_ENTRY = "env-entry";
    public static final String ENVIRONMENT_NAME = "env-entry-name";
    public static final String ENVIRONMENT_VALUE = "env-entry-value";
    public static final String ENVIRONMENT_TYPE = "env-entry-type";

    public static final String SECURITY_ROLE = "security-role";
    public static final String ROLE_NAME = "role-name";
    public static final String NAME = "name";

    public static final String SECURITY_CONSTRAINT = "security-constraint";
    public static final String WEB_RESOURCE_COLLECTION = "web-resource-collection";
    public static final String AUTH_CONSTRAINT = "auth-constraint";
    public static final String USERDATA_CONSTRAINT = "user-data-constraint";
    public static final String TRANSPORT_GUARANTEE = "transport-guarantee";
    public static final String WEB_RESOURCE_NAME = "web-resource-name";
    public static final String URL_PATTERN = "url-pattern";
    public static final String HTTP_METHOD = "http-method";

    public static final String SECURITY_ROLE_REFERENCE = "security-role-ref";
    public static final String ROLE_LINK = "role-link";


    public static final String EJB_REFERENCE = "ejb-ref";
    public static final String EJB_LOCAL_REFERENCE = "ejb-local-ref";
    public static final String EJB_NAME = "ejb-ref-name";
    public static final String EJB_TYPE = "ejb-ref-type";
    public static final String EJB_HOME = "home";
    public static final String EJB_REMOTE = "remote";
    public static final String EJB_LOCAL_HOME = "local-home";
    public static final String EJB_LOCAL = "local";
    public static final String EJB_LINK = "ejb-link";
    public static final String RUN_AS = "run-as";

    public static final String SESSION_CONFIG = "session-config";

    public static final String LOGIN_CONFIG = "login-config";
    public static final String AUTH_METHOD = "auth-method";
    public static final String REALM_NAME = "realm-name";
    public static final String FORM_LOGIN_CONFIG = "form-login-config";
    public static final String FORM_LOGIN_PAGE = "form-login-page";
    public static final String FORM_ERROR_PAGE = "form-error-page";

    /* -----
    ** TagLibConfiguration (TagLib reference)
    */

    public static final String TAGLIB = "taglib";
    public static final String TAGLIB_URI = "taglib-uri";
    public static final String TAGLIB_LOCATION = "taglib-location";

    /* -----
    ** TagLib tags
    */

    public static final String TagLib                   = "taglib";
    public static final String TagLib_VERSION           = "tlibversion";
    public static final String TagLib_JSPVERSION        = "jspversion";
    public static final String TagLib_SHORTNAME         = "shortname";
    public static final String TagLib_URI               = "uri";
    public static final String TagLib_INFO              = "info";
    public static final String TagLib_TAGS              = "tag";

    public static final String Tag_NAME                 = "name";
    public static final String Tag_CLASS                = "tagclass";
    public static final String Tag_EXTRA_INFO           = "teiclass";
    public static final String Tag_BODYCONTENT          = "bodycontent";
    public static final String Tag_INFO                 = "info";
    public static final String Tag_ATTRS                = "attribute";

    public static final String TagAttr_NAME             = "name";
    public static final String TagAttr_REQUIRED         = "required";
    public static final String TagAttr_ALLOWEXPR        = "rtexprvalue";
    public static final String TagAttr_TYPE             = "type";

    public static final String TagLib12_VERSION         = "tlib-version";
    public static final String TagLib12_JSPVERSION      = "jsp-version";
    public static final String TagLib12_SHORTNAME       = "short-name";
    public static final String TagLib12_URI             = TagLib_URI;
    public static final String TagLib12_DISPLAYNAME     = DISPLAY_NAME;
    public static final String TagLib12_SMALLICON       = SMALL_ICON;
    public static final String TagLib12_LARGEICON       = LARGE_ICON;
    public static final String TagLib12_DESCRIPTION     = DESCRIPTION;
    public static final String TagLib12_VALIDATOR       = "validator";
    public static final String TagLib12_LISTENER        = LISTENER;
    public static final String TagLib12_TAGS            = TagLib_TAGS;

    public static final String TagList12_CLASS        = LISTENER_CLASS;

    public static final String TagVal12_CLASS           = "validator-class";
    public static final String TagVal12_INIT_PARMS      = Parameter;

    public static final String Tag12_NAME               = Tag_NAME;
    public static final String Tag12_CLASS              = "tag-class";
    public static final String Tag12_EXTRA_INFO         = "tei-class";
    public static final String Tag12_BODYCONTENT        = "body-content";
    public static final String Tag12_DISPLAYNAME        = DISPLAY_NAME;
    public static final String Tag12_SMALLICON          = SMALL_ICON;
    public static final String Tag12_LARGEICON          = LARGE_ICON;
    public static final String Tag12_DESCRIPTION        = DESCRIPTION;
    public static final String Tag12_VARIABLE           = "variable";
    public static final String Tag12_ATTRS              = Tag_ATTRS;

    public static final String TagVar12_NAME_GIVEN      = "name-given";
    public static final String TagVar12_NAME_ATTR       = "name-from-attribute";
    public static final String TagVar12_CLASS           = "variable-class";
    public static final String TagVar12_DECLARE         = "declare";
    public static final String TagVar12_SCOPE           = "scope";

}

