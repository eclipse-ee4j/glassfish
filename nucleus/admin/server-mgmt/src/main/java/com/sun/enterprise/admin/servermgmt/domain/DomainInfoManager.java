/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.servermgmt.domain;

import com.sun.enterprise.admin.servermgmt.SLogger;
import com.sun.enterprise.admin.servermgmt.xml.domaininfo.DomainInfo;
import com.sun.enterprise.admin.servermgmt.xml.domaininfo.ObjectFactory;
import com.sun.enterprise.admin.servermgmt.xml.domaininfo.TemplateRef;
import com.sun.enterprise.admin.servermgmt.xml.templateinfo.TemplateInfo;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.logging.LogHelper;

import static org.glassfish.embeddable.GlassFishVariable.JAVA_HOME;

public class DomainInfoManager {

    private static final Logger _logger = SLogger.getLogger();

    /**
     * Parses template information file and uses its information to create domain info file.
     */
    public void process(DomainTemplate domainTemplate, File domainDir) {
        FileOutputStream outputStream = null;
        try {
            TemplateInfo templateInfo = domainTemplate.getInfo();
            File infoDir = new File(domainDir, DomainConstants.INFO_DIRECTORY);
            if (!infoDir.exists() && !infoDir.mkdirs()) {
                _logger.log(Level.INFO, SLogger.DIR_CREATION_ERROR, infoDir.getAbsolutePath());
                return;
            }
            File domainInfoXML = new File(infoDir, DomainConstants.DOMAIN_INFO_XML);
            outputStream = new FileOutputStream(domainInfoXML);
            ObjectFactory objFactory = new ObjectFactory();
            DomainInfo domainInfo = objFactory.createDomainInfo();
            String javaHome = System.getenv(JAVA_HOME.getEnvName());
            if (javaHome == null || javaHome.isEmpty()) {
                javaHome = System.getProperty(JAVA_HOME.getSystemPropertyName());
            }
            domainInfo.setJavahome(javaHome);
            domainInfo.setMwhome(System.getProperty(SystemPropertyConstants.PRODUCT_ROOT_PROPERTY));
            TemplateRef templateRef = new TemplateRef();
            templateRef.setName(templateInfo.getName());
            templateRef.setVersion(templateInfo.getVersion());
            templateRef.setLocation(domainTemplate.getLocation());
            domainInfo.setDomainTemplateRef(templateRef);

            JAXBContext context = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(objFactory.createDomainInfo(domainInfo), outputStream);
        } catch (Exception e) {
            LogHelper.log(_logger, Level.WARNING, SLogger.DOMAIN_INFO_CREATION_ERROR, e, DomainConstants.DOMAIN_INFO_XML);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception io) {
                    /** ignore */
                }
            }
        }
    }
}
