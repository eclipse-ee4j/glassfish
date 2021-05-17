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

package com.sun.enterprise.deployment.util.webservice;

/**
 * This class is used by the deployment tool to set the required information for jaxrpc-config.xml
 * that is passed as an argument to the wscompile tool. This class is to be used when the developer is
 * using the deploytool to generate SEI from WSDL
 */

public class WSDLConfig {

    private String webServiceName;
    private String wsdlLocation;
    private String packageName;

    /**
     * Constructor takes all required arguments and sets them appropriately
     * @param wsName  Name of the webservice
     * @param wsdl    the WSDL file location
     * @param pkg     the package name where the SEI and its implementations are present
     */

    public WSDLConfig(String wsName, String wsdl, String pkg) {
        this.webServiceName = wsName;
        this.wsdlLocation = wsdl;
        this.packageName = pkg;
    }

    public String getWebServiceName() { return this.webServiceName; }

    public String getWsdlLocation() { return this.wsdlLocation; }

    public String getPackageName() { return this.packageName; }
}
