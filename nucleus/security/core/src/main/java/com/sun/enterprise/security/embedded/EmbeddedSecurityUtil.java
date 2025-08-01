/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.embedded;

import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.security.EmbeddedSecurity;
import com.sun.enterprise.security.SecurityLoggerInfo;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Singleton;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_FILENAME_DEFAULT;
import static com.sun.enterprise.util.SystemPropertyConstants.TRUSTSTORE_FILENAME_DEFAULT;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility file to copy the security related config files from the passed non-embedded instanceDir to the embedded server
 * instance's config. This is a service that is protected. This implements the Contract EmbeddedSecurity
 *
 * @author Nithya Subramanian
 */
@Service
@Singleton
public class EmbeddedSecurityUtil implements EmbeddedSecurity {

    private static final Logger _logger = SecurityLoggerInfo.getLogger();

    @Override
    public void copyConfigFiles(ServiceLocator habitat, File fromInstanceDir, File domainXml) {
        //For security reasons, permit only an embedded server instance to carry out the copy operations
        ServerEnvironment se = habitat.getService(ServerEnvironment.class);
        if (!isEmbedded(se)) {
            return;
        }

        if ((fromInstanceDir == null) || (domainXml == null)) {
            throw new IllegalArgumentException("Null inputs");
        }

        File toInstanceDir = habitat.<ServerEnvironmentImpl>getService(ServerEnvironmentImpl.class).getInstanceRoot();

        List<String> fileNames = new ArrayList<>();

        //Handling the exception here, since it is causing CTS failure - CR 6981191

        try {

            //Add FileRealm keyfiles to the list
            fileNames
                .addAll(new EmbeddedSecurityUtil().new DomainXmlSecurityParser(domainXml).getAbsolutePathKeyFileNames(fromInstanceDir));

            //Add keystore and truststore files

            // For the embedded server case, will the system properties be set in case of multiple embedded instances?
            //Not sure - so obtain the other files from the usual locations instead of from the System properties

            String keyStoreFileName = fromInstanceDir + File.separator + "config" + File.separator + KEYSTORE_FILENAME_DEFAULT;
            String trustStoreFileName = fromInstanceDir + File.separator + "config" + File.separator + TRUSTSTORE_FILENAME_DEFAULT;

            fileNames.add(keyStoreFileName);
            fileNames.add(trustStoreFileName);

            //Add login.conf and security policy

            String loginConf = fromInstanceDir + File.separator + "config" + File.separator + "login.conf";
            String secPolicy = fromInstanceDir + File.separator + "config" + File.separator + "server.policy";

            fileNames.add(loginConf);
            fileNames.add(secPolicy);

            File toConfigDir = new File(toInstanceDir, "config");
            if (!toConfigDir.exists()) {
                if (!toConfigDir.mkdir()) {
                    throw new IOException();
                }
            }

            //Copy files into new directory
            for (String fileName : fileNames) {
                FileUtils.copy(new File(fileName), new File(toConfigDir, parseFileName(fileName)));
            }
        } catch (IOException e) {
            _logger.log(Level.WARNING, SecurityLoggerInfo.ioError, e);
        } catch (XMLStreamException e) {
            _logger.log(Level.WARNING, SecurityLoggerInfo.xmlStreamingError, e);
        }

    }

    @Override
    public String parseFileName(String fullFilePath) {
        if (fullFilePath == null) {
            return null;
        }
        File file = new File(fullFilePath);
        return file.getName();

    }

    @Override
    public boolean isEmbedded(ServerEnvironment se) {
        if (se.getRuntimeType() == RuntimeType.EMBEDDED) {
            return true;
        }
        return false;
    }

    @Override
    public List<String> getKeyFileNames(SecurityService securityService) {
        List<String> keyFileNames = new ArrayList<>();

        List<AuthRealm> authRealms = securityService.getAuthRealm();
        for (AuthRealm authRealm : authRealms) {
            String className = authRealm.getClassname();
            if ("com.sun.enterprise.security.auth.realm.file.FileRealm".equals(className)) {
                List<Property> props = authRealm.getProperty();
                for (Property prop : props) {
                    if ("file".equals(prop.getName())) {
                        keyFileNames.add(prop.getValue());
                    }
                }
            }
        }

        return keyFileNames;
    }

    //Inner class to parse the domainXml to obtain the keyfile names
    class DomainXmlSecurityParser {
        XMLStreamReader xmlReader;
        XMLInputFactory xif = XMLInputFactory.class.getClassLoader() == null ? XMLInputFactory.newFactory()
            : XMLInputFactory.newFactory(XMLInputFactory.class.getName(), XMLInputFactory.class.getClassLoader());

        private static final String AUTH_REALM = "auth-realm";
        private static final String CONFIG = "config";
        private static final String CLASSNAME = "classname";
        private static final String FILE_REALM_CLASS = "com.sun.enterprise.security.auth.realm.file.FileRealm";
        private static final String PROPERTY = "property";
        private static final String NAME = "name";
        private static final String VALUE = "value";
        private static final String FILE = "file";
        private static final String INSTANCE_DIR_PLACEHOLDER = "${com.sun.aas.instanceRoot}";

        DomainXmlSecurityParser(File domainXml) throws XMLStreamException, IOException {
            xif.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
            xif.setProperty("javax.xml.stream.isValidating", true);
            xmlReader = xif.createXMLStreamReader(new FileReader(domainXml, UTF_8));
        }

        private String replaceInstanceDir(String fromInstanceDir, String keyFileName) {
            return StringUtils.replace(keyFileName, INSTANCE_DIR_PLACEHOLDER, fromInstanceDir);

        }

        //Obtain the keyfile names for the server-config (the first appearing config in domain.xml
        List<String> getAbsolutePathKeyFileNames(File fromInstanceDir) throws XMLStreamException {
            List<String> keyFileNames = new ArrayList<>();
            while (skipToStartButNotPast(AUTH_REALM, CONFIG)) {
                String realmClass = xmlReader.getAttributeValue(null, CLASSNAME);
                if (realmClass.equals(FILE_REALM_CLASS)) {
                    while (skipToStartButNotPast(PROPERTY, AUTH_REALM)) {
                        if (FILE.equals(xmlReader.getAttributeValue(null, NAME))) {
                            String keyFileName = xmlReader.getAttributeValue(null, VALUE);
                            //Replace the Placeholder in the keyfile names
                            keyFileNames.add(replaceInstanceDir(fromInstanceDir.getAbsolutePath(), keyFileName));

                        }
                    }
                }
            }
            return keyFileNames;
        }

        private boolean skipToStartButNotPast(String startName, String stopName) throws XMLStreamException {
            if (!StringUtils.ok(startName) || !StringUtils.ok(stopName)) {
                throw new IllegalArgumentException();
            }

            while (xmlReader.hasNext()) {
                xmlReader.next();
                // getLocalName() will throw an exception in many states.  Be careful!!
                if (xmlReader.isStartElement() && startName.equals(xmlReader.getLocalName())) {
                    return true;
                }
                if (xmlReader.isEndElement() && stopName.equals(xmlReader.getLocalName())) {
                    return false;
                }
            }
            return false;
        }
    }
}
