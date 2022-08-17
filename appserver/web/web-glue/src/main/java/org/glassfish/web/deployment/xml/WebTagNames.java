/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.web.deployment.xml;

import com.sun.enterprise.deployment.xml.TagNames;

/**
 * Holds all tag names for the web application dtd
 * Created on February 20, 2002, 5:15 PM
 *
 * @author  Jerome Dochez
 */
public interface WebTagNames extends TagNames {

    String COMMON_NAME = "name";
    String WEB_BUNDLE = "web-app";
    String WEB_FRAGMENT = "web-fragment";
    String SERVLET = "servlet";
    String SERVLET_NAME = "servlet-name";
    String SERVLET_CLASS = "servlet-class";
    String JSP_FILENAME = "jsp-file";
    String LOAD_ON_STARTUP = "load-on-startup";

    String FILTER = "filter";
    String FILTER_MAPPING = "filter-mapping";
    String FILTER_CLASS = "filter-class";
    String FILTER_NAME = "filter-name";
    String DISPATCHER = "dispatcher";

    String INIT_PARAM = "init-param";
    String PARAM_NAME = "param-name";
    String PARAM_VALUE = "param-value";
    String CONTEXT_PARAM = "context-param";
    String ENABLED = "enabled";
    String ASYNC_SUPPORTED = "async-supported";

    String SECURITY_CONSTRAINT = "security-constraint";
    String WEB_RESOURCE_COLLECTION = "web-resource-collection";
    String AUTH_CONSTRAINT = "auth-constraint";
    String USERDATA_CONSTRAINT = "user-data-constraint";
    String TRANSPORT_GUARANTEE = "transport-guarantee";
    String WEB_RESOURCE_NAME = "web-resource-name";
    String URL_PATTERN = "url-pattern";
    String HTTP_METHOD = "http-method";
    String HTTP_METHOD_OMISSION = "http-method-omission";
    String DISTRIBUTABLE = "distributable";
    String SESSION_CONFIG = "session-config";
    String SESSION_TIMEOUT = "session-timeout";
    String COOKIE_CONFIG = "cookie-config";
    String DOMAIN = "domain";
    String PATH = "path";
    String COMMENT = "comment";
    String HTTP_ONLY = "http-only";
    String SECURE = "secure";
    String MAX_AGE = "max-age";
    String TRACKING_MODE = "tracking-mode";
    String WELCOME_FILE_LIST = "welcome-file-list";
    String WELCOME_FILE = "welcome-file";
    String SERVLET_MAPPING = "servlet-mapping";

    String MIME_MAPPING  = "mime-mapping";
    String EXTENSION = "extension";
    String MIME_TYPE  = "mime-type";

    String LISTENER = "listener";
    String LISTENER_CLASS = "listener-class";

    String ERROR_PAGE = "error-page";
    String ERROR_CODE = "error-code";
    String EXCEPTION_TYPE = "exception-type";
    String LOCATION = "location";

    String LOGIN_CONFIG = "login-config";
    String AUTH_METHOD = "auth-method";
    String REALM_NAME = "realm-name";
    String FORM_LOGIN_CONFIG = "form-login-config";
    String FORM_LOGIN_PAGE = "form-login-page";
    String FORM_ERROR_PAGE = "form-error-page";

    String JSPCONFIG = "jsp-config";
    String TAGLIB = "taglib";
    String TAGLIB_URI = "taglib-uri";
    String TAGLIB_LOCATION = "taglib-location";
    String JSP_GROUP = "jsp-property-group";
    String EL_IGNORED = "el-ignored";
    String PAGE_ENCODING = "page-encoding";
    String SCRIPTING_INVALID = "scripting-invalid";
    String INCLUDE_PRELUDE = "include-prelude";
    String INCLUDE_CODA = "include-coda";
    String IS_XML = "is-xml";
    String DEFERRED_SYNTAX_ALLOWED_AS_LITERAL = "deferred-syntax-allowed-as-literal";
    String TRIM_DIRECTIVE_WHITESPACES = "trim-directive-whitespaces";
    String DEFAULT_CONTENT_TYPE = "default-content-type";
    String BUFFER = "buffer";
    String ERROR_ON_UNDECLARED_NAMESPACE = "error-on-undeclared-namespace";

    String LOCALE_ENCODING_MAPPING_LIST = "locale-encoding-mapping-list";
    String LOCALE_ENCODING_MAPPING = "locale-encoding-mapping";
    String LOCALE = "locale";
    String ENCODING = "encoding";
    String DEFAULT_CONTEXT_PATH = "default-context-path";
    String REQUEST_CHARACTER_ENCODING = "request-character-encoding";
    String RESPONSE_CHARACTER_ENCODING = "response-character-encoding";

    //ordering
    String ABSOLUTE_ORDERING = "absolute-ordering";
    String OTHERS = "others";
    String ORDERING = "ordering";
    String AFTER = "after";
    String BEFORE = "before";

    String MULTIPART_CONFIG = "multipart-config";
    String MAX_FILE_SIZE = "max-file-size";
    String MAX_REQUEST_SIZE = "max-request-size";
    String FILE_SIZE_THRESHOLD = "file-size-threshold";

    String DENY_UNCOVERED_HTTP_METHODS = "deny-uncovered-http-methods";
}
