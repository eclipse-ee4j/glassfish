/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.perms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.PermissionCollection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.xml.sax.SAXException;

import com.sun.enterprise.config.serverbeans.DasConfig;
import com.sun.enterprise.deployment.PermissionsDescriptor;
import com.sun.enterprise.deployment.io.PermissionsDeploymentDescriptorFile;
import com.sun.logging.LogDomains;

/**
 *
 * Utility class to get declared permissions
 *
 */

public class XMLPermissionsHandler {

    private static ServiceLocator serviceLocator = Globals.getDefaultBaseServiceLocator();
    private DasConfig dasConfig;

    private PermissionCollection declaredPermXml = null;
    private PermissionCollection restrictedPC = null; // per app based restriction, not used for now

    private SMGlobalPolicyUtil.CommponentType compType;

    private static final Logger logger = Logger.getLogger(LogDomains.SECURITY_LOGGER);

    public XMLPermissionsHandler(File base, SMGlobalPolicyUtil.CommponentType type) throws XMLStreamException, FileNotFoundException {
        this.compType = type;

        configureAppDeclaredPermissions(base);
        checkServerRestrictedPermissions();
    }

    public XMLPermissionsHandler(InputStream restrictPermInput, InputStream allowedPermInput, SMGlobalPolicyUtil.CommponentType type)
            throws XMLStreamException, FileNotFoundException {

        this.compType = type;

        configureAppDeclaredPermissions(allowedPermInput);
        checkServerRestrictedPermissions();
    }

    public PermissionCollection getAppDeclaredPermissions() {
        return declaredPermXml;
    }

    public PermissionCollection getRestrictedPermissions() {
        return restrictedPC;
    }

    private void configureAppDeclaredPermissions(File base) throws XMLStreamException, FileNotFoundException {

        File permissionsXml = new File(base.getAbsolutePath(), PermissionXMLParser.PERMISSIONS_XML);

        if (permissionsXml.exists()) {
            FileInputStream fi = null;
            try {
                // this one uses the Node approach
                PermissionsDeploymentDescriptorFile pddf = new PermissionsDeploymentDescriptorFile();

                if (serviceLocator != null) {
                    dasConfig = serviceLocator.getService(DasConfig.class);
                    if (dasConfig != null) {
                        String xmlValidationLevel = dasConfig.getDeployXmlValidation();
                        if (xmlValidationLevel.equals("none")) {
                            pddf.setXMLValidation(false);
                        } else {
                            pddf.setXMLValidation(true);
                        }
                        pddf.setXMLValidationLevel(xmlValidationLevel);
                    }
                }

                fi = new FileInputStream(permissionsXml);
                PermissionsDescriptor pd = (PermissionsDescriptor) pddf.read(fi);

                declaredPermXml = pd.getDeclaredPermissions();

            } catch (SAXException | IOException e) {
                throw new SecurityException(e);
            } finally {
                if (fi != null) {
                    try {
                        fi.close();
                    } catch (IOException e) {
                    }
                }
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("App declared permission = " + declaredPermXml);
            }
        }
    }

    private void configureAppDeclaredPermissions(InputStream permInput) throws XMLStreamException, FileNotFoundException {

        if (permInput != null) {
            // this one has no shchema check (for client)
            PermissionXMLParser parser = new PermissionXMLParser(permInput, restrictedPC);
            this.declaredPermXml = parser.getPermissions();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("App declared permission = " + declaredPermXml);
            }

        }
    }

    // check the app declared permissions against server restricted policy
    private void checkServerRestrictedPermissions() {

        if (this.declaredPermXml == null) {
            return;
        }

        if (compType == null) {
            return;
        }

        SMGlobalPolicyUtil.checkRestrictionOfComponentType(declaredPermXml, this.compType);
    }

}
