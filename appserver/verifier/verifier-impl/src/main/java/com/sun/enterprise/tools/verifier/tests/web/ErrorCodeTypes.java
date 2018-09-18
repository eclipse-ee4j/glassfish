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

package com.sun.enterprise.tools.verifier.tests.web;

/** Error code element contains an HTTP error code type within web application 
 * test.
 *    i.e. 404 
 *  Define all error code type here
 *   taken from  -
 *     http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-rev-05.txt
 */
public class ErrorCodeTypes { 
    //   taken from  -
    //   http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-rev-05.txt
    //  Chapter 10   Status Code Definitions ......................................53
  
    // 10.1 Informational 1xx ...........................................53
    public static final int CONTINUE = 100; // Continue 
    public static final int SWITCHING_PROTOCOLS = 101; // Switching Protocols
  
    // 10.2    Successful 2xx ...........................................54
    public static final int OK = 200; // OK
    public static final int CREATED = 201; // Created
    public static final int ACCEPTED = 202; // Accepted
    public static final int NON_AUTHORITATIVE_INFORMATION = 203; // Non-Authoritative Information
    public static final int NO_CONTENT = 204; // No Content
    public static final int RESET_CONTENT = 205; // Reset Content
    public static final int PARTIAL_CONTENT = 206; // Partial Content
  
    // Redirection 3xx ..........................................57
    public static final int MULTIPLE_CHOICES = 300; // Multiple Choices
    public static final int MOVED_PERMANENTLY = 301; // Moved Permanently
    public static final int FOUND = 302; // Found
    public static final int SEE_OTHER = 303; // See Other
    public static final int NOT_MODIFIED = 304; // Not Modified
    public static final int USE_PROXY = 305; // Use Proxy
    public static final int UNUSED = 306; // (Unused)
    public static final int TEMPORARY_REDIRECT = 307; // Temporary Redirect
  
    // Client Error 4xx .........................................60
    public static final int BAD_REQUEST = 400; // Bad Request
    public static final int UNAUTHORIZED = 401; // Unauthorized
    public static final int PAYMENT_REQUIRED = 402; // Payment Required
    public static final int FORBIDDEN = 403; // Forbidden
    public static final int NOT_FOUND = 404; // Not Found
    public static final int METHOD_NOT_ALLOWED = 405; // Method Not Allowed
    public static final int NOT_ACCEPTABLE = 406; // Not Acceptable
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407; // Proxy Authentication Required
    public static final int REQUEST_TIMEOUT = 408; // Request Timeout
    public static final int CONFLICT = 409; // Conflict
    public static final int GONE = 410; // Gone
    public static final int LENGTH_REQUIRED = 411; // Length Required
    public static final int PRECONDITION_FAILED = 412; // Precondition Failed
    public static final int REQUEST_ENTITY_TOO_LARGE = 413; // Request Entity Too Large
    public static final int REQUEST_URI_TOO_LONG = 414; // Request-URI Too Long
    public static final int UNSUPPORTED_MEDIA_TYPE = 415; // Unsupported Media Type
    public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416; // Requested Range Not Satisfiable
    public static final int EXPECTATION_FAILED = 417; // Expectation Failed
  
    // Server Error 5xx .........................................65
    public static final int INTERNAL_SERVER_ERROR = 500; // Internal Server Error
    public static final int NOT_IMPLEMENTED = 501; // Not Implemented
    public static final int BAD_GATEWAY = 502; // Bad Gateway
    public static final int SERVICE_UNAVAILABLE = 503; // Service Unavailable
    public static final int GATEWAY_TIMEOUT = 504; // Gateway Timeout
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505; // HTTP Version Not Supported
 
}
