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

/** The XML tag names for the connector dtd
 * @author Vivek Nagar
 * @author Tony Ng
 */
public interface ConnectorTagNames extends TagNames {

    public static String CONNECTOR = "connector";
    public static String RESOURCE_ADAPTER = "resourceadapter";
    public static String AUTH_MECHANISM = "authentication-mechanism";
    public static String CREDENTIAL_INTF = "credential-interface";
    public static String AUTH_MECH_TYPE = "authentication-mechanism-type";
    public static String CONNECTION_FACTORY_INTF =
        "connectionfactory-interface";
    public static String CONNECTION_FACTORY_IMPL =
        "connectionfactory-impl-class";
    public static String CONNECTION_INTF = "connection-interface";
    public static String CONNECTION_IMPL = "connection-impl-class";
    public static String CONFIG_PROPERTY = "config-property";
    public static String CONFIG_PROPERTY_NAME = "config-property-name";
    public static String CONFIG_PROPERTY_TYPE = "config-property-type";
    public static String CONFIG_PROPERTY_VALUE = "config-property-value";
    public static String CONFIG_PROPERTY_IGNORE = "config-property-ignore";
    public static String CONFIG_PROPERTY_SUPPORTS_DYNAMIC_UPDATES = "config-property-supports-dynamic-updates";
    public static String CONFIG_PROPERTY_CONFIDENTIAL = "config-property-confidential";
    public static String EIS_TYPE = "eis-type";
    public static String MANAGED_CONNECTION_FACTORY =
        "managedconnectionfactory-class";
    public static String REAUTHENTICATION_SUPPORT = "reauthentication-support";
    public static String SPEC_VERSION = "spec-version";
    public static String SECURITY_PERMISSION = "security-permission";
    public static String SECURITY_PERMISSION_SPEC = "security-permission-spec";
    public static String TRANSACTION_SUPPORT = "transaction-support";
    public static String VENDOR_NAME = "vendor-name";
    public static String VERSION = "version";
    public static String RESOURCEADAPTER_VERSION = "resourceadapter-version";
    public static String LICENSE_REQUIRED = "license-required";
    public static String LICENSE = "license";

    //connector1.5
    public static String OUTBOUND_RESOURCE_ADAPTER = "outbound-resourceadapter";
    public static String INBOUND_RESOURCE_ADAPTER = "inbound-resourceadapter";
    public static String CONNECTION_DEFINITION = "connection-definition";
    public static String RESOURCE_ADAPTER_CLASS = "resourceadapter-class";
    public static String MSG_ADAPTER = "messageadapter";
    public static String MSG_LISTENER = "messagelistener";
    public static String MSG_LISTENER_TYPE = "messagelistener-type";
    public static String REQUIRED_WORK_CONTEXT = "required-work-context";
    public static String ADMIN_OBJECT = "adminobject";
    public static String ADMIN_OBJECT_INTERFACE = "adminobject-interface";
    public static String ADMIN_OBJECT_CLASS = "adminobject-class";
    public static String ACTIVATION_SPEC = "activationspec";
    public static String ACTIVATION_SPEC_CLASS = "activationspec-class";
    public static String REQUIRED_CONFIG_PROP = "required-config-property";
    public static String CONNECTION = "connection";
    public static String CONNECTION_FACTORY = "connectionfactory";

    //FIXME.  the following are no longer valid. need clean up when
    //inbound-ra-class is completed removed from the implementation
    public static String INBOUND_RESOURCE_ADAPTER_CLASS = "resourceadapter-class";
    public static String MSG_LISTENER_NAME = "messagelistener-name";

    // Connector DD element valid values
    public static final String DD_BASIC_PASSWORD    = "BasicPassword";
    public static final String DD_KERBEROS          = "Kerbv5";
    public static final String DD_NO_TRANSACTION    = "NoTransaction";
    public static final String DD_LOCAL_TRANSACTION = "LocalTransaction";
    public static final String DD_XA_TRANSACTION    = "XATransaction";
}
