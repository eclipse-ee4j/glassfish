/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security.ee.perms;

import com.sun.enterprise.config.serverbeans.DasConfig;
import com.sun.enterprise.deployment.PermissionsDescriptor;
import com.sun.enterprise.deployment.io.PermissionsDeploymentDescriptorFile;
import com.sun.logging.LogDomains;

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

/**
 * Utility class to get declared permissions
 */
public class XMLPermissionsHandler {

    private static final Logger LOG = Logger.getLogger(LogDomains.SECURITY_LOGGER);
    private static final String PERMISSIONS_XML = "META-INF/permissions.xml";

    private static ServiceLocator serviceLocator = Globals.getDefaultBaseServiceLocator();

    private DasConfig dasConfig;
    private PermissionCollection declaredPermXml;
    private final SMGlobalPolicyUtil.CommponentType compType;

    public XMLPermissionsHandler(File base, SMGlobalPolicyUtil.CommponentType type)
        throws XMLStreamException, FileNotFoundException {
        this.compType = type;

        configureAppDeclaredPermissions(base);
        checkServerRestrictedPermissions();
    }


    public XMLPermissionsHandler(InputStream restrictPermInput, InputStream allowedPermInput,
        SMGlobalPolicyUtil.CommponentType type) throws XMLStreamException, FileNotFoundException {
        this.compType = type;

        configureAppDeclaredPermissions(allowedPermInput);
        checkServerRestrictedPermissions();
    }


    public PermissionCollection getAppDeclaredPermissions() {
        return declaredPermXml;
    }


    private void configureAppDeclaredPermissions(File base) {

        File permissionsXml = new File(base.getAbsolutePath(), PERMISSIONS_XML);

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
                LOG.log(Level.FINE, "App declared permission = {0}", declaredPermXml);

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
        }
    }

    private void configureAppDeclaredPermissions(InputStream permInput) throws XMLStreamException, FileNotFoundException {
        if (permInput != null) {
            // this one has no shchema check (for client)
            PermissionXMLParser parser = new PermissionXMLParser(permInput, null);
            this.declaredPermXml = parser.getPermissions();
            LOG.log(Level.FINE, "App declared permission = {0}", declaredPermXml);
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
