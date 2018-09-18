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

package com.sun.enterprise.deployment.node.ws;

/**
 * Tag names that appear inside weblogic-webservices.xml
 *
 * @author Rama Pulavarthi
 */
public class WLWebServicesTagNames {

    public static final String WEB_SERVICES = "weblogic-webservices";
    public static final String WEB_SERVICE = "webservice-description";

    public static final String PORT_COMPONENT = "port-component";
    public static final String WEB_SERVICE_DESCRIPTION_NAME = "webservice-description-name";
    public static final String WSDL_PUBLISH_FILE = "wsdl-publish-file";
    public static final String WEBSERVICE_TYPE = "webservice-type";
    public static final String SERVICE_ENDPOINT_ADDRESS = "service-endpoint-address";
    public static final String WEBSERVICE_CONTEXTPATH = "webservice-contextpath";
    public static final String WEBSERVICE_SERVICEURI="webservice-serviceuri";
    public static final String WSDL = "wsdl";
    public static final String WSDL_EXPOSED = "exposed";
    public static final String STREAM_ATTACHMENTS = "stream-attachments";
    public static final String VALIDATE_REQUEST = "validate-request";

    public static final String RELIABILITY_CONFIG = "reliability-config";
    public static final String INACTIVITY_TIMEOUT = "inactivity-timeout";
    public static final String BASE_RETRANSMISSION_INTERVAL = "base-retransmission-interval";
    public static final String RETRANSMISSION_EXPONENTIAL_BACKOFF = "retransmission-exponential-backoff";
    public static final String ACKNOWLEDGEMENT_INTERVAL = "acknowledgement-interval";
    public static final String SEQUENCE_EXPIRATION = "sequence-expiration";
    public static final String BUFFER_RETRY_COUNT = "buffer-retry-count";
    public static final String BUFFER_RETRY_DELAY = "buffer-retry-delay";

    public static final String SERVICE_REFERENCE_DESCRIPTION="service-reference-description";
    public static final String SERVICE_REFERENCE_WSDL_URL="wsdl-url";
    public static final String SERVICE_REFERENCE_PORT_INFO="port-info";
    public static final String SERVICE_REFERENCE_PORT_NAME="port-name";


    //Unsupported config
    public static final String WEBSERVICE_SECURITY = "webservice-security";
    public static final String DEPLOYMENT_LISTENER_LIST = "deployment-listener-list";
    public static final String TRANSACTION_TIMEOUT="transaction-tiemout";
    public static final String CALLBACK_PROTOCOL = "callback-protocol";
    public static final String HTTP_FLUSH_RESPONSE = "http-flush-resposne";
    public static final String HTTP_RESPONSE_BUFFERSIZE = "http-response-buffersize";


}
