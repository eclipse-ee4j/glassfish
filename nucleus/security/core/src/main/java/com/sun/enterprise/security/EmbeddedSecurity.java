/*
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security;

import com.sun.enterprise.config.serverbeans.SecurityService;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Contract;

/**
 * Utility contact to copy the security related config files from the passed non-embedded instanceDir to the embedded server
 * instance's config. This is implemented by the EmbeddedSecurityUtil class
 *
 * @author Nithya Subramanian
 */

@Contract
public interface EmbeddedSecurity {

    public void copyConfigFiles(ServiceLocator habitat, File fromInstanceDir, File domainXml) throws IOException, XMLStreamException;

    public String parseFileName(String fullFilePath);

    public boolean isEmbedded(ServerEnvironment se);

    public List<String> getKeyFileNames(SecurityService securityService);
}
