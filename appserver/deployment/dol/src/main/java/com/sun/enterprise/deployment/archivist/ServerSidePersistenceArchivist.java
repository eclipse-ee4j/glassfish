/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.util.DOLUtils;

import jakarta.inject.Inject;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Service;


/**
 * Archivist that reads persitence.xml for ejb jars and appclient while running on server side
 */
@Service
@ExtensionsArchivistFor("jpa")
public class ServerSidePersistenceArchivist extends PersistenceArchivist {
    @Inject
    private ProcessEnvironment env;

    @Override
    public boolean supportsModuleType(ArchiveType moduleType) {
        // Reads persitence.xml for ejb jars
        // Or App client modules if running inside server
        return DOLUtils.ejbType().equals(moduleType)
            || (DOLUtils.carType().equals(moduleType) && env.getProcessType().isServer());
    }

    @Override
    protected String getPuRoot(ReadableArchive archive) {
        //PU root for ejb jars and acc (while on server) is the current exploded archive on server side
        return "";
    }

}
