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

    String CONNECTOR = "connector";
    String RESOURCE_ADAPTER = "resourceadapter";
    String AUTH_MECHANISM = "authentication-mechanism";
    String CREDENTIAL_INTF = "credential-interface";
    String AUTH_MECH_TYPE = "authentication-mechanism-type";
    String CONNECTION_FACTORY_INTF =
        "connectionfactory-interface";
    String CONNECTION_FACTORY_IMPL =
        "connectionfactory-impl-class";
    String CONNECTION_INTF = "connection-interface";
    String CONNECTION_IMPL = "connection-impl-class";
    String CONFIG_PROPERTY = "config-property";
    String CONFIG_PROPERTY_NAME = "config-property-name";
    String CONFIG_PROPERTY_TYPE = "config-property-type";
    String CONFIG_PROPERTY_VALUE = "config-property-value";
    String CONFIG_PROPERTY_IGNORE = "config-property-ignore";
    String CONFIG_PROPERTY_SUPPORTS_DYNAMIC_UPDATES = "config-property-supports-dynamic-updates";
    String CONFIG_PROPERTY_CONFIDENTIAL = "config-property-confidential";
    String EIS_TYPE = "eis-type";
    String MANAGED_CONNECTION_FACTORY =
        "managedconnectionfactory-class";
    String REAUTHENTICATION_SUPPORT = "reauthentication-support";
    String SPEC_VERSION = "spec-version";
    String SECURITY_PERMISSION = "security-permission";
    String SECURITY_PERMISSION_SPEC = "security-permission-spec";
    String TRANSACTION_SUPPORT = "transaction-support";
    String VENDOR_NAME = "vendor-name";
    String VERSION = "version";
    String RESOURCEADAPTER_VERSION = "resourceadapter-version";
    String LICENSE_REQUIRED = "license-required";
    String LICENSE = "license";

    //connector1.5
    String OUTBOUND_RESOURCE_ADAPTER = "outbound-resourceadapter";
    String INBOUND_RESOURCE_ADAPTER = "inbound-resourceadapter";
    String CONNECTION_DEFINITION = "connection-definition";
    String RESOURCE_ADAPTER_CLASS = "resourceadapter-class";
    String MSG_ADAPTER = "messageadapter";
    String MSG_LISTENER = "messagelistener";
    String MSG_LISTENER_TYPE = "messagelistener-type";
    String REQUIRED_WORK_CONTEXT = "required-work-context";
    String ADMIN_OBJECT = "adminobject";
    String ADMIN_OBJECT_INTERFACE = "adminobject-interface";
    String ADMIN_OBJECT_CLASS = "adminobject-class";
    String ACTIVATION_SPEC = "activationspec";
    String ACTIVATION_SPEC_CLASS = "activationspec-class";
    String REQUIRED_CONFIG_PROP = "required-config-property";
    String CONNECTION = "connection";
    String CONNECTION_FACTORY = "connectionfactory";

    //FIXME.  the following are no longer valid. need clean up when
    //inbound-ra-class is completed removed from the implementation
    String INBOUND_RESOURCE_ADAPTER_CLASS = "resourceadapter-class";
    String MSG_LISTENER_NAME = "messagelistener-name";

    // Connector DD element valid values
    String DD_BASIC_PASSWORD    = "BasicPassword";
    String DD_KERBEROS          = "Kerbv5";
    String DD_NO_TRANSACTION    = "NoTransaction";
    String DD_LOCAL_TRANSACTION = "LocalTransaction";
    String DD_XA_TRANSACTION    = "XATransaction";
}
