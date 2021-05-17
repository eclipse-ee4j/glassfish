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

package com.sun.enterprise.deployment.xml;

/**
 * Repository of all the JSR 109 deployment descriptor elements
 *
 * @author Kenneth Saks
 */
public class WebServicesTagNames {

    public static final String IBM_NAMESPACE = "http://www.ibm.com/webservices/xsd";

    public static final String TRANSPORT_GUARANTEE = "transport-guarantee";
    public static final String CALL_PROPERTY = "call-property";
    public static final String WEB_SERVICE_ENDPOINT = "webservice-endpoint";
    public static final String ENDPOINT_ADDRESS_URI = "endpoint-address-uri";
    public static final String EJB_LINK = "ejb-link";
    public static final String CLIENT_WSDL_PUBLISH_URL="wsdl-publish-location";
    public static final String FINAL_WSDL_URL = "final-wsdl-location";
    public static final String SERVLET_LINK = "servlet-link";
    public static final String SERVLET_IMPL_CLASS = "servlet-impl-class";
    public static final String SERVICE_IMPL_BEAN = "service-impl-bean";
    public static final String SERVICE_QNAME = "service-qname";
    public static final String WEB_SERVICES_CLIENT = "webservicesclient";
    public static final String WEB_SERVICES = "webservices";
    public static final String WEB_SERVICE = "webservice-description";
    public static final String COMPONENT_SCOPED_REFS = "component-scoped-refs";
    public static final String SERVICE_REF = "service-ref";
    public static final String SERVICE_REF_NAME = "service-ref-name";
    public static final String SERVICE_INTERFACE = "service-interface";
    public static final String SERVICE_ENDPOINT_INTERFACE =
        "service-endpoint-interface";
    public static final String WEB_SERVICE_DESCRIPTION_NAME =
        "webservice-description-name";
    public static final String WSDL_PORT = "wsdl-port";
    public static final String RESPECT_BINDING = "respect-binding";
    public static final String RESPECT_BINDING_ENABLED = "enabled";
    public static final String ADDRESSING = "addressing";
    public static final String ADDRESSING_ENABLED = "enabled";
    public static final String ADDRESSING_REQUIRED = "required";
    public static final String ADDRESSING_RESPONSES = "responses";
    public static final String WSDL_SERVICE = "wsdl-service";
    public static final String ENABLE_MTOM= "enable-mtom";
    public static final String MTOM_THRESHOLD= "mtom-threshold";
    public static final String PROTOCOL_BINDING = "protocol-binding";
    public static final String HANDLER_CHAINS = "handler-chains";
    public static final String HANDLER_CHAIN= "handler-chain";
    public static final String SERVICE_NAME_PATTERN = "service-name-pattern";
    public static final String PORT_NAME_PATTERN = "port-name-pattern";
    public static final String PROTOCOL_BINDINGS = "protocol-bindings";
    public static final String PORT_INFO = "port-info";
    public static final String STUB_PROPERTY = "stub-property";
    public static final String TIE_CLASS = "tie-class";
    public static final String DEBUGGING_ENABLED = "debugging-enabled";
    public static final String SERVICE_IMPL_CLASS = "service-impl-class";
    public static final String WSDL_FILE = "wsdl-file";
    public static final String WSDL_OVERRIDE = "wsdl-override";
    public static final String JAXRPC_MAPPING_FILE = "jaxrpc-mapping-file";
    public static final String JAXRPC_MAPPING_FILE_ROOT = "java-wsdl-mapping";
    public static final String JAVA_XML_TYPE_MAPPING = "java-xml-type-mapping";
    public static final String EXCEPTION_MAPPING = "exception-mapping";
    public static final String SERVICE_INTERFACE_MAPPING = "service-interface-mapping";
    public static final String SERVICE_ENDPOINT_INTERFACE_MAPPING = "service-endpoint-interface-mapping";

    public static final String PORT_COMPONENT = "port-component";
    public static final String PORT_COMPONENT_NAME = "port-component-name";
    public static final String PORT_COMPONENT_REF = "port-component-ref";
    public static final String PORT_COMPONENT_LINK = "port-component-link";
    public static final String NAMESPACE_URI = "namespaceURI";
    public static final String LOCAL_PART = "localpart";
    public static final String PACKAGE_TYPE = "package-type";
    public static final String MAPPED_NAME = "mapped-name";
    public static final String SERVICE_REF_TYPE="service-ref-type";

    public static final String COMPONENT_NAME = "component-name";
    public static final String ENDPOINT_ADDRESS = "endpoint-address";

    public static final String HANDLER = "handler";
    public static final String HANDLER_NAME = "handler-name";
    public static final String HANDLER_CLASS = "handler-class";
    public static final String INIT_PARAM = "init-param";
    public static final String INIT_PARAM_NAME = "param-name";
    public static final String INIT_PARAM_VALUE = "param-value";
    public static final String SOAP_HEADER = "soap-header";
    public static final String SOAP_ROLE = "soap-role";
    public static final String HANDLER_PORT_NAME = "port-name";

    // security
    public static final String AUTH_SOURCE = "auth-source";
    public static final String AUTH_RECIPIENT = "auth-recipient";
    public static final String REQUEST_PROTECTION = "request-protection";
    public static final String RESPONSE_PROTECTION = "response-protection";
    public static final String MESSAGE = "message";
    public static final String OPERATION_NAME = "operation-name";
    public static final String MESSAGE_SECURITY = "message-security";
    public static final String MESSAGE_SECURITY_BINDING =
        "message-security-binding";
    public static final String AUTH_LAYER = "auth-layer";
    public static final String PROVIDER_ID = "provider-id";
}
