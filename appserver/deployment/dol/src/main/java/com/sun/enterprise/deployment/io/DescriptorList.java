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

package com.sun.enterprise.deployment.io;

import com.sun.enterprise.deployment.util.DOLUtils;

import org.glassfish.api.deployment.archive.ArchiveType;

/**
 * Repository of descriptors
 * This class will evolve to provide a comprhensive list of
 * descriptors for any given type of j2ee application or
 * stand-alone module.
 *
 * @author Sreenivas Munnangi
 */

public class DescriptorList {

    private final static String [] earList = {
        DescriptorConstants.APPLICATION_DD_ENTRY,
        DescriptorConstants.S1AS_APPLICATION_DD_ENTRY
    };

    private final static String [] ejbList = {
        DescriptorConstants.EJB_DD_ENTRY,
        DescriptorConstants.S1AS_EJB_DD_ENTRY,
        DescriptorConstants.S1AS_CMP_MAPPING_DD_ENTRY,
        DescriptorConstants.EJB_WEBSERVICES_JAR_ENTRY
    };

    private final static String [] warList = {
        DescriptorConstants.WEB_DD_ENTRY,
        DescriptorConstants.S1AS_WEB_DD_ENTRY,
        DescriptorConstants.WEB_WEBSERVICES_JAR_ENTRY,
        DescriptorConstants.JAXRPC_JAR_ENTRY
    };

    private final static String [] rarList = {
        DescriptorConstants.RAR_DD_ENTRY,
        DescriptorConstants.S1AS_RAR_DD_ENTRY
    };

    private final static String [] carList = {
        DescriptorConstants.APP_CLIENT_DD_ENTRY,
        DescriptorConstants.S1AS_APP_CLIENT_DD_ENTRY
    };

    public final static String [] getDescriptorsList (ArchiveType moduleType) {
        if (moduleType == null) return null;
        if (moduleType.equals(DOLUtils.earType())) {
            return (String[])earList.clone();
        } else if (moduleType.equals(DOLUtils.ejbType())) {
            return (String[])ejbList.clone();
        } else if (moduleType.equals(DOLUtils.warType())) {
            return (String[])warList.clone();
        } else if (moduleType.equals(DOLUtils.rarType())) {
            return (String[])rarList.clone();
        } else if (moduleType.equals(DOLUtils.carType())) {
            return (String[])carList.clone();
        }
        return null;
    }
}
