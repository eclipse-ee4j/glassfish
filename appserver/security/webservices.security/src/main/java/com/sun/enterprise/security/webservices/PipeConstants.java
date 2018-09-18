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

package com.sun.enterprise.security.webservices;

/**
 * This pipe is used to do client side security for app server
 */
public interface PipeConstants {

    static final String BINDING = "BINDING";
    static final String CLIENT_SUBJECT = "CLIENT_SUBJECT";
    static final String ENDPOINT = "ENDPOINT";
    static final String ENDPOINT_ADDRESS = "ENDPOINT_ADDRESS";
    static final String NEXT_PIPE = "NEXT_PIPE";
    static final String POLICY = "POLICY";
    static final String SEI_MODEL = "SEI_MODEL";
    static final String SECURITY_TOKEN = "SECURITY_TOKEN";
    static final String SECURITY_PIPE = "SECURITY_PIPE";
    static final String SERVER_SUBJECT = "SERVER_SUBJECT";
    static final String SERVICE = "SERVICE";
    static final String SERVICE_REF = "SERVICE_REF";
    static final String SOAP_LAYER = "SOAP";
    static final String SERVICE_ENDPOINT = "SERVICE_ENDPOINT";
    static final String WSDL_MODEL = "WSDL_MODEL";
    static final String WSDL_SERVICE = "WSDL_SERVICE";
    static final String CONTAINER = "CONTAINER";
    static final String ASSEMBLER_CONTEXT = "ASSEMBLER_CONTEXT";
}
