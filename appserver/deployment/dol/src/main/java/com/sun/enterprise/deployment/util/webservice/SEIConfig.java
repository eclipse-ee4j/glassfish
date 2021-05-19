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
 * that is passed as an argument to the wscompile tool. This class is to be used when the developer
 * is using the deploytool to generate WSDL given an SEI
 */
public class SEIConfig {

    private String webServiceName;
    private String nameSpace;
    private String packageName;
    private String interfaceName;
    private String servantName;

    /**
     * Constructor takes all required arguments and sets them appropriately
     *
     * @param svcName Name of the webservice
     * @param space namespace to be used for the webservice
     * @param pkg the package name where the SEI and its implementations are present
     * @param svcIntf the name of the SEI
     * @param svcImpl the name of SEI implementation
     */

    public SEIConfig(String svcName, String space, String pkg, String svcIntf, String svcImpl) {
        this.webServiceName = svcName;
        this.nameSpace = space;
        this.packageName = pkg;
        this.interfaceName = svcIntf;
        this.servantName = svcImpl;
    }


    public String getWebServiceName() {
        return this.webServiceName;
    }


    public String getNameSpace() {
        return this.nameSpace;
    }


    public String getPackageName() {
        return this.packageName;
    }


    public String getInterface() {
        return this.interfaceName;
    }


    public String getServant() {
        return this.servantName;
    }
}
